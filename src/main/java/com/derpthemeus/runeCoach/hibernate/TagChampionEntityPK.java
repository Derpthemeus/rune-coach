package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class TagChampionEntityPK implements Serializable {
	private short championId;
	private String tagId;

	@Column(name = "champion_id", nullable = false)
	@Id
	public short getChampionId() {
		return championId;
	}

	public void setChampionId(short championId) {
		this.championId = championId;
	}

	@Column(name = "tag_id", nullable = false, length = 20)
	@Id
	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TagChampionEntityPK that = (TagChampionEntityPK) o;
		return championId == that.championId &&
				Objects.equals(tagId, that.tagId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(championId, tagId);
	}
}
