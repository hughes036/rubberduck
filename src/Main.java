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
    public static void main(String[] args) {
        try {
            // Check if arguments are provided
            if (args.length < 4) {
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
            
            if (compositionPrompt.length() == 0) {
                System.err.println("Error: Composition prompt is required");
                printUsage();
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
     * Get API key for the specified service.
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
     * Extract explanation text from LLM response (everything after the serialized MIDI data).
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
     * Prints the usage information for the CLI tool.
     */
    private static void printUsage() {
        System.out.println("🎵 LLM-Powered MIDI Composer");
        System.out.println("============================");
        System.out.println("Usage: java -jar rubberduck.jar <input-midi> <output-midi> <llm-service> <api-key> <composition-prompt>");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  <input-midi>         Path to input MIDI file");
        System.out.println("  <output-midi>        Path for output MIDI file");
        System.out.println("  <llm-service>        LLM service to use: " + String.join(", ", LLMServiceFactory.getSupportedServices()));
        System.out.println("  <api-key>            API key (use \"\" to load from apikey.txt)");
        System.out.println("  <composition-prompt> What you want the LLM to do to the music");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # Add a bassline using Gemini with API key from file");
        System.out.println("  java -jar rubberduck.jar input.mid output.mid gemini \"\" \"Add a walking bassline\"");
        System.out.println();
        System.out.println("  # Add drums using Gemini with inline API key");
        System.out.println("  java -jar rubberduck.jar song.mid enhanced.mid gemini \"your-api-key\" \"Add a simple drum pattern\"");
        System.out.println();
        System.out.println("API Key File Format (apikey.txt):");
        System.out.println("  # Single key (for backward compatibility):");
        System.out.println("  your-gemini-api-key-here");
        System.out.println();
        System.out.println("  # Or service-specific keys:");
        System.out.println("  gemini=your-gemini-key");
        System.out.println("  gpt4=your-openai-key");
        System.out.println("  claude=your-anthropic-key");
    }

    /**
     * Determines if a file is a MIDI file based on its content.
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
