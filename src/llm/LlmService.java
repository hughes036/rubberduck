package llm;

/**
 * Interface for LLM (Large Language Model) services.
 * This interface defines the contract that all LLM service implementations must follow.
 */
public interface LlmService {
    
    /**
     * Sends a prompt to the LLM and returns the response.
     * 
     * @param prompt The prompt to send to the LLM
     * @return The response from the LLM
     * @throws Exception If there's an error communicating with the LLM
     */
    String generateResponse(String prompt) throws Exception;
    
    /**
     * Gets the name of the LLM service.
     * 
     * @return The name of the LLM service
     */
    String getName();
    
    /**
     * Sets the API key for the LLM service.
     * 
     * @param apiKey The API key to use for authentication
     */
    void setApiKey(String apiKey);
}