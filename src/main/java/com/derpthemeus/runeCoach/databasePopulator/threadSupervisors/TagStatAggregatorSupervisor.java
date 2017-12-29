package com.derpthemeus.runeCoach.databasePopulator.threadSupervisors;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.TagStatAggregatorThread;
import com.derpthemeus.runeCoach.hibernate.AggregatedTagStatsEntity;
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

public class TagStatAggregatorSupervisor extends PopulatorThreadSupervisor<TagStatAggregatorThread> {

	private static TagStatAggregatorSupervisor instance = new TagStatAggregatorSupervisor();

	@Override
	public Class<TagStatAggregatorThread> getThreadClass() {
		return TagStatAggregatorThread.class;
	}

	public static TagStatAggregatorSupervisor getInstance() {
		return instance;
	}

	private Map<String, Queue<AggregatedTagStatsEntity>> statsToAggregateByPatch = new HashMap<>();

	public synchronized AggregatedTagStatsEntity getStatToAggregate(String patch) {
		if (!statsToAggregateByPatch.containsKey(patch)) {
			statsToAggregateByPatch.put(patch, new ArrayDeque<>());
		}
		Queue<AggregatedTagStatsEntity> statsToAggregate = statsToAggregateByPatch.get(patch);

		// Refill the queue by querying the database
		if (statsToAggregate.isEmpty()) {
			List<AggregatedTagStatsEntity> activeStats = getRunningThreads().stream().map(
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
				Query query = session.createQuery("FROM AggregatedTagStatsEntity WHERE patch=:patch ORDER BY lastUpdated ASC")
						.setParameter("patch", patch);
				List<AggregatedTagStatsEntity> stats = query.getResultList();

				if (stats.isEmpty()) {
					// Create aggregated stats for the patch if they haven't been created yet

					String ddragonVersion = DDragonManager.getLongVersion(patch);

					List<Short> tagIds = session.createQuery("SELECT tagId FROM TagEntity").getResultList();
					List<Short> perkIds = getPerkIds(ddragonVersion);

					// Create a new List so it doesn't need to be reallocated multiple times as it's filled
					stats = new ArrayList<>(perkIds.size() * tagIds.size());

					for (short tagId : tagIds) {
						for (short perkId : perkIds) {
							AggregatedTagStatsEntity stat = new AggregatedTagStatsEntity();
							stat.setTagId(tagId);
							stat.setPerkId(perkId);
							stat.setPatch(patch);
							stat.setLastUpdated(new Timestamp(0));

							session.save(stat);
							stats.add(stat);
						}
					}
				} else {
					// Remove stats that are already being processed
					for (AggregatedTagStatsEntity activeStat : activeStats) {
						stats.removeIf(stat -> stat.getPerkId() == activeStat.getPerkId() && stat.getTagId() == activeStat.getTagId());
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
