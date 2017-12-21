package com.derpthemeus.runeCoach.databasePopulator.threadSupervisors;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.SummonerFinderThread;
import com.derpthemeus.runeCoach.hibernate.LeagueEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.sql.Time;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

public class SummonerFinderSupervisor extends PopulatorThreadSupervisor<SummonerFinderThread> {

	private static SummonerFinderSupervisor instance = new SummonerFinderSupervisor();


	@Override
	public Class<SummonerFinderThread> getThreadClass() {
		return SummonerFinderThread.class;
	}


	private Queue<LeagueEntity> leaguesToCheck = new ArrayDeque<>();

	public synchronized LeagueEntity getLeagueToCheck() {
		// Refill the queue by querying the database
		if (leaguesToCheck.isEmpty()) {
			List<String> activeLeagueIds = getRunningThreads().stream().map(
					thread -> {
						if (thread.getActiveLeague() != null) {
							return thread.getActiveLeague().getUuid();
						} else {
							return null;
						}
					}
			).filter(Objects::nonNull).collect(Collectors.toList());

			// The database query doesn't work if the list is empty, so a dummy element must be added
			if (activeLeagueIds.isEmpty()) {
				activeLeagueIds.add("");
			}

			try (Session session = getSessionFactory().openSession()) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE, -2);

				Query query = session.createQuery("FROM LeagueEntity WHERE lastChecked < :cutoffDate AND uuid NOT IN :activeLeagueIds ORDER BY lastChecked ASC")
						.setParameter("cutoffDate", new Time(calendar.getTimeInMillis())).setParameter("activeLeagueIds", activeLeagueIds);
				// TODO pick max results, and update dynamically based on thread count?
				List<LeagueEntity> leagues = query.setMaxResults(40).getResultList();
				leaguesToCheck.addAll(leagues);
			} catch (Exception ex) {
				// TODO handle exception
				ex.printStackTrace();
			}
		}
		return leaguesToCheck.remove();
	}

	public static SummonerFinderSupervisor getInstance() {
		return instance;
	}
}
