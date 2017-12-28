package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerAccountIdUpdaterSupervisor;
import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import no.stelar7.api.l4j8.basic.constants.api.Platform;
import no.stelar7.api.l4j8.pojo.summoner.Summoner;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.LockModeType;


/**
 * Sets the `account_id` field for summoners in the database that have not had it set yet.
 */
public class SummonerAccountIdUpdaterThread extends PopulatorThread {
	private SummonerEntity summonerEntity;


	@Override
	public void runOperation() {
		summonerEntity = getSupervisor().getSummonerToUpdate();
		// Sleep for 10 seconds if there is no work to be done
		if (summonerEntity == null) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				getLogger().error(ex);
			}
			return;
		}

		Transaction tx = null;
		try (Session session = getSupervisor().getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.load(summonerEntity, summonerEntity.getSummonerId());
			session.lock(summonerEntity, LockModeType.PESSIMISTIC_WRITE);

			Summoner summoner = getSupervisor().getL4j8().getSummonerAPI().getSummonerById(Platform.NA1, summonerEntity.getSummonerId());
			summonerEntity.setAccountId(summoner.getAccountId());

			session.update(summonerEntity);
			tx.commit();
		} catch (HibernateException ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			getLogger().error("Error updating account ID for summoner " + summonerEntity.getSummonerId(), ex);
		}
		summonerEntity = null;
	}

	/**
	 * Returns the SummonerEntity this thread is currently updating, or `null` if a summoner is nor currently being updated
	 *
	 * @return The SummonerEntity being updated, or `null` if the thread is not currently updating a summoner
	 */
	public SummonerEntity getActiveSummoner() {
		return summonerEntity;
	}

	@Override
	public SummonerAccountIdUpdaterSupervisor getSupervisor() {
		return SummonerAccountIdUpdaterSupervisor.getInstance();
	}
}
