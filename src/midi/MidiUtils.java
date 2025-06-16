/**
 * This class provides utility methods for working with MIDI data.
 */
package midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

public class MidiUtils {
    
    // Note names for MIDI note numbers (0-127)
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    
    /**
     * Gets the name of a MIDI note number (e.g., 60 -> "C4").
     * 
     * @param noteNumber The MIDI note number (0-127)
     * @return The name of the note including octave
     */
    public static String getNoteName(int noteNumber) {
        if (noteNumber < 0 || noteNumber > 127) {
            throw new IllegalArgumentException("Note number must be between 0 and 127");
        }
        
        int octave = (noteNumber / 12) - 1;
        int note = noteNumber % 12;
        return NOTE_NAMES[note] + octave;
    }
    
    /**
     * Converts a MIDI note number to its frequency in Hz.
     * 
     * @param noteNumber The MIDI note number (0-127)
     * @return The frequency in Hz
     */
    public static double getNoteFrequency(int noteNumber) {
        // A4 (MIDI note 69) is 440 Hz
        return 440.0 * Math.pow(2.0, (noteNumber - 69.0) / 12.0);
    }
    
    /**
     * Converts a frequency in Hz to the closest MIDI note number.
     * 
     * @param frequency The frequency in Hz
     * @return The closest MIDI note number
     */
    public static int frequencyToNoteNumber(double frequency) {
        // A4 (MIDI note 69) is 440 Hz
        return (int) Math.round(69 + 12 * Math.log(frequency / 440.0) / Math.log(2));
    }
    
    /**
     * Gets information about a MIDI file.
     * 
     * @param midiFile The MIDI file
     * @return A string containing information about the MIDI file
     * @throws InvalidMidiDataException If the MIDI file contains invalid data
     * @throws IOException If there's an error reading the file
     */
    public static String getMidiFileInfo(File midiFile) throws InvalidMidiDataException, IOException {
        Sequence sequence = MidiSystem.getSequence(midiFile);
        StringBuilder info = new StringBuilder();
        
        info.append("MIDI File: ").append(midiFile.getName()).append("\n");
        info.append("Division Type: ").append(sequence.getDivisionType()).append("\n");
        info.append("Resolution: ").append(sequence.getResolution()).append(" ticks per beat\n");
        info.append("Length: ").append(sequence.getTickLength()).append(" ticks\n");
        info.append("Duration: ").append(sequence.getMicrosecondLength() / 1000000.0).append(" seconds\n");
        
        Track[] tracks = sequence.getTracks();
        info.append("Tracks: ").append(tracks.length).append("\n");
        
        for (int i = 0; i < tracks.length; i++) {
            Track track = tracks[i];
            info.append("Track ").append(i).append(": ").append(track.size()).append(" events\n");
            
            // Look for track name
            for (int j = 0; j < track.size(); j++) {
                MidiEvent event = track.get(j);
                MidiMessage message = event.getMessage();
                
                if (message instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) message;
                    if (mm.getType() == 0x03) { // Track name
                        String name = new String(mm.getData());
                        info.append("  Name: ").append(name).append("\n");
                        break;
                    }
                }
            }
            
            // Count note events
            int noteOnCount = 0;
            for (int j = 0; j < track.size(); j++) {
                MidiEvent event = track.get(j);
                MidiMessage message = event.getMessage();
                
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                        noteOnCount++;
                    }
                }
            }
            info.append("  Note events: ").append(noteOnCount).append("\n");
        }
        
        return info.toString();
    }
    
    /**
     * Creates a simple MIDI sequence with a single note.
     * 
     * @param noteNumber The MIDI note number (0-127)
     * @param velocity The velocity (0-127)
     * @param durationTicks The duration in ticks
     * @return A MIDI Sequence containing the note
     * @throws InvalidMidiDataException If the MIDI data is invalid
     */
    public static Sequence createSingleNoteSequence(int noteNumber, int velocity, long durationTicks) 
            throws InvalidMidiDataException {
        Sequence sequence = new Sequence(Sequence.PPQ, 480);
        Track track = sequence.createTrack();
        
        // Add a program change (instrument selection)
        ShortMessage programChange = new ShortMessage();
        programChange.setMessage(ShortMessage.PROGRAM_CHANGE, 0, 0, 0);
        track.add(new MidiEvent(programChange, 0));
        
        // Note on
        ShortMessage noteOn = new ShortMessage();
        noteOn.setMessage(ShortMessage.NOTE_ON, 0, noteNumber, velocity);
        track.add(new MidiEvent(noteOn, 0));
        
        // Note off
        ShortMessage noteOff = new ShortMessage();
        noteOff.setMessage(ShortMessage.NOTE_OFF, 0, noteNumber, 0);
        track.add(new MidiEvent(noteOff, durationTicks));
        
        return sequence;
    }
}