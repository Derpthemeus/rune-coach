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
@Table(name = "aggregated_stats", schema = "rune_coach")
@IdClass(AggregatedStatsEntityPK.class)
public class AggregatedStatsEntity {
	private short perkId;
	private short championId;
	private String patch;
	private long totalMatches;
	private long totalWins;
	private long var1Total;
	private long var2Total;
	private long var3Total;
	private long lastPlayerId;
	private Timestamp lastUpdated;

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
	@Column(name = "patch", nullable = false, length = 8)
	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	@Basic
	@Column(name = "total_matches", nullable = false)
	public long getTotalMatches() {
		return totalMatches;
	}

	public void setTotalMatches(long totalMatches) {
		this.totalMatches = totalMatches;
	}

	@Basic
	@Column(name = "total_wins", nullable = false)
	public long getTotalWins() {
		return totalWins;
	}

	public void setTotalWins(long totalWins) {
		this.totalWins = totalWins;
	}

	@Basic
	@Column(name = "var1_total", nullable = false)
	public long getVar1Total() {
		return var1Total;
	}

	public void setVar1Total(long var1Total) {
		this.var1Total = var1Total;
	}

	@Basic
	@Column(name = "var2_total", nullable = false)
	public long getVar2Total() {
		return var2Total;
	}

	public void setVar2Total(long var2Total) {
		this.var2Total = var2Total;
	}

	@Basic
	@Column(name = "var3_total", nullable = false)
	public long getVar3Total() {
		return var3Total;
	}

	public void setVar3Total(long var3Total) {
		this.var3Total = var3Total;
	}

	@Basic
	@Column(name = "last_player_id", nullable = false)
	public long getLastPlayerId() {
		return lastPlayerId;
	}

	public void setLastPlayerId(long lastPlayerId) {
		this.lastPlayerId = lastPlayerId;
	}

	@Basic
	@Column(name = "last_updated", nullable = false)
	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AggregatedStatsEntity that = (AggregatedStatsEntity) o;
		return championId == that.championId &&
				perkId == that.perkId &&
				var1Total == that.var1Total &&
				var2Total == that.var2Total &&
				var3Total == that.var3Total &&
				lastPlayerId == that.lastPlayerId &&
				totalMatches == that.totalMatches &&
				Objects.equals(patch, that.patch);
	}

	@Override
	public int hashCode() {
		return Objects.hash(championId, perkId, patch, var1Total, var2Total, var3Total, lastPlayerId, totalMatches);
	}
}
