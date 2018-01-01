package com.derpthemeus.runeCoach.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

public class RuneCoachWebServer {
	private final Server jettyServer;

	public RuneCoachWebServer(int port) throws Exception {
		jettyServer = new Server(port);

		ResourceHandler staticHandler = new ResourceHandler();
		staticHandler.setDirectoriesListed(false);
		staticHandler.setResourceBase(Resource.newClassPathResource("frontend_static/").toString());

		ContextHandler championInfoHandler = new ContextHandler("/getChampionInfo");
		championInfoHandler.setHandler(new ChampionInfoHandler());

		ContextHandler staticDataHandler = new ContextHandler("/getStaticData");
		staticDataHandler.setHandler(new StaticDataHandler());

		ContextHandler runesHandler = new ContextHandler("/getRunes");
		runesHandler.setHandler(new RunesHandler());

		HandlerCollection handlers = new HandlerCollection(
				staticHandler, championInfoHandler, staticDataHandler, runesHandler
		);
		jettyServer.setHandler(handlers);
		jettyServer.start();
	}
}
