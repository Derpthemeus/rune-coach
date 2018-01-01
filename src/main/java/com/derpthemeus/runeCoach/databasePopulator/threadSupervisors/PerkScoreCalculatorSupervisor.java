package com.derpthemeus.runeCoach.databasePopulator.threadSupervisors;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.PerkScoreCalculatorThread;
import com.derpthemeus.runeCoach.hibernate.PerkScoreEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

public class PerkScoreCalculatorSupervisor extends PopulatorThreadSupervisor<PerkScoreCalculatorThread> {
	private static PerkScoreCalculatorSupervisor instance = new PerkScoreCalculatorSupervisor();


	@Override
	public Class<PerkScoreCalculatorThread> getThreadClass() {
		return PerkScoreCalculatorThread.class;
	}

	public static PerkScoreCalculatorSupervisor getInstance() {
		return instance;
	}

	private Map<String, Queue<PerkScoreEntity>> scoresToAggregateByPatch = new HashMap<>();

	public synchronized PerkScoreEntity getScoreToCalculate(String patch) {
		if (!scoresToAggregateByPatch.containsKey(patch)) {
			scoresToAggregateByPatch.put(patch, new ArrayDeque<>());
		}
		Queue<PerkScoreEntity> scoresToAggregate = scoresToAggregateByPatch.get(patch);

		// Refill the queue by querying the database
		if (scoresToAggregate.isEmpty()) {
			List<PerkScoreEntity> activeScores = getRunningThreads().stream().map(PerkScoreCalculatorThread::getActiveScore).filter(Objects::nonNull).collect(Collectors.toList());

			Transaction tx = null;
			try (Session session = getSessionFactory().openSession()) {
				tx = session.beginTransaction();
				Query query = session.createQuery("FROM PerkScoreEntity WHERE patch=:patch ORDER BY lastUpdated ASC")
						.setParameter("patch", patch);
				List<PerkScoreEntity> scores = query.getResultList();

				if (scores.isEmpty()) {
					// Create scores for the patch if they haven't been created yet
					String ddragonVersion = DDragonManager.getLongVersion(patch);
					List<Short> championAndTagIds = DDragonManager.getChampionIds(ddragonVersion);
					championAndTagIds.addAll(session.createQuery("SELECT -tagId FROM TagEntity").getResultList());
					List<Short> perkAndStyleIds = DDragonManager.getPerkAndStyleIds(ddragonVersion);
					List<Short> perkIds = DDragonManager.getPerkIds(ddragonVersion);

					// Create a new List so it doesn't need to be reallocated multiple times as it's filled
					scores = new ArrayList<>(championAndTagIds.size() * (2 * perkAndStyleIds.size() + perkIds.size()));

					// Setup RAW and RELATIVE
					for (PerkScoreEntity.ScoreType scoreType : new PerkScoreEntity.ScoreType[]{PerkScoreEntity.ScoreType.RAW, PerkScoreEntity.ScoreType.RELATIVE}) {
						for (short perkId : perkAndStyleIds) {
							for (short championId : championAndTagIds) {
								scores.add(createScore(session, perkId, championId, scoreType, patch));
							}
						}
					}

					// Setup SLOT
					for (short perkId : perkIds) {
						for (short championId : championAndTagIds) {
							scores.add(createScore(session, perkId, championId, PerkScoreEntity.ScoreType.SLOT, patch));
						}
					}

					// TODO add support for ScoreType.SUBSTYLE
				} else {
					// Remove scores that are already being processed
					for (PerkScoreEntity activeScore : activeScores) {
						scores.removeIf(score ->
								score.getPerkId() == activeScore.getPerkId()
										&& score.getChampionId() == activeScore.getChampionId()
										&& score.getScoreType() == activeScore.getScoreType()
										&& score.getPatch().equals(activeScore.getPatch())
						);
					}
				}
				tx.commit();
				scoresToAggregate.addAll(scores);
			} catch (Exception ex) {
				if (tx != null) {
					tx.markRollbackOnly();
				}
				getLogger().error("Error refilling lazy list", ex);
			}
		}
		return scoresToAggregate.poll();
	}

	private PerkScoreEntity createScore(Session session, short perkId, short championId, PerkScoreEntity.ScoreType scoreType, String patch) {
		PerkScoreEntity score = new PerkScoreEntity();

		score.setChampionId(championId);
		score.setPerkId(perkId);
		score.setScoreType(scoreType);
		score.setPatch(patch);
		score.setLastUpdated(new Timestamp(0));
		score.setScore(null);

		session.save(score);
		return score;
	}
}
