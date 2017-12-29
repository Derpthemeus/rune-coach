package com.derpthemeus.runeCoach.databasePopulator.threadSupervisors;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.PerkScoreCalculatorThread;
import com.derpthemeus.runeCoach.hibernate.PerkScoreEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
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
			List<PerkScoreEntity> activeScores = getRunningThreads().stream().map(
					thread -> {
						if (thread.getActiveScore() != null) {
							return thread.getActiveScore();
						} else {
							return null;
						}
					}
			).filter(Objects::nonNull).collect(Collectors.toList());

			Transaction tx = null;
			try (Session session = getSessionFactory().openSession()) {
				tx = session.beginTransaction();
				Query query = session.createQuery("FROM PerkScoreEntity WHERE patch=:patch ORDER BY lastUpdated ASC")
						.setParameter("patch", patch);
				List<PerkScoreEntity> scores = query.getResultList();

				if (scores.isEmpty()) {
					// Create scores for the patch if they haven't been created yet
					String ddragonVersion = DDragonManager.getLongVersion(patch);
					List<Short> championIds = getChampionIds(ddragonVersion);
					List<Short> perkAndStyleIds = getPerkAndStyleIds(ddragonVersion);

					// Create a new List so it doesn't need to be reallocated multiple times as it's filled
					scores = new ArrayList<>(perkAndStyleIds.size() * championIds.size());

					for (short championId : championIds) {
						for (short perkId : perkAndStyleIds) {
							PerkScoreEntity score = new PerkScoreEntity();
							score.setChampionId(championId);
							score.setPerkId(perkId);
							score.setPatch(patch);
							score.setLastUpdated(new Timestamp(0));
							score.setScore(null);

							session.save(score);
							scores.add(score);
						}
					}
				} else {
					// Remove scores that are already being processed
					for (PerkScoreEntity activeScore : activeScores) {
						scores.removeIf(score -> score.getPerkId() == activeScore.getPerkId() && score.getChampionId() == activeScore.getChampionId());
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

	/**
	 * Gets the IDs of all available champions for a certain DDragon version
	 */
	private List<Short> getChampionIds(String ddragonVersion) throws IOException {
		return DDragonManager.getChampionList(ddragonVersion).data.values().stream().map(champion -> Short.parseShort(champion.key)).collect(Collectors.toList());
	}


	/**
	 * Gets the IDs of all available perks and styles for a certain DDragon version
	 */
	private List<Short> getPerkAndStyleIds(String ddragonVersion) throws IOException {
		List<Short> ids = new ArrayList<>();

		DDragonManager.StyleInfo[] styles = DDragonManager.getRuneInfo(ddragonVersion);
		for (DDragonManager.StyleInfo style : styles) {
			ids.add(style.id);
			for (DDragonManager.StyleInfo.SlotInfo slot : style.slots) {
				for (DDragonManager.StyleInfo.Rune rune : slot.runes) {
					ids.add(rune.id);
				}
			}
		}

		return ids;
	}
}
