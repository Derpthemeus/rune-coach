package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "tag__champion", schema = "rune_coach")
@IdClass(TagChampionEntityPK.class)
public class TagChampionEntity {
	private short championId;
	private short tagId;

	@Id
	@Column(name = "champion_id", nullable = false)
	public short getChampionId() {
		return championId;
	}

	public void setChampionId(short championId) {
		this.championId = championId;
	}

	@Id
	@Column(name = "tag_id", nullable = false, length = 20)
	public short getTagId() {
		return tagId;
	}

	public void setTagId(short tagId) {
		this.tagId = tagId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TagChampionEntity that = (TagChampionEntity) o;
		return championId == that.championId &&
				Objects.equals(tagId, that.tagId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(championId, tagId);
	}
}
