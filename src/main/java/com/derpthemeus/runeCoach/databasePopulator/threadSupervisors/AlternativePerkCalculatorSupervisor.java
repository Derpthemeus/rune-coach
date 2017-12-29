package com.derpthemeus.runeCoach.databasePopulator.threadSupervisors;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.AlternativePerkCalculatorThread;
import com.derpthemeus.runeCoach.hibernate.PerkAlternativeEntity;
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

public class AlternativePerkCalculatorSupervisor extends PopulatorThreadSupervisor<AlternativePerkCalculatorThread> {

	private static AlternativePerkCalculatorSupervisor instance = new AlternativePerkCalculatorSupervisor();
	// Which scopes should be calculated
	// TODO add support for other types
	private static PerkAlternativeEntity.Scope[] scopes = new PerkAlternativeEntity.Scope[]{PerkAlternativeEntity.Scope.SLOT};

	public static AlternativePerkCalculatorSupervisor getInstance() {
		return instance;
	}

	@Override
	public Class<AlternativePerkCalculatorThread> getThreadClass() {
		return AlternativePerkCalculatorThread.class;
	}

	private Map<String, Queue<PerkAlternativeEntity>> alternativesToCalculateByPatch = new HashMap<>();

	public synchronized PerkAlternativeEntity getAlternativeToCalculate(String patch) {
		if (!alternativesToCalculateByPatch.containsKey(patch)) {
			alternativesToCalculateByPatch.put(patch, new ArrayDeque<>());
		}
		Queue<PerkAlternativeEntity> alternativeToCalculate = alternativesToCalculateByPatch.get(patch);

		// Refill the queue by querying the database
		if (alternativeToCalculate.isEmpty()) {
			List<PerkAlternativeEntity> activeAlternatives = getRunningThreads().stream().map(
					thread -> {
						if (thread.getActiveAlternative() != null) {
							return thread.getActiveAlternative();
						} else {
							return null;
						}
					}
			).filter(Objects::nonNull).collect(Collectors.toList());

			Transaction tx = null;
			try (Session session = getSessionFactory().openSession()) {
				tx = session.beginTransaction();
				Query query = session.createQuery("FROM PerkAlternativeEntity WHERE patch=:patch ORDER BY lastUpdated ASC")
						.setParameter("patch", patch);
				List<PerkAlternativeEntity> alternatives = query.getResultList();

				if (alternatives.isEmpty()) {
					// Create alternatives for the patch if they haven't been created yet
					String ddragonVersion = DDragonManager.getLongVersion(patch);
					List<Short> championIds = getChampionIds(ddragonVersion);
					List<Short> perkAndStyleIds = getPerkAndStyleIds(ddragonVersion);

					// Create a new List so it doesn't need to be reallocated multiple times as it's filled
					alternatives = new ArrayList<>(perkAndStyleIds.size() * championIds.size() * scopes.length);

					for (short championId : championIds) {
						for (short perkId : perkAndStyleIds) {
							for (PerkAlternativeEntity.Scope scope : scopes) {
								PerkAlternativeEntity alternative = new PerkAlternativeEntity();
								alternative.setChampionId(championId);
								alternative.setPerkId(perkId);
								alternative.setPatch(patch);
								alternative.setLastUpdated(new Timestamp(0));
								alternative.setScope(scope);

								session.save(alternative);
								alternatives.add(alternative);
							}
						}
					}
				} else {
					// Remove alternatives that are already being processed
					for (PerkAlternativeEntity activeAlternative : activeAlternatives) {
						alternatives.removeIf(alternative -> alternative.getPerkId() == activeAlternative.getPerkId() && alternative.getChampionId() == activeAlternative.getChampionId());
					}
				}
				tx.commit();
				alternativeToCalculate.addAll(alternatives);
			} catch (Exception ex) {
				if (tx != null) {
					tx.markRollbackOnly();
				}
				getLogger().error("Error refilling lazy list", ex);
			}
		}
		return alternativeToCalculate.poll();
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
