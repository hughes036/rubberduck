# Scriabin Project

An LLM-powered MIDI composition tool that uses artificial intelligence to enhance and modify MIDI files based on natural language prompts. Features both a command-line interface and a modern graphical user interface built with Kotlin Multiplatform Compose.

## Features

- 🎵 **LLM-Powered MIDI Composition** - Transform MIDI files using AI with natural language prompts
- 🖥️ **Modern GUI Interface** - Intuitive desktop application with drag-and-drop MIDI processing
- 📱 **Cross-Platform Desktop** - Built with Kotlin Multiplatform Compose for Windows, macOS, and Linux
- 🎹 **MIDI Processing** - Convert MIDI files to serialized text format and back
- 🤖 **Multiple LLM Support** - Pluggable architecture supporting various AI services
- 🎼 **Intelligent Composition** - Add basslines, drums, harmonies, and more using AI
- 🛠️ **Developer-Friendly** - Comprehensive API for MIDI manipulation
- 🧪 **Well-Tested** - Comprehensive test suite ensuring reliability

## Quick Start

### �️ For Users (GUI Application)
```bash
git clone https://github.com/your-username/scriabin.git
cd scriabin
# Set up your API key in apikeys.json (copy from apikeys.json.example)
./gradlew run  # Launches the GUI application
```

### �🚀 For Users (Install CLI)
```bash
git clone https://github.com/your-username/scriabin.git
cd scriabin
./gradlew installDist
export PATH="$PATH:$(pwd)/build/install/scriabin/bin"
scriabin input.mid output.mid gemini "" "Add a walking bassline"
```

### 🔧 For Developers (Use Gradle)
```bash
git clone https://github.com/your-username/scriabin.git
cd scriabin
# Set up your API key in apikeys.json
./gradlew runCLI --args="example.mid output.mid gemini \"\" \"Add drums\""
```

## Installation

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/your-username/scriabin.git
    cd scriabin
    ```

2.  **Build the project and install the CLI:**

    ```bash
    ./gradlew installDist
    ```

    This will create a runnable version of the application in `build/install/scriabin`.

3.  **Add the CLI to your PATH:**

    For easy access, add the `bin` directory to your system's `PATH`.

    *   For Bash (usually on Linux and macOS):

        ```bash
        echo 'export PATH="$PATH:'$(pwd)'/build/install/scriabin/bin"' >> ~/.bashrc
        source ~/.bashrc
        ```

    *   For Zsh (default on newer macOS):

        ```bash
        echo 'export PATH="$PATH:'$(pwd)'/build/install/scriabin/bin"' >> ~/.zshrc
        source ~/.zshrc
        ```

4.  **Configure API Keys:**

    Create a file named `apikeys.json` in the root of the project and add your API keys. You can copy the example file:

    ```bash
    cp apikeys.json.example apikeys.json
    ```

    Then, edit `apikeys.json` with your keys.

## Usage

### GUI Application

The desktop GUI provides an intuitive interface for MIDI composition with LLMs:

```bash
# Launch the GUI application
./gradlew run

# Or use the specific UI task
./gradlew runUI
```

**GUI Features:**
- **File Selection**: Click "➕ Add Input MIDI File" to select MIDI files from your system
- **LLM Integration**: Choose from available LLM services (Gemini, GPT-4, Claude) via radio buttons
- **Prompt Input**: Enter natural language prompts describing desired musical changes
- **Real-time Processing**: Submit requests and monitor processing status with progress indicators
- **MIDI Playback**: Preview input and output MIDI files with built-in playback controls
- **Error Handling**: Clear error messages and status feedback
- **Multiple Rows**: Process multiple MIDI files simultaneously in a vertical scrolling layout
- **Output Chaining**: Use LLM output as input for subsequent processing steps

### Command Line Interface

Once installed, you can use the `scriabin` command from anywhere in your terminal.

```bash
scriabin <input-midi> <output-midi> <llm-service> <api-key> <composition-prompt>
```

**Arguments:**

*   `<input-midi>`: Path to the input MIDI file.
*   `<output-midi>`: Path for the output MIDI file.
*   `<llm-service>`: LLM service to use (e.g., `gemini`).
*   `<api-key>`: Your API key. Use `""` (an empty string) to load the key from `apikeys.json`.
*   `<composition-prompt>`: A description of the changes you want to make to the MIDI file.

**Examples:**

```bash
# Add a walking bassline to a MIDI file
scriabin input.mid output.mid gemini "" "Add a walking bassline"

# Add a drum pattern using a specific API key
scriabin song.mid with_drums.mid gemini "your-api-key-here" "Add a simple rock beat"
```

### Development Usage with Gradle

For development purposes, you can run both the GUI and CLI applications directly with Gradle without installing them first. This is especially useful when you're modifying the code and want to test changes quickly.

#### GUI Development
```bash
# Launch the GUI application for testing
./gradlew run

# Alternative UI-specific task
./gradlew runUI
```

#### CLI Development
```bash
# Basic CLI run command
./gradlew runCLI --args="input.mid output.mid gemini \"\" \"Add a walking bassline\""

# Add drums to an existing melody
./gradlew runCLI --args="example.mid enhanced.mid gemini \"\" \"Add a simple rock drum pattern\""

# Create harmony parts
./gradlew runCLI --args="melody.mid harmonized.mid gemini \"\" \"Add three-part vocal harmony\""

# Use a custom API key inline
./gradlew runCLI --args="song.mid final.mid gemini \"your-api-key\" \"Add bass and percussion\""
```

**Note:** When using `--args` with Gradle:
- Wrap the entire argument string in quotes
- Use `\"\"` for empty strings (like when loading API key from file)
- Escape inner quotes with backslashes
- The Gradle run task will automatically load your API key from `apikeys.json` and set the `GOOGLE_API_KEY` environment variable

**Development Examples:**

```bash
# Test with a simple composition change
./gradlew run --args="example.mid test-output.mid gemini \"\" \"make it sound more jazzy\""

# Add multiple instruments
./gradlew run --args="basic.mid full-band.mid gemini \"\" \"add drums, bass, and piano accompaniment\""

# Modify existing arrangement
./gradlew run --args="song.mid remix.mid gemini \"\" \"change the drum pattern to bossa nova style\""

# Quick development iteration
./gradlew run --args="input.mid output.mid gemini \"\" \"add a simple snare on beat 2\""
```

### MidiSerializer

The `MidiSerializer` class provides methods to convert MIDI files to a serialized text format.

```java
// Convert a MIDI file to a serialized text format
String serialized = MidiSerializer.serializeMidiFile(midiFile);

// Convert a MIDI Sequence object to a serialized text format
String serialized = MidiSerializer.serializeSequence(sequence);
```

### MidiDeserializer

The `MidiDeserializer` class provides methods to convert the serialized text format back to MIDI files.

```java
// Convert serialized text to a MIDI file
MidiDeserializer.deserializeToMidiFile(serializedMidi, outputFile);

// Convert serialized text to a MIDI Sequence object
Sequence sequence = MidiDeserializer.deserializeToSequence(serializedMidi);
```

### MidiUtils

The `MidiUtils` class provides utility methods for working with MIDI data.

```java
// Get the name of a MIDI note number (e.g., 60 -> "C4")
String noteName = MidiUtils.getNoteName(60);

// Convert a MIDI note number to its frequency in Hz
double frequency = MidiUtils.getNoteFrequency(69); // A4 = 440 Hz

// Convert a frequency in Hz to the closest MIDI note number
int noteNumber = MidiUtils.frequencyToNoteNumber(440.0); // 440 Hz = A4 (69)

// Get information about a MIDI file
String info = MidiUtils.getMidiFileInfo(midiFile);

// Create a simple MIDI sequence with a single note
Sequence sequence = MidiUtils.createSingleNoteSequence(60, 100, 480);
```

## Serialized Format

The serialized format is a text-based representation of MIDI data, designed to be easily understood by LLMs. It uses a key-value pair format with pipe (|) separators.

Example:
```
MIDI_HEADER|divisionType=0.0|resolution=480
TRACKS|count=1
TRACK|number=0|events=4
EVENT|tick=0|type=ShortMessage|command=192|channel=0|data1=0|data2=0|description=PROGRAM_CHANGE|program=0
EVENT|tick=0|type=ShortMessage|command=144|channel=0|data1=60|data2=100|description=NOTE_ON|note=60|velocity=100
EVENT|tick=480|type=ShortMessage|command=128|channel=0|data1=60|data2=0|description=NOTE_OFF|note=60|velocity=0
```

## Usage Example

```java
import midi.MidiSerializer;
import midi.MidiDeserializer;
import midi.MidiUtils;

import javax.sound.midi.*;
import java.io.File;

public class Example {
    public static void main(String[] args) {
        try {
            // Create a simple MIDI sequence and save it to a file
            Sequence sequence = MidiUtils.createSingleNoteSequence(60, 100, 960);
            File midiFile = new File("example.mid");
            MidiSystem.write(sequence, 1, midiFile);

            // Serialize the MIDI file to a text format
            String serialized = MidiSerializer.serializeMidiFile(midiFile);
            System.out.println("Serialized MIDI data:");
            System.out.println(serialized);

            // Deserialize the text format back to a MIDI file
            File outputFile = new File("deserialized.mid");
            MidiDeserializer.deserializeToMidiFile(serialized, outputFile);

            // Get information about the MIDI file
            String midiInfo = MidiUtils.getMidiFileInfo(outputFile);
            System.out.println(midiInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## Testing

The library includes a comprehensive test suite using JUnit Jupiter. The tests are located in the `src/test` directory. To run the tests, use the Gradle test task:

```bash
./gradlew test
```

This will run all the tests and generate a test report in the `build/reports/tests/test` directory.

## Requirements

- Java 8 or higher
- Gradle (or use the included Gradle wrapper)

## Building and Testing

This project uses Gradle for building and testing. You can use the included Gradle wrapper to build and test the project without installing Gradle.

### Building the Project

To build the project, run:

```bash
./gradlew build
```

This will compile the code, run the tests, and create a JAR file in the `build/libs` directory.

### Running the Tests

To run the tests, run:

```bash
./gradlew test
```

### Running the Application

To run the application and see usage instructions, run:

```bash
./gradlew run
```

This will show the help message with all available options and examples.

### Using the CLI Tool with Gradle

The application is primarily used as a command-line tool for LLM-powered MIDI composition. During development, you can run it directly with Gradle:

```bash
./gradlew run --args="<input-midi> <output-midi> <llm-service> <api-key> <composition-prompt>"
```

**Complete Development Examples:**

```bash
# Basic example - add drums to a melody
./gradlew run --args="example.mid with-drums.mid gemini \"\" \"Add a simple rock beat\""

# Add a bassline
./gradlew run --args="melody.mid with-bass.mid gemini \"\" \"Add a walking jazz bassline\""

# Create a full arrangement
./gradlew run --args="simple.mid arranged.mid gemini \"\" \"Add drums, bass, and piano accompaniment for a pop song\""

# Test with different prompts
./gradlew run --args="input.mid output1.mid gemini \"\" \"make it swing\""
./gradlew run --args="input.mid output2.mid gemini \"\" \"add latin percussion\""
./gradlew run --args="input.mid output3.mid gemini \"\" \"create a countermelody\""
```

**Development Tips:**
- The Gradle run task automatically loads API keys from `apikeys.json`
- Use `\"\"` for the API key parameter to load from file
- Escape quotes properly when using `--args`
- Output files are created in the project directory
- Check console output for detailed processing steps

### Development Configuration

The `build.gradle` file includes a special configuration for the `run` task that automatically loads API keys from your `apikeys.json` file and sets them as environment variables. This means you don't need to manually set environment variables during development.

```gradle
// The run task automatically loads from apikeys.json
run {
    environment System.getenv()
    doFirst {
        // Loads gemini key from apikeys.json and sets GOOGLE_API_KEY
        // Loads gpt4 key and sets OPENAI_API_KEY  
        // Loads claude key and sets ANTHROPIC_API_KEY
    }
}
```

This makes development much easier - just edit your `apikeys.json` file and run with Gradle!

### Building for Distribution

To create a standalone JAR file or distribution package:

```bash
# Build a standard JAR
./gradlew jar

# Build a distribution with all dependencies and scripts
./gradlew installDist

# Create distribution archives
./gradlew distTar distZip
```

The installDist task creates a complete distribution in `build/install/scriabin/` with:
- `bin/scriabin` - Unix shell script
- `bin/scriabin.bat` - Windows batch script  
- `lib/` - All JAR dependencies

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes and test with `./gradlew run --args="..."`
4. Run tests with `./gradlew test`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
