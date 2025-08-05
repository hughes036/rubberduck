package llm;

/**
 * Utility class for constructing prompts for LLM-based MIDI composition.
 */
public class PromptBuilder {
    
    private static final String SERIALIZATION_EXPLANATION = 
        "You are a MIDI composition assistant. You will receive a serialized MIDI file and a composition request.\n\n" +
        "SERIALIZED MIDI FORMAT:\n" +
        "The MIDI data is represented in a text format with pipe-separated key-value pairs:\n" +
        "- MIDI_HEADER|divisionType=X|resolution=Y\n" +
        "- TRACKS|count=N\n" +
        "- TRACK|number=X|events=Y\n" +
        "- EVENT|tick=T|type=MessageType|command=C|channel=Ch|data1=D1|data2=D2|description=DESC|additional_fields...\n\n" +
        "IMPORTANT RESPONSE REQUIREMENTS:\n" +
        "1. Your response MUST include the complete modified MIDI data in the EXACT same serialized format\n" +
        "2. Preserve the original structure and formatting precisely\n" +
        "3. Make only the changes requested in the composition prompt\n" +
        "4. Ensure all tick timing, channels, and MIDI commands are valid\n" +
        "5. Start your response with the modified serialized MIDI data\n" +
        "6. You may add explanatory text AFTER the serialized data\n\n";
    
    /**
     * Build a complete prompt for LLM MIDI composition.
     * 
     * @param serializedMidi The original MIDI file in serialized format
     * @param compositionPrompt The user's composition request
     * @return Complete prompt ready to send to LLM
     */
    public static String buildCompositionPrompt(String serializedMidi, String compositionPrompt) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(SERIALIZATION_EXPLANATION);
        
        prompt.append("ORIGINAL MIDI DATA:\n");
        prompt.append(serializedMidi);
        prompt.append("\n\n");
        
        prompt.append("COMPOSITION REQUEST:\n");
        prompt.append(compositionPrompt);
        prompt.append("\n\n");
        
        prompt.append("Please modify the MIDI data according to the composition request and provide the complete modified serialized MIDI data in your response.");
        
        return prompt.toString();
    }
    
    /**
     * Extract serialized MIDI data from LLM response.
     * Looks for the serialized MIDI format in the response.
     * 
     * @param llmResponse The complete response from the LLM
     * @return The extracted serialized MIDI data
     * @throws IllegalArgumentException if no valid serialized MIDI is found
     */
    public static String extractSerializedMidi(String llmResponse) {
        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("LLM response is empty");
        }
        
        // Look for the start of MIDI data (should start with MIDI_HEADER)
        String[] lines = llmResponse.split("\n");
        StringBuilder midiData = new StringBuilder();
        boolean inMidiSection = false;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.startsWith("MIDI_HEADER|")) {
                inMidiSection = true;
                midiData.append(line).append("\n");
            } else if (inMidiSection) {
                if (line.startsWith("TRACKS|") || 
                    line.startsWith("TRACK|") || 
                    line.startsWith("EVENT|")) {
                    midiData.append(line).append("\n");
                } else if (!line.isEmpty() && !line.startsWith("```") && !line.contains("|")) {
                    // We've likely reached the end of the MIDI data
                    break;
                }
            }
        }
        
        String result = midiData.toString().trim();
        if (result.isEmpty() || !result.startsWith("MIDI_HEADER|")) {
            throw new IllegalArgumentException("No valid serialized MIDI data found in LLM response");
        }
        
        return result;
    }
}
