package llm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing API keys from apikey.txt file.
 */
public class ApiKeyManager {
    
    private static final String API_KEY_FILE = "apikey.txt";
    
    /**
     * Load API keys from apikey.txt file.
     * The file can contain either:
     * 1. A single API key (for backward compatibility)
     * 2. Service->key pairs in format "service=key" (one per line)
     * 
     * @return Map of service names to API keys
     * @throws IOException if the file cannot be read
     */
    public static Map<String, String> loadApiKeys() throws IOException {
        Map<String, String> apiKeys = new HashMap<>();
        File file = new File(API_KEY_FILE);
        
        if (!file.exists()) {
            throw new IOException("API key file not found: " + API_KEY_FILE);
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
}
