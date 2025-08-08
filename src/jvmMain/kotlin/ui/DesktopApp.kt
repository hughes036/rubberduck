package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import model.*
import integration.MidiProcessingService
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RubberDuck - LLM MIDI Composer",
        state = rememberWindowState(size = DpSize(1000.dp, 800.dp))
    ) {
        MaterialTheme {
            RubberDuckDesktopApp()
        }
    }
}

@Composable
fun RubberDuckDesktopApp() {
    val processingService = remember { MidiProcessingService() }
    val playbackService = remember { JvmMidiPlaybackService(processingService) }
    val scope = rememberCoroutineScope()
    
    // Debug: Print available services
    val availableServiceStrings = processingService.availableServices
    println("🔍 DEBUG: Available services from Java: $availableServiceStrings")
    
    val availableServices = try {
        availableServiceStrings.map { serviceName -> LlmService.valueOf(serviceName) }.toSet()
    } catch (e: Exception) {
        println("❌ ERROR mapping services: ${e.message}")
        emptySet<LlmService>()
    }
    println("🔍 DEBUG: Mapped services: $availableServices")
    
    var appState by remember { 
        mutableStateOf(
            AppState(
                rows = emptyList(),
                availableServices = availableServices
            )
        )
    }
    
    // Real-time position updates during playback
    LaunchedEffect(Unit) {
        while (true) {
            delay(100) // Update every 100ms
            
            if (playbackService.isPlaying()) {
                val currentPosition = (playbackService.getPosition() * playbackService.getDuration()).toFloat()
                val duration = playbackService.getDuration().toFloat()
                
                // Update all rows that might be playing
                val updatedRows = appState.rows.map { row ->
                    val updatedInputFile = row.inputFile?.let { inputFile ->
                        if (inputFile.isPlaying) {
                            inputFile.copy(
                                currentPosition = currentPosition,
                                duration = if (inputFile.duration <= 0f) duration else inputFile.duration
                            )
                        } else inputFile
                    }
                    
                    val updatedOutputFile = row.outputFile?.let { outputFile ->
                        if (outputFile.isPlaying) {
                            outputFile.copy(
                                currentPosition = currentPosition,
                                duration = if (outputFile.duration <= 0f) duration else outputFile.duration
                            )
                        } else outputFile
                    }
                    
                    if (updatedInputFile != row.inputFile || updatedOutputFile != row.outputFile) {
                        row.copy(
                            inputFile = updatedInputFile,
                            outputFile = updatedOutputFile
                        )
                    } else {
                        row
                    }
                }
                
                if (updatedRows != appState.rows) {
                    appState = appState.copy(rows = updatedRows)
                }
            }
        }
    }
    
    RubberDuckApp(
        state = appState,
        playbackService = playbackService,
        onAddMidiFile = {
            selectMidiFile { selectedFile ->
                // Get actual duration from the MIDI file
                val actualDuration = try {
                    processingService.getDurationForFile(selectedFile.absolutePath).toFloat()
                } catch (e: Exception) {
                    println("⚠️ Could not get duration for ${selectedFile.name}: ${e.message}")
                    100f // Fallback duration
                }
                
                val newRow = MidiRow(
                    id = UUID.randomUUID().toString(),
                    inputFile = MidiFile(
                        path = selectedFile.absolutePath,
                        name = selectedFile.name,
                        isPlaying = false,
                        currentPosition = 0f,
                        duration = actualDuration
                    ),
                    prompt = "",
                    selectedLlm = appState.availableServices.firstOrNull() ?: LlmService.GEMINI,
                    isProcessing = false,
                    outputFile = null,
                    error = null
                )
                appState = appState.copy(rows = appState.rows + newRow)
            }
        },
        onRowUpdate = { rowId, updatedRow ->
            println("🔍 DEBUG: Row update triggered for row $rowId")
            println("  Updated row: $updatedRow")
            
            appState = appState.copy(
                rows = appState.rows.map { 
                    if (it.id == rowId) {
                        println("🔍 DEBUG: Found matching row, updating...")
                        updatedRow
                    } else {
                        it
                    }
                }
            )
        },
        onProcessRequest = { rowId ->
            scope.launch {
                processRow(appState, rowId, processingService) { newState ->
                    appState = newState
                }
            }
        }
    )
}

private suspend fun processRow(
    appState: AppState,
    rowId: String,
    processingService: MidiProcessingService,
    onStateUpdate: (AppState) -> Unit
) = withContext(Dispatchers.IO) {
    val row = appState.rows.find { it.id == rowId } ?: return@withContext
    
    // Mark as processing
    withContext(Dispatchers.Main) {
        onStateUpdate(
            appState.copy(
                rows = appState.rows.map {
                    if (it.id == rowId) it.copy(isProcessing = true, error = null) else it
                }
            )
        )
    }
    
    try {
        // Create output file path
        val inputFile = File(row.inputFile!!.path)
        val outputFile = File(
            inputFile.parent,
            "${inputFile.nameWithoutExtension}_${row.selectedLlm.name.lowercase()}_output.mid"
        )
        
        // Process with LLM using the integration service
        val result = processingService.processWithLLM(
            row.inputFile.path,
            row.prompt,
            row.selectedLlm.name.lowercase(),
            outputFile.absolutePath
        )
        
        withContext(Dispatchers.Main) {
            if (result.isSuccess) {
                // Get actual duration from the output MIDI file
                val actualDuration = try {
                    processingService.getDurationForFile(outputFile.absolutePath).toFloat()
                } catch (e: Exception) {
                    println("⚠️ Could not get duration for output file ${outputFile.name}: ${e.message}")
                    120f // Fallback duration
                }
                
                val outputMidiFile = MidiFile(
                    path = outputFile.absolutePath,
                    name = outputFile.name,
                    isPlaying = false,
                    currentPosition = 0f,
                    duration = actualDuration
                )
                
                onStateUpdate(
                    appState.copy(
                        rows = appState.rows.map {
                            if (it.id == rowId) {
                                it.copy(
                                    isProcessing = false,
                                    outputFile = outputMidiFile,
                                    error = null
                                )
                            } else it
                        }
                    )
                )
            } else {
                onStateUpdate(
                    appState.copy(
                        rows = appState.rows.map {
                            if (it.id == rowId) {
                                it.copy(
                                    isProcessing = false,
                                    error = result.message
                                )
                            } else it
                        }
                    )
                )
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            onStateUpdate(
                appState.copy(
                    rows = appState.rows.map {
                        if (it.id == rowId) {
                            it.copy(
                                isProcessing = false,
                                error = e.message ?: "Unknown error"
                            )
                        } else it
                    }
                )
            )
        }
    }
}

private fun selectMidiFile(onFileSelected: (File) -> Unit) {
    val fileChooser = JFileChooser()
    fileChooser.fileFilter = FileNameExtensionFilter("MIDI Files", "mid", "midi")
    
    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        onFileSelected(fileChooser.selectedFile)
    }
}
