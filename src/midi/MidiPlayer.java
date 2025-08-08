package midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MIDI player for playing MIDI files with playback controls.
 */
public class MidiPlayer {
    private static MidiPlayer instance;
    private Sequencer sequencer;
    private Sequence currentSequence;
    private String currentFile;
    private List<PlaybackListener> listeners = new ArrayList<>();
    
    /**
     * Interface for listening to playback events.
     */
    public interface PlaybackListener {
        void onPlaybackStarted(String filename);
        void onPlaybackPaused(String filename);
        void onPlaybackStopped(String filename);
        void onPlaybackFinished(String filename);
        void onPositionChanged(String filename, long position, long length);
    }
    
    private MidiPlayer() {
        try {
            sequencer = MidiSystem.getSequencer();
            if (sequencer == null) {
                throw new RuntimeException("No MIDI sequencer available on this system");
            }
            sequencer.open();
            
            // Add a meta event listener to detect when playback finishes
            sequencer.addMetaEventListener(new MetaEventListener() {
                @Override
                public void meta(MetaMessage meta) {
                    if (meta.getType() == END_OF_TRACK_META_EVENT) { // End of track
                        notifyPlaybackFinished();
                    }
                }
            });
            
        } catch (MidiUnavailableException e) {
            throw new RuntimeException("Failed to initialize MIDI sequencer: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the singleton instance of the MIDI player.
     */
    public static synchronized MidiPlayer getInstance() {
        if (instance == null) {
            instance = new MidiPlayer();
        }
        return instance;
    }
    
    /**
     * Loads a MIDI file for playback.
     */
    public void loadFile(String filePath) throws InvalidMidiDataException, IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("MIDI file not found: " + filePath);
        }
        
        currentSequence = MidiSystem.getSequence(file);
        currentFile = file.getName();
        sequencer.setSequence(currentSequence);
        sequencer.setTickPosition(0);
        
        System.out.println("🎵 Loaded MIDI file: " + currentFile);
        System.out.println("   Duration: " + (currentSequence.getMicrosecondLength() / 1000000.0) + " seconds");
        System.out.println("   Tracks: " + currentSequence.getTracks().length);
    }
    
    /**
     * Starts playback of the loaded MIDI file.
     */
    public void play() {
        if (currentSequence == null) {
            System.out.println("⚠️ No MIDI file loaded");
            return;
        }
        
        if (!sequencer.isRunning()) {
            sequencer.start();
            System.out.println("▶️ Started playback: " + currentFile);
            notifyPlaybackStarted();
        } else {
            System.out.println("⏸️ Paused playback: " + currentFile);
            sequencer.stop();
            notifyPlaybackPaused();
        }
    }
    
    /**
     * Pauses playback.
     */
    public void pause() {
        if (sequencer.isRunning()) {
            sequencer.stop();
            System.out.println("⏸️ Paused playback: " + currentFile);
            notifyPlaybackPaused();
        }
    }
    
    /**
     * Stops playback and resets position to beginning.
     */
    public void stop() {
        if (sequencer.isRunning()) {
            sequencer.stop();
        }
        sequencer.setTickPosition(0);
        System.out.println("⏹️ Stopped playback: " + currentFile);
        notifyPlaybackStopped();
    }
    
    /**
     * Returns true if currently playing.
     */
    public boolean isPlaying() {
        return sequencer != null && sequencer.isRunning();
    }
    
    /**
     * Gets the current playback position as a percentage (0.0 to 1.0).
     */
    public double getPosition() {
        if (currentSequence == null) return 0.0;
        long currentTick = sequencer.getTickPosition();
        long totalTicks = currentSequence.getTickLength();
        return totalTicks > 0 ? (double) currentTick / totalTicks : 0.0;
    }
    
    /**
     * Sets the playback position as a percentage (0.0 to 1.0).
     */
    public void setPosition(double position) {
        if (currentSequence == null) return;
        long totalTicks = currentSequence.getTickLength();
        long newTick = (long) (position * totalTicks);
        sequencer.setTickPosition(newTick);
    }
    
    /**
     * Gets the duration of the current sequence in seconds.
     */
    public double getDuration() {
        if (currentSequence == null) return 0.0;
        return currentSequence.getMicrosecondLength() / 1000000.0;
    }
    
    /**
     * Adds a playback listener.
     */
    public void addListener(PlaybackListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a playback listener.
     */
    public void removeListener(PlaybackListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyPlaybackStarted() {
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackStarted(currentFile);
        }
    }
    
    private void notifyPlaybackPaused() {
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackPaused(currentFile);
        }
    }
    
    private void notifyPlaybackStopped() {
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackStopped(currentFile);
        }
    }
    
    private void notifyPlaybackFinished() {
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackFinished(currentFile);
        }
    }
    
    /**
     * Closes the sequencer and releases resources.
     */
    public void close() {
        if (sequencer != null) {
            sequencer.close();
        }
    }
}
