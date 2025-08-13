package ui

import model.MidiPlaybackService as MidiPlaybackServiceInterface
import model.MidiVisualizationData
import model.NoteEvent
import integration.MidiProcessingService

/**
 * JVM implementation of the MidiPlaybackService interface that wraps the Java integration service.
 */
class JvmMidiPlaybackService(private val processingService: MidiProcessingService) : MidiPlaybackServiceInterface {
    
    override fun playPause(filePath: String): Boolean {
        return processingService.playPause(filePath)
    }
    
    override fun playPauseInMemory(serializedMidi: String, sessionId: String): Boolean {
        return processingService.playPauseInMemory(serializedMidi, sessionId)
    }
    
    override fun stop(filePath: String) {
        processingService.stop(filePath)
    }
    
    override fun stopInMemory(sessionId: String) {
        processingService.stopInMemory(sessionId)
    }
    
    override fun isPlaying(): Boolean {
        return processingService.isPlaying()
    }
    
    override fun getPosition(): Double {
        return processingService.position
    }
    
    override fun setPosition(filePath: String, position: Double) {
        processingService.setPosition(filePath, position)
    }
    
    override fun getDuration(): Double {
        return processingService.duration
    }
    
    override fun hasPlaybackJustFinished(): Boolean {
        return integration.MidiPlaybackService.getInstance().hasPlaybackJustFinished()
    }
    
    override fun getVisualizationData(filePath: String): MidiVisualizationData? {
        return try {
            val javaData = processingService.getVisualizationData(filePath)
            convertJavaVisualizationData(javaData)
        } catch (e: Exception) {
            println("Error getting visualization data: ${e.message}")
            null
        }
    }
    
    /**
     * Get visualization data for in-memory MIDI
     */
    private fun getVisualizationDataFromMemory(serializedMidi: String): MidiVisualizationData? {
        return try {
            val javaData = processingService.getVisualizationDataFromMemory(serializedMidi)
            convertJavaVisualizationData(javaData)
        } catch (e: Exception) {
            println("Error getting visualization data from memory: ${e.message}")
            null
        }
    }
    
    /**
     * Convert Java MidiVisualizationData to Kotlin model
     */
    private fun convertJavaVisualizationData(javaData: integration.MidiVisualizationService.MidiVisualizationData?): MidiVisualizationData? {
        return if (javaData != null) {
            // Convert Java objects to Kotlin objects
            val noteEvents = javaData.noteEvents.map { javaEvent ->
                NoteEvent(
                    startTimeSeconds = javaEvent.getStartTimeSeconds(javaData.ticksPerSecond),
                    endTimeSeconds = javaEvent.getEndTimeSeconds(javaData.ticksPerSecond),
                    noteNumber = javaEvent.noteNumber,
                    velocity = javaEvent.velocity,
                    channel = javaEvent.channel
                )
            }
            
            MidiVisualizationData(
                noteEvents = noteEvents,
                durationSeconds = javaData.durationSeconds,
                minNote = javaData.minNote,
                maxNote = javaData.maxNote
            )
        } else {
            null
        }
    }
    
    override fun getAllApiKeys(): Map<String, String> {
        return try {
            processingService.allApiKeys
        } catch (e: Exception) {
            println("Error getting API keys: ${e.message}")
            throw e
        }
    }
    
    override fun updateApiKey(serviceName: String, apiKey: String) {
        try {
            processingService.updateApiKey(serviceName, apiKey)
        } catch (e: Exception) {
            println("Error updating API key: ${e.message}")
            throw e
        }
    }
    
    override fun saveAllApiKeys(apiKeys: Map<String, String>) {
        try {
            processingService.saveAllApiKeys(apiKeys)
        } catch (e: Exception) {
            println("Error saving API keys: ${e.message}")
            throw e
        }
    }
}
