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
            // Load the file if it's different or if nothing is loaded
            player.loadFile(filePath);
            
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
        try {
            player.loadFile(filePath);
            player.stop();
        } catch (InvalidMidiDataException | IOException e) {
            System.err.println("❌ Error stopping MIDI file: " + e.getMessage());
            e.printStackTrace();
        }
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
     */
    public void setPosition(double position) {
        player.setPosition(position);
    }
}
