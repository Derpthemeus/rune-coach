package com.derpthemeus.runeCoach;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThreadSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.ChampionStatAggregatorSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.MatchDownloaderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.MatchFinderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.PerkScoreCalculatorSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerAccountIdUpdaterSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerFinderSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.SummonerLeagueUpdaterSupervisor;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.TagStatAggregatorSupervisor;

import java.util.HashMap;
import java.util.Map;

public class RuneCoach {

	// How many of each PopulatorThread should be run
	private static HashMap<PopulatorThreadSupervisor, Integer> threadCounts = new HashMap<>();

	static {
		threadCounts.put(SummonerAccountIdUpdaterSupervisor.getInstance(), 1);
		threadCounts.put(SummonerLeagueUpdaterSupervisor.getInstance(), 1);
		threadCounts.put(SummonerFinderSupervisor.getInstance(), 1);
		threadCounts.put(MatchFinderSupervisor.getInstance(), 1);
		threadCounts.put(MatchDownloaderSupervisor.getInstance(), 2);
		threadCounts.put(ChampionStatAggregatorSupervisor.getInstance(), 7);
		threadCounts.put(TagStatAggregatorSupervisor.getInstance(), 4);
		threadCounts.put(PerkScoreCalculatorSupervisor.getInstance(), 2);
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
}
