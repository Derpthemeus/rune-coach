package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class ParticipantPerkEntityPK implements Serializable {
	private long playerId;
	private short perkId;

	@Column(name = "player_id", nullable = false)
	@Id
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
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
		ParticipantPerkEntityPK that = (ParticipantPerkEntityPK) o;
		return playerId == that.playerId &&
				perkId == that.perkId;
	}

	@Override
	public int hashCode() {

		return Objects.hash(playerId, perkId);
	}
}
