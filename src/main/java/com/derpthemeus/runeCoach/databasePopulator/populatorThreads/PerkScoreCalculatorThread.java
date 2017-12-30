package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.PerkScoreCalculatorSupervisor;
import com.derpthemeus.runeCoach.hibernate.PerkScoreEntity;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class PerkScoreCalculatorThread extends PopulatorThread {

	private PerkScoreEntity score;
	// The LoL patch (e.g. "7.24") that this thread is aggregating stats for
	private String patch;

	@Override
	public void runOperation() {
		// Use the latest patch if one hasn't been specified
		if (patch == null) {
			getLogger().info("Defaulting to most recent patch");
			try {
				patch = DDragonManager.convertToShortVersion(DDragonManager.getLatestVersion());
			} catch (IOException ex) {
				getLogger().error("Error getting DDragon version", ex);
				return;
			}
		}

		score = getSupervisor().getScoreToCalculate(patch);
		// Sleep for 10 seconds if there is no work to be done
		if (score == null) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				getLogger().throwing(ex);
			}
			return;
		}

		Transaction tx = null;
		try (Session session = getSupervisor().getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.lock(score, LockMode.PESSIMISTIC_WRITE);

			// The IDs of keystones in this style, or `null` if this score is for a single perk
			List<Short> keystoneIds = null;
			// Check if this score is for a style, and set `keystoneIds` if it is
			for (DDragonManager.StyleInfo style : DDragonManager.getRuneInfo(DDragonManager.getLongVersion(score.getPatch()))) {
				if (style.id == score.getPerkId()) {
					keystoneIds = Arrays.stream(style.slots[0].runes).map(rune -> rune.id).collect(Collectors.toList());
					break;
				}
			}

			// Will only be set if this score is for a tag. A list of champion IDs that have this tag.
			List<Short> championIds = null;
			String perkClause, champClause;

			if (keystoneIds == null) {
				// Calculate for a single rune
				perkClause = "perkId=:perkId";
			} else {
				// Calculate for a style
				perkClause = "perkId IN (:keystoneIds)";
			}
			if (score.getChampionId() < 0) {
				// Calculate for a tag
				champClause = "championId IN (SELECT championId FROM TagChampionEntity WHERE tagId=-:championId)";
			} else {
				// Calculate for a single champion
				champClause = "championId=:championId";
			}

			// Summing will only actually be necessary if aggregating winrate for a style or tag
			Query query = session.createQuery("SELECT SUM(totalWins)*1.0, CAST(SUM(totalMatches) as java.lang.Integer) FROM AggregatedChampionStatsEntity WHERE " + perkClause + " AND " + champClause + " AND patch=:patch");
			// Set the parameter names (the query may not contain all parameter names, and only used ones can be set)
			if (keystoneIds == null) {
				query.setParameter("perkId", score.getPerkId());
			} else {
				query.setParameter("keystoneIds", keystoneIds);
			}
			query.setParameter("championId", score.getChampionId()).setParameter("patch", score.getPatch());


			Object[] result = (Object[]) query.getSingleResult();
			Integer gamesCounted = (Integer) result[1];
			// gamesCounted could be null if stats have not been aggregated yet
			if (gamesCounted != null && gamesCounted != 0) {
				// TODO use a better score calculation equation that also considers perk vars
				Double winRate = (Double) result[0] / gamesCounted;
				score.setScore(winRate);
				score.setGames(gamesCounted);
			}

			score.setLastUpdated(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			session.update(score);

			tx.commit();
		} catch (Exception ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			getLogger().error("Error calculating score for perk " + score.getPerkId() + " on champion " + score.getChampionId() + " during patch " + score.getPatch(), ex);
		}
		score = null;
	}

	@Override
	public PerkScoreCalculatorSupervisor getSupervisor() {
		return PerkScoreCalculatorSupervisor.getInstance();
	}

	public PerkScoreEntity getActiveScore() {
		return score;
	}
}
