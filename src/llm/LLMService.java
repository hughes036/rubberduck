package llm;

/**
 * Abstract interface for LLM services that can process MIDI composition requests.
 */
public interface LLMService {
    
    /**
     * Process a composition request using the LLM.
     * 
     * @param prompt The complete prompt including instructions and serialized MIDI
     * @return The LLM's response containing modified serialized MIDI
     * @throws Exception if the LLM request fails
     */
    String processCompositionRequest(String prompt) throws Exception;
    
    /**
     * Get the name of this LLM service.
     * 
     * @return The service name (e.g., "gemini", "gpt4", "claude")
     */
    String getServiceName();
    
    /**
     * Set the API key for this service.
     * 
     * @param apiKey The API key to use
     */
    void setApiKey(String apiKey);
}
