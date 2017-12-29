package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class PerkAlternativeEntityPK implements Serializable {
	private short perkId;
	private short championId;
	private String patch;
	private PerkAlternativeEntity.Scope scope;

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

	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "scope", nullable = false)
	public PerkAlternativeEntity.Scope getScope() {
		return scope;
	}

	public void setScope(PerkAlternativeEntity.Scope scope) {
		this.scope = scope;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PerkAlternativeEntityPK that = (PerkAlternativeEntityPK) o;
		return perkId == that.perkId &&
				championId == that.championId &&
				Objects.equals(patch, that.patch) &&
				Objects.equals(scope, that.scope);
	}

	@Override
	public int hashCode() {
		return Objects.hash(perkId, championId, patch, scope);
	}
}
