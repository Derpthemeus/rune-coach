package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "aggregated_champion_stats", schema = "rune_coach")
@IdClass(AggregatedChampionStatsEntityPK.class)
public class AggregatedChampionStatsEntity extends AggregatedStatsEntity {
	private short championId;
	private long lastPlayerId;

	@Id
	@Column(name = "champion_id", nullable = false)
	public short getChampionId() {
		return championId;
	}

	public void setChampionId(short championId) {
		this.championId = championId;
	}

	@Basic
	@Column(name = "last_player_id", nullable = false)
	public long getLastPlayerId() {
		return lastPlayerId;
	}

	public void setLastPlayerId(long lastPlayerId) {
		this.lastPlayerId = lastPlayerId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AggregatedChampionStatsEntity that = (AggregatedChampionStatsEntity) o;
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
