package com.derpthemeus.runeCoach.jetty;

import com.derpthemeus.runeCoach.DDragonManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RunesHandler extends AbstractHandler {

	private String cachedResponse;

	public RunesHandler() throws IOException {
		Gson gson = new Gson();
		JsonObject builderRunes = gson.toJsonTree(gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/builder-runes.json")), Object.class)).getAsJsonObject();
		JsonArray lcuRunes = gson.toJsonTree(gson.fromJson(new InputStreamReader(getClass().getResourceAsStream("/LCU-runes.json")), Object.class)).getAsJsonArray();
		DDragonManager.StyleInfo[] ddragonRunes = DDragonManager.getRuneInfo(DDragonManager.getLatestVersion());

		// Map LCU runes by ID
		Map<Integer, JsonObject> lcuMappedRunes = new HashMap<>();
		// The first element is a template, not an actual rune
		for (int i = 1; i < lcuRunes.size(); i++) {
			JsonObject rune = lcuRunes.get(i).getAsJsonObject();
			lcuMappedRunes.put(rune.get("id").getAsInt(), rune);
		}

		builderRunes.getAsJsonArray("styles").forEach(builderStyleEl -> {
			JsonObject builderStyle = builderStyleEl.getAsJsonObject();
			// Copy style IDs from DDragon
			for (DDragonManager.StyleInfo ddragStyle : ddragonRunes) {
				if (builderStyle.get("name").getAsString().equals(ddragStyle.name)) {
					builderStyle.getAsJsonObject().addProperty("id", ddragStyle.id);
					break;
				}
			}

			// Copy formatted descriptions from LCU
			builderStyle.getAsJsonArray("slots").forEach(slot -> {
				slot.getAsJsonObject().getAsJsonArray("runes").forEach(runeEl -> {
					JsonObject builderRune = runeEl.getAsJsonObject();
					JsonObject lcuRune = lcuMappedRunes.get(builderRune.get("runeId").getAsInt());

					String longDesc = lcuRune.get("longDesc").getAsString();
					String shortDesc = lcuRune.get("shortDesc").getAsString();

					builderRune.addProperty("longDescription", longDesc);
					builderRune.addProperty("shortDescription", shortDesc);
				});
			});

			// TODO bonuses (secondary styles)
		});

		this.cachedResponse = gson.toJson(builderRunes);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(cachedResponse);
		baseRequest.setHandled(true);
	}
}