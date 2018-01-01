package com.derpthemeus.runeCoach.databasePopulator.threadSupervisors;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.StatAggregatorThread;
import com.derpthemeus.runeCoach.hibernate.AggregatedStatsEntity;
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

public class StatAggregatorSupervisor extends PopulatorThreadSupervisor<StatAggregatorThread> {

	private static StatAggregatorSupervisor instance = new StatAggregatorSupervisor();

	@Override
	public Class<StatAggregatorThread> getThreadClass() {
		return StatAggregatorThread.class;
	}

	public static StatAggregatorSupervisor getInstance() {
		return instance;
	}

	private Map<String, Queue<AggregatedStatsEntity>> statsToAggregateByPatch = new HashMap<>();

	public synchronized AggregatedStatsEntity getStatToAggregate(String patch) {
		if (!statsToAggregateByPatch.containsKey(patch)) {
			statsToAggregateByPatch.put(patch, new ArrayDeque<>());
		}
		Queue<AggregatedStatsEntity> statsToAggregate = statsToAggregateByPatch.get(patch);

		// Refill the queue by querying the database
		if (statsToAggregate.isEmpty()) {
			List<AggregatedStatsEntity> activeStats = getRunningThreads().stream().map(StatAggregatorThread::getActiveStat).filter(Objects::nonNull).collect(Collectors.toList());

			Transaction tx = null;
			try (Session session = getSessionFactory().openSession()) {
				tx = session.beginTransaction();
				Query query = session.createQuery("FROM AggregatedStatsEntity WHERE patch=:patch ORDER BY lastUpdated ASC")
						.setParameter("patch", patch);
				List<AggregatedStatsEntity> stats = query.getResultList();


				// Create aggregated stats for the patch if they haven't been created yet
				if (stats.isEmpty()) {
					String ddragonVersion = DDragonManager.getLongVersion(patch);
					List<Short> championIds = DDragonManager.getChampionIds(ddragonVersion);
					championIds.addAll(session.createQuery("SELECT -tagId FROM TagEntity").getResultList());
					List<Short> perkIds = DDragonManager.getPerkIds(ddragonVersion);

					// Create a new List so it doesn't need to be reallocated multiple times as it's filled
					stats = new ArrayList<>(perkIds.size() * (championIds.size()));

					for (short championId : championIds) {
						for (short perkId : perkIds) {
							AggregatedStatsEntity stat = new AggregatedStatsEntity();
							stat.setChampionId(championId);
							stat.setPerkId(perkId);
							stat.setPatch(patch);
							stat.setLastUpdated(new Timestamp(0));

							session.save(stat);
							stats.add(stat);
						}
					}
				} else {
					// Remove stats that are already being processed
					for (AggregatedStatsEntity activeStat : activeStats) {
						stats.removeIf(stat ->
								stat.getPerkId() == activeStat.getPerkId()
										&& stat.getChampionId() == activeStat.getChampionId()
										&& stat.getPerkId() == activeStat.getPerkId()
										&& stat.getPatch().equals(activeStat.getPatch())
						);
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
}
