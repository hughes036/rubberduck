package midi;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * MIDI player for playing MIDI files with playback controls.
 */
public class MidiPlayer {
    private static MidiPlayer instance;
    private Sequencer sequencer;
    private Sequence currentSequence;
    private String currentFile;
    private List<PlaybackListener> listeners = new ArrayList<>();
    private Timer positionUpdateTimer;
    private TimerTask positionUpdateTask;
    
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
            
            // Connect to a synthesizer for audio output
            if (sequencer instanceof Sequencer) {
                try {
                    Synthesizer synthesizer = MidiSystem.getSynthesizer();
                    if (synthesizer != null) {
                        synthesizer.open();
                        Receiver receiver = synthesizer.getReceiver();
                        Transmitter transmitter = sequencer.getTransmitter();
                        transmitter.setReceiver(receiver);
                        System.out.println("🔊 MIDI synthesizer connected successfully");
                    } else {
                        System.out.println("⚠️ No synthesizer available, trying default receiver");
                        // Try to get the default receiver (system MIDI)
                        Receiver receiver = MidiSystem.getReceiver();
                        Transmitter transmitter = sequencer.getTransmitter();
                        transmitter.setReceiver(receiver);
                        System.out.println("🔊 Default MIDI receiver connected");
                    }
                } catch (MidiUnavailableException e) {
                    System.out.println("⚠️ Could not connect synthesizer: " + e.getMessage());
                    System.out.println("   MIDI files may not produce sound");
                }
            }
            
            // Add a meta event listener to detect when playback finishes
            sequencer.addMetaEventListener(new MetaEventListener() {
                @Override
                public void meta(MetaMessage meta) {
                    if (meta.getType() == 47) { // End of track
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
     * Loads MIDI data from serialized string (in-memory version)
     */
    public void loadFromSerializedData(String serializedMidi, String sessionId) throws Exception {
        // Use MidiDeserializer to convert serialized data to Sequence
        currentSequence = midi.MidiDeserializer.deserializeToSequence(serializedMidi);
        currentFile = sessionId; // Use session ID as filename identifier
        sequencer.setSequence(currentSequence);
        sequencer.setTickPosition(0);
        
        System.out.println("🎵 Loaded MIDI from memory: " + sessionId);
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
            // Pause instead of stop to maintain position
            pause();
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
     * Checks if the specified file is currently loaded.
     */
    public boolean isFileLoaded(String filePath) {
        if (currentFile == null) return false;
        File file = new File(filePath);
        return currentFile.equals(file.getName());
    }
    
    /**
     * Checks if the specified session is currently loaded (for in-memory data).
     */
    public boolean isSessionLoaded(String sessionId) {
        if (currentFile == null) return false;
        return currentFile.equals(sessionId);
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
        startPositionUpdates();
    }
    
    private void notifyPlaybackPaused() {
        stopPositionUpdates();
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackPaused(currentFile);
        }
    }
    
    private void notifyPlaybackStopped() {
        stopPositionUpdates();
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackStopped(currentFile);
        }
    }
    
    private void notifyPlaybackFinished() {
        stopPositionUpdates();
        // Reset position to beginning when playback finishes
        if (sequencer != null) {
            sequencer.setTickPosition(0);
        }
        for (PlaybackListener listener : listeners) {
            listener.onPlaybackFinished(currentFile);
        }
    }
    
    /**
     * Starts regular position updates during playback.
     */
    private void startPositionUpdates() {
        stopPositionUpdates(); // Stop any existing timer
        
        positionUpdateTimer = new Timer(true); // Daemon thread
        positionUpdateTask = new TimerTask() {
            @Override
            public void run() {
                if (sequencer != null && sequencer.isRunning() && currentSequence != null) {
                    long currentTick = sequencer.getTickPosition();
                    long totalTicks = currentSequence.getTickLength();
                    notifyPositionChanged(currentTick, totalTicks);
                } else {
                    // Stop updates if not playing
                    stopPositionUpdates();
                }
            }
        };
        
        // Update position every 100ms
        positionUpdateTimer.schedule(positionUpdateTask, 0, 100);
    }
    
    /**
     * Stops position updates.
     */
    private void stopPositionUpdates() {
        if (positionUpdateTask != null) {
            positionUpdateTask.cancel();
            positionUpdateTask = null;
        }
        if (positionUpdateTimer != null) {
            positionUpdateTimer.cancel();
            positionUpdateTimer = null;
        }
    }
    
    /**
     * Notifies listeners of position changes.
     */
    private void notifyPositionChanged(long currentTick, long totalTicks) {
        for (PlaybackListener listener : listeners) {
            listener.onPositionChanged(currentFile, currentTick, totalTicks);
        }
    }

    /**
     * Closes the sequencer and releases resources.
     */
    public void close() {
        stopPositionUpdates();
        if (sequencer != null) {
            sequencer.close();
        }
    }
}
