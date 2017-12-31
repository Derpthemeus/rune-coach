package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.AlternativePerkCalculatorSupervisor;
import com.derpthemeus.runeCoach.hibernate.PerkAlternativeEntity;
import com.derpthemeus.runeCoach.hibernate.PerkScoreEntity;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO add support for reasons being tags that perks are exclusively good on (perk X is generally only good on Y or Z champions)
public class AlternativePerkCalculatorThread extends PopulatorThread {

	private PerkAlternativeEntity alternativePerkEntity;
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

		alternativePerkEntity = getSupervisor().getAlternativeToCalculate(patch);

		// Sleep for 10 seconds if there is no work to be done
		if (alternativePerkEntity == null) {
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
			session.lock(alternativePerkEntity, LockMode.PESSIMISTIC_WRITE);

			if (alternativePerkEntity.getScope() == PerkAlternativeEntity.Scope.SLOT) {
				List<Short> slotOptions = getSlotAlternatives(alternativePerkEntity.getPerkId());
				Query query = session.createQuery("FROM PerkScoreEntity WHERE championId=:champion AND perkId IN (:slotOptions) AND patch=:patch AND games>=75 AND score IS NOT NULL")
						.setParameter("champion", alternativePerkEntity.getChampionId()).setParameter("patch", alternativePerkEntity.getPatch()).setParameter("slotOptions", slotOptions);
				List<PerkScoreEntity> options = query.getResultList();
				Optional<PerkScoreEntity> optional = options.stream().filter(score -> score.getPerkId() == alternativePerkEntity.getPerkId()).findAny();
				if (!optional.isPresent()) {
					// Skip for now if scores have not been calculated yet, or sample size is too small
					return;
				}

				PerkScoreEntity original = optional.get();

				// TODO this should really be calculated elsewhere - it is redundant if calculated here across multiple contexts
				// Get the average winrate for this rune and this champion
				Query expectedWinrateQuery = session.createQuery("SELECT SUM(score*games)/SUM(games) FROM PerkScoreEntity WHERE (perkId=:perk OR championId=:championId) AND patch=:patch")
						.setParameter("perk", alternativePerkEntity.getPerkId()).setParameter("championId", alternativePerkEntity.getChampionId()).setParameter("patch", alternativePerkEntity.getPatch());

				double expectedWinrate = (double) expectedWinrateQuery.getSingleResult();
				// Absolute score is relative to average winrates for this champion and rune, but absolute compared to other runes
				alternativePerkEntity.setAbsoluteScore(original.getScore() - expectedWinrate);


				options.sort(Comparator.comparingDouble(PerkScoreEntity::getScore).reversed());
				// Determine if there is an alternative that is decently better than the original choice.
				double improvement = options.get(0).getScore() - original.getScore();
				// Suggest an alternative, if there is an option that is decently better
				if (improvement >= 0.015) {
					alternativePerkEntity.setAlternative(options.get(0).getPerkId());
					alternativePerkEntity.setImprovement(improvement);
				} else {
					alternativePerkEntity.setAlternative(null);
					alternativePerkEntity.setImprovement(0);
				}


				// Determine if the selected rune is good or bad relative to its alternatives.

				double avgScore = 0;
				for (PerkScoreEntity option : options) {
					avgScore += option.getScore();
				}
				avgScore /= options.size();

				// How the score of the original perk compares to alternatives within this scope. A positive value indicates it is better, a negative value indicates it is worse
				double scoreDifference = original.getScore() - avgScore;
				alternativePerkEntity.setRelativeScore(scoreDifference);

				// How results should be sorted so the largest contributing factors are first
				String sortingMethod = null;
				// If there is less than a 1.5% difference, it's not significant enough to care about
				if (scoreDifference > 0.015) {
					sortingMethod = "DESC";
				} else if (scoreDifference < -0.015) {
					sortingMethod = "ASC";
				}

				// If the score difference is large enough to matter, determine what caused it
				if (sortingMethod != null) {
					Query tagScoresQuery = session.createQuery("FROM PerkScoreEntity WHERE -championId IN (SELECT tagId FROM TagChampionEntity WHERE championId=:champion) AND perkId=:perkId AND score IS NOT NULL AND patch=:patch ORDER BY score " + sortingMethod)
							.setParameter("champion", alternativePerkEntity.getChampionId()).setParameter("perkId", alternativePerkEntity.getPerkId()).setParameter("patch", alternativePerkEntity.getPatch());
					List<PerkScoreEntity> tagAverageScores = tagScoresQuery.setMaxResults(2).getResultList();
					// Only use the tags as reasons if they have a significant enough impact on score
					if (tagAverageScores.size() > 0 && Math.abs(tagAverageScores.get(0).getScore() - 0.5) > 0.015) {
						alternativePerkEntity.setReason1((short) -tagAverageScores.get(0).getChampionId());
						if (tagAverageScores.size() > 1 && Math.abs(tagAverageScores.get(1).getScore() - 0.5) > 0.015) {
							alternativePerkEntity.setReason2((short) -tagAverageScores.get(1).getChampionId());
						}
					}
				} else if (alternativePerkEntity.getScope() == PerkAlternativeEntity.Scope.SUBSTYLE) {
					// TODO
				} else if (alternativePerkEntity.getScope() == PerkAlternativeEntity.Scope.STYLE) {
					// TODO
				}
			}

			alternativePerkEntity.setLastUpdated(new Timestamp(Calendar.getInstance().getTimeInMillis()));

			session.update(alternativePerkEntity);

			tx.commit();
		} catch (Exception ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			getLogger().error("Error calculating alternative for perk " + alternativePerkEntity.getPerkId() + " in scope " + alternativePerkEntity.getScope() + " on champion " + alternativePerkEntity.getChampionId() + " during patch " + alternativePerkEntity.getPatch(), ex);
		}
		alternativePerkEntity = null;
	}

	@Override
	public AlternativePerkCalculatorSupervisor getSupervisor() {
		return AlternativePerkCalculatorSupervisor.getInstance();
	}

	public PerkAlternativeEntity getActiveAlternative() {
		return alternativePerkEntity;
	}

	/**
	 * If `perkId` is the ID of a single rune, gets a list of all rune IDs that can go in the same slot as the specified rune (including the specified rune).
	 * If `perkId` is the ID of a style, gets a list of all style IDs (including the specified style).
	 *
	 * @param runeId
	 * @return
	 */
	private List<Short> getSlotAlternatives(short runeId) throws IOException {
		DDragonManager.StyleInfo[] styles = DDragonManager.getRuneInfo(DDragonManager.getLongVersion(patch));
		for (DDragonManager.StyleInfo style : styles) {
			for (DDragonManager.StyleInfo.SlotInfo slot : style.slots) {
				if (style.id == runeId) {
					return Arrays.stream(styles).map(styleInfo -> styleInfo.id).collect(Collectors.toList());
				}
				for (DDragonManager.StyleInfo.Rune rune : slot.runes) {
					if (rune.id == runeId) {
						return Arrays.stream(slot.runes).map(runeInfo -> runeInfo.id).collect(Collectors.toList());
					}
				}
			}
		}
		return null;
	}
}
