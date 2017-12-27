package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "matches", schema = "rune_coach")
public class MatchEntity {
	private long matchId;
	private String patch;
	private boolean downloaded;
	private Timestamp matchTimestamp;

	@Id
	@Column(name = "match_id", nullable = false)
	public long getMatchId() {
		return matchId;
	}

	public void setMatchId(long matchId) {
		this.matchId = matchId;
	}

	@Basic
	@Column(name = "patch", length = 8)
	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	@Basic
	@Column(name = "downloaded_match", nullable = false, columnDefinition = "BOOLEAN")
	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloadedMatch) {
		this.downloaded = downloadedMatch;
	}

	@Basic
	@Column(name = "match_timestamp", nullable = false)
	public Timestamp getMatchTimestamp() {
		return matchTimestamp;
	}

	public void setMatchTimestamp(Timestamp matchTimestamp) {
		this.matchTimestamp = matchTimestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MatchEntity that = (MatchEntity) o;
		return matchId == that.matchId &&
				downloaded == that.downloaded &&
				Objects.equals(patch, that.patch) &&
				Objects.equals(matchTimestamp, that.matchTimestamp);
	}

	@Override
	public int hashCode() {

		return Objects.hash(matchId, patch, downloaded, matchTimestamp);
	}
}
