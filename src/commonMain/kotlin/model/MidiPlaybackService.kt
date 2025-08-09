package model

/**
 * Interface for MIDI playback functionality that can be implemented in platform-specific code.
 */
interface MidiPlaybackService {
    /**
     * Plays or pauses the specified MIDI file.
     * @param filePath The path to the MIDI file
     * @return true if now playing, false if paused
     */
    fun playPause(filePath: String): Boolean
    
    /**
     * Stops playback of the specified MIDI file.
     * @param filePath The path to the MIDI file
     */
    fun stop(filePath: String)
    
    /**
     * Returns true if currently playing.
     */
    fun isPlaying(): Boolean
    
    /**
     * Gets the current playback position as a percentage (0.0 to 1.0).
     */
    fun getPosition(): Double
    
    /**
     * Sets the current playback position as a percentage (0.0 to 1.0).
     * @param filePath The file path to seek in
     * @param position The position to seek to (0.0 = start, 1.0 = end)
     */
    fun setPosition(filePath: String, position: Double)
    
    /**
     * Gets the duration of the current file in seconds.
     */
    fun getDuration(): Double
    
    /**
     * Checks if playback just finished (for UI state management).
     * This flag is automatically reset when checked.
     */
    fun hasPlaybackJustFinished(): Boolean
    
    /**
     * Gets visualization data for a MIDI file
     */
    fun getVisualizationData(filePath: String): MidiVisualizationData?
}
