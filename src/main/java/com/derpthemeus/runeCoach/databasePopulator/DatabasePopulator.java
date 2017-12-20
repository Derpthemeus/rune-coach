package com.derpthemeus.runeCoach.databasePopulator;

import com.derpthemeus.runeCoach.databasePopulator.threadManagers.MatchDownloaderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadManagers.MatchFinderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadManagers.SummonerAccountIdUpdaterSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadManagers.SummonerFinderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadManagers.SummonerLeagueUpdaterSupervisor;

import java.util.HashMap;
import java.util.Map;

public class DatabasePopulator {

	private static HashMap<PopulatorThreadSupervisor, Integer> threadCounts = new HashMap<>();

	static {
		threadCounts.put(SummonerAccountIdUpdaterSupervisor.getInstance(), 2);
		threadCounts.put(SummonerLeagueUpdaterSupervisor.getInstance(), 2);
		threadCounts.put(SummonerFinderSupervisor.getInstance(), 2);
		threadCounts.put(MatchFinderSupervisor.getInstance(), 2);
		threadCounts.put(MatchDownloaderSupervisor.getInstance(), 4);
	}

	public static void main(String[] args) {
		for (Map.Entry<PopulatorThreadSupervisor, Integer> entry : threadCounts.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				entry.getKey().spawnThread();
			}
		}
	}
}
