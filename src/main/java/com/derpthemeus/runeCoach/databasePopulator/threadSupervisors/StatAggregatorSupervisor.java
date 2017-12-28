package com.derpthemeus.runeCoach.databasePopulator.threadSupervisors;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.StatAggregatorThread;
import com.derpthemeus.runeCoach.hibernate.AggregatedChampionStatsEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

public class StatAggregatorSupervisor extends PopulatorThreadSupervisor<StatAggregatorThread> {

	private static StatAggregatorSupervisor instance = new StatAggregatorSupervisor();

	@Override
	public Class<StatAggregatorThread> getThreadClass() {
		return StatAggregatorThread.class;
	}

	public static StatAggregatorSupervisor getInstance() {
		return instance;
	}

	private Map<String, Queue<AggregatedChampionStatsEntity>> statsToAggregateByPatch = new HashMap<>();

	public synchronized AggregatedChampionStatsEntity getStatToAggregate(String patch) {
		if (!statsToAggregateByPatch.containsKey(patch)) {
			statsToAggregateByPatch.put(patch, new ArrayDeque<>());
		}
		Queue<AggregatedChampionStatsEntity> statsToAggregate = statsToAggregateByPatch.get(patch);

		// Refill the queue by querying the database
		if (statsToAggregate.isEmpty()) {
			List<AggregatedChampionStatsEntity> activeStats = getRunningThreads().stream().map(
					thread -> {
						if (thread.getActiveStat() != null) {
							return thread.getActiveStat();
						} else {
							return null;
						}
					}
			).filter(Objects::nonNull).collect(Collectors.toList());

			Transaction tx = null;
			try (Session session = getSessionFactory().openSession()) {
				tx = session.beginTransaction();
				Query query = session.createQuery("FROM AggregatedChampionStatsEntity WHERE patch=:patch ORDER BY lastPlayerId ASC")
						.setParameter("patch", patch);
				List<AggregatedChampionStatsEntity> stats = query.getResultList();

				if (stats.isEmpty()) {
					// Create aggregated stats for the patch if they haven't been created yet

					String ddragonVersion = DDragonManager.getLongVersion(patch);
					List<Short> championIds = getChampionIds(ddragonVersion);
					List<Short> perkIds = getPerkIds(ddragonVersion);

					// Create a new List so it doesn't need to be reallocated multiple times as it's filled
					stats = new ArrayList<>(perkIds.size() * championIds.size());

					for (short championId : championIds) {
						for (short perkId : perkIds) {
							AggregatedChampionStatsEntity stat = new AggregatedChampionStatsEntity();
							stat.setChampionId(championId);
							stat.setPerkId(perkId);
							stat.setPatch(patch);

							session.save(stat);
							stats.add(stat);
						}
					}
				} else {
					// Remove stats that are already being processed
					for (AggregatedChampionStatsEntity activeStat : activeStats) {
						stats.removeIf(stat -> stat.getPerkId() == activeStat.getPerkId() && stat.getChampionId() == activeStat.getChampionId());
					}
				}
				tx.commit();
				statsToAggregate.addAll(stats);
			} catch (Exception ex) {
				if (tx != null) {
					tx.markRollbackOnly();
				}
				getLogger().error("Error refilling lazy list", ex);
			}
		}

		return statsToAggregate.poll();
	}

	/**
	 * Gets the IDs of all available champions for a certain DDragon version
	 */
	private List<Short> getChampionIds(String ddragonVersion) throws IOException {
		return DDragonManager.getChampionList(ddragonVersion).data.values().stream().map(champion -> Short.parseShort(champion.key)).collect(Collectors.toList());
	}


	/**
	 * Gets the IDs of all available perks for a certain DDragon version
	 */
	private List<Short> getPerkIds(String ddragonVersion) throws IOException {
		List<Short> perkIds = new ArrayList<>();

		DDragonManager.StyleInfo[] styles = DDragonManager.getRuneInfo(ddragonVersion);
		for (DDragonManager.StyleInfo style : styles) {
			for (DDragonManager.StyleInfo.SlotInfo slot : style.slots) {
				for (DDragonManager.StyleInfo.Rune rune : slot.runes) {
					perkIds.add(rune.id);
				}
			}
		}

		return perkIds;
	}
}
