package llm;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating LLM service instances.
 */
public class LLMServiceFactory {
    
    private static final Map<String, Class<? extends LLMService>> serviceMap = new HashMap<>();
    
    static {
        // Register available LLM services
        serviceMap.put("gemini", GeminiService.class);
        // Add more services here as they're implemented:
        // serviceMap.put("gpt4", GPT4Service.class);
        // serviceMap.put("claude", ClaudeService.class);
    }
    
    /**
     * Create an LLM service instance by name.
     * 
     * @param serviceName The name of the service (e.g., "gemini", "gpt4", "claude")
     * @param apiKey The API key for the service
     * @return An initialized LLM service instance
     * @throws IllegalArgumentException if the service name is not supported
     */
    public static LLMService createService(String serviceName, String apiKey) {
        Class<? extends LLMService> serviceClass = serviceMap.get(serviceName.toLowerCase());
        
        if (serviceClass == null) {
            throw new IllegalArgumentException("Unsupported LLM service: " + serviceName + 
                ". Supported services: " + String.join(", ", serviceMap.keySet()));
        }
        
        try {
            LLMService service = serviceClass.getDeclaredConstructor().newInstance();
            service.setApiKey(apiKey);
            return service;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create LLM service: " + serviceName, e);
        }
    }
    
    /**
     * Get a list of supported LLM service names.
     * 
     * @return Array of supported service names
     */
    public static String[] getSupportedServices() {
        return serviceMap.keySet().toArray(new String[0]);
    }
}
