package com.derpthemeus.runeCoach;

import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import no.stelar7.api.l4j8.basic.utils.Utils;
import no.stelar7.api.l4j8.pojo.match.Match;
import no.stelar7.api.l4j8.pojo.match.ParticipantIdentity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;

/**
 * Adds players from seed data (matches are from before Runes Reforged, and cannot be used) to the database
 */
public class SetupSeedData {
	public static void main(String[] args) {


		Configuration config = new Configuration()
				.configure()
				.setProperty("hibernate.connection.username", System.getenv("MYSQL_USERNAME"))
				.setProperty("hibernate.connection.password", System.getenv("MYSQL_PASSWORD"));

		SessionFactory sessionFactory = config.buildSessionFactory();


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

			for (Match match : matches) {
				// Seed data is from before runes reforged, so the matches themselves can't be used
				for (ParticipantIdentity identity : match.getParticipantIdentities()) {
					SummonerEntity summonerEntity = new SummonerEntity();
					summonerEntity.setAccountId(identity.getPlayer().getAccountId());
					summonerEntity.setSummonerId(identity.getPlayer().getSummonerId());


					// This will error a few times due to some summoner appearing in the seed data twice.
					Transaction tx = null;
					try (Session session = sessionFactory.openSession()) {
						tx = session.beginTransaction();
						session.save(summonerEntity);
						tx.commit();
					} catch (Exception ex) {
						if (tx != null) {
							tx.markRollbackOnly();
						}
						ex.printStackTrace();
					}
				}
			}
			System.out.println("Finished file " + seedFile);
		}
	}

	private class Matches implements Serializable {
		public Match[] matches;
	}
}

