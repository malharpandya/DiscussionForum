package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import dagger.Module;
import dagger.Provides;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Module
public class DaggerModule {
	
	static int port = 8080;
	private static HttpServer server;
	private static MongoClient mongoclient;
	
    @Provides public MongoClient provideMongoClient() {
        /* TODO: Fill in this function */
    	mongoclient = MongoClients.create();
    	return mongoclient;
    }

    @Provides public HttpServer provideHttpServer() {
        /* TODO: Fill in this function */
    	try {
			server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return server;
    }
}
