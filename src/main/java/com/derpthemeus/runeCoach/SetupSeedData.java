package com.derpthemeus.runeCoach;

import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import no.stelar7.api.l4j8.basic.utils.Utils;
import no.stelar7.api.l4j8.pojo.match.Match;
import no.stelar7.api.l4j8.pojo.match.ParticipantIdentity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;

/**
 * Adds players from seed data (matches are from before Runes Reforged, and cannot be used) to the database
 */
public class SetupSeedData {

	private static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
		for (int seedFile = 1; seedFile <= 10; seedFile++) {
			InputStreamReader reader;
			try {
				URL url = new URL("https://s3-us-west-1.amazonaws.com/riot-developer-portal/seed-data/matches" + seedFile + ".json");
				reader = new InputStreamReader(url.openStream());
			} catch (IOException ex) {
				logger.error("Error downloading file " + seedFile, ex);
				continue;
			}

			Match[] matches = Utils.getGson().fromJson(reader, Matches.class).matches;

			for (Match match : matches) {
				// Seed data is from before runes reforged, so the matches themselves can't be used
				for (ParticipantIdentity identity : match.getParticipantIdentities()) {
					SummonerEntity summonerEntity = new SummonerEntity();
					summonerEntity.setAccountId(identity.getPlayer().getAccountId());
					summonerEntity.setSummonerId(identity.getPlayer().getSummonerId());


					// This will error a few times due to some summoner appearing in the seed data twice.
					Transaction tx = null;
					try (Session session = RuneCoach.getSessionFactory().openSession()) {
						tx = session.beginTransaction();
						session.save(summonerEntity);
						tx.commit();
					} catch (Exception ex) {
						if (tx != null) {
							tx.markRollbackOnly();
						}
						logger.error("Error adding summoner" + summonerEntity.getSummonerId() + " to database (they may have already been added)", ex);
					}
				}
			}
			logger.info("Finished processing file " + seedFile + "/10");
		}
	}

	private class Matches implements Serializable {
		public Match[] matches;
	}
}

