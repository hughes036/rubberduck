package ui

import model.MidiPlaybackService as MidiPlaybackServiceInterface
import integration.MidiProcessingService

/**
 * JVM implementation of the MidiPlaybackService interface that wraps the Java integration service.
 */
class JvmMidiPlaybackService(private val processingService: MidiProcessingService) : MidiPlaybackServiceInterface {
    
    override fun playPause(filePath: String): Boolean {
        return processingService.playPause(filePath)
    }
    
    override fun stop(filePath: String) {
        processingService.stop(filePath)
    }
    
    override fun isPlaying(): Boolean {
        return processingService.isPlaying()
    }
    
    override fun getPosition(): Double {
        return processingService.position
    }
    
    override fun setPosition(position: Double) {
        processingService.setPosition(position)
    }
    
    override fun getDuration(): Double {
        return processingService.duration
    }
}
