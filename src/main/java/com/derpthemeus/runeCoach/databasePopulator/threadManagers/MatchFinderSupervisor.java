package com.derpthemeus.runeCoach.databasePopulator.threadManagers;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.MatchFinderThread;
import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.sql.Time;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

public class MatchFinderSupervisor extends PopulatorThreadSupervisor<MatchFinderThread> {

	private static MatchFinderSupervisor instance = new MatchFinderSupervisor();

	@Override
	public Class<MatchFinderThread> getThreadClass() {
		return MatchFinderThread.class;
	}

	public static MatchFinderSupervisor getInstance() {
		return instance;
	}

	private Queue<SummonerEntity> summonersToCheck = new ArrayDeque<>();

	public synchronized SummonerEntity getSummonerToCheck() {
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
				Calendar calendar = Calendar.getInstance();
				// TODO pick update cutoff range
				calendar.add(Calendar.DATE, -3);

				// TODO lock results?
				Query query = session.createQuery("FROM SummonerEntity WHERE accountId != null AND matchesLastUpdated < :cutoffDate AND summonerId NOT IN :activeSummonerIds ORDER BY matchesLastUpdated ASC")
						.setParameter("cutoffDate", new Time(calendar.getTimeInMillis())).setParameter("activeSummonerIds", activeSummonerIds);
				// TODO pick max results, and update dynamically based on thread count?
				List<SummonerEntity> summoners = query.setMaxResults(30).getResultList();
				summonersToCheck.addAll(summoners);
			} catch (Exception ex) {
				// TODO handle exception
				ex.printStackTrace();
			}
		}
		return summonersToCheck.remove();
	}
}
