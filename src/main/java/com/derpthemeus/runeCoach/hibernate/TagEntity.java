package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "tags", schema = "rune_coach")
public class TagEntity {
	private String tagId;
	private String noun;
	private String adjective;
	private String verb;
	private String note;

	@Id
	@Column(name = "tag_id", nullable = false, length = 20)
	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	@Basic
	@Column(name = "noun", length = 50)
	public String getNoun() {
		return noun;
	}

	public void setNoun(String noun) {
		this.noun = noun;
	}

	@Basic
	@Column(name = "adjective", length = 50)
	public String getAdjective() {
		return adjective;
	}

	public void setAdjective(String adjective) {
		this.adjective = adjective;
	}

	@Basic
	@Column(name = "verb", length = 50)
	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	@Basic
	@Column(name = "_note", length = 500)
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TagEntity tagEntity = (TagEntity) o;
		return Objects.equals(tagId, tagEntity.tagId) &&
				Objects.equals(noun, tagEntity.noun) &&
				Objects.equals(adjective, tagEntity.adjective) &&
				Objects.equals(verb, tagEntity.verb) &&
				Objects.equals(note, tagEntity.note);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tagId, noun, adjective, verb, note);
	}
}
