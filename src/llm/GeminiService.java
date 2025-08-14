package llm;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import java.util.Optional;

/**
 * Gemini LLM service implementation.
 * Based on the GeminiHelloWorld example.
 */
public class GeminiService implements LLMService {
    
    private String apiKey;
    private Client client;
    
    /**
     * Constructs a GeminiService instance by reading the API key from the GOOGLE_API_KEY environment variable.
     * 
     * @throws RuntimeException if GOOGLE_API_KEY is not set or client initialization fails
     */
    public GeminiService() {
        // Read API key from environment variable as required by Google GenAI library
        String envApiKey = System.getenv("GOOGLE_API_KEY");
        
        // If environment variable is not set, try system property as fallback
        if (envApiKey == null || envApiKey.trim().isEmpty()) {
            envApiKey = System.getProperty("GOOGLE_API_KEY");
        }
        
        if (envApiKey == null || envApiKey.trim().isEmpty()) {
            throw new RuntimeException(
                "GOOGLE_API_KEY environment variable not set. " +
                "The Google GenAI library requires this environment variable. " +
                "Please set it manually: export GOOGLE_API_KEY=\"your-api-key\" " +
                "Or use './gradlew run' which automatically sets this from apikeys.json."
            );
        }
        
        this.apiKey = envApiKey;
        
        try {
            // Initialize the client (reads from GOOGLE_API_KEY environment variable)
            this.client = new Client();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Gemini client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sends a prompt to the Gemini LLM and returns the generated text response.
     *
     * @param prompt The input prompt to be processed by the Gemini model.
     * @return The generated text from the Gemini model.
     * @throws IllegalStateException if the API key has not been set.
     * @throws Exception if the request to the Gemini service fails.
     */
    @Override
    public String processCompositionRequest(String prompt) throws Exception {
        if (client == null) {
            throw new IllegalStateException("Gemini client not initialized. This should not happen if constructor succeeded.");
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
    
    /**
     * Returns the name of the LLM service provided by this implementation.
     *
     * @return the string "gemini"
     */
    @Override
    public String getServiceName() {
        return "gemini";
    }
}
