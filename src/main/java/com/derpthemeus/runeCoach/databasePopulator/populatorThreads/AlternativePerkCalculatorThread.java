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
import java.util.stream.Collectors;

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
				Query query = session.createQuery("FROM PerkScoreEntity WHERE championId=:champion AND perkId IN (:slotOptions) AND patch=:patch")
						.setParameter("champion", alternativePerkEntity.getChampionId()).setParameter("patch", alternativePerkEntity.getPatch()).setParameter("slotOptions", slotOptions);
				List<PerkScoreEntity> options = query.getResultList();

				PerkScoreEntity original = options.stream().filter(score -> score.getPerkId() == alternativePerkEntity.getPerkId()).findAny().get();
				options.sort(Comparator.comparingDouble((PerkScoreEntity score) -> {
					if (score.getScore() == null) {
						// Default to 0.5 if score hasn't been calculated yet
						score.setScore(0.5);
					}
					return score.getScore();
				}).reversed());

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
					Query tagScoresQuery = session.createQuery("FROM PerkScoreEntity WHERE championId IN (SELECT tagId FROM TagChampionEntity WHERE championId=:champion) AND perkId=:perkId AND score IS NOT NULL ORDER BY score " + sortingMethod)
							.setParameter("champion", alternativePerkEntity.getChampionId()).setParameter("perkId", alternativePerkEntity.getPerkId());
					List<PerkScoreEntity> tagAverageScores = tagScoresQuery.setMaxResults(2).getResultList();
					// Only use the tags as reasons if they have a significant enough impact on score
					if (tagAverageScores.size() > 0 && Math.abs(tagAverageScores.get(0).getScore() - 0.5) > 0.015) {
						alternativePerkEntity.setReason1(tagAverageScores.get(0).getPerkId());
						if (tagAverageScores.size() > 1 && Math.abs(tagAverageScores.get(1).getScore() - 0.5) > 0.015) {
							alternativePerkEntity.setReason2(tagAverageScores.get(1).getPerkId());
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
