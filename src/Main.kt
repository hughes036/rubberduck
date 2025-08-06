import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import javax.swing.JFileChooser

@Composable
@Preview
fun App() {
    var selectedFile by remember { mutableStateOf<java.io.File?>(null) }
    var prompt by remember { mutableStateOf("") }
    val llmOptions = listOf("Gemini", "GPT-4", "Claude")
    var selectedLlm by remember { mutableStateOf(llmOptions[0]) }

    Column(modifier = Modifier.padding(16.dp)) {
        // File selection
        Button(onClick = {
            val fileChooser = JFileChooser()
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.selectedFile
            }
        }) {
            Text("Select MIDI File")
        }

        selectedFile?.let {
            Text("Selected file: ${it.absolutePath}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Midi visualization
        if (selectedFile != null) {
            MidiFileVisualizer(selectedFile!!)
        } else {
            Text("Select a MIDI file to see the visualization")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prompt input
        TextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("LLM Prompt") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // LLM selection
        Row {
            llmOptions.forEach { llm ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedLlm == llm,
                        onClick = { selectedLlm = llm }
                    )
                    Text(llm)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val generatedFiles = remember { mutableStateOf(listOf<java.io.File>()) }

        // Submit button
        Button(onClick = {
            selectedFile?.let {
                val outputFileName = "output_${System.currentTimeMillis()}.mid"
                val result = com.rubberduck.Main.runHeadless(
                    it.absolutePath,
                    outputFileName,
                    selectedLlm.lowercase(),
                    "",
                    prompt
                )
                if (result != null) {
                    generatedFiles.value = generatedFiles.value + java.io.File(result)
                }
            }
        }) {
            Text("Submit")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generated files
        Text("Generated Files:")
        generatedFiles.value.forEach { file ->
            Text(file.absolutePath)
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
