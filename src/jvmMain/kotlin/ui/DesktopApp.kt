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
            
            // Check if playback just finished
            val playbackJustFinished = playbackService.hasPlaybackJustFinished()
            
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
            } else if (playbackJustFinished) {
                // Playback just finished - reset all playing files to beginning and stopped state
                val updatedRows = appState.rows.map { row ->
                    val updatedInputFile = row.inputFile?.let { inputFile ->
                        if (inputFile.isPlaying) {
                            inputFile.copy(
                                isPlaying = false,
                                currentPosition = 0f
                            )
                        } else inputFile
                    }
                    
                    val updatedOutputFile = row.outputFile?.let { outputFile ->
                        if (outputFile.isPlaying) {
                            outputFile.copy(
                                isPlaying = false,
                                currentPosition = 0f
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
                
                // Load visualization data
                val visualizationData = playbackService.getVisualizationData(selectedFile.absolutePath)
                
                val newRow = MidiRow(
                    id = UUID.randomUUID().toString(),
                    rowNumber = appState.nextRowNumber,
                    inputFile = MidiFile(
                        path = selectedFile.absolutePath,
                        name = selectedFile.name,
                        isPlaying = false,
                        currentPosition = 0f,
                        duration = actualDuration,
                        visualizationData = visualizationData
                    ),
                    prompt = "",
                    selectedLlm = LlmService.GEMINI,
                    isProcessing = false,
                    outputFile = null,
                    error = null
                )
                appState = appState.copy(
                    rows = appState.rows + newRow,
                    nextRowNumber = appState.nextRowNumber + 1
                )
            }
        },
        onAddPromptOnlyRow = {
            val newRow = MidiRow(
                id = UUID.randomUUID().toString(),
                rowNumber = appState.nextRowNumber,
                inputFile = null, // No input file for prompt-only generation
                prompt = "",
                selectedLlm = LlmService.GEMINI,
                isProcessing = false,
                outputFile = null,
                error = null
            )
            appState = appState.copy(
                rows = appState.rows + newRow,
                nextRowNumber = appState.nextRowNumber + 1
            )
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
                processRow(appState, rowId, processingService, playbackService) { newState ->
                    appState = newState
                }
            }
        },
        onDeleteRow = { rowId ->
            val rowToDelete = appState.rows.find { it.id == rowId }
            rowToDelete?.let { row ->
                // Delete generated files if this row was derived
                row.outputFile?.let { outputFile ->
                    try {
                        val file = File(outputFile.path)
                        if (file.exists()) {
                            file.delete()
                            println("🗑️ Deleted generated file: ${outputFile.path}")
                        }
                    } catch (e: Exception) {
                        println("⚠️ Could not delete file ${outputFile.path}: ${e.message}")
                    }
                }
                
                // Stop playback if any files from this row are playing
                row.inputFile?.let { if (it.isPlaying) playbackService.stop(it.path) }
                row.outputFile?.let { if (it.isPlaying) playbackService.stop(it.path) }
                
                // Remove row and renumber remaining rows
                val remainingRows = appState.rows.filter { it.id != rowId }
                val renumberedRows = remainingRows.mapIndexed { index, row ->
                    row.copy(rowNumber = index + 1)
                }
                
                appState = appState.copy(
                    rows = renumberedRows,
                    nextRowNumber = renumberedRows.size + 1
                )
            }
        },
        onClearAll = {
            // Delete all generated files
            appState.rows.forEach { row ->
                row.outputFile?.let { outputFile ->
                    try {
                        val file = File(outputFile.path)
                        if (file.exists()) {
                            file.delete()
                            println("🗑️ Deleted generated file: ${outputFile.path}")
                        }
                    } catch (e: Exception) {
                        println("⚠️ Could not delete file ${outputFile.path}: ${e.message}")
                    }
                }
                
                // Stop all playback
                row.inputFile?.let { if (it.isPlaying) playbackService.stop(it.path) }
                row.outputFile?.let { if (it.isPlaying) playbackService.stop(it.path) }
            }
            
            // Clear all rows
            appState = appState.copy(
                rows = emptyList(),
                nextRowNumber = 1
            )
        }
    )
}

private suspend fun processRow(
    appState: AppState,
    rowId: String,
    processingService: MidiProcessingService,
    playbackService: ui.JvmMidiPlaybackService,
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
        val outputFile = if (row.inputFile != null) {
            val inputFile = File(row.inputFile.path)
            File(
                inputFile.parent,
                "${inputFile.nameWithoutExtension}_${row.selectedLlm.name.lowercase()}_output.mid"
            )
        } else {
            // For prompt-only generation, create output in current directory
            File("generated_${row.selectedLlm.name.lowercase()}_${System.currentTimeMillis()}.mid")
        }
        
        // Process with LLM using the integration service
        val result = processingService.processWithLLM(
            row.inputFile?.path, // Can be null for prompt-only generation
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
                
                // Load visualization data for the output file
                val visualizationData = playbackService.getVisualizationData(outputFile.absolutePath)
                
                val outputMidiFile = MidiFile(
                    path = outputFile.absolutePath,
                    name = outputFile.name,
                    isPlaying = false,
                    currentPosition = 0f,
                    duration = actualDuration,
                    visualizationData = visualizationData
                )
                
                // Create a new derived row instead of updating the existing one
                val derivedRow = MidiRow(
                    id = UUID.randomUUID().toString(),
                    rowNumber = appState.nextRowNumber,
                    inputFile = outputMidiFile, // The derived row's input is the LLM output
                    prompt = "",
                    selectedLlm = LlmService.GEMINI,
                    isProcessing = false,
                    outputFile = null,
                    error = null,
                    derivedFrom = RowDerivation(
                        sourceRowNumber = row.rowNumber,
                        prompt = row.prompt,
                        llmService = row.selectedLlm
                    )
                )
                
                onStateUpdate(
                    appState.copy(
                        rows = appState.rows.map {
                            if (it.id == rowId) {
                                it.copy(
                                    isProcessing = false,
                                    error = null
                                )
                            } else it
                        } + derivedRow, // Add the new derived row
                        nextRowNumber = appState.nextRowNumber + 1
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
