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
    val scope = rememberCoroutineScope()
    
    var appState by remember { 
        mutableStateOf(
            AppState(
                rows = emptyList(),
                availableServices = processingService.availableServices.map { 
                    LlmService.valueOf(it) 
                }.toSet()
            )
        )
    }
    
    RubberDuckApp(
        state = appState,
        onAddMidiFile = {
            selectMidiFile { selectedFile ->
                val newRow = MidiRow(
                    id = UUID.randomUUID().toString(),
                    inputFile = MidiFile(
                        path = selectedFile.absolutePath,
                        name = selectedFile.name,
                        isPlaying = false,
                        currentPosition = 0f,
                        duration = 100f // TODO: Get actual duration
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
            appState = appState.copy(
                rows = appState.rows.map { 
                    if (it.id == rowId) updatedRow else it 
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
            row.selectedLlm.name,
            outputFile.absolutePath
        )
        
        withContext(Dispatchers.Main) {
            if (result.isSuccess) {
                val outputMidiFile = MidiFile(
                    path = outputFile.absolutePath,
                    name = outputFile.name,
                    isPlaying = false,
                    currentPosition = 0f,
                    duration = 120f // TODO: Get actual duration
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
