/**
 * This class provides functionality to convert MIDI files to a serialized text format
 * that can be understood by Large Language Models (LLMs).
 */
package midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MidiSerializer {
    
    /**
     * Converts a MIDI file to a serialized text format.
     * 
     * @param midiFile The MIDI file to convert
     * @return A string representation of the MIDI file in a serialized format
     * @throws InvalidMidiDataException If the MIDI file contains invalid data
     * @throws IOException If there's an error reading the file
     */
    public static String serializeMidiFile(File midiFile) throws InvalidMidiDataException, IOException {
        Sequence sequence = MidiSystem.getSequence(midiFile);
        return serializeSequence(sequence);
    }
    
    /**
     * Converts a MIDI Sequence object to a serialized text format.
     * 
     * @param sequence The MIDI Sequence to convert
     * @return A string representation of the MIDI Sequence in a serialized format
     */
    public static String serializeSequence(Sequence sequence) {
        StringBuilder serialized = new StringBuilder();
        
        // Add header information
        float divisionType = sequence.getDivisionType();
        serialized.append("MIDI_HEADER|divisionType=").append(divisionType);
        serialized.append("|resolution=").append(sequence.getResolution()).append("\n");
        
        // Process all tracks
        Track[] tracks = sequence.getTracks();
        serialized.append("TRACKS|count=").append(tracks.length).append("\n");
        
        for (int t = 0; t < tracks.length; t++) {
            Track track = tracks[t];
            serialized.append("TRACK|number=").append(t).append("|events=").append(track.size()).append("\n");
            
            // Process all events in the track
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                long tick = event.getTick();
                
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    int command = sm.getCommand();
                    int channel = sm.getChannel();
                    int data1 = sm.getData1();
                    int data2 = sm.getData2();
                    
                    serialized.append("EVENT|tick=").append(tick)
                             .append("|type=ShortMessage")
                             .append("|command=").append(command)
                             .append("|channel=").append(channel)
                             .append("|data1=").append(data1)
                             .append("|data2=").append(data2);
                    
                    // Add human-readable descriptions for common commands
                    if (command == ShortMessage.NOTE_ON) {
                        serialized.append("|description=NOTE_ON")
                                 .append("|note=").append(data1)
                                 .append("|velocity=").append(data2);
                    } else if (command == ShortMessage.NOTE_OFF) {
                        serialized.append("|description=NOTE_OFF")
                                 .append("|note=").append(data1)
                                 .append("|velocity=").append(data2);
                    } else if (command == ShortMessage.PROGRAM_CHANGE) {
                        serialized.append("|description=PROGRAM_CHANGE")
                                 .append("|program=").append(data1);
                    }
                } else if (message instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) message;
                    int type = mm.getType();
                    byte[] data = mm.getData();
                    
                    serialized.append("EVENT|tick=").append(tick)
                             .append("|type=MetaMessage")
                             .append("|metaType=").append(type);
                    
                    // Add human-readable descriptions for common meta events
                    if (type == 0x51) { // Tempo
                        int tempo = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                        float bpm = 60000000f / tempo;
                        serialized.append("|description=TEMPO")
                                 .append("|bpm=").append(String.format("%.2f", bpm));
                    } else if (type == 0x58) { // Time Signature
                        serialized.append("|description=TIME_SIGNATURE")
                                 .append("|numerator=").append(data[0])
                                 .append("|denominator=").append(1 << data[1]);
                    } else if (type == 0x59) { // Key Signature
                        serialized.append("|description=KEY_SIGNATURE")
                                 .append("|key=").append(data[0])
                                 .append("|scale=").append(data[1]);
                    } else if (type == 0x03 || type == 0x04) { // Track Name or Instrument Name
                        String name = new String(data);
                        serialized.append("|description=").append(type == 0x03 ? "TRACK_NAME" : "INSTRUMENT_NAME")
                                 .append("|name=").append(name);
                    }
                } else if (message instanceof SysexMessage) {
                    SysexMessage sm = (SysexMessage) message;
                    serialized.append("EVENT|tick=").append(tick)
                             .append("|type=SysexMessage")
                             .append("|description=SYSTEM_EXCLUSIVE");
                }
                
                serialized.append("\n");
            }
        }
        
        return serialized.toString();
    }
}