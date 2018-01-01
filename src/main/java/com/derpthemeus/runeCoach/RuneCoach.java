package com.derpthemeus.runeCoach;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.MatchDownloaderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.MatchFinderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.PerkScoreCalculatorSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.StatAggregatorSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerAccountIdUpdaterSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerFinderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerLeagueUpdaterSupervisor;
import no.stelar7.api.l4j8.basic.APICredentials;
import no.stelar7.api.l4j8.impl.L4J8;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.HashMap;
import java.util.Map;

public class RuneCoach {

	// How many of each PopulatorThread should be run
	private static HashMap<PopulatorThreadSupervisor, Integer> threadCounts = new HashMap<>();


	private static SessionFactory sessionFactory;
	private static L4J8 l4j8;

	static {
		threadCounts.put(SummonerAccountIdUpdaterSupervisor.getInstance(), 1);
		threadCounts.put(SummonerLeagueUpdaterSupervisor.getInstance(), 1);
		threadCounts.put(SummonerFinderSupervisor.getInstance(), 1);
		threadCounts.put(MatchFinderSupervisor.getInstance(), 1);
		threadCounts.put(MatchDownloaderSupervisor.getInstance(), 1);
		threadCounts.put(StatAggregatorSupervisor.getInstance(), 3);
		threadCounts.put(PerkScoreCalculatorSupervisor.getInstance(), 6);

		l4j8 = new L4J8(new APICredentials(System.getenv("API_KEY"), null));
		Configuration config = new Configuration()
				.configure()
				.setProperty("hibernate.connection.url", System.getenv("MYSQL_CONNECTION_URL"))
				.setProperty("hibernate.connection.username", System.getenv("MYSQL_USERNAME"))
				.setProperty("hibernate.connection.password", System.getenv("MYSQL_PASSWORD"));

		sessionFactory = config.buildSessionFactory();
	}


	public static void main(String[] args) {
		startDatabasePopulators();
	}

	private static void startDatabasePopulators() {
		for (Map.Entry<PopulatorThreadSupervisor, Integer> entry : threadCounts.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				entry.getKey().spawnThread();
			}
		}
	}

	public static L4J8 getL4J8() {
		return l4j8;
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
