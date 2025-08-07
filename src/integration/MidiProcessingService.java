package integration;

import llm.ApiKeyManager;
import llm.LLMServiceFactory;
import llm.LLMService;
import midi.MidiDeserializer;
import midi.MidiSerializer;
import java.io.File;
import java.util.Set;
import java.util.HashSet;

/**
 * Integration layer to connect UI with existing Java CLI code
 */
public class MidiProcessingService {
    
    public static class ProcessingResult {
        public final boolean isSuccess;
        public final String message;
        public final Exception exception;
        
        private ProcessingResult(boolean isSuccess, String message, Exception exception) {
            this.isSuccess = isSuccess;
            this.message = message;
            this.exception = exception;
        }
        
        public static ProcessingResult success(String message) {
            return new ProcessingResult(true, message, null);
        }
        
        public static ProcessingResult failure(Exception exception) {
            return new ProcessingResult(false, exception.getMessage(), exception);
        }
    }
    
    public ProcessingResult processWithLLM(
        String inputFilePath,
        String prompt,
        String llmService,
        String outputFilePath
    ) {
        try {
            // Load API keys
            ApiKeyManager apiKeyManager = new ApiKeyManager();
            String geminiApiKey = apiKeyManager.getApiKey("gemini");
            if (geminiApiKey == null) {
                return ProcessingResult.failure(new Exception("Gemini API key not found"));
            }
            
                        // Create LLM service based on selection
            LLMService service;
            switch (llmService.toLowerCase()) {
                case "gemini":
                    service = LLMServiceFactory.createService("gemini", geminiApiKey);
                    break;
                default:
                    return ProcessingResult.failure(new Exception("Unsupported LLM service: " + llmService));
            }
            
            // Process MIDI file
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                return ProcessingResult.failure(new Exception("Input file does not exist: " + inputFilePath));
            }
            
            // Serialize MIDI to text (note: this reads and serializes the file)
            String serializedMidi = MidiSerializer.serializeMidiFile(inputFile);
            
            // Create full prompt with serialized MIDI data
            String fullPrompt = buildPrompt(serializedMidi, prompt);
            
            // Get LLM response (this returns modified MIDI data)
            String llmResponse = service.processCompositionRequest(fullPrompt);
            
            // Deserialize back to MIDI file
            File outputFile = new File(outputFilePath);
            MidiDeserializer.deserializeToMidiFile(llmResponse, outputFile);
            
            return ProcessingResult.success("Successfully processed MIDI file. Output saved to: " + outputFilePath);
        } catch (Exception e) {
            return ProcessingResult.failure(e);
        }
    }
    
    private String buildPrompt(String serializedMidi, String userPrompt) {
        return "You are a MIDI composition assistant. Given the following serialized MIDI data and user request, " +
               "please modify the MIDI data according to the request and return the modified serialized MIDI data.\n\n" +
               "Original MIDI data:\n" + serializedMidi + "\n\n" +
               "User request: " + userPrompt + "\n\n" +
               "Please return only the modified serialized MIDI data, maintaining the same format as the input.";
    }
    
    public Set<String> getAvailableServices() {
        Set<String> available = new HashSet<>();
        
        try {
            ApiKeyManager apiKeyManager = new ApiKeyManager();
            
            // Check for Gemini API key
            try {
                String geminiKey = apiKeyManager.getApiKey("gemini");
                if (geminiKey != null && !geminiKey.trim().isEmpty()) {
                    available.add("GEMINI");
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not load Gemini API key: " + e.getMessage());
            }
            
            // TODO: Check for other services when implemented
            
        } catch (Exception e) {
            System.out.println("Warning: Could not load API keys: " + e.getMessage());
        }
        
        return available;
    }
}
