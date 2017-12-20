package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "summoners", schema = "rune_coach")
public class SummonerEntity {
	private long summonerId;
	private Long accountId;
	private String league;
	private Timestamp matchesLastUpdated = new Timestamp(0);
	private Timestamp leagueLastUpdated = new Timestamp(0);
	private Timestamp lastMatchTimestamp = new Timestamp(0);

	@Id
	@Column(name = "summoner_id", nullable = false)
	public long getSummonerId() {
		return summonerId;
	}

	public void setSummonerId(long summonerId) {
		this.summonerId = summonerId;
	}

	@Basic
	@Column(name = "account_id")
	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	@Basic
	@Column(name = "league", length = 36)
	public String getLeague() {
		return league;
	}

	public void setLeague(String league) {
		this.league = league;
	}

	@Basic
	@Column(name = "matches_last_updated")
	public Timestamp getMatchesLastUpdated() {
		return matchesLastUpdated;
	}

	public void setMatchesLastUpdated(Timestamp matchesLastUpdated) {
		this.matchesLastUpdated = matchesLastUpdated;
	}

	@Basic
	@Column(name = "league_last_updated", nullable = false)
	public Timestamp getLeagueLastUpdated() {
		return leagueLastUpdated;
	}

	public void setLeagueLastUpdated(Timestamp leagueLastUpdated) {
		this.leagueLastUpdated = leagueLastUpdated;
	}

	@Basic
	@Column(name = "last_match_timestamp", nullable = false)
	public Timestamp getLastMatchTimestamp() {
		return lastMatchTimestamp;
	}

	public void setLastMatchTimestamp(Timestamp lastMatchTimestamp) {
		this.lastMatchTimestamp = lastMatchTimestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SummonerEntity that = (SummonerEntity) o;
		return summonerId == that.summonerId &&
				Objects.equals(accountId, that.accountId) &&
				Objects.equals(league, that.league) &&
				Objects.equals(matchesLastUpdated, that.matchesLastUpdated) &&
				Objects.equals(leagueLastUpdated, that.leagueLastUpdated) &&
				Objects.equals(lastMatchTimestamp, that.lastMatchTimestamp);
	}

	@Override
	public int hashCode() {

		return Objects.hash(summonerId, accountId, league, matchesLastUpdated, leagueLastUpdated, lastMatchTimestamp);
	}
}
