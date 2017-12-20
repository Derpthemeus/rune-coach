package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadManagers.MatchDownloaderSupervisor;
import com.derpthemeus.runeCoach.hibernate.MatchEntity;
import com.derpthemeus.runeCoach.hibernate.ParticipantEntity;
import com.derpthemeus.runeCoach.hibernate.ParticipantPerkEntity;
import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import no.stelar7.api.l4j8.basic.constants.api.Platform;
import no.stelar7.api.l4j8.pojo.match.Match;
import no.stelar7.api.l4j8.pojo.match.MatchPerk;
import no.stelar7.api.l4j8.pojo.match.Participant;
import no.stelar7.api.l4j8.pojo.match.ParticipantIdentity;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.LockModeType;

/**
 * Download matches that have been tracked but not downloaded, and tracks participants
 */
public class MatchDownloaderThread extends PopulatorThread {
	private MatchEntity matchEntity;

	@Override
	public void runOperation() {
		matchEntity = getSupervisor().getMatchToDownload();

		Transaction tx = null;
		try (Session session = getSupervisor().getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			Match match = getSupervisor().getL4j8().getMatchAPI().getMatch(Platform.NA1, matchEntity.getMatchId());

			// Trim the version down to just major and minor version
			String longVersion = match.getMatchVersion();
			String shortVersion = longVersion.substring(0, longVersion.indexOf(".", longVersion.indexOf(".") + 1));
			matchEntity.setPatch(shortVersion);

			// Download data for each participant in the match
			for (Participant participant : match.getParticipants()) {
				ParticipantEntity participantEntity = new ParticipantEntity();

				participantEntity.setChampionId((short) participant.getChampionId());
				participantEntity.setWinner(match.didWin(participant));
				participantEntity.setMatchId(match.getMatchId());

				session.save(participantEntity);

				for (MatchPerk perk : participant.getStats().getPerks().getPerks()) {
					ParticipantPerkEntity perkEntity = new ParticipantPerkEntity();

					perkEntity.setPerkId((short) perk.getPerkId());
					perkEntity.setVar1(perk.getPerkVar1());
					perkEntity.setVar2(perk.getPerkVar2());
					perkEntity.setVar3(perk.getPerkVar3());
					perkEntity.setPlayerId(participantEntity.getPlayerId());

					session.save(perkEntity);
				}
			}

			// Track the summoners in the match (if they haven't been tracked already)
			for (ParticipantIdentity identity : match.getParticipantIdentities()) {
				Query summonerQuery = session.createQuery("FROM SummonerEntity WHERE summonerId = :summonerId").setParameter("summonerId", identity.getPlayer().getSummonerId()).setLockMode(LockModeType.PESSIMISTIC_READ);

				// Skip the summoner if they are already tracked
				if (summonerQuery.uniqueResult() != null) {
					continue;
				}

				SummonerEntity summonerEntity = new SummonerEntity();
				summonerEntity.setSummonerId(identity.getPlayer().getSummonerId());
				summonerEntity.setAccountId(identity.getPlayer().getCurrentAccountId());

				session.save(summonerEntity);
			}

			matchEntity.setHasBeenDownloaded(true);
			session.update(matchEntity);
			tx.commit();
		} catch (HibernateException ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			handleException(ex);
		}
		matchEntity = null;
	}

	/**
	 * Returns the MatchEntity this thread is currently downloading, or `null` if a match is nor currently being downloaded
	 *
	 * @return The MatchEntity being downloaded, or `null` if the thread is not currently downloading a match.
	 */
	public MatchEntity getActiveMatch() {
		return matchEntity;
	}


	@Override
	public MatchDownloaderSupervisor getSupervisor() {
		return MatchDownloaderSupervisor.getInstance();
	}
}
