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
    
    /**
     * Constructs a GeminiService instance without initializing the client.
     *
     * The client will be initialized when an API key is set using {@link #setApiKey(String)}.
     */
    public GeminiService() {
        // Constructor - client will be initialized when API key is set
    }
    
    /**
     * Constructs a GeminiService instance with the specified API key and initializes the Gemini client.
     *
     * @param apiKey the API key used for authenticating requests to the Gemini service
     */
    public GeminiService(String apiKey) {
        setApiKey(apiKey);
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
    
    /**
     * Returns the name of the LLM service provided by this implementation.
     *
     * @return the string "gemini"
     */
    @Override
    public String getServiceName() {
        return "gemini";
    }
    
    /**
     * Sets the API key for authenticating with the Gemini service and initializes the client.
     *
     * This method also sets the required system property for the Gemini client to use the provided API key.
     */
    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        
        // Set the environment variable that the Gemini client expects
        System.setProperty("GOOGLE_API_KEY", apiKey);
        
        // Initialize the client
        this.client = new Client();
    }
}
