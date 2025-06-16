import midi.MidiSerializer;
import midi.MidiDeserializer;
import midi.MidiUtils;

import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

            // Get the input file path from arguments
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
        System.out.println("Usage: java -jar midi-processor.jar <input-file>");
        System.out.println("  <input-file>: Path to a MIDI file (.mid) or a serialized text file");
        System.out.println();
        System.out.println("The tool automatically detects the file type and performs the appropriate conversion:");
        System.out.println("  - If the input is a MIDI file, it will be converted to a serialized text format");
        System.out.println("  - If the input is a serialized text file, it will be converted to a MIDI file");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar midi-processor.jar input.mid");
        System.out.println("  java -jar midi-processor.jar serialized.txt");
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
