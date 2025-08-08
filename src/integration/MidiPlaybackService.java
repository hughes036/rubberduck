package integration;

import midi.MidiPlayer;
import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;

/**
 * Service for managing MIDI playback with UI integration.
 */
public class MidiPlaybackService {
    private static MidiPlaybackService instance;
    private MidiPlayer player;
    
    private MidiPlaybackService() {
        player = MidiPlayer.getInstance();
    }
    
    /**
     * Gets the singleton instance.
     */
    public static synchronized MidiPlaybackService getInstance() {
        if (instance == null) {
            instance = new MidiPlaybackService();
        }
        return instance;
    }
    
    /**
     * Plays or pauses the specified MIDI file.
     * If the file is different from the currently loaded file, loads and plays it.
     * If the same file is loaded, toggles play/pause.
     */
    public boolean playPause(String filePath) {
        try {
            boolean fileAlreadyLoaded = player.isFileLoaded(filePath);
            System.out.println("🔍 DEBUG: playPause called for " + filePath);
            System.out.println("  File already loaded: " + fileAlreadyLoaded);
            
            // Only load the file if it's different from the currently loaded file
            if (!fileAlreadyLoaded) {
                System.out.println("  Loading file...");
                player.loadFile(filePath); // This will reset position for new files
            } else {
                System.out.println("  File already loaded, skipping loadFile to preserve position");
            }
            // If same file is already loaded, don't call loadFile to preserve position
            
            // Toggle play/pause
            if (player.isPlaying()) {
                player.pause();
                return false; // Now paused
            } else {
                player.play();
                return true; // Now playing
            }
        } catch (InvalidMidiDataException | IOException e) {
            System.err.println("❌ Error playing MIDI file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Stops playback and resets to the beginning.
     */
    public void stop(String filePath) {
        // Just stop playback, don't reload the file
        player.stop();
    }
    
    /**
     * Returns true if currently playing.
     */
    public boolean isPlaying() {
        return player.isPlaying();
    }
    
    /**
     * Gets the current playback position as a percentage (0.0 to 1.0).
     */
    public double getPosition() {
        return player.getPosition();
    }
    
    /**
     * Gets the duration of the current file in seconds.
     */
    public double getDuration() {
        return player.getDuration();
    }

    /**
     * Sets the current playback position as a percentage (0.0 to 1.0).
     * @param filePath The file path to seek in
     * @param position The position to seek to (0.0 = start, 1.0 = end)
     */
    public void setPosition(String filePath, double position) {
        // Ensure file is loaded before seeking
        if (filePath != null && !player.isFileLoaded(filePath)) {
            System.out.println("🔍 DEBUG: setPosition called but file not loaded, loading: " + filePath);
            try {
                player.loadFile(filePath);
            } catch (Exception e) {
                System.err.println("❌ Error loading file for seek: " + e.getMessage());
                return;
            }
        }
        player.setPosition(position);
    }
}
