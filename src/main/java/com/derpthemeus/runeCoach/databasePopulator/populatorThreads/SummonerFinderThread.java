package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadManagers.SummonerFinderSupervisor;
import com.derpthemeus.runeCoach.hibernate.LeagueEntity;
import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import no.stelar7.api.l4j8.basic.constants.api.Platform;
import no.stelar7.api.l4j8.pojo.league.LeagueItem;
import no.stelar7.api.l4j8.pojo.league.LeagueList;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Find and track summoners in tracked leagues
 */
public class SummonerFinderThread extends PopulatorThread {
	private LeagueEntity leagueEntity;

	@Override
	public void runOperation() {
		Transaction tx = null;
		try (Session session = getSupervisor().getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			leagueEntity = getSupervisor().getLeagueToCheck();
			LeagueList league = getSupervisor().getL4j8().getLeagueAPI().getLeague(Platform.NA1, leagueEntity.getUuid());
			List<Long> summonerIds = league.getEntries().stream().map(LeagueItem::getSummonerId).collect(Collectors.toList());

			// Get all summoners in the league that are already in the database
			Query query = session.createQuery("FROM SummonerEntity WHERE summonerId IN (:summonerIds)").setParameter("summonerIds", summonerIds);
			List<SummonerEntity> summonerEntities = query.getResultList();

			for (SummonerEntity summonerEntity : summonerEntities) {
				summonerEntity.setLeagueLastUpdated(new Timestamp(Calendar.getInstance().getTimeInMillis()));
				summonerEntity.setLeague(league.getLeagueId());
				session.update(summonerEntity);

				// Remove the ID from the list so when the loop finishes, the list only contains summoners who are not yet tracked
				summonerIds.remove(summonerEntity.getSummonerId());
			}

			// Add new summoners to the database
			for (long summonerId : summonerIds) {
				SummonerEntity summonerEntity = new SummonerEntity();
				summonerEntity.setSummonerId(summonerId);
				summonerEntity.setLeague(league.getLeagueId());
				summonerEntity.setLeagueLastUpdated(new Timestamp(Calendar.getInstance().getTimeInMillis()));

				session.save(summonerEntity);
			}

			leagueEntity.setLastChecked(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			session.update(leagueEntity);
			tx.commit();
		} catch (HibernateException ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			handleException(ex);
		}
	}

	public LeagueEntity getActiveLeague() {
		return leagueEntity;
	}

	@Override
	public SummonerFinderSupervisor getSupervisor() {
		return SummonerFinderSupervisor.getInstance();
	}
}
