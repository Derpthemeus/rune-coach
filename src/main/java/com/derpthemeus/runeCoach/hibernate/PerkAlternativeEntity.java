package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "perk_alternatives", schema = "rune_coach")
@IdClass(PerkAlternativeEntityPK.class)
public class PerkAlternativeEntity {
	private short perkId;
	private short championId;
	private Short alternative;
	private Short reason1;
	private Short reason2;
	private String patch;
	private Scope scope;
	private Timestamp lastUpdated;
	private double improvement;
	private double relativeScore;
	private Double absoluteScore;

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

	@Basic
	@Column(name = "alternative")
	public Short getAlternative() {
		return alternative;
	}

	public void setAlternative(Short alternative) {
		this.alternative = alternative;
	}

	@Basic
	@Column(name = "reason_1", length = 20)
	public Short getReason1() {
		return reason1;
	}

	public void setReason1(Short reason1) {
		this.reason1 = reason1;
	}

	@Basic
	@Column(name = "reason_2", length = 20)
	public Short getReason2() {
		return reason2;
	}

	public void setReason2(Short reason2) {
		this.reason2 = reason2;
	}

	@Id
	@Column(name = "patch", nullable = false, length = 6)
	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "scope", nullable = false)
	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
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
	@Column(name = "improvement", nullable = false)
	public double getImprovement() {
		return improvement;
	}

	public void setImprovement(double improvement) {
		this.improvement = improvement;
	}

	@Basic
	@Column(name = "relative_score", nullable = false)
	public double getRelativeScore() {
		return relativeScore;
	}

	public void setRelativeScore(double relativeScore) {
		this.relativeScore = relativeScore;
	}

	@Basic
	@Column(name = "absolute_score")
	public Double getAbsoluteScore() {
		return absoluteScore;
	}

	public void setAbsoluteScore(Double absoluteScore) {
		this.absoluteScore = absoluteScore;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PerkAlternativeEntity that = (PerkAlternativeEntity) o;
		return perkId == that.perkId &&
				championId == that.championId &&
				Objects.equals(alternative, that.alternative) &&
				Objects.equals(reason1, that.reason1) &&
				Objects.equals(reason2, that.reason2) &&
				Objects.equals(patch, that.patch) &&
				Objects.equals(scope, that.scope) &&
				Objects.equals(lastUpdated, that.lastUpdated);
	}

	@Override
	public int hashCode() {
		return Objects.hash(perkId, championId, alternative, reason1, reason2, patch, scope, lastUpdated);
	}

	public enum Scope {
		SLOT,
		SUBSTYLE,
		STYLE
	}
}
