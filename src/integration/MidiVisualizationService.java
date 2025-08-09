package integration;

import javax.sound.midi.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for extracting visualization data from MIDI files
 */
public class MidiVisualizationService {
    
    public static class NoteEvent {
        public final long startTick;
        public final long endTick;
        public final int noteNumber;
        public final int velocity;
        public final int channel;
        
        public NoteEvent(long startTick, long endTick, int noteNumber, int velocity, int channel) {
            this.startTick = startTick;
            this.endTick = endTick;
            this.noteNumber = noteNumber;
            this.velocity = velocity;
            this.channel = channel;
        }
        
        public double getStartTimeSeconds(double ticksPerSecond) {
            return startTick / ticksPerSecond;
        }
        
        public double getEndTimeSeconds(double ticksPerSecond) {
            return endTick / ticksPerSecond;
        }
        
        public double getDurationSeconds(double ticksPerSecond) {
            return (endTick - startTick) / ticksPerSecond;
        }
    }
    
    public static class MidiVisualizationData {
        public final List<NoteEvent> noteEvents;
        public final double durationSeconds;
        public final int resolution;
        public final double ticksPerSecond;
        public final int minNote;
        public final int maxNote;
        
        public MidiVisualizationData(List<NoteEvent> noteEvents, double durationSeconds, 
                                   int resolution, double ticksPerSecond, int minNote, int maxNote) {
            this.noteEvents = noteEvents;
            this.durationSeconds = durationSeconds;
            this.resolution = resolution;
            this.ticksPerSecond = ticksPerSecond;
            this.minNote = minNote;
            this.maxNote = maxNote;
        }
    }
    
    /**
     * Extracts visualization data from a MIDI file
     */
    public static MidiVisualizationData extractVisualizationData(String filePath) {
        try {
            File file = new File(filePath);
            Sequence sequence = MidiSystem.getSequence(file);
            
            List<NoteEvent> noteEvents = new ArrayList<>();
            List<NoteOnEvent> pendingNotes = new ArrayList<>();
            
            double durationSeconds = sequence.getMicrosecondLength() / 1000000.0;
            int resolution = sequence.getResolution();
            
            // Calculate ticks per second
            double ticksPerSecond = (resolution * 120.0) / 60.0; // Assume 120 BPM default
            
            int minNote = 127;
            int maxNote = 0;
            
            // Process all tracks
            Track[] tracks = sequence.getTracks();
            for (Track track : tracks) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    MidiMessage message = event.getMessage();
                    
                    if (message instanceof ShortMessage) {
                        ShortMessage sm = (ShortMessage) message;
                        
                        if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                            // Note ON event
                            int noteNumber = sm.getData1();
                            int velocity = sm.getData2();
                            int channel = sm.getChannel();
                            
                            pendingNotes.add(new NoteOnEvent(event.getTick(), noteNumber, velocity, channel));
                            
                            minNote = Math.min(minNote, noteNumber);
                            maxNote = Math.max(maxNote, noteNumber);
                            
                        } else if (sm.getCommand() == ShortMessage.NOTE_OFF || 
                                 (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)) {
                            // Note OFF event (or Note ON with velocity 0)
                            int noteNumber = sm.getData1();
                            int channel = sm.getChannel();
                            
                            // Find matching note on event
                            for (int j = pendingNotes.size() - 1; j >= 0; j--) {
                                NoteOnEvent noteOn = pendingNotes.get(j);
                                if (noteOn.noteNumber == noteNumber && noteOn.channel == channel) {
                                    // Create complete note event
                                    noteEvents.add(new NoteEvent(
                                        noteOn.startTick,
                                        event.getTick(),
                                        noteNumber,
                                        noteOn.velocity,
                                        channel
                                    ));
                                    pendingNotes.remove(j);
                                    break;
                                }
                            }
                        }
                    } else if (message instanceof MetaMessage) {
                        MetaMessage mm = (MetaMessage) message;
                        if (mm.getType() == 0x51) { // Set Tempo
                            if (mm.getData().length >= 3) {
                                byte[] data = mm.getData();
                                int microsecondsPerQuarter = ((data[0] & 0xFF) << 16) |
                                                           ((data[1] & 0xFF) << 8) |
                                                           (data[2] & 0xFF);
                                double bpm = 60000000.0 / microsecondsPerQuarter;
                                ticksPerSecond = (resolution * bpm) / 60.0;
                            }
                        }
                    }
                }
            }
            
            // Handle any remaining pending notes (assume they end at the sequence end)
            long sequenceLength = sequence.getTickLength();
            for (NoteOnEvent noteOn : pendingNotes) {
                noteEvents.add(new NoteEvent(
                    noteOn.startTick,
                    sequenceLength,
                    noteOn.noteNumber,
                    noteOn.velocity,
                    noteOn.channel
                ));
            }
            
            return new MidiVisualizationData(noteEvents, durationSeconds, resolution, ticksPerSecond, minNote, maxNote);
            
        } catch (Exception e) {
            System.err.println("Error extracting MIDI visualization data: " + e.getMessage());
            e.printStackTrace();
            return new MidiVisualizationData(new ArrayList<>(), 0.0, 480, 960.0, 60, 72);
        }
    }
    
    private static class NoteOnEvent {
        final long startTick;
        final int noteNumber;
        final int velocity;
        final int channel;
        
        NoteOnEvent(long startTick, int noteNumber, int velocity, int channel) {
            this.startTick = startTick;
            this.noteNumber = noteNumber;
            this.velocity = velocity;
            this.channel = channel;
        }
    }
}
