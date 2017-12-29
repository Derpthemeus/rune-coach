package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class AggregatedTagStatsEntityPK implements Serializable {
	private short tagId;
	private String patch;
	private short perkId;

	@Column(name = "tag_id", nullable = false, length = 20)
	@Id
	public short getTagId() {
		return tagId;
	}

	public void setTagId(short tagId) {
		this.tagId = tagId;
	}

	@Column(name = "patch", nullable = false, length = 6)
	@Id
	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	@Column(name = "perk_id", nullable = false)
	@Id
	public short getPerkId() {
		return perkId;
	}

	public void setPerkId(short perkId) {
		this.perkId = perkId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AggregatedTagStatsEntityPK that = (AggregatedTagStatsEntityPK) o;
		return perkId == that.perkId &&
				Objects.equals(tagId, that.tagId) &&
				Objects.equals(patch, that.patch);
	}

	@Override
	public int hashCode() {

		return Objects.hash(tagId, patch, perkId);
	}
}
