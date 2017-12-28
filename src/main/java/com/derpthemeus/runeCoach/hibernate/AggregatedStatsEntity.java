package com.derpthemeus.runeCoach.hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AggregatedStatsEntity {

	protected short perkId;
	protected String patch;
	protected long var1Total;
	protected long var2Total;
	protected long var3Total;
	protected long totalMatches;
	protected long totalWins;

	@Id
	@Column(name = "perk_id", nullable = false)
	public short getPerkId() {
		return perkId;
	}

	public void setPerkId(short perkId) {
		this.perkId = perkId;
	}

	@Id
	@Column(name = "patch", nullable = false, length = 8)
	public String getPatch() {
		return patch;
	}

	public void setPatch(String patch) {
		this.patch = patch;
	}

	@Basic
	@Column(name = "var1_total", nullable = false)
	public long getVar1Total() {
		return var1Total;
	}

	public void setVar1Total(long var1Total) {
		this.var1Total = var1Total;
	}

	@Basic
	@Column(name = "var2_total", nullable = false)
	public long getVar2Total() {
		return var2Total;
	}

	public void setVar2Total(long var2Total) {
		this.var2Total = var2Total;
	}

	@Basic
	@Column(name = "var3_total", nullable = false)
	public long getVar3Total() {
		return var3Total;
	}

	public void setVar3Total(long var3Total) {
		this.var3Total = var3Total;
	}

	@Basic
	@Column(name = "total_matches", nullable = false)
	public long getTotalMatches() {
		return totalMatches;
	}

	public void setTotalMatches(long totalMatches) {
		this.totalMatches = totalMatches;
	}

	@Basic
	@Column(name = "total_wins", nullable = false)
	public long getTotalWins() {
		return totalWins;
	}

	public void setTotalWins(long totalWins) {
		this.totalWins = totalWins;
	}

}
