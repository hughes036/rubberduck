import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.io.File
import javax.sound.midi.MidiSystem

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import javax.sound.midi.MidiEvent
import javax.sound.midi.MidiMessage
import javax.sound.midi.ShortMessage

@Composable
fun MidiFileVisualizer(file: File) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        drawGrid()
        val sequence = MidiSystem.getSequence(file)
        val noteHeight = 2f
        val noteWidth = 2f
        for (track in sequence.tracks) {
            for (i in 0 until track.size()) {
                val event: MidiEvent = track[i]
                val message: MidiMessage = event.message
                if (message is ShortMessage) {
                    if (message.command == ShortMessage.NOTE_ON) {
                        val key = message.data1
                        val velocity = message.data2
                        if (velocity > 0) {
                            val x = event.tick / 10f
                            val y = size.height - (key * noteHeight)
                            drawRect(
                                color = Color.Green,
                                topLeft = Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(noteWidth, noteHeight)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawGrid() {
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    val noteHeight = 2f
    val octaveColor = Color.Gray.copy(alpha = 0.5f)

    // Vertical lines (time divisions)
    for (x in 0..size.width.toInt() step 100) {  // Align with note width
        drawLine(
            color = gridColor,
            start = Offset(x.toFloat(), 0f),
            end = Offset(x.toFloat(), size.height),
            strokeWidth = 1f
        )
    }

    // Horizontal lines (pitch divisions)
    for (pitch in 0..127) {  // MIDI note range
        val y = size.height - (pitch * noteHeight) - noteHeight
        val color = if (pitch % 12 == 0) octaveColor else gridColor  // Highlight octaves
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = if (pitch % 12 == 0) 2f else 1f
        )
    }
}
