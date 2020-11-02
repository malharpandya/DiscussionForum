package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Post implements HttpHandler {
	

 
	protected MongoClient mongoclient;
	private MongoDatabase db;
	private MongoCollection<Document> collection;
	
	public Post(MongoClient client) {
		// TODO Auto-generated constructor stub
		this.mongoclient = client;
		setDatabaseAndCollection("csc301a2", "posts");
	}
	
	// setter
    private void setDatabaseAndCollection (String dbName, String collName){

        this.db = this.mongoclient.getDatabase(dbName); // csc301a2
        this.collection = this.db.getCollection(collName); // posts

    }
    
	private Document createPost(String title, String author, String content, List<String> tags) {
		Document doc = new Document()
				.append("title", title)
				.append("author", author)
				.append("content", content)
				.append("tags", tags);
		return doc;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		// TODO Auto-generated method stub
		try {
			if (exchange.getRequestMethod().equals("PUT")) {
                handlePut(exchange);
            }
			
		} catch (IOException e){
			exchange.sendResponseHeaders(405, -1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handlePut(HttpExchange exchange) throws IOException, JSONException {
		// TODO Auto-generated method stub
		try {

			String body = Utils.convert(exchange.getRequestBody());
			JSONObject deserialized = new JSONObject(body);

			if (deserialized.has("title") && deserialized.has("author") && deserialized.has("content") && deserialized.has("tags")) {

				String title = deserialized.getString("title");
				String author = deserialized.getString("author");
				String content = deserialized.getString("content");
				List<String> tags = new ArrayList<String>();
				JSONArray tagsArray = deserialized.getJSONArray("tags");
				for (int i = 0; i < tagsArray.length(); i++) {
					tags.add(tagsArray.getString(i));
				}

				Document post = createPost(title, author, content, tags);

				
				collection.insertOne(post);
				ObjectId id = post.getObjectId("_id");
				
				JSONObject response = new JSONObject().put("_id", id);
				
				exchange.sendResponseHeaders(200, response.toString().length());
				
				OutputStream os = exchange.getResponseBody();
				os.write(response.toString().getBytes());
				os.close();
			} else {
				exchange.sendResponseHeaders(400, -1); // missing information
			}
			
		} catch (JSONException e) {
			exchange.sendResponseHeaders(400, -1); // bad format
         
        } catch (Exception e) {
        	exchange.sendResponseHeaders(500, -1); // internal error
        }
	}

}
