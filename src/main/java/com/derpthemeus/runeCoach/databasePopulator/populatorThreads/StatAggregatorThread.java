package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.StatAggregatorSupervisor;
import com.derpthemeus.runeCoach.hibernate.AggregatedChampionStatsEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class StatAggregatorThread extends PopulatorThread {

	private AggregatedChampionStatsEntity stat;
	// The LoL patch (e.g. "7.24") that this thread is aggregating stats for
	private String patch;

	@Override
	public void runOperation() {
		Transaction tx = null;
		try (Session session = getSupervisor().getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			// Use the latest patch if one hasn't been specified
			if (patch == null) {
				System.out.println("StatAggregatorThread #" + this.getId() + " is defaulting to most recent patch");
				patch = DDragonManager.convertToShortVersion(DDragonManager.getLatestVersion());
			}

			stat = getSupervisor().getStatToAggregate(patch);
			// TODO lock `stat`?

			Query query = session.createQuery(
					"SELECT COUNT(*),SUM(perk.var1),SUM(perk.var2),SUM(perk.var3),SUM(CASE WHEN participant.winner=TRUE THEN 1 ELSE 0 END),MAX(perk.playerId)" +
							" FROM ParticipantPerkEntity AS perk INNER JOIN ParticipantEntity AS participant ON participant.playerId=perk.playerId INNER JOIN MatchEntity AS game ON game.matchId=participant.matchId" +
							" WHERE participant.championId=:champId AND perk.perkId=:perkId AND game.patch=:patch AND participant.playerId>:lastPlayerId"
			).setParameter("perkId", stat.getPerkId()).setParameter("champId", stat.getChampionId()).setParameter("patch", stat.getPatch()).setParameter("lastPlayerId", stat.getLastPlayerId());
			Object[] result = (Object[]) query.getSingleResult();

			long count = (long) result[0];
			// The values for `SUM` and `MAX` columns will be `null` if `count` is 0
			if (count > 0) {
				stat.setTotalMatches(stat.getTotalMatches() + count);
				stat.setVar1Total(stat.getVar1Total() + (long) result[1]);
				stat.setVar2Total(stat.getVar2Total() + (long) result[2]);
				stat.setVar3Total(stat.getVar3Total() + (long) result[3]);

				stat.setTotalWins((long) result[4]);
				stat.setLastPlayerId((long) result[5]);

				session.saveOrUpdate(stat);
			}

			tx.commit();
		} catch (Exception ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			handleException(ex);
		}
		stat = null;
	}

	@Override
	public StatAggregatorSupervisor getSupervisor() {
		return StatAggregatorSupervisor.getInstance();
	}

	public AggregatedChampionStatsEntity getActiveStat() {
		return this.stat;
	}

	/**
	 * Sets the patch that this thread should aggregate stats for. If not called or the patch is set to `null`, the thread will aggregate stats for the latest patch (as of the time that it starts)
	 */
	public void setPatch(String patch) {
		this.patch = patch;
	}
}
