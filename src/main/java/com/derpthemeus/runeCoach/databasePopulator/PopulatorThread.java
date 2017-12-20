package com.derpthemeus.runeCoach.databasePopulator;

public abstract class PopulatorThread<T extends PopulatorThread> extends Thread {

	private boolean keepAlive = true;

	// TODO handle exceptions better
	protected void handleException(Exception ex) {
		ex.printStackTrace();
	}

	/**
	 * Kills the thread after it finishes its current operation
	 */
	public void killThread() {
		this.keepAlive = false;
	}


	/**
	 * Removes this thread from the list of running threads
	 */
	private void disposeThread() {
		getSupervisor().getRunningThreads().remove(this);
	}


	@Override
	public void run() {
		while (keepAlive) {
			runOperation();
		}
		disposeThread();
	}

	public abstract void runOperation();

	public abstract PopulatorThreadSupervisor getSupervisor();
}
