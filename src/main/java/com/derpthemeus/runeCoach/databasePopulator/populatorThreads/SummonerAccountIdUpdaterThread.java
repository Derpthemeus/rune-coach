package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerAccountIdUpdaterSupervisor;
import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import no.stelar7.api.l4j8.basic.constants.api.Platform;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;


/**
 * Sets the `account_id` field for summoners in the database that have not had it set yet.
 */
public class SummonerAccountIdUpdaterThread extends PopulatorThread {
	private SummonerEntity summonerEntity;


	@Override
	public void runOperation() {
		summonerEntity = getSupervisor().getSummonerToUpdate();
		Transaction tx = null;
		try (Session session = getSupervisor().getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			getSupervisor().getL4j8().getSummonerAPI().getSummonerById(Platform.NA1, summonerEntity.getSummonerId());
			summonerEntity.setAccountId(summonerEntity.getAccountId());

			session.update(summonerEntity);
			tx.commit();
		} catch (HibernateException ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			handleException(ex);
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
