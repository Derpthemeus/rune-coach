package com.derpthemeus.runeCoach.databasePopulator.populatorThreads;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.databasePopulator.PopulatorThread;
import com.derpthemeus.runeCoach.databasePopulator.threadSupervisors.MatchDownloaderSupervisor;
import com.derpthemeus.runeCoach.hibernate.MatchEntity;
import com.derpthemeus.runeCoach.hibernate.ParticipantEntity;
import com.derpthemeus.runeCoach.hibernate.ParticipantPerkEntity;
import com.derpthemeus.runeCoach.hibernate.SummonerEntity;
import no.stelar7.api.l4j8.basic.constants.api.Platform;
import no.stelar7.api.l4j8.pojo.match.Match;
import no.stelar7.api.l4j8.pojo.match.MatchPerk;
import no.stelar7.api.l4j8.pojo.match.Participant;
import no.stelar7.api.l4j8.pojo.match.ParticipantIdentity;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.LockModeType;

/**
 * Download matches that have been tracked but not downloaded, and tracks participants
 */
public class MatchDownloaderThread extends PopulatorThread {
	private MatchEntity matchEntity;

	@Override
	public void runOperation() {
		matchEntity = getSupervisor().getMatchToDownload();
		// Sleep for 10 seconds if there is no work to be done
		if (matchEntity == null) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				getLogger().throwing(ex);
			}
			return;
		}

		Transaction tx = null;
		try (Session session = getSupervisor().getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			session.load(matchEntity, matchEntity.getMatchId());
			session.lock(matchEntity, LockModeType.PESSIMISTIC_WRITE);

			Match match = getSupervisor().getL4j8().getMatchAPI().getMatch(Platform.NA1, matchEntity.getMatchId());

			matchEntity.setPatch(DDragonManager.convertToShortVersion(match.getMatchVersion()));

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
				// Skip the summoner if they are already tracked
				if (session.get(SummonerEntity.class, identity.getPlayer().getSummonerId()) != null) {
					continue;
				}

				SummonerEntity summonerEntity = new SummonerEntity();
				summonerEntity.setSummonerId(identity.getPlayer().getSummonerId());
				summonerEntity.setAccountId(identity.getPlayer().getCurrentAccountId());

				session.save(summonerEntity);
			}

			matchEntity.setDownloaded(true);
			session.update(matchEntity);
			tx.commit();
		} catch (Exception ex) {
			if (tx != null) {
				tx.markRollbackOnly();
			}
			getLogger().error("Error downloading match " + matchEntity.getMatchId(), ex);
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
