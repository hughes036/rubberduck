package llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the LlmService interface for OpenAI's GPT models.
 * This class uses the OpenAI API to generate responses from prompts.
 */
public class OpenAiService implements LlmService {
    
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo"; // Default model
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private String apiKey;
    private final OkHttpClient client;
    private final Gson gson;
    
    /**
     * Creates a new OpenAiService instance.
     */
    public OpenAiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    @Override
    public String generateResponse(String prompt) throws Exception {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("API key is not set");
        }
        
        // Create the request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        
        requestBody.add("messages", messages);
        
        // Create the request
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();
        
        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            
            // Parse the response
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            // Extract the generated text
            String generatedText = jsonResponse
                    .getAsJsonArray("choices")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();
            
            return generatedText;
        }
    }
    
    @Override
    public String getName() {
        return "openai";
    }
    
    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}