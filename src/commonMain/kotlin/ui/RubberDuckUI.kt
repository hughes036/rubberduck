package ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import model.*
import kotlin.math.floor

/**
 * Formats time in seconds to MM:SS format
 */
fun formatTime(seconds: Float): String {
    val minutes = floor(seconds / 60).toInt()
    val remainingSeconds = (seconds % 60).toInt()
    return "%d:%02d".format(minutes, remainingSeconds)
}

fun DrawScope.drawMidiGrid(width: Float, height: Float, trackColor: Color, lineColor: Color) {
    // Draw horizontal lines for octaves (simplified)
    val noteCount = 12 // One octave
    val noteHeight = height / noteCount
    
    for (i in 0..noteCount) {
        val y = i * noteHeight
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // Draw vertical time grid lines
    val timeLines = 8
    val timeStep = width / timeLines
    for (i in 0..timeLines) {
        val x = i * timeStep
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx()
        )
    }
}

fun DrawScope.drawActualMidiNotes(
    midiFile: MidiFile,
    width: Float,
    height: Float,
    noteColor: Color,
    currentPosition: Float
) {
    val visualizationData = midiFile.visualizationData
    if (visualizationData == null || visualizationData.noteEvents.isEmpty()) {
        // Fallback to simulated data if no visualization data available
        drawSimulatedMidiNotes(width, height, noteColor, currentPosition, midiFile.duration)
        return
    }
    
    val noteRange = visualizationData.maxNote - visualizationData.minNote
    val noteHeight = if (noteRange > 0) height / noteRange else height / 12
    val duration = midiFile.duration.toDouble()
    
    // Group notes by channel for color coding
    val channelColors = listOf(
        noteColor,
        noteColor.copy(alpha = 0.8f),
        noteColor.copy(red = noteColor.red * 0.8f),
        noteColor.copy(green = noteColor.green * 0.8f),
        noteColor.copy(blue = noteColor.blue * 0.8f)
    )
    
    visualizationData.noteEvents.forEach { noteEvent ->
        if (duration > 0) {
            val startX = (noteEvent.startTimeSeconds / duration * width).toFloat()
            val noteWidth = ((noteEvent.endTimeSeconds - noteEvent.startTimeSeconds) / duration * width).toFloat()
            val normalizedNote = noteEvent.noteNumber - visualizationData.minNote
            val y = height - (normalizedNote * noteHeight) - noteHeight
            
            // Make notes currently playing more prominent
            val isCurrentlyPlaying = currentPosition >= noteEvent.startTimeSeconds && 
                                   currentPosition <= noteEvent.endTimeSeconds
            val baseAlpha = (noteEvent.velocity / 127f).coerceIn(0.3f, 1.0f)
            val alpha = if (isCurrentlyPlaying) baseAlpha else baseAlpha * 0.6f
            
            // Use channel-based coloring
            val channelColor = channelColors[noteEvent.channel % channelColors.size]
            
            drawRect(
                color = channelColor.copy(alpha = alpha),
                topLeft = Offset(startX, y),
                size = Size(noteWidth.coerceAtLeast(2f), noteHeight * 0.8f)
            )
        }
    }
}

fun DrawScope.drawSimulatedMidiNotes(
    width: Float,
    height: Float,
    noteColor: Color,
    currentPosition: Float,
    duration: Float
) {
    // This is a simulation - in a real implementation, you'd parse actual MIDI data
    // For now, we'll draw some sample notes to demonstrate the visualization
    
    val noteHeight = height / 12 // 12 semitones per octave
    val notes = listOf(
        // Format: (startTime, duration, note)
        Triple(0.2f, 0.8f, 4), // C
        Triple(1.0f, 0.5f, 6), // E
        Triple(1.5f, 0.3f, 8), // G
        Triple(2.0f, 1.0f, 4), // C
        Triple(3.2f, 0.4f, 9), // A
        Triple(3.8f, 0.6f, 6), // E
        Triple(4.5f, 0.8f, 2), // D
    )
    
    notes.forEach { (startTime, noteDuration, noteIndex) ->
        val startX = (startTime / maxOf(duration, 1f)) * width
        val noteWidth = (noteDuration / maxOf(duration, 1f)) * width
        val y = noteIndex * noteHeight
        
        // Make notes currently playing more prominent
        val isCurrentlyPlaying = currentPosition >= startTime && 
                               currentPosition <= (startTime + noteDuration)
        val alpha = if (isCurrentlyPlaying) 1.0f else 0.6f
        
        drawRect(
            color = noteColor.copy(alpha = alpha),
            topLeft = Offset(startX, height - y - noteHeight),
            size = Size(noteWidth, noteHeight * 0.8f)
        )
    }
}

@Composable
fun MidiNoteVisualization(
    midiFile: MidiFile,
    currentPosition: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val playheadColor = MaterialTheme.colorScheme.error
    
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val width = size.width
        val height = size.height
        
        // Draw background grid
        drawMidiGrid(width, height, surfaceVariant, onSurface.copy(alpha = 0.3f))
        
        // Draw actual MIDI notes from the file data
        drawActualMidiNotes(midiFile, width, height, primaryColor, currentPosition)
        
        // Draw playhead
        if (midiFile.duration > 0f) {
            val playheadX = (currentPosition / midiFile.duration) * width
            drawLine(
                color = playheadColor,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, height),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

@Composable
fun ApiKeyConfigDialog(
    playbackService: model.MidiPlaybackService,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var apiKeys by remember { mutableStateOf(playbackService.getAllApiKeys() ?: emptyMap()) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🔑 API Key Configuration",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configure your API keys for LLM services:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gemini API Key
                    item {
                        ApiKeyField(
                            label = "Google Gemini API Key",
                            serviceName = "gemini",
                            currentValue = apiKeys["gemini"] ?: "",
                            onValueChange = { apiKeys = apiKeys + ("gemini" to it) },
                            placeholder = "Enter your Google AI Studio API key"
                        )
                    }
                    
                    // GPT-4 API Key
                    item {
                        ApiKeyField(
                            label = "OpenAI GPT-4 API Key",
                            serviceName = "gpt4",
                            currentValue = apiKeys["gpt4"] ?: "",
                            onValueChange = { apiKeys = apiKeys + ("gpt4" to it) },
                            placeholder = "Enter your OpenAI API key"
                        )
                    }
                    
                    // Claude API Key
                    item {
                        ApiKeyField(
                            label = "Anthropic Claude API Key",
                            serviceName = "claude",
                            currentValue = apiKeys["claude"] ?: "",
                            onValueChange = { apiKeys = apiKeys + ("claude" to it) },
                            placeholder = "Enter your Anthropic API key"
                        )
                    }
                }
                
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Error: $errorMessage",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSaving = true
                    errorMessage = null
                    try {
                        // Filter out empty keys
                        val filteredKeys = apiKeys.filterValues { it.isNotBlank() }
                        playbackService.saveAllApiKeys(filteredKeys)
                        onSaved()
                    } catch (e: Exception) {
                        errorMessage = e.message
                        isSaving = false
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    Text("Saving...")
                } else {
                    Text("💾 Save Configuration")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ApiKeyField(
    label: String,
    serviceName: String,
    currentValue: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    var isVisible by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        OutlinedTextField(
            value = currentValue,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { isVisible = !isVisible }) {
                    Text(if (isVisible) "👁️" else "🔒")
                }
            },
            singleLine = true
        )
        
        if (currentValue.isNotBlank()) {
            Text(
                text = "✅ API key configured",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun RubberDuckApp(
    state: AppState,
    playbackService: model.MidiPlaybackService,
    onAddMidiFile: () -> Unit,
    onAddPromptOnlyRow: () -> Unit,
    onRowUpdate: (String, MidiRow) -> Unit,
    onProcessRequest: (String) -> Unit,
    onDeleteRow: (String) -> Unit,
    onClearAll: () -> Unit,
    onShowApiKeyConfig: () -> Unit,
    onHideApiKeyConfig: () -> Unit,
    onExportMidiFile: (MidiFile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with API Config button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎵 RubberDuck - LLM-Powered MIDI Composer",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Button(
                onClick = onShowApiKeyConfig,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("🔑 API Keys")
            }
        }
        
        // Main content
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.rows, key = { it.id }) { row ->
                MidiRowComponent(
                    row = row,
                    availableServices = state.availableServices,
                    playbackService = playbackService,
                    onRowUpdate = { updatedRow -> onRowUpdate(row.id, updatedRow) },
                    onProcessRequest = { onProcessRequest(row.id) },
                    onDeleteRow = { onDeleteRow(row.id) },
                    onExportMidiFile = onExportMidiFile
                )
            }
        }
        
        // Add buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddMidiFile,
                modifier = Modifier.weight(1f)
            ) {
                Text("➕ Add MIDI File")
            }
            
            Button(
                onClick = onAddPromptOnlyRow,
                modifier = Modifier.weight(1f)
            ) {
                Text("✨ Generate from Prompt")
            }
        }
        
        // Clear all button
        if (state.rows.isNotEmpty()) {
            Button(
                onClick = onClearAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("🗑️ Clear All Rows")
            }
        }
    }
    
    // Show API Key Configuration dialog
    if (state.showApiKeyConfig) {
        ApiKeyConfigDialog(
            playbackService = playbackService,
            onDismiss = onHideApiKeyConfig,
            onSaved = {
                onHideApiKeyConfig()
                // The app will automatically refresh available services
            }
        )
    }
}

@Composable
fun MidiRowComponent(
    row: MidiRow,
    availableServices: Set<LlmService>,
    playbackService: model.MidiPlaybackService,
    onRowUpdate: (MidiRow) -> Unit,
    onProcessRequest: () -> Unit,
    onDeleteRow: () -> Unit,
    onExportMidiFile: (MidiFile) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Color.Black, RectangleShape),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row header with delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Row ${row.rowNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Derivation subtitle (if this row was derived from another)
                    row.derivedFrom?.let { derivation ->
                        Text(
                            text = "(Derived from Row ${derivation.sourceRowNumber} using prompt: \"${derivation.prompt}\")",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                // Delete button
                IconButton(
                    onClick = onDeleteRow,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        text = "❌",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            // MIDI file section
            MidiFileSection(
                midiFile = row.inputFile,
                label = "Input MIDI",
                onPlayPause = { midiFile ->
                    println("🔍 DEBUG: Play/Pause clicked for input file")
                    println("  Current isPlaying: ${midiFile.isPlaying}")
                    println("  Is in-memory: ${midiFile.isInMemory}")
                    println("  Will toggle to: ${!midiFile.isPlaying}")
                    
                    // Use smart playback method that handles both file and in-memory
                    val nowPlaying = playbackService.playPauseFile(midiFile)
                    
                    onRowUpdate(row.copy(
                        inputFile = midiFile.copy(isPlaying = nowPlaying)
                    ))
                },
                onStop = { midiFile ->
                    println("🔍 DEBUG: Stop clicked for input file")
                    println("  Current isPlaying: ${midiFile.isPlaying}")
                    println("  Is in-memory: ${midiFile.isInMemory}")
                    
                    // Use smart stop method that handles both file and in-memory
                    playbackService.stopFile(midiFile)
                    
                    onRowUpdate(row.copy(
                        inputFile = midiFile.copy(
                            isPlaying = false,
                            currentPosition = 0f
                        )
                    ))
                },
                onSeek = { midiFile, position ->
                    println("🔍 DEBUG: Seek to position: $position seconds")
                    
                    // Convert to percentage and seek
                    val positionPercent = if (midiFile.duration > 0f) {
                        (position / midiFile.duration).toDouble()
                    } else 0.0
                    
                    playbackService.setPosition(midiFile.path, positionPercent)
                    
                    onRowUpdate(row.copy(
                        inputFile = midiFile.copy(currentPosition = position)
                    ))
                },
                onExportToFile = { midiFile ->
                    onExportMidiFile(midiFile)
                }
            )
            
            // Output file section (if available)
            if (row.outputFile != null) {
                MidiFileSection(
                    midiFile = row.outputFile,
                    label = "Output MIDI",
                    onPlayPause = { midiFile ->
                        println("🔍 DEBUG: Play/Pause clicked for output file")
                        println("  Current isPlaying: ${midiFile.isPlaying}")
                        println("  Is in-memory: ${midiFile.isInMemory}")
                        
                        // Use smart playback method that handles both file and in-memory
                        val nowPlaying = playbackService.playPauseFile(midiFile)
                        
                        onRowUpdate(row.copy(
                            outputFile = midiFile.copy(isPlaying = nowPlaying)
                        ))
                    },
                    onStop = { midiFile ->
                        println("🔍 DEBUG: Stop clicked for output file")
                        println("  Is in-memory: ${midiFile.isInMemory}")
                        
                        // Use smart stop method that handles both file and in-memory
                        playbackService.stopFile(midiFile)
                        
                        onRowUpdate(row.copy(
                            outputFile = midiFile.copy(
                                isPlaying = false,
                                currentPosition = 0f
                            )
                        ))
                    },
                    onSeek = { midiFile, position ->
                        println("🔍 DEBUG: Seek output file to position: $position seconds")
                        
                        // Convert to percentage and seek
                        val positionPercent = if (midiFile.duration > 0f) {
                            (position / midiFile.duration).toDouble()
                        } else 0.0
                        
                        playbackService.setPosition(midiFile.path, positionPercent)
                        
                        onRowUpdate(row.copy(
                            outputFile = midiFile.copy(currentPosition = position)
                        ))
                    },
                    onExportToFile = { midiFile ->
                        onExportMidiFile(midiFile)
                    }
                )
            }
            
            // Prompt input
            OutlinedTextField(
                value = row.prompt,
                onValueChange = { newPrompt ->
                    onRowUpdate(row.copy(prompt = newPrompt))
                },
                label = { Text("LLM Prompt") },
                placeholder = { 
                    if (row.inputFile != null) {
                        Text("e.g., Add a walking bassline")
                    } else {
                        Text("e.g., Create a funk shuffle with syncopated kick and snare for 8 bars")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !row.isProcessing
            )
            
            // LLM service selection
            LlmServiceSelector(
                selectedService = row.selectedLlm,
                availableServices = availableServices,
                onServiceSelected = { service ->
                    println("🔍 DEBUG: Service selected callback triggered: $service")
                    println("  Row ID: ${row.id}")
                    println("  Previous service: ${row.selectedLlm}")
                    val updatedRow = row.copy(selectedLlm = service)
                    println("  Updated row: $updatedRow")
                    onRowUpdate(updatedRow)
                },
                enabled = !row.isProcessing
            )
            
            // Submit button and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onProcessRequest,
                    enabled = !row.isProcessing && 
                            row.prompt.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (row.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processing...")
                    } else {
                        Text("🚀 Submit to ${row.selectedLlm.displayName}")
                    }
                }
            }
            
            // Error display
            if (row.error != null) {
                Text(
                    text = "❌ Error: ${row.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MidiFileSection(
    midiFile: MidiFile?,
    label: String,
    onPlayPause: ((MidiFile) -> Unit)? = null,
    onStop: ((MidiFile) -> Unit)? = null,
    onSeek: ((MidiFile, Float) -> Unit)? = null,
    onExportToFile: ((MidiFile) -> Unit)? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        
        if (midiFile != null) {
            // File name and controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = midiFile.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // File path with copy button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = midiFile.path,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        Button(
                            onClick = { 
                                onExportToFile?.invoke(midiFile)
                            },
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "� Export",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                // Playback controls
                Row {
                    IconButton(
                        onClick = { 
                            println("🔍 DEBUG: Play/Pause button clicked")
                            println("  MidiFile: ${midiFile.name}")
                            println("  Current isPlaying: ${midiFile.isPlaying}")
                            onPlayPause?.invoke(midiFile) 
                        }
                    ) {
                        Text(if (midiFile.isPlaying) "⏸️" else "▶️")
                    }
                    IconButton(
                        onClick = { 
                            println("🔍 DEBUG: Stop button clicked")
                            println("  MidiFile: ${midiFile.name}")
                            onStop?.invoke(midiFile) 
                        }
                    ) {
                        Text("⏹️")
                    }
                }
            }
            
            // Enhanced playback section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Time display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(midiFile.currentPosition),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(midiFile.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Progress bar with scrubbing capability
                val progress = if (midiFile.duration > 0f) {
                    midiFile.currentPosition / midiFile.duration
                } else 0f
                
                Slider(
                    value = progress,
                    onValueChange = { newProgress ->
                        val newPosition = newProgress * midiFile.duration
                        onSeek?.invoke(midiFile, newPosition)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                
                // Visual progress bar (decorative)
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                // MIDI Note Visualization
                MidiNoteVisualization(
                    midiFile = midiFile,
                    currentPosition = midiFile.currentPosition,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        } else {
            Text(
                text = if (label == "Input MIDI") "Generate from prompt only" else "No file selected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LlmServiceSelector(
    selectedService: LlmService,
    availableServices: Set<LlmService>,
    onServiceSelected: (LlmService) -> Unit,
    enabled: Boolean = true
) {
    Column {
        Text(
            text = "LLM Service",
            style = MaterialTheme.typography.labelMedium
        )
        
        LlmService.values().forEach { service ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedService == service,
                    onClick = { 
                        println("🔍 DEBUG: Radio button clicked for service: $service")
                        println("  Currently selected: $selectedService")
                        println("  Service available: ${service in availableServices}")
                        println("  Enabled: ${enabled && service in availableServices}")
                        onServiceSelected(service)
                    },
                    enabled = enabled && service in availableServices
                )
                Text(
                    text = service.displayName + if (service !in availableServices) " (not configured)" else "",
                    modifier = Modifier.padding(start = 8.dp),
                    color = if (service in availableServices) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
