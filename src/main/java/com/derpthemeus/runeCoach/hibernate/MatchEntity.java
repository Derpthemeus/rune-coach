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
	private boolean havePerksBeenProcessed;
	private String patch;
	private boolean hasBeenDownloaded;
	private Timestamp matchTimestamp;

	@Id
	@Column(name = "match_id", nullable = false)
	public long getMatchId() {
		return matchId;
	}

	public void setMatchId(long matchId) {
		this.matchId = matchId;
	}

	// TODO fix this name
	@Basic
	@Column(name = "processed_perks", nullable = false, columnDefinition = "BOOLEAN")
	public boolean isHavePerksBeenProcessed() {
		return havePerksBeenProcessed;
	}

	public void setHavePerksBeenProcessed(boolean processedPerks) {
		this.havePerksBeenProcessed = processedPerks;
	}

	@Basic
	@Column(name = "patch", length = 8)
	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	// TODO fix this name
	@Basic
	@Column(name = "downloaded_match", nullable = false, columnDefinition = "BOOLEAN")
	public boolean isHasBeenDownloaded() {
		return hasBeenDownloaded;
	}

	public void setHasBeenDownloaded(boolean downloadedMatch) {
		this.hasBeenDownloaded = downloadedMatch;
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
				havePerksBeenProcessed == that.havePerksBeenProcessed &&
				hasBeenDownloaded == that.hasBeenDownloaded &&
				Objects.equals(patch, that.patch) &&
				Objects.equals(matchTimestamp, that.matchTimestamp);
	}

	@Override
	public int hashCode() {

		return Objects.hash(matchId, havePerksBeenProcessed, patch, hasBeenDownloaded, matchTimestamp);
	}
}
