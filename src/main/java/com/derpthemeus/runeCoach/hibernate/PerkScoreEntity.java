package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "perk_scores", schema = "rune_coach")
@IdClass(PerkScoreEntityPK.class)
public class PerkScoreEntity {
	private short perkId;
	private short championId;
	private String patch;
	private Double score;
	private Timestamp lastUpdated;
	private int games;

	@Id
	@Column(name = "perk_id", nullable = false)
	public short getPerkId() {
		return perkId;
	}

	public void setPerkId(short perkId) {
		this.perkId = perkId;
	}

	@Id
	@Column(name = "champion_id", nullable = false)
	public short getChampionId() {
		return championId;
	}

	public void setChampionId(short championId) {
		this.championId = championId;
	}

	@Id
	@Column(name = "patch", nullable = false, length = 6)
	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	@Basic
	@Column(name = "score")
	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	@Basic
	@Column(name = "last_updated", nullable = false)
	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Basic
	@Column(name = "games")
	public int getGames() {
		return games;
	}

	public void setGames(int games) {
		this.games = games;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PerkScoreEntity that = (PerkScoreEntity) o;
		return perkId == that.perkId &&
				championId == that.championId &&
				Double.compare(that.score, score) == 0 &&
				Objects.equals(patch, that.patch) &&
				Objects.equals(lastUpdated, that.lastUpdated);
	}

	@Override
	public int hashCode() {
		return Objects.hash(perkId, championId, patch, score, lastUpdated);
	}
}
