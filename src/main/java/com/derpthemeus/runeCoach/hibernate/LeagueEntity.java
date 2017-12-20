package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "leagues", schema = "rune_coach")
public class LeagueEntity {
	private String uuid;
	private Timestamp lastChecked;

	@Id
	@Column(name = "uuid", nullable = false, length = 36)
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Basic
	@Column(name = "last_checked", nullable = false)
	public Timestamp getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(Timestamp lastChecked) {
		this.lastChecked = lastChecked;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LeagueEntity that = (LeagueEntity) o;
		return Objects.equals(uuid, that.uuid) &&
				Objects.equals(lastChecked, that.lastChecked);
	}

	@Override
	public int hashCode() {

		return Objects.hash(uuid, lastChecked);
	}
}
