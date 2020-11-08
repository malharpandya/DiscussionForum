package ca.utoronto.utm.mcs;

import java.io.IOException;

public class App {
	static int port = 8080;

	public static void main(String[] args) throws IOException {
		Dagger service = DaggerDaggerComponent.create().buildMongoHttp();

		service.getServer().createContext("/api/v1/post", service.getPost());

		service.getServer().start();

		System.out.printf("Server started on port %d", port);
	}
}
