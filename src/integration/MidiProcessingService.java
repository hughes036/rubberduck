package integration;

import llm.ApiKeyManager;
import llm.LLMServiceFactory;
import llm.LLMService;
import llm.PromptBuilder;
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
    
    /**
     * Process MIDI data in-memory without file I/O (web-ready version)
     */
    public ProcessingResult processWithLLMInMemory(
        String inputSerializedMidi,
        String prompt,
        String llmService
    ) {
        try {
            System.out.println("🔍 DEBUG: processWithLLMInMemory called");
            System.out.println("  Input MIDI length: " + (inputSerializedMidi != null ? inputSerializedMidi.length() : "null"));
            System.out.println("  Prompt: " + prompt);
            System.out.println("  LLM service: " + llmService);
            
            // Load API keys using static method
            System.out.println("🔍 DEBUG: Attempting to load API key for: " + llmService);
            String apiKey = ApiKeyManager.getApiKey(llmService);
            System.out.println("🔍 DEBUG: API key loaded: " + (apiKey != null ? "YES (length=" + apiKey.length() + ")" : "NO"));
            
            if (apiKey == null) {
                String errorMsg = "API key not found for service: " + llmService;
                System.out.println("❌ ERROR: " + errorMsg);
                return ProcessingResult.failure(new Exception(errorMsg));
            }

            // Create LLM service based on selection
            System.out.println("🔍 DEBUG: Creating LLM service for: " + llmService);
            LLMService service;
            switch (llmService.toLowerCase()) {
                case "gemini":
                    System.out.println("🔍 DEBUG: Creating Gemini service with API key");
                    String originalGoogleApiKey = System.getProperty("GOOGLE_API_KEY");
                    System.setProperty("GOOGLE_API_KEY", apiKey);
                    try {
                        service = LLMServiceFactory.createService("gemini", apiKey);
                    } finally {
                        if (originalGoogleApiKey != null) {
                            System.setProperty("GOOGLE_API_KEY", originalGoogleApiKey);
                        } else {
                            System.clearProperty("GOOGLE_API_KEY");
                        }
                    }
                    break;
                case "gpt4":
                    System.out.println("🔍 DEBUG: Creating GPT-4 service with API key");
                    service = LLMServiceFactory.createService("gpt4", apiKey);
                    break;
                case "claude":
                    System.out.println("🔍 DEBUG: Creating Claude service with API key");
                    service = LLMServiceFactory.createService("claude", apiKey);
                    break;
                default:
                    String errorMsg = "Unsupported LLM service: " + llmService;
                    System.out.println("❌ ERROR: " + errorMsg);
                    return ProcessingResult.failure(new Exception(errorMsg));
            }

            // Use provided serialized MIDI or hardcoded example
            String serializedMidi = (inputSerializedMidi != null) ? inputSerializedMidi : getHardcodedMidiExample();
            System.out.println("🔍 DEBUG: Using MIDI data, length: " + serializedMidi.length() + " characters");
            
            // Create full prompt with serialized MIDI data
            String fullPrompt = buildPrompt(serializedMidi, prompt);
            System.out.println("🔍 DEBUG: Full prompt created, length: " + fullPrompt.length() + " characters");
            
            // Get LLM response (this returns modified MIDI data)
            System.out.println("🔍 DEBUG: Sending request to LLM service...");
            String llmResponse = service.processCompositionRequest(fullPrompt);
            System.out.println("🔍 DEBUG: LLM response received, length: " + llmResponse.length() + " characters");
            
            // Extract serialized MIDI data from LLM response
            System.out.println("🔍 DEBUG: Extracting serialized MIDI data from LLM response...");
            String extractedMidi = PromptBuilder.extractSerializedMidi(llmResponse);
            System.out.println("🔍 DEBUG: Extracted MIDI data, length: " + extractedMidi.length() + " characters");
            
            String successMsg = "Successfully processed MIDI data in memory. Result length: " + extractedMidi.length() + " characters";
            System.out.println("✅ SUCCESS: " + successMsg);
            return ProcessingResult.success(extractedMidi); // Return the serialized MIDI data instead of file path
        } catch (Exception e) {
            String errorMsg = "Error processing MIDI: " + e.getMessage();
            System.out.println("❌ ERROR: " + errorMsg);
            e.printStackTrace();
            return ProcessingResult.failure(e);
        }
    }

    public ProcessingResult processWithLLM(
        String inputFilePath,
        String prompt,
        String llmService,
        String outputFilePath
    ) {
        try {
            System.out.println("🔍 DEBUG: processWithLLM called");
            System.out.println("  Input file: " + inputFilePath);
            System.out.println("  Prompt: " + prompt);
            System.out.println("  LLM service: " + llmService);
            System.out.println("  Output file: " + outputFilePath);
            
            // Load API keys using static method
            System.out.println("🔍 DEBUG: Attempting to load API key for: " + llmService);
            String apiKey = ApiKeyManager.getApiKey(llmService);
            System.out.println("🔍 DEBUG: API key loaded: " + (apiKey != null ? "YES (length=" + apiKey.length() + ")" : "NO"));
            
            if (apiKey == null) {
                String errorMsg = "API key not found for service: " + llmService;
                System.out.println("❌ ERROR: " + errorMsg);
                return ProcessingResult.failure(new Exception(errorMsg));
            }

            // Create LLM service based on selection
            System.out.println("🔍 DEBUG: Creating LLM service for: " + llmService);
            LLMService service;
            switch (llmService.toLowerCase()) {
                case "gemini":
                    System.out.println("🔍 DEBUG: Creating Gemini service with API key");
                    // Gemini service reads from GOOGLE_API_KEY environment variable
                    // Set it temporarily for this service creation
                    String originalGoogleApiKey = System.getProperty("GOOGLE_API_KEY");
                    System.setProperty("GOOGLE_API_KEY", apiKey);
                    try {
                        service = LLMServiceFactory.createService("gemini", apiKey);
                    } finally {
                        // Restore original value
                        if (originalGoogleApiKey != null) {
                            System.setProperty("GOOGLE_API_KEY", originalGoogleApiKey);
                        } else {
                            System.clearProperty("GOOGLE_API_KEY");
                        }
                    }
                    break;
                case "gpt4":
                    System.out.println("🔍 DEBUG: Creating GPT-4 service with API key");
                    service = LLMServiceFactory.createService("gpt4", apiKey);
                    break;
                case "claude":
                    System.out.println("🔍 DEBUG: Creating Claude service with API key");
                    service = LLMServiceFactory.createService("claude", apiKey);
                    break;
                default:
                    String errorMsg = "Unsupported LLM service: " + llmService;
                    System.out.println("❌ ERROR: " + errorMsg);
                    return ProcessingResult.failure(new Exception(errorMsg));
            }

            // Get serialized MIDI data - either from input file or example
            String serializedMidi;
            if (inputFilePath != null) {
                // Process MIDI file
                File inputFile = new File(inputFilePath);
                System.out.println("🔍 DEBUG: Checking input file: " + inputFile.getAbsolutePath());
                if (!inputFile.exists()) {
                    String errorMsg = "Input file does not exist: " + inputFilePath;
                    System.out.println("❌ ERROR: " + errorMsg);
                    return ProcessingResult.failure(new Exception(errorMsg));
                }
                System.out.println("🔍 DEBUG: Input file exists, proceeding with serialization");
                
                // Serialize MIDI to text (note: this reads and serializes the file)
                System.out.println("🔍 DEBUG: Serializing MIDI file...");
                serializedMidi = MidiSerializer.serializeMidiFile(inputFile);
                System.out.println("🔍 DEBUG: MIDI serialized, length: " + serializedMidi.length() + " characters");
            } else {
                // Use hardcoded example for prompt-only generation
                System.out.println("🔍 DEBUG: No input file provided, using hardcoded MIDI example");
                serializedMidi = getHardcodedMidiExample();
                System.out.println("🔍 DEBUG: Using hardcoded MIDI example, length: " + serializedMidi.length() + " characters");
            }            // Create full prompt with serialized MIDI data
            String fullPrompt = buildPrompt(serializedMidi, prompt);
            System.out.println("🔍 DEBUG: Full prompt created, length: " + fullPrompt.length() + " characters");
            
            // Get LLM response (this returns modified MIDI data)
            System.out.println("🔍 DEBUG: Sending request to LLM service...");
            String llmResponse = service.processCompositionRequest(fullPrompt);
            System.out.println("🔍 DEBUG: LLM response received, length: " + llmResponse.length() + " characters");
            
            // Extract serialized MIDI data from LLM response
            System.out.println("🔍 DEBUG: Extracting serialized MIDI data from LLM response...");
            String extractedMidi = PromptBuilder.extractSerializedMidi(llmResponse);
            System.out.println("🔍 DEBUG: Extracted MIDI data, length: " + extractedMidi.length() + " characters");
            
            // Deserialize back to MIDI file
            File outputFile = new File(outputFilePath);
            System.out.println("🔍 DEBUG: Deserializing response to MIDI file: " + outputFile.getAbsolutePath());
            MidiDeserializer.deserializeToMidiFile(extractedMidi, outputFile);
            System.out.println("🔍 DEBUG: MIDI file written successfully");
            
            String successMsg = "Successfully processed MIDI file. Output saved to: " + outputFilePath;
            System.out.println("✅ SUCCESS: " + successMsg);
            return ProcessingResult.success(successMsg);
        } catch (Exception e) {
            String errorMsg = "Error processing MIDI: " + e.getMessage();
            System.out.println("❌ ERROR: " + errorMsg);
            e.printStackTrace();
            return ProcessingResult.failure(e);
        }
    }
    
    private String buildPrompt(String serializedMidi, String userPrompt) {
        return PromptBuilder.buildCompositionPrompt(serializedMidi, userPrompt);
    }
    
    public Set<String> getAvailableServices() {
        Set<String> available = new HashSet<>();
        
        try {
            // Use ApiKeyManager to get only services with valid API keys
            Set<String> servicesWithKeys = ApiKeyManager.getAvailableServices();
            
            // Convert to uppercase format expected by UI
            for (String service : servicesWithKeys) {
                available.add(service.toUpperCase());
            }
            
        } catch (Exception e) {
            System.out.println("Warning: Could not load API keys: " + e.getMessage());
        }
        
        return available;
    }
    
    /**
     * Plays or pauses MIDI data from serialized string (in-memory version)
     * @param serializedMidi The serialized MIDI data string
     * @param sessionId Unique identifier for this playback session
     * @return true if now playing, false if paused
     */
    public boolean playPauseInMemory(String serializedMidi, String sessionId) {
        return MidiPlaybackService.getInstance().playPauseInMemory(serializedMidi, sessionId);
    }
    
    /**
     * Stops playback of in-memory MIDI data
     * @param sessionId The session identifier
     */
    public void stopInMemory(String sessionId) {
        MidiPlaybackService.getInstance().stopInMemory(sessionId);
    }

    /**
     * Plays or pauses the specified MIDI file.
     * @param filePath The path to the MIDI file
     * @return true if now playing, false if paused
     */
    public boolean playPause(String filePath) {
        return MidiPlaybackService.getInstance().playPause(filePath);
    }
    
    /**
     * Stops playback of the specified MIDI file.
     * @param filePath The path to the MIDI file
     */
    public void stop(String filePath) {
        MidiPlaybackService.getInstance().stop(filePath);
    }
    
    /**
     * Returns true if currently playing.
     */
    public boolean isPlaying() {
        return MidiPlaybackService.getInstance().isPlaying();
    }
    
    /**
     * Gets the current playback position as a percentage (0.0 to 1.0).
     */
    public double getPosition() {
        return MidiPlaybackService.getInstance().getPosition();
    }
    
    /**
     * Gets the duration of the current file in seconds.
     */
    public double getDuration() {
        return MidiPlaybackService.getInstance().getDuration();
    }

    /**
     * Sets the current playback position as a percentage (0.0 to 1.0).
     */
    public void setPosition(String filePath, double position) {
        MidiPlaybackService.getInstance().setPosition(filePath, position);
    }
    
    /**
     * Gets the duration of a specific MIDI file without loading it for playback.
     */
    public double getDurationForFile(String filePath) {
        try {
            // Temporarily load the file to get its duration
            javax.sound.midi.Sequence sequence = javax.sound.midi.MidiSystem.getSequence(new java.io.File(filePath));
            return sequence.getMicrosecondLength() / 1000000.0;
        } catch (Exception e) {
            System.err.println("❌ Error getting duration for file " + filePath + ": " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Creates an in-memory MidiFile from serialized MIDI data
     */
    public static class MidiFileFromMemory {
        public final String name;
        public final String sessionId;
        public final String serializedData;
        public final double duration;
        
        public MidiFileFromMemory(String name, String sessionId, String serializedData, double duration) {
            this.name = name;
            this.sessionId = sessionId;
            this.serializedData = serializedData;
            this.duration = duration;
        }
    }
    
    /**
     * Creates a MidiFile object for in-memory MIDI data
     */
    public MidiFileFromMemory createInMemoryMidiFile(String serializedMidi, String baseName) {
        try {
            // Generate unique session ID
            String sessionId = "session_" + System.currentTimeMillis();
            String name = baseName + "_" + sessionId;
            
            // Get duration by temporarily creating a Sequence
            javax.sound.midi.Sequence sequence = midi.MidiDeserializer.deserializeToSequence(serializedMidi);
            double duration = sequence.getMicrosecondLength() / 1000000.0;
            
            return new MidiFileFromMemory(name, sessionId, serializedMidi, duration);
        } catch (Exception e) {
            System.err.println("Error creating in-memory MIDI file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets visualization data for serialized MIDI data
     */
    public MidiVisualizationService.MidiVisualizationData getVisualizationDataFromMemory(String serializedMidi) {
        try {
            // Create a temporary file to pass to the visualization service
            // Note: This is a temporary workaround until we enhance MidiVisualizationService for in-memory data
            java.io.File tempFile = java.io.File.createTempFile("temp_midi_viz", ".mid");
            tempFile.deleteOnExit();
            
            // Deserialize to file temporarily
            midi.MidiDeserializer.deserializeToMidiFile(serializedMidi, tempFile);
            
            // Get visualization data
            MidiVisualizationService.MidiVisualizationData data = MidiVisualizationService.extractVisualizationData(tempFile.getAbsolutePath());
            
            // Clean up temp file
            tempFile.delete();
            
            return data;
        } catch (Exception e) {
            System.err.println("Error getting visualization data from memory: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns a hardcoded MIDI example for prompt-only generation.
     * This provides a simple 4/4 kick and snare pattern that LLMs can learn from.
     */
    private String getHardcodedMidiExample() {
        return "MIDI_HEADER|divisionType=0.0|resolution=480\n" +
               "TRACKS|count=1\n" +
               "TRACK|number=0|events=19\n" +
               "EVENT|tick=0|type=MetaMessage|metaType=3|description=TRACK_NAME|name=Drums\n" +
               "EVENT|tick=0|type=ShortMessage|command=192|channel=9|data1=0|data2=0|description=PROGRAM_CHANGE|program=0\n" +
               "EVENT|tick=0|type=ShortMessage|command=144|channel=9|data1=36|data2=100|description=NOTE_ON|note=36|velocity=100\n" +
               "EVENT|tick=480|type=ShortMessage|command=128|channel=9|data1=36|data2=0|description=NOTE_OFF|note=36|velocity=0\n" +
               "EVENT|tick=480|type=ShortMessage|command=144|channel=9|data1=38|data2=90|description=NOTE_ON|note=38|velocity=90\n" +
               "EVENT|tick=960|type=ShortMessage|command=128|channel=9|data1=38|data2=0|description=NOTE_OFF|note=38|velocity=0\n" +
               "EVENT|tick=960|type=ShortMessage|command=144|channel=9|data1=36|data2=100|description=NOTE_ON|note=36|velocity=100\n" +
               "EVENT|tick=1440|type=ShortMessage|command=128|channel=9|data1=36|data2=0|description=NOTE_OFF|note=36|velocity=0\n" +
               "EVENT|tick=1440|type=ShortMessage|command=144|channel=9|data1=38|data2=90|description=NOTE_ON|note=38|velocity=90\n" +
               "EVENT|tick=1920|type=ShortMessage|command=128|channel=9|data1=38|data2=0|description=NOTE_OFF|note=38|velocity=0\n" +
               "EVENT|tick=1920|type=ShortMessage|command=144|channel=9|data1=36|data2=100|description=NOTE_ON|note=36|velocity=100\n" +
               "EVENT|tick=2400|type=ShortMessage|command=128|channel=9|data1=36|data2=0|description=NOTE_OFF|note=36|velocity=0\n" +
               "EVENT|tick=2400|type=ShortMessage|command=144|channel=9|data1=38|data2=90|description=NOTE_ON|note=38|velocity=90\n" +
               "EVENT|tick=2880|type=ShortMessage|command=128|channel=9|data1=38|data2=0|description=NOTE_OFF|note=38|velocity=0\n" +
               "EVENT|tick=2880|type=ShortMessage|command=144|channel=9|data1=36|data2=100|description=NOTE_ON|note=36|velocity=100\n" +
               "EVENT|tick=3360|type=ShortMessage|command=128|channel=9|data1=36|data2=0|description=NOTE_OFF|note=36|velocity=0\n" +
               "EVENT|tick=3360|type=ShortMessage|command=144|channel=9|data1=38|data2=90|description=NOTE_ON|note=38|velocity=90\n" +
               "EVENT|tick=3840|type=ShortMessage|command=128|channel=9|data1=38|data2=0|description=NOTE_OFF|note=38|velocity=0\n" +
               "EVENT|tick=3840|type=MetaMessage|metaType=47|description=END_OF_TRACK\n";
    }
    
    /**
     * Gets visualization data for a MIDI file
     */
    public MidiVisualizationService.MidiVisualizationData getVisualizationData(String filePath) {
        return MidiVisualizationService.extractVisualizationData(filePath);
    }
    
    /**
     * Gets all API keys for configuration UI
     */
    public java.util.Map<String, String> getAllApiKeys() {
        try {
            return ApiKeyManager.loadApiKeys();
        } catch (Exception e) {
            System.out.println("Warning: Could not load API keys: " + e.getMessage());
            return new java.util.HashMap<>();
        }
    }
    
    /**
     * Updates an API key and saves configuration
     */
    public void updateApiKey(String serviceName, String apiKey) {
        try {
            ApiKeyManager.updateApiKey(serviceName, apiKey);
        } catch (Exception e) {
            System.err.println("Error updating API key for " + serviceName + ": " + e.getMessage());
            throw new RuntimeException("Failed to update API key", e);
        }
    }
    
    /**
     * Saves all API keys at once
     */
    public void saveAllApiKeys(java.util.Map<String, String> apiKeys) {
        try {
            ApiKeyManager.saveApiKeys(apiKeys);
        } catch (Exception e) {
            System.err.println("Error saving API keys: " + e.getMessage());
            throw new RuntimeException("Failed to save API keys", e);
        }
    }
}
