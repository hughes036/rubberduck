package llm;

import midi.MidiSerializer;
import midi.MidiDeserializer;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for transforming MIDI files using LLMs.
 * This class encapsulates the workflow of serializing a MIDI file, sending it to an LLM with a prompt,
 * and deserializing the response back to a MIDI file.
 */
public class MidiLlmTransformer {
    
    /**
     * Transforms a MIDI file using an LLM.
     * 
     * @param inputFile The input MIDI file
     * @param outputFile The output MIDI file
     * @param prompt The prompt to send to the LLM
     * @param llmService The LLM service to use
     * @throws InvalidMidiDataException If the MIDI data is invalid
     * @throws IOException If there's an error reading or writing files
     * @throws Exception If there's an error communicating with the LLM
     */
    public static void transformMidi(File inputFile, File outputFile, String prompt, LlmService llmService) 
            throws InvalidMidiDataException, IOException, Exception {
        // Serialize the MIDI file
        String serializedMidi = MidiSerializer.serializeMidiFile(inputFile);
        
        // Create the full prompt for the LLM
        String fullPrompt = createLlmPrompt(prompt, serializedMidi);
        
        // Send the prompt to the LLM and get the response
        String llmResponse = llmService.generateResponse(fullPrompt);
        
        // Extract the serialized MIDI from the response
        String extractedMidi = extractSerializedMidi(llmResponse);
        
        // Deserialize the response to a MIDI file
        MidiDeserializer.deserializeToMidiFile(extractedMidi, outputFile);
    }
    
    /**
     * Creates a prompt for the LLM that includes the serialized MIDI data.
     * 
     * @param userPrompt The user's prompt explaining the desired transformation
     * @param serializedMidi The serialized MIDI data
     * @return A full prompt for the LLM
     */
    private static String createLlmPrompt(String userPrompt, String serializedMidi) {
        StringBuilder fullPrompt = new StringBuilder();
        
        // Add instructions for the LLM
        fullPrompt.append("I have a MIDI file that I want to transform. ")
                 .append("Below is the serialized representation of the MIDI file, ")
                 .append("enclosed between ")
                 .append(MidiSerializer.MIDI_START_DELIMITER)
                 .append(" and ")
                 .append(MidiSerializer.MIDI_END_DELIMITER)
                 .append(" delimiters.\n\n");
        
        // Add the serialized MIDI data
        fullPrompt.append(serializedMidi).append("\n\n");
        
        // Add the user's prompt
        fullPrompt.append("Please transform this MIDI file according to these instructions:\n")
                 .append(userPrompt).append("\n\n");
        
        // Add instructions for the response format
        fullPrompt.append("Your response should include the transformed MIDI file in the same serialized format, ")
                 .append("enclosed between the same delimiters. ")
                 .append("Do not modify the format of the serialized MIDI data, ")
                 .append("only the content according to my instructions.");
        
        return fullPrompt.toString();
    }
    
    /**
     * Extracts the serialized MIDI data from the LLM response.
     * 
     * @param llmResponse The response from the LLM
     * @return The extracted serialized MIDI data
     * @throws IllegalArgumentException If the response doesn't contain valid serialized MIDI data
     */
    private static String extractSerializedMidi(String llmResponse) {
        // Look for the start and end delimiters in the response
        int startIndex = llmResponse.indexOf(MidiSerializer.MIDI_START_DELIMITER);
        int endIndex = llmResponse.indexOf(MidiSerializer.MIDI_END_DELIMITER);
        
        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
            throw new IllegalArgumentException("LLM response doesn't contain valid serialized MIDI data");
        }
        
        // Extract the serialized MIDI data between the delimiters
        return llmResponse.substring(startIndex, endIndex + MidiSerializer.MIDI_END_DELIMITER.length());
    }
}