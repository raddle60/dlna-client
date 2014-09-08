package com.raddle.dlna.http;

import org.eclipse.jetty.server.Server;

public class LocalFileHttpHandlerTest {

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);
		server.setHandler(new LocalFileHttpHandler());
		server.start();
		server.join();
	}

}
