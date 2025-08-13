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
     * Plays or pauses MIDI data from memory.
     * @param midiFile The MIDI file object containing in-memory data
     * @return true if now playing, false if paused
     */
    fun playPauseFile(midiFile: MidiFile): Boolean {
        return if (
            midiFile.isInMemory 
            && midiFile.serializedMidiData != null 
            && midiFile.sessionId != null
        ) {
            // In-memory playback: use serialized MIDI data with session ID
            playPauseInMemory(midiFile.serializedMidiData, midiFile.sessionId)
        } else if (midiFile.path.isNotBlank()) {
            // File-based playback: use file path for traditional MIDI file playback
            playPause(midiFile.path)
        } else {
            // No valid playback method available - throw error
            throw IllegalArgumentException("Cannot play MIDI file: path is blank and no valid in-memory data available")
        }
    }
    
    /**
     * Plays or pauses serialized MIDI data (to be implemented by JVM service)
     */
    fun playPauseInMemory(serializedMidi: String, sessionId: String): Boolean {
        throw UnsupportedOperationException("In-memory playback not supported on this platform")
    }
    
    /**
     * Stops playback of the specified MIDI file.
     * @param filePath The path to the MIDI file
     */
    fun stop(filePath: String)
    
    /**
     * Stops playback of MIDI data (smart method that handles both file and in-memory)
     */
    fun stopFile(midiFile: MidiFile) {
        if (midiFile.isInMemory && midiFile.sessionId != null) {
            stopInMemory(midiFile.sessionId)
        } else {
            stop(midiFile.path)
        }
    }
    
    /**
     * Stops in-memory MIDI playback (to be implemented by JVM service)
     */
    fun stopInMemory(sessionId: String) {
        throw UnsupportedOperationException("In-memory stop not supported on this platform")
    }
    
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
    
    /**
     * Gets all API keys for configuration
     */
    fun getAllApiKeys(): Map<String, String>
    
    /**
     * Updates a specific API key
     */
    fun updateApiKey(serviceName: String, apiKey: String)
    
    /**
     * Saves all API keys at once
     */
    fun saveAllApiKeys(apiKeys: Map<String, String>)
}
