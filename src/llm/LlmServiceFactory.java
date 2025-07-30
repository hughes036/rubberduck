package llm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Factory class for creating and managing LLM service instances.
 * This class uses the factory pattern to create instances of different LLM services.
 */
public class LlmServiceFactory {

    private static final Map<String, Class<? extends LlmService>> serviceRegistry = new HashMap<>();

    // Register known LLM service implementations
    static {
        // Register OpenAI service
        serviceRegistry.put("openai", OpenAiService.class);
    }

    /**
     * Gets an instance of an LLM service by name.
     * 
     * @param serviceName The name of the LLM service
     * @return An instance of the requested LLM service
     * @throws IllegalArgumentException If the requested service is not found
     * @throws Exception If there's an error creating the service instance
     */
    public static LlmService getService(String serviceName) throws Exception {
        Class<? extends LlmService> serviceClass = serviceRegistry.get(serviceName.toLowerCase());
        if (serviceClass == null) {
            throw new IllegalArgumentException("Unknown LLM service: " + serviceName);
        }

        return serviceClass.getDeclaredConstructor().newInstance();
    }

    /**
     * Gets the names of all registered LLM services.
     * 
     * @return A set of service names
     */
    public static Set<String> getAvailableServices() {
        return serviceRegistry.keySet();
    }

    /**
     * Registers a new LLM service implementation.
     * 
     * @param serviceName The name of the LLM service
     * @param serviceClass The class of the LLM service implementation
     */
    public static void registerService(String serviceName, Class<? extends LlmService> serviceClass) {
        serviceRegistry.put(serviceName.toLowerCase(), serviceClass);
    }
}
