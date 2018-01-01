package com.derpthemeus.runeCoach.jetty;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.RuneCoach;
import com.derpthemeus.runeCoach.hibernate.PerkScoreEntity;
import com.derpthemeus.runeCoach.hibernate.TagEntity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns a champion list, tag list, the best tag for each rune, and announcement messages
 */
public class StaticDataHandler extends AbstractHandler {

	private String cachedResponse;
	private final Gson gson = new Gson();
	private final Logger logger = LogManager.getLogger();

	public StaticDataHandler() throws IOException {
		updateCachedResponse();
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(cachedResponse);
		baseRequest.setHandled(true);
	}

	// TODO automatically update (also requires updating DDragonManager, since it currently caches latest version)
	private void updateCachedResponse() throws IOException {
		logger.info("Updating static data...");
		String ddragVersion = DDragonManager.getLatestVersion();

		JsonObject obj = new JsonObject();
		obj.addProperty("announcementMessage", System.getenv("ANNOUNCEMENT_MESSAGE"));
		obj.addProperty("announcementLink", System.getenv("ANNOUNCEMENT_LINK"));
		obj.addProperty("ddragonVersion", ddragVersion);
		obj.add("champions", gson.toJsonTree(DDragonManager.getChampionList(ddragVersion).data.values().toArray()));
		try (Session session = RuneCoach.getSessionFactory().openSession()) {
			// Get tags
			Map<Short, TagEntity> tags = new HashMap<>();
			for (TagEntity tag : (List<TagEntity>) session.createQuery("FROM TagEntity").getResultList()) {
				tags.put(tag.getTagId(), tag);
			}
			obj.add("tags", gson.toJsonTree(tags));

			// Get best tags for each perk
			List<PerkScoreEntity> bestTags = new ArrayList<>();
			// TODO do some fancy HQL stuff here instead of iterating over perk IDs
			for (Short perkId : DDragonManager.getPerkAndStyleIds(DDragonManager.getLatestVersion())) {
				// TODO allow patch to be specified through config, or automatically switch to latest patch once enough data has been aggregated
				Query query = session.createQuery("SELECT NEW MAP(perkScore.perkId AS perkId, perkScore.score AS score, -perkScore.championId AS tagId) " +
						"FROM PerkScoreEntity AS perkScore WHERE games>200 AND championId<0 AND patch=:patch AND scoreType='RELATIVE' AND perkId=:perkId ORDER BY score DESC")
						.setParameter("patch", "7.24").setParameter("perkId", perkId);
				bestTags.addAll(query.setMaxResults(2).getResultList());
			}
			obj.add("bestTags", gson.toJsonTree(bestTags));
		}

		cachedResponse = gson.toJson(obj);
		logger.info("Updated static data");
	}
}
