package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class PerkScoreEntityPK implements Serializable {
	private short perkId;
	private short championId;
	private String patch;
	private PerkScoreEntity.ScoreType scoreType;

	@Column(name = "perk_id", nullable = false)
	@Id
	public short getPerkId() {
		return perkId;
	}

	public void setPerkId(short perkId) {
		this.perkId = perkId;
	}

	@Column(name = "champion_id", nullable = false)
	@Id
	public short getChampionId() {
		return championId;
	}

	public void setChampionId(short championId) {
		this.championId = championId;
	}

	@Column(name = "patch", nullable = false, length = 6)
	@Id
	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	@Column(name = "score_type", nullable = false)
	@Id
	@Enumerated(EnumType.STRING)
	public PerkScoreEntity.ScoreType getScoreType() {
		return scoreType;
	}

	public void setScoreType(PerkScoreEntity.ScoreType scoreType) {
		this.scoreType = scoreType;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PerkScoreEntityPK that = (PerkScoreEntityPK) o;
		return perkId == that.perkId &&
				championId == that.championId &&
				Objects.equals(patch, that.patch) &&
				scoreType == that.scoreType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(perkId, championId, patch, scoreType);
	}
}
