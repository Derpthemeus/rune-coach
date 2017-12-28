package com.derpthemeus.runeCoach.databasePopulator;

import no.stelar7.api.l4j8.basic.APICredentials;
import no.stelar7.api.l4j8.impl.L4J8;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;

public abstract class PopulatorThreadSupervisor<T extends PopulatorThread> {

	private static SessionFactory sessionFactory;
	private static L4J8 l4j8;

	private List<T> runningThreads = new ArrayList<>();

	static {
		l4j8 = new L4J8(new APICredentials(System.getenv("API_KEY"), null));
		Configuration config = new Configuration()
				.configure()
				.setProperty("hibernate.connection.url", System.getenv("MYSQL_CONNECTION_URL"))
				.setProperty("hibernate.connection.username", System.getenv("MYSQL_USERNAME"))
				.setProperty("hibernate.connection.password", System.getenv("MYSQL_PASSWORD"));

		sessionFactory = config.buildSessionFactory();
	}


	public L4J8 getL4j8() {
		return l4j8;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public abstract Class<T> getThreadClass();

	/**
	 * Spawns a new thread and adds it to the list of running threads
	 *
	 * @return The spawned thread, or `null` if an error occurred
	 */
	public T spawnThread() {
		try {
			T thread = getThreadClass().newInstance();
			runningThreads.add(thread);
			thread.start();
			return thread;
		} catch (Exception ex) {
			getLogger().error("Error spawning new PopulatorThread:", ex);
		}
		return null;
	}

	public List<T> getRunningThreads() {
		return runningThreads;
	}

	protected Logger getLogger() {
		return LogManager.getLogger();
	}
}
