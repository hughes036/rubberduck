package llm;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import java.util.Collections;

/**
 * Gemini LLM service implementation.
 * Based on the GeminiHelloWorld example.
 */
public class GeminiService implements LLMService {
    
    private String apiKey;
    private Client client;
    
    public GeminiService() {
        // Constructor - client will be initialized when API key is set
    }
    
    public GeminiService(String apiKey) {
        setApiKey(apiKey);
    }
    
    @Override
    public String processCompositionRequest(String prompt) throws Exception {
        if (client == null) {
            throw new IllegalStateException("API key not set. Call setApiKey() first.");
        }
        
        try {
            GenerateContentResponse response = client.models.generateContent(
                "gemini-2.5-flash",
                prompt,
                null
            );
            
            return response.text();
        } catch (Exception e) {
            throw new Exception("Failed to process composition request with Gemini: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getServiceName() {
        return "gemini";
    }
    
    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        
        // The Google GenAI library expects the API key in environment variable GOOGLE_API_KEY
        // Our Gradle build.gradle file should set this environment variable when running
        // If not set, provide helpful error message
        String envApiKey = System.getenv("GOOGLE_API_KEY");
        if (envApiKey == null || envApiKey.trim().isEmpty()) {
            throw new RuntimeException(
                "GOOGLE_API_KEY environment variable not set. " +
                "This should be automatically set by the Gradle build when running with apikeys.json. " +
                "If running outside Gradle, set manually: export GOOGLE_API_KEY=\"your-key\""
            );
        }
        
        try {
            // Initialize the client (it will read from GOOGLE_API_KEY environment variable)
            this.client = new Client();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Gemini client: " + e.getMessage(), e);
        }
    }
}
