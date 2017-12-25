package com.derpthemeus.runeCoach.databasePopulator;

import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.MatchDownloaderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.MatchFinderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.StatAggregatorSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerAccountIdUpdaterSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerFinderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerLeagueUpdaterSupervisor;

import java.util.HashMap;
import java.util.Map;

public class DatabasePopulator {

	private static HashMap<PopulatorThreadSupervisor, Integer> threadCounts = new HashMap<>();

	static {
		threadCounts.put(SummonerAccountIdUpdaterSupervisor.getInstance(), 2);
		threadCounts.put(SummonerLeagueUpdaterSupervisor.getInstance(), 2);
		threadCounts.put(SummonerFinderSupervisor.getInstance(), 2);
		threadCounts.put(MatchFinderSupervisor.getInstance(), 2);
		threadCounts.put(MatchDownloaderSupervisor.getInstance(), 3);
		threadCounts.put(StatAggregatorSupervisor.getInstance(), 2);
	}

	public static void main(String[] args) {
		for (Map.Entry<PopulatorThreadSupervisor, Integer> entry : threadCounts.entrySet()) {
			for (int i = 0; i < entry.getValue(); i++) {
				entry.getKey().spawnThread();
			}
		}
	}
}
