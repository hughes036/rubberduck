package model

/**
 * Represents a MIDI processing row in the UI
 */
data class MidiRow(
    val id: String,
    val rowNumber: Int,
    val inputFile: MidiFile?,
    val prompt: String = "",
    val selectedLlm: LlmService = LlmService.GEMINI,
    val isProcessing: Boolean = false,
    val outputFile: MidiFile? = null,
    val error: String? = null,
    val derivedFrom: RowDerivation? = null
)

/**
 * Represents a MIDI file with metadata and in-memory support
 */
data class MidiFile(
    val path: String,
    val name: String,
    val isPlaying: Boolean = false,
    val currentPosition: Float = 0f,
    val duration: Float = 0f,
    val visualizationData: MidiVisualizationData? = null,
    val serializedMidiData: String? = null, // In-memory MIDI data
    val sessionId: String? = null // For in-memory playback sessions
) {
    // Helper properties
    val isInMemory: Boolean get() = serializedMidiData != null
    val playbackIdentifier: String get() = sessionId ?: path
}

/**
 * Tracks the derivation history of a MIDI row
 */
data class RowDerivation(
    val sourceRowNumber: Int,
    val prompt: String,
    val llmService: LlmService
)

/**
 * Available LLM services
 */
enum class LlmService(val displayName: String, val serviceName: String) {
    GEMINI("Google Gemini", "gemini"),
    GPT4("OpenAI GPT-4", "gpt4"),
    CLAUDE("Anthropic Claude", "claude")
}

/**
 * Application state
 */
data class AppState(
    val rows: List<MidiRow> = emptyList(),
    val availableServices: Set<LlmService> = emptySet(),
    val nextRowNumber: Int = 1,
    val showApiKeyConfig: Boolean = false
)

/**
 * MIDI visualization data
 */
data class NoteEvent(
    val startTimeSeconds: Double,
    val endTimeSeconds: Double,
    val noteNumber: Int,
    val velocity: Int,
    val channel: Int
) {
    val durationSeconds: Double get() = endTimeSeconds - startTimeSeconds
}

data class MidiVisualizationData(
    val noteEvents: List<NoteEvent>,
    val durationSeconds: Double,
    val minNote: Int,
    val maxNote: Int
)
