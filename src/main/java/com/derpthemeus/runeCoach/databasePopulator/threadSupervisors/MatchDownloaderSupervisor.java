package com.derpthemeus.runeCoach.databasePopulator.threadSupervisors;

import com.derpthemeus.runeCoach.databasePopulator.populatorThreads.MatchDownloaderThread;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.hibernate.MatchEntity;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

public class MatchDownloaderSupervisor extends PopulatorThreadSupervisor<MatchDownloaderThread> {

	private static MatchDownloaderSupervisor instance = new MatchDownloaderSupervisor();

	@Override
	public Class<MatchDownloaderThread> getThreadClass() {
		return MatchDownloaderThread.class;
	}

	public static MatchDownloaderSupervisor getInstance() {
		return instance;
	}

	private Queue<MatchEntity> matchesToDownload = new ArrayDeque<>();

	public synchronized MatchEntity getMatchToDownload() {
		// Refill the queue by querying the database
		if (matchesToDownload.isEmpty()) {
			List<Long> activeMatchIds = getRunningThreads().stream().map(
					thread -> {
						if (thread.getActiveMatch() != null) {
							return thread.getActiveMatch().getMatchId();
						} else {
							return null;
						}
					}
			).filter(Objects::nonNull).collect(Collectors.toList());

			// The database query doesn't work if the list is empty, so a dummy element must be added
			if (activeMatchIds.isEmpty()) {
				activeMatchIds.add(-1L);
			}

			try (Session session = getSessionFactory().openSession()) {
				Query query = session.createQuery("FROM MatchEntity WHERE hasBeenDownloaded=false AND matchId NOT IN :activeMatchIds")
						.setParameter("activeMatchIds", activeMatchIds);
				// TODO pick max results, and update dynamically based on thread count?
				List<MatchEntity> matches = query.setMaxResults(200).getResultList();
				matchesToDownload.addAll(matches);
			} catch (Exception ex) {
				// TODO handle exception
				ex.printStackTrace();
			}
		}
		return matchesToDownload.remove();
	}
}
