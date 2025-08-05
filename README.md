# Rubberduck Project

A Java project that combines MIDI processing with Gemini AI integration. This library provides functionality to convert MIDI files to a serialized text format that can be understood by LLMs, and includes a working example of Gemini AI integration.

## Features

- Convert MIDI files to a serialized text format
- Convert serialized text format back to MIDI files
- Utility functions for working with MIDI data
- Gemini AI integration example
- Comprehensive test suite

## Getting Started

### Prerequisites

- Java 21 (configured via SDKMAN)
- Gradle
- Gemini API key

### Setup

1. Create an `apikey.txt` file with your Gemini API key:
   ```bash
   echo "your-gemini-api-key-here" > apikey.txt
   ```

2. Build and run the project:
   ```bash
   ./run.sh
   ```

   Or manually:
   ```bash
   ./gradlew clean build
   export GEMINI_API_KEY=$(cat ./apikey.txt)
   ./gradlew run
   ```

## Examples

### Gemini AI Integration

The project includes a working example (`src/gemini/GeminiHelloWorld.java`) that demonstrates how to:
- Set up the Gemini client
- Send prompts to the Gemini API
- Process responses

## API Documentation

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

To run the application in demo mode (without arguments), run:

```bash
./gradlew run
```

This will run the `Main` class, which demonstrates the usage of the MIDI processing library.

### Using the CLI Tool

The application can also be used as a command-line tool to convert between MIDI files and serialized text format. To use the CLI tool:

```bash
./gradlew run --args="<input-file>"
```

Where `<input-file>` is the path to either a MIDI file or a serialized text file. The tool automatically detects the file type and performs the appropriate conversion:

- If the input is a MIDI file, it will be converted to a serialized text format (with a `.txt` extension)
- If the input is a serialized text file, it will be converted to a MIDI file (with a `.mid` extension)

The output file will be created in the same directory as the input file, with the same base name but a different extension.

#### Examples

Convert a MIDI file to serialized text:

```bash
./gradlew run --args="path/to/input.mid"
```

This will create a file named `path/to/input.txt` containing the serialized representation of the MIDI file.

Convert a serialized text file to MIDI:

```bash
./gradlew run --args="path/to/serialized.txt"
```

This will create a MIDI file named `path/to/serialized.mid` based on the serialized text.

#### Building a Standalone JAR

To create a standalone JAR file that can be run directly with Java:

```bash
./gradlew jar
```

Then you can run the CLI tool with:

```bash
java -jar build/libs/rubberduck-1.0-SNAPSHOT.jar <input-file>
```
