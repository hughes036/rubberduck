import midi.MidiSerializer;
import midi.MidiDeserializer;
import midi.MidiUtils;
import llm.LlmService;
import llm.LlmServiceFactory;
import llm.MidiLlmTransformer;

import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Main class for the MIDI processing CLI tool.
 * This class provides a command-line interface for converting between MIDI files
 * and serialized text format.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Check if arguments are provided
            if (args.length < 1) {
                printUsage();
                return;
            }

            // Check if the first argument is a command
            if (args[0].equals("transform")) {
                // LLM-based MIDI transformation mode
                if (args.length < 5) {
                    System.err.println("Error: Not enough arguments for transform command");
                    printUsage();
                    return;
                }

                // Parse arguments
                String inputFilePath = args[1];
                String outputFilePath = args[2];
                String llmName = args[3];
                String apiKey = args[4];

                // Combine remaining arguments as the prompt
                StringBuilder promptBuilder = new StringBuilder();
                for (int i = 5; i < args.length; i++) {
                    promptBuilder.append(args[i]).append(" ");
                }
                String prompt = promptBuilder.toString().trim();

                // Check if the input file exists
                File inputFile = new File(inputFilePath);
                if (!inputFile.exists()) {
                    System.err.println("Error: Input file does not exist: " + inputFilePath);
                    printUsage();
                    return;
                }

                // Check if the input is a MIDI file
                if (!isMidiFile(inputFile)) {
                    System.err.println("Error: Input file is not a MIDI file: " + inputFilePath);
                    printUsage();
                    return;
                }

                // Create the output file
                File outputFile = new File(outputFilePath);

                // Get the LLM service
                LlmService llmService;
                try {
                    llmService = LlmServiceFactory.getService(llmName);
                    llmService.setApiKey(apiKey);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    printAvailableLlmServices();
                    return;
                }

                // Transform the MIDI file
                System.out.println("Transforming MIDI file using " + llmService.getName() + "...");
                MidiLlmTransformer.transformMidi(inputFile, outputFile, prompt, llmService);
                System.out.println("Successfully transformed MIDI file.");
                System.out.println("Output file: " + outputFilePath);

                return;
            }

            // Standard conversion mode (original functionality)
            String inputFilePath = args[0];
            File inputFile = new File(inputFilePath);

            // Check if the file exists
            if (!inputFile.exists()) {
                System.err.println("Error: File does not exist: " + inputFilePath);
                printUsage();
                return;
            }

            // Determine if the input is a MIDI file or a serialized text file
            boolean isMidiFile = isMidiFile(inputFile);

            if (isMidiFile) {
                // Convert MIDI file to serialized text
                String outputFilePath = getOutputFilePath(inputFilePath, ".txt");
                String serialized = MidiSerializer.serializeMidiFile(inputFile);

                // Write the serialized data to a file
                Files.write(Paths.get(outputFilePath), serialized.getBytes());

                System.out.println("Successfully converted MIDI file to serialized format.");
                System.out.println("Output file: " + outputFilePath);

                // Print a sample of the serialized data
                System.out.println("\nSerialized data sample (first 200 chars):");
                System.out.println(serialized.substring(0, Math.min(serialized.length(), 200)) + "...");
            } else {
                // Convert serialized text to MIDI file
                String outputFilePath = getOutputFilePath(inputFilePath, ".mid");
                File outputFile = new File(outputFilePath);

                // Read the serialized data from the file
                String serialized = new String(Files.readAllBytes(Paths.get(inputFilePath)));

                // Deserialize to MIDI file
                MidiDeserializer.deserializeToMidiFile(serialized, outputFile);

                System.out.println("Successfully converted serialized format to MIDI file.");
                System.out.println("Output file: " + outputFilePath);

                // Print information about the created MIDI file
                String midiInfo = MidiUtils.getMidiFileInfo(outputFile);
                System.out.println("\nMIDI file information:");
                System.out.println(midiInfo);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            printUsage();
        }
    }

    /**
     * Prints the usage information for the CLI tool.
     */
    private static void printUsage() {
        System.out.println("MIDI Processing CLI Tool");
        System.out.println("======================");
        System.out.println("Usage:");
        System.out.println("  1. Standard conversion mode:");
        System.out.println("     java -jar midi-processor.jar <input-file>");
        System.out.println("       <input-file>: Path to a MIDI file (.mid) or a serialized text file");
        System.out.println();
        System.out.println("     The tool automatically detects the file type and performs the appropriate conversion:");
        System.out.println("       - If the input is a MIDI file, it will be converted to a serialized text format");
        System.out.println("       - If the input is a serialized text file, it will be converted to a MIDI file");
        System.out.println();
        System.out.println("     Examples:");
        System.out.println("       java -jar midi-processor.jar input.mid");
        System.out.println("       java -jar midi-processor.jar serialized.txt");
        System.out.println();
        System.out.println("  2. LLM-based transformation mode:");
        System.out.println("     java -jar midi-processor.jar transform <input-file> <output-file> <llm-name> <api-key> <prompt>");
        System.out.println("       <input-file>: Path to a MIDI file (.mid)");
        System.out.println("       <output-file>: Path where the transformed MIDI file will be saved");
        System.out.println("       <llm-name>: Name of the LLM service to use (e.g., openai)");
        System.out.println("       <api-key>: API key for the LLM service");
        System.out.println("       <prompt>: Natural language prompt explaining the desired transformation");
        System.out.println();
        System.out.println("     Example:");
        System.out.println("       java -jar midi-processor.jar transform input.mid output.mid openai sk-your-api-key \"Make this melody more jazzy\"");
        System.out.println();
        System.out.println("     Available LLM services:");
        printAvailableLlmServices();
    }

    /**
     * Prints the available LLM services.
     */
    private static void printAvailableLlmServices() {
        Set<String> services = LlmServiceFactory.getAvailableServices();
        if (services.isEmpty()) {
            System.out.println("     No LLM services available.");
            return;
        }

        for (String service : services) {
            System.out.println("     - " + service);
        }
    }

    /**
     * Determines if a file is a MIDI file based on its content.
     * 
     * @param file The file to check
     * @return true if the file is a MIDI file, false otherwise
     */
    private static boolean isMidiFile(File file) throws IOException {
        // Check file extension first
        if (file.getName().toLowerCase().endsWith(".mid") || 
            file.getName().toLowerCase().endsWith(".midi")) {
            return true;
        }

        // If extension doesn't help, try to read the first few bytes
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine != null) {
                // Check if the file starts with the MIDI header marker
                if (firstLine.startsWith("MIDI_HEADER")) {
                    return false; // It's a serialized text file
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

        // Default to false if we can't determine
        return false;
    }

    /**
     * Generates an output file path based on the input file path and a new extension.
     * 
     * @param inputFilePath The input file path
     * @param newExtension The new file extension (including the dot)
     * @return The output file path
     */
    private static String getOutputFilePath(String inputFilePath, String newExtension) {
        // Remove the extension from the input file path
        int lastDotIndex = inputFilePath.lastIndexOf('.');
        String basePath = (lastDotIndex > 0) ? inputFilePath.substring(0, lastDotIndex) : inputFilePath;

        // Add the new extension
        return basePath + newExtension;
    }
}
