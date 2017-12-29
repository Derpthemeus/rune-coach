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
@Table(name = "aggregated_tag_stats", schema = "rune_coach")
@IdClass(AggregatedTagStatsEntityPK.class)
public class AggregatedTagStatsEntity extends AggregatedStatsEntity {
	private short tagId;
	private Timestamp lastUpdated;

	@Id
	@Column(name = "tag_id", nullable = false, length = 20)
	public short getTagId() {
		return tagId;
	}

	public void setTagId(short tagId) {
		this.tagId = tagId;
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
		AggregatedTagStatsEntity that = (AggregatedTagStatsEntity) o;
		return perkId == that.perkId &&
				Objects.equals(tagId, that.tagId) &&
				Objects.equals(patch, that.patch);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tagId, patch, perkId);
	}
}
