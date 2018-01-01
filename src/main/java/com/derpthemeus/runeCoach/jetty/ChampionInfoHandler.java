package com.derpthemeus.runeCoach.jetty;

import com.derpthemeus.runeCoach.DDragonManager;
import com.derpthemeus.runeCoach.RuneCoach;
import com.google.gson.Gson;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChampionInfoHandler extends AbstractHandler {
	// How long data should be cached for (in milliseconds)
	private static final int CACHE_TIME = 1000 * 60 * 60;

	private final List<String> championIds;
	private final Gson gson = new Gson();

	// Cached JSON responses for champions. Shorts are champion IDs, Longs are timestamps, Strings are JSON
	private final Map<Short, Map.Entry<Long, String>> cache = new HashMap<>();


	public ChampionInfoHandler() throws IOException {
		DDragonManager.ChampionList champions = DDragonManager.getChampionList(DDragonManager.getLatestVersion());
		championIds = champions.data.values().stream().map(champion -> champion.key).collect(Collectors.toList());
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (!championIds.contains(request.getParameter("championId"))) {
			response.setContentType(MimeTypes.Type.TEXT_PLAIN.asString());
			// TODO use a raw message instead of an HTML page
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Error: champion with specified ID does not exist");
			baseRequest.setHandled(true);
			return;
		}

		short championId = Short.parseShort(request.getParameter("championId"));
		Map.Entry<Long, String> cacheEntry = cache.get(championId);

		if (cacheEntry == null || System.currentTimeMillis() - cacheEntry.getKey() > CACHE_TIME) {
			try (Session session = RuneCoach.getSessionFactory().openSession()) {
				// TODO allow patch to be specified through config, or automatically switch to latest patch once enough data has been aggregated
				Query query = session.createQuery("SELECT NEW MAP(perkScore.perkId AS perkId, perkScore.score AS score, perkScore.scoreType AS type)" +
						" FROM PerkScoreEntity AS perkScore WHERE patch=:patch AND championId=:championId AND games>200 AND scoreType!='RAW'")
						.setParameter("patch", "7.24").setParameter("championId", championId);
				List<Map> scores = query.getResultList();

				String json = gson.toJson(scores);

				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().write(json);
				baseRequest.setHandled(true);

				cache.put(championId, new AbstractMap.SimpleEntry<>(System.currentTimeMillis(), json));
			}
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(cacheEntry.getValue());
		}
		baseRequest.setHandled(true);
	}
}