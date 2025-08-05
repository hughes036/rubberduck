package llm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
 * Utility class for managing API keys from apikeys.json file (with fallback to legacy apikey.txt).
 */
public class ApiKeyManager {
    
    private static final String API_KEYS_JSON_FILE = "apikeys.json";
    private static final String LEGACY_API_KEY_FILE = "apikey.txt";
    
    /**
     * Loads API keys from apikeys.json file, with fallback to legacy apikey.txt.
     *
     * @return a map of service names (in lowercase) to their corresponding API keys
     * @throws IOException if no API key files exist or cannot be read
     */
    public static Map<String, String> loadApiKeys() throws IOException {
        Path jsonFile = Paths.get(API_KEYS_JSON_FILE);
        
        if (Files.exists(jsonFile)) {
            return loadApiKeysFromJson(jsonFile);
        } else {
            return loadApiKeysFromLegacyFile();
        }
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
     * Loads API keys from the legacy apikey.txt file format.
     * Supports both single-key and service=key mapping formats.
     */
    private static Map<String, String> loadApiKeysFromLegacyFile() throws IOException {
        Map<String, String> apiKeys = new HashMap<>();
        File file = new File(LEGACY_API_KEY_FILE);
        
        if (!file.exists()) {
            throw new IOException("No API key files found. Please create " + API_KEYS_JSON_FILE + " or " + LEGACY_API_KEY_FILE);
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean foundPairs = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }
                
                if (line.contains("=")) {
                    // Service=key format
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        apiKeys.put(parts[0].trim().toLowerCase(), parts[1].trim());
                        foundPairs = true;
                    }
                } else if (!foundPairs) {
                    // Single key format (backward compatibility)
                    // Assume it's for Gemini since that's our default service
                    apiKeys.put("gemini", line);
                    break;
                }
            }
        }
        
        return apiKeys;
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
