package com.derpthemeus.runeCoach.databasePopulator;

import com.derpthemeus.runeCoach.RuneCoach;
import no.stelar7.api.l4j8.impl.L4J8;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class PopulatorThreadSupervisor<T extends PopulatorThread> {

	private List<T> runningThreads = new ArrayList<>();

	public L4J8 getL4j8() {
		return RuneCoach.getL4J8();
	}

	public SessionFactory getSessionFactory() {
		return RuneCoach.getSessionFactory();
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
