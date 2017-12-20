package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "participant_perks", schema = "rune_coach")
@IdClass(ParticipantPerkEntityPK.class)
public class ParticipantPerkEntity {
	private long playerId;
	private short perkId;
	private int var1;
	private int var2;
	private int var3;

	@Id
	@Column(name = "player_id", nullable = false)
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	@Id
	@Column(name = "perk_id", nullable = false)
	public short getPerkId() {
		return perkId;
	}

	public void setPerkId(short perkId) {
		this.perkId = perkId;
	}

	@Basic
	@Column(name = "var1", nullable = false)
	public int getVar1() {
		return var1;
	}

	public void setVar1(int var1) {
		// Some matches have perk variables set to negative values; this seems to be a bug
		if (var1 > 0) {
			this.var1 = var1;
		} else {
			this.var1 = 0;
		}
	}

	@Basic
	@Column(name = "var2", nullable = false)
	public int getVar2() {
		return var2;
	}

	public void setVar2(int var2) {
		this.var2 = var2;
		// Some matches have perk variables set to negative values; this seems to be a bug
		if (var2 > 0) {
			this.var2 = var2;
		} else {
			this.var2 = 0;
		}
	}

	@Basic
	@Column(name = "var3", nullable = false)
	public int getVar3() {
		return var3;
	}

	public void setVar3(int var3) {
		// Some matches have perk variables set to negative values; this seems to be a bug
		if (var3 > 0) {
			this.var3 = var3;
		} else {
			this.var3 = 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ParticipantPerkEntity that = (ParticipantPerkEntity) o;
		return playerId == that.playerId &&
				perkId == that.perkId &&
				var1 == that.var1 &&
				var2 == that.var2 &&
				var3 == that.var3;
	}

	@Override
	public int hashCode() {

		return Objects.hash(playerId, perkId, var1, var2, var3);
	}
}
