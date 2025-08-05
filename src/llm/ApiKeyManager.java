package llm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for managing API keys from apikeys.json file.
 */
public class ApiKeyManager {
    
    private static final String API_KEY_FILE_JSON = "apikeys.json";
    private static final String API_KEY_FILE_TXT = "apikey.txt"; // Backward compatibility
    
    /**
     * Load API keys from apikeys.json file.
     * Falls back to apikey.txt for backward compatibility.
     * 
     * @return Map of service names to API keys
     * @throws IOException if neither file can be read
     */
    public static Map<String, String> loadApiKeys() throws IOException {
        // Try JSON format first
        File jsonFile = new File(API_KEY_FILE_JSON);
        if (jsonFile.exists()) {
            return loadApiKeysFromJson();
        }
        
        // Fall back to legacy txt format
        File txtFile = new File(API_KEY_FILE_TXT);
        if (txtFile.exists()) {
            return loadApiKeysFromTxt();
        }
        
        throw new IOException("No API key file found. Expected: " + API_KEY_FILE_JSON + " or " + API_KEY_FILE_TXT);
    }
    
    /**
     * Load API keys from JSON format.
     */
    private static Map<String, String> loadApiKeysFromJson() throws IOException {
        Map<String, String> apiKeys = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(API_KEY_FILE_JSON))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line.trim());
            }
            
            // Simple JSON parsing (avoiding external dependencies)
            String json = jsonContent.toString();
            if (!json.startsWith("{") || !json.endsWith("}")) {
                throw new IOException("Invalid JSON format in " + API_KEY_FILE_JSON);
            }
            
            // Remove outer braces and split by commas
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            
            for (String pair : pairs) {
                pair = pair.trim();
                if (pair.isEmpty()) continue;
                
                // Split by colon
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length != 2) {
                    continue; // Skip malformed entries
                }
                
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                
                if (!key.isEmpty() && !value.isEmpty()) {
                    apiKeys.put(key.toLowerCase(), value);
                }
            }
        }
        
        return apiKeys;
    }
    
    /**
     * Load API keys from legacy txt format for backward compatibility.
     */
    private static Map<String, String> loadApiKeysFromTxt() throws IOException {
        Map<String, String> apiKeys = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(API_KEY_FILE_TXT))) {
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
     * Get API key for a specific service.
     * 
     * @param serviceName The name of the LLM service
     * @return The API key for the service, or null if not found
     * @throws IOException if the API key file cannot be read
     */
    public static String getApiKey(String serviceName) throws IOException {
        Map<String, String> apiKeys = loadApiKeys();
        return apiKeys.get(serviceName.toLowerCase());
    }
    
    /**
     * Get all available LLM services (those with configured API keys).
     * 
     * @return Set of service names that have API keys configured
     * @throws IOException if the API key file cannot be read
     */
    public static Set<String> getAvailableServices() throws IOException {
        Map<String, String> apiKeys = loadApiKeys();
        // Only return services that have non-empty API keys
        return apiKeys.entrySet().stream()
            .filter(entry -> !entry.getValue().trim().isEmpty())
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Validate that a service has a configured API key.
     * 
     * @param serviceName The name of the LLM service
     * @return true if the service has a valid API key configured
     * @throws IOException if the API key file cannot be read
     */
    public static boolean isServiceConfigured(String serviceName) throws IOException {
        String apiKey = getApiKey(serviceName);
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
