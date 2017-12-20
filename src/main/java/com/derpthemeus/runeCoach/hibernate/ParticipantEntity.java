package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "participants", schema = "rune_coach")
public class ParticipantEntity {
	private Long playerId;
	private boolean winner;
	private short championId;
	private long matchId;

	@Id
	@Column(name = "player_id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	@Basic
	@Column(name = "winner", nullable = false)
	public boolean isWinner() {
		return winner;
	}

	public void setWinner(boolean winner) {
		this.winner = winner;
	}

	@Basic
	@Column(name = "champion_id", nullable = false)
	public short getChampionId() {
		return championId;
	}

	public void setChampionId(short championId) {
		this.championId = championId;
	}

	@Basic
	@Column(name = "match_id", nullable = false)
	public long getMatchId() {
		return matchId;
	}

	public void setMatchId(long matchId) {
		this.matchId = matchId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ParticipantEntity that = (ParticipantEntity) o;
		return Objects.equals(playerId, that.playerId) &&
				winner == that.winner &&
				championId == that.championId &&
				matchId == that.matchId;
	}

	@Override
	public int hashCode() {

		return Objects.hash(playerId, winner, championId, matchId);
	}
}
