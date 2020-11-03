package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.TextSearchOptions;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Post implements HttpHandler {
	

 
	protected MongoClient mongoclient;
	private MongoDatabase db;
	private MongoCollection<Document> collection;
	
	public Post(MongoClient client) {
		this.mongoclient = client;
		setDatabaseAndCollection("csc301a2", "posts");
	}
	
	// setter
    private void setDatabaseAndCollection (String dbName, String collName){

        this.db = this.mongoclient.getDatabase(dbName); // csc301a2
        this.collection = this.db.getCollection(collName); // posts

    }
    
	private Document createPost(String title, String author, String content, ArrayList<String> tags) {
		Document doc = new Document()
				.append("title", title)
				.append("author", author)
				.append("content", content)
				.append("tags", tags);
		return doc;
	}
	
	@Override
	public void handle(HttpExchange exchange) {
		
		try {
			if (exchange.getRequestMethod().equals("PUT")) {
                handlePut(exchange);
            } else if (exchange.getRequestMethod().equals("GET")) {
                handleGet(exchange);
            } else if (exchange.getRequestMethod().equals("DELETE")){
            	handleDelete(exchange);
            } else {
            	exchange.sendResponseHeaders(405, -1);
            }
		} catch (Exception e) {
            e.printStackTrace();
        }
	}

	private void handlePut(HttpExchange exchange) throws IOException, JSONException {
		
		try {

			String body = Utils.convert(exchange.getRequestBody());
			JSONObject deserialized = new JSONObject(body);

			if (deserialized.has("title") && deserialized.has("author") && deserialized.has("content") && deserialized.has("tags")) {

				String title = deserialized.getString("title");
				String author = deserialized.getString("author");
				String content = deserialized.getString("content");
				ArrayList<String> tags = new ArrayList<String>();
				JSONArray tagsArray = deserialized.getJSONArray("tags");
				for (int i = 0; i < tagsArray.length(); i++) {
					tags.add(tagsArray.getString(i));
				}

				Document post = createPost(title, author, content, tags);

				
				collection.insertOne(post);
				
				JSONObject response = new JSONObject().put("_id", post.getObjectId("_id"));
				
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
	
	private void handleGet(HttpExchange exchange) throws IOException, JSONException {

		try {
			
			String body = Utils.convert(exchange.getRequestBody());
			JSONObject deserialized = new JSONObject(body);
			
			if (deserialized.has("_id")) {
				
				String idString = deserialized.getString("_id");
				
				if(!ObjectId.isValid(idString)) {
					exchange.sendResponseHeaders(400, -1); // invalid id
					
				} else {
					
					ObjectId id = new ObjectId(idString);
					Iterator<Document> posts = collection.find(eq("_id", id)).iterator();
					if (!posts.hasNext()){
						exchange.sendResponseHeaders(404, -1);
					} else {
						Document post  = posts.next();
						JSONArray response = new JSONArray().put(post);
						exchange.sendResponseHeaders(200, response.toString().getBytes().length);
						
						OutputStream os = exchange.getResponseBody();
						os.write(response.toString().getBytes());
						os.close();
					}
				}
			} else if (deserialized.has("title")) {
				
				String search = deserialized.getString("title");
				collection.createIndex(Indexes.text("title"));
				
				Iterator<Document> posts = collection.find(text("\""+search+"\"")).sort(Sorts.ascending("title")).iterator();
				if (!posts.hasNext()) {
					exchange.sendResponseHeaders(404, -1); // documents not found
				} else {
					
					JSONArray response = new JSONArray();
					while(posts.hasNext()) {
						Document post = posts.next();
						JSONObject postJSON = new JSONObject(post.toJson());
						if (postJSON.getString("title").contains(search)) {
							response.put(post);
						}
					}
					
					exchange.sendResponseHeaders(200, response.toString().getBytes().length);
					
					OutputStream os = exchange.getResponseBody();
					os.write(response.toString().getBytes());
					os.close();
				}
				
			} else {
				exchange.sendResponseHeaders(400, -1); // missing information
			}
			
			
		} catch (JSONException e) {
			exchange.sendResponseHeaders(400, -1); // bad format
         
        } catch (Exception e) {
        	exchange.sendResponseHeaders(500, -1); // internal error
        }
	}
	
	private void handleDelete(HttpExchange exchange) throws IOException, JSONException {
		
		try {
			
			String body = Utils.convert(exchange.getRequestBody());
			JSONObject deserialized = new JSONObject(body);
			
			if (deserialized.has("_id")) {
				
				String idString = deserialized.getString("_id");
				if (!ObjectId.isValid(idString)) {
					exchange.sendResponseHeaders(400, -1); //invalid id string
					
				} else {
					
					ObjectId id = new ObjectId(idString);
					Document deletedPost = collection.findOneAndDelete(eq("_id", id));
					
					if (deletedPost == null) {
						exchange.sendResponseHeaders(404, -1); //document not found
					} else {
						exchange.sendResponseHeaders(200, -1);
					}
				}
				
				
			} else {
				exchange.sendResponseHeaders(400, -1); // missing information
			}
			
		} catch (JSONException e){
			exchange.sendResponseHeaders(400, -1); // bad format
			
		} catch (Exception e) {
			exchange.sendResponseHeaders(500, -1); // internal error
		}
		
	}

}
