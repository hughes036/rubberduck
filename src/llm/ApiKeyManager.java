package llm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Utility class for managing API keys from apikeys.json file.
 */
public class ApiKeyManager {
    
    private static final String API_KEYS_JSON_FILE = "apikeys.json";
    
    /**
     * Loads API keys from apikeys.json file.
     *
     * @return a map of service names (in lowercase) to their corresponding API keys
     * @throws IOException if apikeys.json file does not exist or cannot be read
     */
    public static Map<String, String> loadApiKeys() throws IOException {
        Path jsonFile = Paths.get(API_KEYS_JSON_FILE);
        
        if (!Files.exists(jsonFile)) {
            throw new IOException("API key file not found: " + API_KEYS_JSON_FILE + ". Please create this file with your API keys.");
        }
        
        return loadApiKeysFromJson(jsonFile);
    }
    
    /**
     * Gets all available LLM service names that have non-empty API keys.
     * 
     * @return a set of service names with configured API keys
     * @throws IOException if API key files cannot be read
     */
    public static Set<String> getAvailableServices() throws IOException {
        Map<String, String> apiKeys = loadApiKeys();
        
        return apiKeys.entrySet().stream()
            .filter(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
    
    /**
     * Checks if the specified LLM service is configured with an API key.
     * 
     * @param serviceName the name of the LLM service
     * @return true if the service has a non-empty API key configured
     * @throws IOException if API key files cannot be read
     */
    public static boolean isServiceAvailable(String serviceName) throws IOException {
        Map<String, String> apiKeys = loadApiKeys();
        String key = apiKeys.get(serviceName.toLowerCase());
        return key != null && !key.trim().isEmpty();
    }
    
    /**
     * Loads API keys from the JSON format file.
     */
    private static Map<String, String> loadApiKeysFromJson(Path jsonFile) throws IOException {
        try {
            String content = Files.readString(jsonFile);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> rawKeys = mapper.readValue(content, new TypeReference<Map<String, String>>() {});
            
            // Convert all service names to lowercase for consistency
            Map<String, String> apiKeys = new HashMap<>();
            for (Map.Entry<String, String> entry : rawKeys.entrySet()) {
                apiKeys.put(entry.getKey().toLowerCase(), entry.getValue());
            }
            
            return apiKeys;
        } catch (Exception e) {
            throw new IOException("Failed to parse " + API_KEYS_JSON_FILE + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves the API key for the specified service.
     *
     * @param serviceName the name of the service whose API key is requested
     * @return the API key for the service, or {@code null} if not found
     * @throws IOException if the API key files cannot be read
     */
    public static String getApiKey(String serviceName) throws IOException {
        Map<String, String> apiKeys = loadApiKeys();
        return apiKeys.get(serviceName.toLowerCase());
    }
}
