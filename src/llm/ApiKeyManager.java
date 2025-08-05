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
     * Loads API keys from the `apikey.txt` file, supporting both single-key and service-to-key mapping formats.
     *
     * The file may contain either a single API key (assigned to the default service "gemini") or multiple lines in the format `service=key`, one per line. Lines that are empty or start with `#` are ignored.
     *
     * @return a map of service names (in lowercase) to their corresponding API keys
     * @throws IOException if the `apikey.txt` file does not exist or cannot be read
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
     * Retrieves the API key for the specified service from the `apikey.txt` file.
     *
     * @param serviceName the name of the service whose API key is requested
     * @return the API key for the service, or {@code null} if not found
     * @throws IOException if the API key file does not exist or cannot be read
     */
    public static String getApiKey(String serviceName) throws IOException {
        Map<String, String> apiKeys = loadApiKeys();
        return apiKeys.get(serviceName.toLowerCase());
    }
}
