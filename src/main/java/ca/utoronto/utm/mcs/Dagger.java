package ca.utoronto.utm.mcs;

import javax.inject.Inject;

import com.mongodb.client.MongoClient;
import com.sun.net.httpserver.HttpServer;

public class Dagger {

	private HttpServer server;
	private MongoClient mongoclient;

	@Inject
	public Dagger(HttpServer server, MongoClient mongoclient) {
		this.server = server;
		this.mongoclient = mongoclient;
	}

	public HttpServer getServer() {
		return this.server;
	}

	public void setServer(HttpServer server) {
		this.server = server;
	}

	public MongoClient getClient() {
		return this.mongoclient;
	}

	public void setDb(MongoClient mongoclient) {
		this.mongoclient = mongoclient;
	}

}
