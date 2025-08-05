package llm;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

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
        
        // Set the environment variable that the Gemini client expects
        System.setProperty("GOOGLE_API_KEY", apiKey);
        
        // Initialize the client
        this.client = new Client();
    }
}
