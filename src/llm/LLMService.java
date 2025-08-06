package llm;

/**
 * Abstract interface for LLM services that can process MIDI composition requests.
 */
public interface LLMService {
    
    /**
 * Processes a MIDI composition request using the large language model.
 *
 * @param prompt The full prompt containing instructions and serialized MIDI data.
 * @return The response from the LLM with modified serialized MIDI data.
 * @throws Exception if the LLM service fails to process the request.
 */
    String processCompositionRequest(String prompt) throws Exception;
    
    /**
     * Returns the name of the LLM service.
     *
     * @return the service name, such as "gemini", "gpt4", or "claude"
     */
    String getServiceName();
}