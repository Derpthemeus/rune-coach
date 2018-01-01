package com.derpthemeus.runeCoach;

import com.derpthemeus.runeCoach.hibernate.PerkScoreEntity;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DDragonManager {

	private static Gson gson = new Gson();
	// A cache of parsed responses, keyed by DDragon URL
	private static Map<String, Object> cache = new HashMap<>();

	private static <T> T makeRequest(String urlString, Class<T> clazz, boolean checkCache) throws IOException {
		if (checkCache && cache.containsKey(urlString)) {
			return (T) cache.get(urlString);
		}

		URL url = new URL(urlString);
		InputStreamReader reader = new InputStreamReader(url.openStream());

		T parsed = gson.fromJson(reader, clazz);
		cache.put(urlString, parsed);
		return parsed;
	}

	public static ChampionList getChampionList(String version) throws IOException {
		return makeRequest("https://ddragon.leagueoflegends.com/cdn/" + version + "/data/en_US/champion.json", ChampionList.class, true);
	}

	public static StyleInfo[] getRuneInfo(String version) throws IOException {
		return makeRequest("https://ddragon.leagueoflegends.com/cdn/" + version + "/data/en_US/runesReforged.json", StyleInfo[].class, true);
	}

	/**
	 * Gets a long version (e.g. "7.20.3") used by DDragon that corresponds to a short version (e.g. "7.20"). Uses the latest version available for the specified short version.
	 *
	 * @return A long version, or `null` if no matching version could be found
	 */
	public static String getLongVersion(String shortVersion) throws IOException {
		// Append "." to the string to prevent partial matches (e.g. "7.2" matching to '7.20.1")
		shortVersion += ".";
		String[] versions = makeRequest("https://ddragon.leagueoflegends.com/api/versions.json", String[].class, true);
		for (String longVersion : versions) {
			if (longVersion.startsWith(shortVersion)) {
				return longVersion;
			}
		}

		return null;
	}

	public static String convertToShortVersion(String longVersion) {
		return longVersion.substring(0, longVersion.indexOf(".", longVersion.indexOf(".") + 1));
	}

	/**
	 * Gets the most recent DDragon version
	 *
	 * @return The latest long version (e.g. "7.20.3")
	 */
	public static String getLatestVersion() throws IOException {
		String[] versions = makeRequest("https://ddragon.leagueoflegends.com/api/versions.json", String[].class, true);
		return versions[0];
	}

	/**
	 * Gets the IDs of all available champions for a certain DDragon version
	 */
	public static List<Short> getChampionIds(String ddragonVersion) throws IOException {
		return getChampionList(ddragonVersion).data.values().stream().map(champion -> Short.parseShort(champion.key)).collect(Collectors.toList());
	}

	/**
	 * Gets the IDs of all available perks for a certain DDragon version
	 */
	public static List<Short> getPerkIds(String ddragonVersion) throws IOException {
		List<Short> perkIds = new ArrayList<>();

		StyleInfo[] styles = getRuneInfo(ddragonVersion);
		for (StyleInfo style : styles) {
			for (StyleInfo.SlotInfo slot : style.slots) {
				for (StyleInfo.Rune rune : slot.runes) {
					perkIds.add(rune.id);
				}
			}
		}
		return perkIds;
	}

	/**
	 * Gets the IDs of all available perks and styles for a certain DDragon version
	 */
	public static List<Short> getPerkAndStyleIds(String ddragonVersion) throws IOException {
		List<Short> ids = new ArrayList<>();

		StyleInfo[] styles = getRuneInfo(ddragonVersion);
		for (StyleInfo style : styles) {
			ids.add(style.id);
			for (StyleInfo.SlotInfo slot : style.slots) {
				for (StyleInfo.Rune rune : slot.runes) {
					ids.add(rune.id);
				}
			}
		}
		return ids;
	}

	/**
	 * Gets the ID of all perks/styles that can replace the specified perk/style within the specified context (including the specified ID)
	 *
	 * @param context
	 * @param perkId
	 * @return
	 */
	public static List<Short> getPerkIdsWithinContext(PerkScoreEntity.ScoreType context, short perkId, String version) throws IOException {
		StyleInfo[] styles = getRuneInfo(version);

		if (context == PerkScoreEntity.ScoreType.SLOT) {
			for (StyleInfo style : styles) {
				for (StyleInfo.SlotInfo slot : style.slots) {
					for (StyleInfo.Rune rune : slot.runes) {
						if (rune.id == perkId) {
							return Arrays.stream(slot.runes).map(rune1 -> rune1.id).collect(Collectors.toList());
						}
					}
				}
			}
		}
		// TODO add support for other types
		return null;
	}

	// https://ddragon.leagueoflegends.com/cdn/7.24.1/data/en_US/champion.json
	public class ChampionList {

		public Map<String, Champion> data;

		public class Champion {
			// String
			public String id;
			// Numeric
			public String key;
			public String name;
			public ChampionImage image;

			public class ChampionImage {
				public String full;
			}
		}
	}

	// https://ddragon.leagueoflegends.com/cdn/7.24.1/data/en_US/runesReforged.json
	public class StyleInfo {
		public short id;
		public String key;
		public String name;
		public SlotInfo[] slots;

		public class SlotInfo {
			public Rune[] runes;
		}

		public class Rune {
			public short id;
			public String key;
			public String name;
		}
	}
}
