/**
 * This class provides functionality to convert a serialized text format
 * back to MIDI files that can be played by MIDI players.
 */
package midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MidiDeserializer {

    // Delimiters for the serialized MIDI format (same as in MidiSerializer)
    public static final String MIDI_START_DELIMITER = "<<MIDI_START>>";
    public static final String MIDI_END_DELIMITER = "<<MIDI_END>>";

    /**
     * Converts a serialized text representation of MIDI data to a MIDI file.
     * 
     * @param serializedMidi The serialized MIDI data as a string
     * @param outputFile The file to write the MIDI data to
     * @throws InvalidMidiDataException If the serialized data cannot be converted to valid MIDI
     * @throws IOException If there's an error writing to the file
     */
    public static void deserializeToMidiFile(String serializedMidi, File outputFile) 
            throws InvalidMidiDataException, IOException {
        Sequence sequence = deserializeToSequence(serializedMidi);
        int fileType = 1; // Multiple track file format
        MidiSystem.write(sequence, fileType, outputFile);
    }

    /**
     * Converts a serialized text representation of MIDI data to a MIDI Sequence object.
     * 
     * @param serializedMidi The serialized MIDI data as a string
     * @return A MIDI Sequence object
     * @throws InvalidMidiDataException If the serialized data cannot be converted to valid MIDI
     */
    public static Sequence deserializeToSequence(String serializedMidi) throws InvalidMidiDataException {
        // Remove delimiters if present
        if (serializedMidi.contains(MIDI_START_DELIMITER)) {
            int startIndex = serializedMidi.indexOf(MIDI_START_DELIMITER) + MIDI_START_DELIMITER.length();
            int endIndex = serializedMidi.indexOf(MIDI_END_DELIMITER);
            if (endIndex > startIndex) {
                serializedMidi = serializedMidi.substring(startIndex, endIndex).trim();
            }
        }

        String[] lines = serializedMidi.split("\n");

        // Parse header information
        if (lines.length < 2 || !lines[0].startsWith("MIDI_HEADER")) {
            throw new InvalidMidiDataException("Invalid serialized MIDI format: missing header");
        }

        Map<String, String> headerInfo = parseKeyValuePairs(lines[0]);
        float divisionType = Float.parseFloat(headerInfo.get("divisionType"));
        int resolution = Integer.parseInt(headerInfo.get("resolution"));

        Sequence sequence = new Sequence(divisionType, resolution);

        // Parse tracks information
        if (!lines[1].startsWith("TRACKS")) {
            throw new InvalidMidiDataException("Invalid serialized MIDI format: missing tracks information");
        }

        int currentLine = 2;
        while (currentLine < lines.length) {
            if (!lines[currentLine].startsWith("TRACK")) {
                break;
            }

            // Create a new track
            Track track = sequence.createTrack();
            currentLine++;

            // Process events for this track
            while (currentLine < lines.length && lines[currentLine].startsWith("EVENT")) {
                Map<String, String> eventInfo = parseKeyValuePairs(lines[currentLine]);

                long tick = Long.parseLong(eventInfo.get("tick"));
                String type = eventInfo.get("type");

                MidiMessage message = null;

                if ("ShortMessage".equals(type)) {
                    int command = Integer.parseInt(eventInfo.get("command"));
                    int channel = Integer.parseInt(eventInfo.get("channel"));
                    int data1 = Integer.parseInt(eventInfo.get("data1"));
                    int data2 = Integer.parseInt(eventInfo.get("data2"));

                    ShortMessage sm = new ShortMessage();
                    sm.setMessage(command, channel, data1, data2);
                    message = sm;
                } else if ("MetaMessage".equals(type)) {
                    int metaType = Integer.parseInt(eventInfo.get("metaType"));
                    byte[] data = null;

                    // Handle specific meta message types
                    if (eventInfo.containsKey("description")) {
                        String description = eventInfo.get("description");

                        if ("TEMPO".equals(description) && eventInfo.containsKey("bpm")) {
                            float bpm = Float.parseFloat(eventInfo.get("bpm"));
                            int tempo = Math.round(60000000f / bpm);
                            data = new byte[3];
                            data[0] = (byte) ((tempo >> 16) & 0xFF);
                            data[1] = (byte) ((tempo >> 8) & 0xFF);
                            data[2] = (byte) (tempo & 0xFF);
                        } else if ("TIME_SIGNATURE".equals(description)) {
                            int numerator = Integer.parseInt(eventInfo.get("numerator"));
                            int denominator = Integer.parseInt(eventInfo.get("denominator"));
                            int denominatorPower = 0;
                            while ((1 << denominatorPower) < denominator) {
                                denominatorPower++;
                            }
                            data = new byte[4];
                            data[0] = (byte) numerator;
                            data[1] = (byte) denominatorPower;
                            data[2] = 24; // MIDI clocks per metronome click
                            data[3] = 8;  // 32nd notes per quarter note
                        } else if ("KEY_SIGNATURE".equals(description)) {
                            int key = Integer.parseInt(eventInfo.get("key"));
                            int scale = Integer.parseInt(eventInfo.get("scale"));
                            data = new byte[2];
                            data[0] = (byte) key;
                            data[1] = (byte) scale;
                        } else if ("TRACK_NAME".equals(description) || "INSTRUMENT_NAME".equals(description)) {
                            String name = eventInfo.get("name");
                            data = name.getBytes();
                        }
                    }

                    if (data == null) {
                        data = new byte[0]; // Default to empty data if not handled specifically
                    }

                    MetaMessage mm = new MetaMessage();
                    mm.setMessage(metaType, data, data.length);
                    message = mm;
                } else if ("SysexMessage".equals(type)) {
                    // For simplicity, we're creating an empty SysexMessage
                    // In a real application, you would need to handle the actual data
                    SysexMessage sm = new SysexMessage();
                    byte[] data = new byte[0];
                    sm.setMessage(SysexMessage.SYSTEM_EXCLUSIVE, data, data.length);
                    message = sm;
                }

                if (message != null) {
                    MidiEvent event = new MidiEvent(message, tick);
                    track.add(event);
                }

                currentLine++;
            }
        }

        return sequence;
    }

    /**
     * Parses a string of key-value pairs separated by pipes (|) into a map.
     * 
     * @param line The string to parse
     * @return A map of key-value pairs
     */
    private static Map<String, String> parseKeyValuePairs(String line) {
        Map<String, String> result = new HashMap<>();
        String[] parts = line.split("\\|");

        for (String part : parts) {
            int equalsIndex = part.indexOf('=');
            if (equalsIndex > 0) {
                String key = part.substring(0, equalsIndex);
                String value = part.substring(equalsIndex + 1);
                result.put(key, value);
            }
        }

        return result;
    }
}
