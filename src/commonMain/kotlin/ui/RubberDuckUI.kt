package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.*

@Composable
fun RubberDuckApp(
    state: AppState,
    onAddMidiFile: () -> Unit,
    onRowUpdate: (String, MidiRow) -> Unit,
    onProcessRequest: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "🎵 RubberDuck - LLM-Powered MIDI Composer",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Main content
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.rows, key = { it.id }) { row ->
                MidiRowComponent(
                    row = row,
                    availableServices = state.availableServices,
                    onRowUpdate = { updatedRow -> onRowUpdate(row.id, updatedRow) },
                    onProcessRequest = { onProcessRequest(row.id) }
                )
            }
        }
        
        // Add MIDI file button
        Button(
            onClick = onAddMidiFile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("➕ Add Input MIDI File")
        }
    }
}

@Composable
fun MidiRowComponent(
    row: MidiRow,
    availableServices: Set<LlmService>,
    onRowUpdate: (MidiRow) -> Unit,
    onProcessRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // MIDI file section
            MidiFileSection(
                midiFile = row.inputFile,
                label = "Input MIDI"
            )
            
            // Output file section (if available)
            if (row.outputFile != null) {
                MidiFileSection(
                    midiFile = row.outputFile,
                    label = "Output MIDI"
                )
            }
            
            // Prompt input
            OutlinedTextField(
                value = row.prompt,
                onValueChange = { newPrompt ->
                    onRowUpdate(row.copy(prompt = newPrompt))
                },
                label = { Text("LLM Prompt") },
                placeholder = { Text("e.g., Add a walking bassline") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !row.isProcessing
            )
            
            // LLM service selection
            LlmServiceSelector(
                selectedService = row.selectedLlm,
                availableServices = availableServices,
                onServiceSelected = { service ->
                    onRowUpdate(row.copy(selectedLlm = service))
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
                            row.inputFile != null && 
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
    label: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        
        if (midiFile != null) {
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
                    Text(
                        text = midiFile.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Playback controls (placeholder for now)
                Row {
                    IconButton(
                        onClick = { /* TODO: Implement play/pause */ }
                    ) {
                        Text(if (midiFile.isPlaying) "⏸️" else "▶️")
                    }
                    IconButton(
                        onClick = { /* TODO: Implement stop */ }
                    ) {
                        Text("⏹️")
                    }
                }
            }
            
            // Progress bar (placeholder)
            LinearProgressIndicator(
                progress = midiFile.currentPosition / maxOf(midiFile.duration, 1f),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "No file selected",
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
                    onClick = { onServiceSelected(service) },
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
