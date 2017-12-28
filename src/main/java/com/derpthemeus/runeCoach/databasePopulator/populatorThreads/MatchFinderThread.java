package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.MatchFinderSupervisor;
import com.derpthemeus.runeCoach.hibernate.MatchEntity;
import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import no.stelar7.api.l4j8.basic.constants.api.Platform;
import no.stelar7.api.l4j8.basic.constants.types.GameQueueType;
import no.stelar7.api.l4j8.pojo.match.MatchReference;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.LockModeType;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Tracks matches for summoners that haven't been updated in a while
 */
public class MatchFinderThread extends PopulatorThread {
	private final HashSet<GameQueueType> QUEUES = new HashSet<>(Collections.singletonList(GameQueueType.TEAM_BUILDER_RANKED_SOLO));

	private SummonerEntity summoner;

	@Override
	public void runOperation() {
		summoner = getSupervisor().getSummonerToCheck();
		// Sleep for 10 seconds if there is no work to be done
		if (summoner == null) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				getLogger().throwing(ex);
			}
			return;
		}

		// TODO calculate this dynamically based on patch date?
		Calendar matchCutoffDate = Calendar.getInstance();
		matchCutoffDate.add(Calendar.DATE, -15);

		Transaction tx = null;
		try (Session session = getSupervisor().getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.load(summoner, summoner.getSummonerId());
			session.lock(summoner, LockModeType.PESSIMISTIC_WRITE);

			List<MatchReference> matchList = getSupervisor().getL4j8().getMatchAPI().getMatchList(Platform.NA1, summoner.getAccountId(), null, null, null, null, QUEUES, null, null);
			for (MatchReference reference : matchList) {
				// Check if all matches before this one have already been tracked, or if the match too old to bother tracking
				if (reference.getTimestamp() <= summoner.getLastMatchTimestamp().getTime() || reference.getTimestamp() < matchCutoffDate.getTimeInMillis()) {
					break;
				}
				// Skip any matches from before a player transferred to NA
				if (reference.getPlatform() != Platform.NA1) {
					break;
				}

				// Skip the match if it has already be tracked
				if (session.get(MatchEntity.class, reference.getGameId()) != null) {
					continue;
				}

				MatchEntity matchEntity = new MatchEntity();
				matchEntity.setMatchId(reference.getGameId());
				matchEntity.setMatchTimestamp(new Timestamp(reference.getTimestamp()));

				session.save(matchEntity);
			}

			// Update the latest downloaded match (if a new match was downloaded)
			if (matchList.size() > 0) {
				summoner.setLastMatchTimestamp(new Timestamp(matchList.get(0).getTimestamp()));
			}
			summoner.setMatchesLastUpdated(new Timestamp(Calendar.getInstance().getTimeInMillis()));

			session.update(summoner);
			tx.commit();
		} catch (Exception ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			getLogger().error("Error processing match history for summoner ID " + summoner.getSummonerId(), ex);
		}
		summoner = null;
	}

	/**
	 * Returns the SummonerEntity this thread is currently checking, or `null` if a summoner is nor currently being checked
	 *
	 * @return The SummonerEntity being checked, or `null` if the thread is not currently checking a summoner
	 */
	public SummonerEntity getActiveSummoner() {
		return summoner;
	}

	@Override
	public MatchFinderSupervisor getSupervisor() {
		return MatchFinderSupervisor.getInstance();
	}
}
