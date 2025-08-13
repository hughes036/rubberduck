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
     * Loads API keys from apikeys.json file or environment variables.
     *
     * @return a map of service names (in lowercase) to their corresponding API keys
     * @throws IOException if no API keys can be found from file or environment
     */
    public static Map<String, String> loadApiKeys() throws IOException {
        Path jsonFile = Paths.get(API_KEYS_JSON_FILE);
        
        if (Files.exists(jsonFile)) {
            return loadApiKeysFromJson(jsonFile);
        } else {
            return loadApiKeysFromEnvironment();
        }
    }
    
    /**
     * Gets all available LLM service names that have valid API keys.
     * Filters out empty values and placeholder values like "YOUR_*_API_KEY_HERE".
     * 
     * @return a set of service names with configured API keys
     * @throws IOException if API key files cannot be read
     */
    public static Set<String> getAvailableServices() throws IOException {
        Map<String, String> apiKeys = loadApiKeys();
        
        return apiKeys.entrySet().stream()
            .filter(entry -> {
                String value = entry.getValue();
                if (value == null || value.trim().isEmpty()) {
                    return false;
                }
                // Filter out placeholder values like "YOUR_*_API_KEY_HERE"
                return !PLACEHOLDER_API_KEY_PATTERN.matcher(value).matches();
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
    
    /**
     * Checks if the specified LLM service is configured with a valid API key.
     * Filters out empty values and placeholder values like "YOUR_*_API_KEY_HERE".
     * 
     * @param serviceName the name of the LLM service
     * @return true if the service has a valid API key configured
     * @throws IOException if API key files cannot be read
     */
    public static boolean isServiceAvailable(String serviceName) throws IOException {
        Map<String, String> apiKeys = loadApiKeys();
        String key = apiKeys.get(serviceName.toLowerCase());
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        // Filter out placeholder values like "YOUR_*_API_KEY_HERE"
        return !key.matches("YOUR_.*_API_KEY_HERE");
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
     * Loads API keys from environment variables as a secure fallback.
     * Looks for standard environment variable names for each service.
     * Also checks system properties as a fallback for GUI applications.
     */
    private static Map<String, String> loadApiKeysFromEnvironment() throws IOException {
        Map<String, String> apiKeys = new HashMap<>();
        
        // Standard environment variable mappings (check both env vars and system properties)
        String geminiKey = System.getenv("GOOGLE_API_KEY");
        if (geminiKey == null || geminiKey.trim().isEmpty()) {
            geminiKey = System.getProperty("GOOGLE_API_KEY");
        }
        
        String gpt4Key = System.getenv("OPENAI_API_KEY");
        if (gpt4Key == null || gpt4Key.trim().isEmpty()) {
            gpt4Key = System.getProperty("OPENAI_API_KEY");
        }
        
        String claudeKey = System.getenv("ANTHROPIC_API_KEY");
        if (claudeKey == null || claudeKey.trim().isEmpty()) {
            claudeKey = System.getProperty("ANTHROPIC_API_KEY");
        }
        
        if (geminiKey != null && !geminiKey.trim().isEmpty()) {
            apiKeys.put("gemini", geminiKey);
        }
        if (gpt4Key != null && !gpt4Key.trim().isEmpty()) {
            apiKeys.put("gpt4", gpt4Key);
        }
        if (claudeKey != null && !claudeKey.trim().isEmpty()) {
            apiKeys.put("claude", claudeKey);
        }
        
        if (apiKeys.isEmpty()) {
            throw new IOException("No API keys found. Please either:\n" +
                "1. Create " + API_KEYS_JSON_FILE + " file (copy from " + API_KEYS_JSON_FILE + ".example)\n" +
                "2. Set environment variables: GOOGLE_API_KEY, OPENAI_API_KEY, ANTHROPIC_API_KEY");
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
    
    /**
     * Saves the API keys to the apikeys.json file.
     * 
     * @param apiKeys map of service names to API keys
     * @throws IOException if the file cannot be written
     */
    public static void saveApiKeys(Map<String, String> apiKeys) throws IOException {
        Path jsonFile = Paths.get(API_KEYS_JSON_FILE);
        ObjectMapper mapper = new ObjectMapper();
        
        // Pretty print the JSON
        String jsonContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(apiKeys);
        Files.writeString(jsonFile, jsonContent);
    }
    
    /**
     * Updates a specific API key and saves the configuration.
     * 
     * @param serviceName the service name
     * @param apiKey the API key (can be null or empty to remove)
     * @throws IOException if the configuration cannot be saved
     */
    public static void updateApiKey(String serviceName, String apiKey) throws IOException {
        Map<String, String> apiKeys;
        try {
            apiKeys = loadApiKeys();
        } catch (IOException e) {
            // If no existing file, create new map
            apiKeys = new HashMap<>();
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKeys.remove(serviceName.toLowerCase());
        } else {
            apiKeys.put(serviceName.toLowerCase(), apiKey.trim());
        }
        
        saveApiKeys(apiKeys);
    }
}
