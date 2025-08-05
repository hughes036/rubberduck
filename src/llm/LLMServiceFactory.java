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
     * Creates and initializes an LLM service instance for the specified service name.
     * 
     * Note: API key management varies by service. Gemini uses GOOGLE_API_KEY environment variable.
     * Future services may use different authentication mechanisms.
     *
     * @param serviceName The name of the LLM service to instantiate (e.g., "gemini").
     * @param apiKey The API key (may be used differently or ignored depending on the service).
     * @return An initialized LLMService instance corresponding to the given service name.
     * @throws IllegalArgumentException if the specified service name is not supported.
     * @throws RuntimeException if instantiation or initialization of the service fails.
     */
    public static LLMService createService(String serviceName, String apiKey) {
        Class<? extends LLMService> serviceClass = serviceMap.get(serviceName.toLowerCase());
        
        if (serviceClass == null) {
            throw new IllegalArgumentException("Unsupported LLM service: " + serviceName + 
                ". Supported services: " + String.join(", ", serviceMap.keySet()));
        }
        
        try {
            // Only allow instantiation of classes in the llm package for security
            if (!serviceClass.getPackage().getName().equals("llm")) {
                throw new SecurityException("Service class must be in llm package");
            }

            // Create the service instance using default constructor
            // Each service handles its own authentication requirements
            // GeminiService reads from GOOGLE_API_KEY environment variable
            LLMService service = serviceClass.getDeclaredConstructor().newInstance();
            return service;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create LLM service: " + serviceName, e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Security violation creating service: " + serviceName, e);
        }
    }
    
    /**
     * Returns the names of all supported LLM services registered in the factory.
     *
     * @return an array containing the supported service names
     */
    public static String[] getSupportedServices() {
        return serviceMap.keySet().toArray(new String[0]);
    }
}
