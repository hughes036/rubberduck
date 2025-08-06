import midi.MidiSerializer;
import midi.MidiDeserializer;
import midi.MidiUtils;
import llm.LLMService;
import llm.LLMServiceFactory;
import llm.ApiKeyManager;
import llm.PromptBuilder;

import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Main class for the LLM-powered MIDI composition tool.
 * This tool uses LLMs to modify MIDI files based on composition prompts.
 */
public class Main {
    /**
     * Entry point for the LLM-powered MIDI composition tool.
     *
     * This method processes command-line arguments to modify a MIDI file based on a user-provided composition prompt using a specified large language model (LLM) service. It validates inputs, retrieves the API key, serializes the input MIDI file, interacts with the LLM to generate a modified composition, deserializes the result, and writes the output MIDI file. Progress and results are printed to the console.
     *
     * Expects at least four arguments: input MIDI file path, output MIDI file path, LLM service name, API key (or empty string to load from file), followed by the composition prompt.
     */
    public static void main(String[] args) {
        try {
            // If no arguments are provided, print usage and exit
            if (args.length == 0) {
                printUsage();
                return;
            }

            // Check for minimum number of arguments
            if (args.length < 5) {
                System.err.println("❌ Error: Missing arguments.");
                System.err.println();
                printUsage();
                return;
            }

            // Parse command line arguments
            String inputMidiPath = args[0];
            String outputMidiPath = args[1];
            String llmService = args[2];
            String apiKeyArg = args[3];
            
            // Composition prompt is all remaining arguments joined together
            StringBuilder compositionPrompt = new StringBuilder();
            for (int i = 4; i < args.length; i++) {
                if (i > 4) compositionPrompt.append(" ");
                compositionPrompt.append(args[i]);
            }
            
            if (compositionPrompt.toString().trim().isEmpty()) {
                System.err.println("Error: Composition prompt is required");
                printUsage();
                return;
            }

            // Validate LLM service name against available services
            try {
                if (!ApiKeyManager.isServiceAvailable(llmService)) {
                    System.err.println("❌ Error: LLM service '" + llmService + "' is not available.");
                    System.err.println("Available services: " + ApiKeyManager.getAvailableServices());
                    System.err.println("Please check your " + (new File("apikeys.json").exists() ? "apikeys.json" : "apikey.txt") + " file.");
                    return;
                }
            } catch (IOException e) {
                System.err.println("❌ Error: Could not load API key configuration: " + e.getMessage());
                return;
            }

            // Validate input file
            File inputFile = new File(inputMidiPath);
            if (!inputFile.exists()) {
                System.err.println("Error: Input MIDI file does not exist: " + inputMidiPath);
                return;
            }

            if (!isMidiFile(inputFile)) {
                System.err.println("Error: Input file is not a valid MIDI file: " + inputMidiPath);
                return;
            }

            // Get API key
            String apiKey = getApiKey(llmService, apiKeyArg);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.err.println("Error: No API key found for service: " + llmService);
                System.err.println("Either provide API key as argument or add it to apikey.txt");
                return;
            }

            System.out.println("🎵 LLM-Powered MIDI Composer");
            System.out.println("============================");
            System.out.println("Input MIDI: " + inputMidiPath);
            System.out.println("Output MIDI: " + outputMidiPath);
            System.out.println("LLM Service: " + llmService);
            System.out.println("Composition Request: " + compositionPrompt.toString());
            System.out.println();

            // Step 1: Serialize the input MIDI file
            System.out.println("Step 1: Converting MIDI to serialized format...");
            String serializedMidi = MidiSerializer.serializeMidiFile(inputFile);
            System.out.println("✓ MIDI file serialized successfully");

            // Step 2: Create LLM service and build prompt
            System.out.println("\nStep 2: Preparing LLM request...");
            LLMService llm = LLMServiceFactory.createService(llmService, apiKey);
            String prompt = PromptBuilder.buildCompositionPrompt(serializedMidi, compositionPrompt.toString());
            System.out.println("✓ LLM service initialized: " + llm.getServiceName());

            // Step 3: Send request to LLM
            System.out.println("\nStep 3: Sending composition request to " + llmService + "...");
            String llmResponse = llm.processCompositionRequest(prompt);
            System.out.println("✓ LLM response received");

            // Step 4: Extract modified MIDI from response
            System.out.println("\nStep 4: Extracting modified MIDI data...");
            String modifiedSerializedMidi = PromptBuilder.extractSerializedMidi(llmResponse);
            System.out.println("✓ Modified MIDI data extracted");

            // Step 5: Deserialize back to MIDI file
            System.out.println("\nStep 5: Creating output MIDI file...");
            File outputFile = new File(outputMidiPath);
            MidiDeserializer.deserializeToMidiFile(modifiedSerializedMidi, outputFile);
            System.out.println("✓ Output MIDI file created: " + outputMidiPath);

            // Print information about the created MIDI file
            String midiInfo = MidiUtils.getMidiFileInfo(outputFile);
            System.out.println("\n🎶 Composition Complete!");
            System.out.println("======================");
            System.out.println(midiInfo);

            // Optionally print a preview of what the LLM said
            System.out.println("\n💭 LLM Explanation:");
            System.out.println("==================");
            String explanation = extractExplanation(llmResponse);
            if (!explanation.trim().isEmpty()) {
                System.out.println(explanation);
            } else {
                System.out.println("(No additional explanation provided)");
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println();
            printUsage();
        }
    }

    /**
     * Retrieves the API key for the specified LLM service.
     *
     * If a non-empty API key argument is provided, it is returned directly; otherwise, the key is loaded from persistent storage.
     *
     * @param serviceName the name of the LLM service for which the API key is required
     * @param apiKeyArg the API key provided as a command-line argument, or an empty string to trigger file-based retrieval
     * @return the API key string for the specified service
     * @throws IOException if loading the API key from storage fails
     */
    private static String getApiKey(String serviceName, String apiKeyArg) throws IOException {
        // If API key is provided as argument and not empty, use it
        if (apiKeyArg != null && !apiKeyArg.trim().isEmpty()) {
            return apiKeyArg;
        }

        // Otherwise, try to load from apikey.txt
        return ApiKeyManager.getApiKey(serviceName);
    }

    /**
     * Extracts and returns any explanation text from an LLM response, omitting all lines that are part of the serialized MIDI data.
     *
     * The method scans the response line by line, skipping lines that begin with MIDI data markers. Once the MIDI data ends, all subsequent lines are collected as explanation.
     *
     * @param llmResponse the full response string from the LLM, containing serialized MIDI data and optional explanation
     * @return the extracted explanation text, or an empty string if none is found
     */
    private static String extractExplanation(String llmResponse) {
        String[] lines = llmResponse.split("\n");
        StringBuilder explanation = new StringBuilder();
        boolean pastMidiData = false;

        for (String line : lines) {
            line = line.trim();
            
            if (pastMidiData) {
                explanation.append(line).append("\n");
            } else if (!line.isEmpty() && 
                       !line.startsWith("MIDI_HEADER|") && 
                       !line.startsWith("TRACKS|") && 
                       !line.startsWith("TRACK|") && 
                       !line.startsWith("EVENT|")) {
                pastMidiData = true;
                explanation.append(line).append("\n");
            }
        }

        return explanation.toString().trim();
    }

    /**
     * Prints detailed usage instructions for the LLM-powered MIDI composition CLI tool, including argument descriptions, example commands, and API key file formats.
     */
    private static void printUsage() {
        System.out.println("🎵 LLM-Powered MIDI Composer");
        System.out.println("============================");
        System.out.println("Usage: rubberduck <input-midi> <output-midi> <llm-service> <api-key> <composition-prompt>");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  <input-midi>         Path to input MIDI file");
        System.out.println("  <output-midi>        Path for output MIDI file");
        System.out.println("  <llm-service>        LLM service to use: " + String.join(", ", LLMServiceFactory.getSupportedServices()));
        System.out.println("  <api-key>            API key (use \"\" to load from apikeys.json)");
        System.out.println("  <composition-prompt> What you want the LLM to do to the music");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # Add a bassline using Gemini with API key from file");
        System.out.println("  rubberduck input.mid output.mid gemini \"\" \"Add a walking bassline\"");
        System.out.println();
        System.out.println("  # Add drums using Gemini with inline API key");
        System.out.println("  rubberduck song.mid enhanced.mid gemini \"your-api-key\" \"Add a simple drum pattern\"");
        System.out.println();
        System.out.println("📁 API Key Configuration:");
        System.out.println("========================");
        System.out.println("Option 1 - JSON File (copy apikeys.json.example to apikeys.json):");
        System.out.println();
        System.out.println("{");
        System.out.println("  \"gemini\": \"your-gemini-api-key-here\",");
        System.out.println("  \"gpt4\": \"your-openai-api-key-here\",");
        System.out.println("  \"claude\": \"your-anthropic-api-key-here\"");
        System.out.println("}");
        System.out.println();
        System.out.println("Option 2 - Environment Variables (more secure):");
        System.out.println("  export GOOGLE_API_KEY=\"your-gemini-key\"");
        System.out.println("  export OPENAI_API_KEY=\"your-openai-key\"");
        System.out.println("  export ANTHROPIC_API_KEY=\"your-anthropic-key\"");
        System.out.println();
        System.out.println("🔒 Security Notes:");
        System.out.println("- apikeys.json is gitignored for security");
        System.out.println("- Environment variables are recommended for production");
        System.out.println("- Never commit real API keys to version control");
        System.out.println("📝 Notes:");
        System.out.println("- Only services with non-empty API keys will be available");
        System.out.println("- You can also provide API keys directly as command line arguments");
        
        // Show currently configured services
        try {
            var availableServices = ApiKeyManager.getAvailableServices();
            if (!availableServices.isEmpty()) {
                System.out.println("- Currently configured services: " + availableServices);
            } else {
                System.out.println("- No services currently configured (no valid API keys found)");
            }
        } catch (IOException e) {
            System.out.println("- Could not read API key configuration");
        }
    }

    /**
     * Checks whether the specified file is a valid MIDI file by extension or by attempting to parse it.
     *
     * @param file the file to check
     * @return true if the file has a MIDI extension or can be parsed as a MIDI file; false otherwise
     * @throws IOException if an I/O error occurs while accessing the file
     */
    private static boolean isMidiFile(File file) throws IOException {
        // Check file extension first
        if (file.getName().toLowerCase().endsWith(".mid") || 
            file.getName().toLowerCase().endsWith(".midi")) {
            return true;
        }

        // Try to parse as a MIDI file
        try {
            MidiSystem.getSequence(file);
            return true; // Successfully parsed as MIDI
        } catch (InvalidMidiDataException e) {
            return false; // Not a valid MIDI file
        }
    }
}
