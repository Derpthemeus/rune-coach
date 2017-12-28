package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.TagStatAggregatorSupervisor;
import com.derpthemeus.runeCoach.hibernate.AggregatedTagStatsEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;

public class TagStatAggregatorThread extends PopulatorThread {

	private AggregatedTagStatsEntity stat;
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

		stat = getSupervisor().getStatToAggregate(patch);
		// Sleep for 10 seconds if there is no work to be done
		if (stat == null) {
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
			// TODO lock `stat`?

			Query query = session.createQuery(
					"SELECT SUM(totalMatches),SUM(var1Total),SUM(var2Total),SUM(var3Total),SUM(totalWins)" +
							" FROM AggregatedChampionStatsEntity WHERE championId IN (SELECT championId FROM TagChampionEntity)" +
							" AND perkId=:perkId AND patch=:patch"
			).setParameter("perkId", stat.getPerkId()).setParameter("patch", stat.getPatch());
			Object[] result = (Object[]) query.getSingleResult();

			// The values for `SUM`s will be `null` if there are no stats to aggregate
			if (result[0] != null) {
				stat.setTotalMatches(stat.getTotalMatches() + (long) result[0]);
				stat.setVar1Total(stat.getVar1Total() + (long) result[1]);
				stat.setVar2Total(stat.getVar2Total() + (long) result[2]);
				stat.setVar3Total(stat.getVar3Total() + (long) result[3]);

				stat.setTotalWins((long) result[4]);
			}
			stat.setLastUpdated(new Timestamp(Calendar.getInstance().getTimeInMillis()));

			session.update(stat);

			tx.commit();
		} catch (Exception ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			getLogger().error("Error aggregating stats for perk " + stat.getPerkId() + " for tag '" + stat.getTagId() + "' during patch " + stat.getPatch(), ex);
		}
		stat = null;
	}

	@Override
	public TagStatAggregatorSupervisor getSupervisor() {
		return TagStatAggregatorSupervisor.getInstance();
	}

	public AggregatedTagStatsEntity getActiveStat() {
		return this.stat;
	}

	/**
	 * Sets the patch that this thread should aggregate stats for. If not called or the patch is set to `null`, the thread will aggregate stats for the latest patch (as of the time that it starts)
	 */
	public void setPatch(String patch) {
		this.patch = patch;
	}
}
