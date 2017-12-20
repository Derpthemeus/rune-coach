package com.derpthemeus.runeCoach;

import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import no.stelar7.api.l4j8.basic.utils.Utils;
import no.stelar7.api.l4j8.pojo.match.Match;
import no.stelar7.api.l4j8.pojo.match.ParticipantIdentity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.sql.Timestamp;

public class SetupSeedData {
	public static void main(String[] args) {

		for (int seedFile = 1; seedFile <= 10; seedFile++) {

			InputStreamReader reader;
			try {
				URL url = new URL("https://s3-us-west-1.amazonaws.com/riot-developer-portal/seed-data/matches" + seedFile + ".json");
				reader = new InputStreamReader(url.openStream());
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}

			Match[] matches = Utils.getGson().fromJson(reader, Matches.class).matches;


			System.out.println(matches.length);
			for (Match match : matches) {
				// Seed data is from before runes reforged, so the matches themselves can't be used
				for (ParticipantIdentity identity : match.getParticipantIdentities()) {
					SummonerEntity summonerEntity = new SummonerEntity();
					summonerEntity.setAccountId(identity.getPlayer().getAccountId());
					summonerEntity.setSummonerId(identity.getPlayer().getSummonerId());

					// TODO set it up so these aren't needed
					summonerEntity.setLeagueLastUpdated(new Timestamp(0));
					summonerEntity.setMatchesLastUpdated(new Timestamp(0));

					try {
						// TODO set this back up
						// DatabasePopulator.addRow(summonerEntity, false);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private class Matches implements Serializable {
		public Match[] matches;
	}
}

