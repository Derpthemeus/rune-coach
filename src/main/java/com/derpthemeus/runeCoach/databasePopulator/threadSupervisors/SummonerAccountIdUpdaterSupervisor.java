package com.derpthemeus.runeCoach.databasePopulator.threadSupervisors;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.SummonerAccountIdUpdaterThread;
import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

public class SummonerAccountIdUpdaterSupervisor extends PopulatorThreadSupervisor<SummonerAccountIdUpdaterThread> {
	private static SummonerAccountIdUpdaterSupervisor instance = new SummonerAccountIdUpdaterSupervisor();

	@Override
	public Class<SummonerAccountIdUpdaterThread> getThreadClass() {
		return SummonerAccountIdUpdaterThread.class;
	}

	public static SummonerAccountIdUpdaterSupervisor getInstance() {
		return instance;
	}

	private Queue<SummonerEntity> summonersToCheck = new ArrayDeque<>();

	public synchronized SummonerEntity getSummonerToUpdate() {
		// Refill the queue by querying the database
		if (summonersToCheck.isEmpty()) {
			List<Long> activeSummonerIds = getRunningThreads().stream().map(
					thread -> {
						if (thread.getActiveSummoner() != null) {
							return thread.getActiveSummoner().getSummonerId();
						} else {
							return null;
						}
					}
			).filter(Objects::nonNull).collect(Collectors.toList());

			// The database query doesn't work if the list is empty, so a dummy element must be added
			if (activeSummonerIds.isEmpty()) {
				activeSummonerIds.add(-1L);
			}

			try (Session session = getSessionFactory().openSession()) {
				Query query = session.createQuery("FROM SummonerEntity WHERE accountId=null AND summonerId NOT IN :activeSummonerIds ORDER BY leagueLastUpdated")
						.setParameter("activeSummonerIds", activeSummonerIds);
				// TODO pick max results, and update dynamically based on thread count?
				List<SummonerEntity> summoners = query.setMaxResults(50).getResultList();
				summonersToCheck.addAll(summoners);
			} catch (Exception ex) {
				getLogger().error("Error refilling lazy list", ex);
			}
		}
		return summonersToCheck.poll();
	}
}
