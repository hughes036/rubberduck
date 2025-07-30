# MIDI Processing Library

A Java library for processing MIDI files, designed to work with Large Language Models (LLMs). This library provides functionality to convert MIDI files to a serialized text format that can be understood by LLMs, and to convert the serialized format back to MIDI files.

## Features

- Convert MIDI files to a serialized text format
- Convert serialized text format back to MIDI files
- Transform MIDI files using Large Language Models (LLMs)
- Utility functions for working with MIDI data
- Command-line interface for file processing and LLM-based transformations
- Comprehensive test suite

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

The serialized format is a text-based representation of MIDI data, designed to be easily understood by LLMs. It uses a key-value pair format with pipe (|) separators. The serialized format can be enclosed between delimiters (`<<MIDI_START>>` and `<<MIDI_END>>`) to make it easier to extract from LLM responses.

Example:
```
<<MIDI_START>>
MIDI_HEADER|divisionType=0.0|resolution=480
TRACKS|count=1
TRACK|number=0|events=4
EVENT|tick=0|type=ShortMessage|command=192|channel=0|data1=0|data2=0|description=PROGRAM_CHANGE|program=0
EVENT|tick=0|type=ShortMessage|command=144|channel=0|data1=60|data2=100|description=NOTE_ON|note=60|velocity=100
EVENT|tick=480|type=ShortMessage|command=128|channel=0|data1=60|data2=0|description=NOTE_OFF|note=60|velocity=0
<<MIDI_END>>
```

## LLM Integration

The library provides integration with Large Language Models (LLMs) for transforming MIDI files. The `MidiLlmTransformer` class encapsulates the workflow of serializing a MIDI file, sending it to an LLM with a prompt, and deserializing the response back to a MIDI file.

### Available LLM Services

The library currently supports the following LLM services:

- **OpenAI** (`openai`): Uses OpenAI's GPT models via their API

### Adding New LLM Services

To add support for a new LLM service:

1. Create a new class that implements the `LlmService` interface
2. Register the new service in the `LlmServiceFactory` class
3. Use the new service in your application or via the CLI

```java
import llm.LlmService;
import llm.LlmServiceFactory;
import llm.MidiLlmTransformer;

import java.io.File;

public class LlmExample {
    public static void main(String[] args) {
        try {
            // Get an LLM service
            LlmService llmService = LlmServiceFactory.getService("openai");
            llmService.setApiKey("your-api-key");

            // Transform a MIDI file using the LLM
            File inputFile = new File("input.mid");
            File outputFile = new File("output.mid");
            String prompt = "Make this melody more jazzy";

            MidiLlmTransformer.transformMidi(inputFile, outputFile, prompt, llmService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### LLM Prompt Examples

When using the LLM to transform MIDI files, the quality of your prompt significantly impacts the results. Here are some examples of effective prompts and what to expect in the output MIDI files:

#### Basic Transformation Prompts

1. **Style Transformation**:
   ```
   Transform this piano melody into a jazz style with swing rhythm and add some seventh chords for harmonic richness.
   ```
   *Expected Output*: The LLM will modify the note timing to create a swing feel, add jazz-style chord voicings, and possibly extend note durations for a more legato jazz piano style.

2. **Instrumentation Changes**:
   ```
   Convert this solo piano piece into a string quartet arrangement. Distribute the melody and harmony across violin, viola, and cello parts.
   ```
   *Expected Output*: The MIDI will be transformed to have multiple tracks with different PROGRAM_CHANGE events to represent string instruments. The original melody will be distributed across these instruments with appropriate ranges.

#### Advanced Musical Transformations

1. **Harmonic Reharmonization**:
   ```
   Reharmonize this melody with modal interchange chords. Add a secondary dominant before each major cadence point and include some borrowed chords from the parallel minor.
   ```
   *Expected Output*: The chord progression will be enriched with more complex harmony while maintaining the original melody. New chord tones will appear in the bass and inner voices.

2. **Structural Modifications**:
   ```
   Take this 8-bar phrase and develop it into a 32-bar AABA form. For the B section, modulate to the relative minor and create variations on the original motifs with increased rhythmic density.
   ```
   *Expected Output*: The MIDI file will be expanded to a longer form with clear sections. The B section will feature the same melodic ideas but in a different key and with more notes per measure.

#### Technical Adjustments

1. **Performance Dynamics**:
   ```
   Add expressive dynamics to this piece. Create a gradual crescendo through the first 8 bars, then a sudden piano for the middle section, and end with a forte final phrase. Also add some subtle rubato at phrase endings.
   ```
   *Expected Output*: The velocity values of notes will vary to create dynamic contrast. Note timing will be slightly adjusted at phrase endings to create a subtle slowing effect.

2. **Fixing Technical Issues**:
   ```
   This MIDI file has some overlapping notes causing dissonance. Please clean up any note overlaps, normalize the velocity of all notes to a range of 70-90, and quantize the rhythm to sixteenth notes.
   ```
   *Expected Output*: The MIDI will have cleaner note transitions, more consistent volume, and more precise rhythmic timing.

#### Understanding the Output MIDI File

After transformation, the output MIDI file will contain:

1. **Original Structure with Modifications**: The basic structure (tempo, time signature) is usually preserved unless specifically requested to change.

2. **New Musical Elements**: Depending on your prompt, you might see:
   - Different note patterns (for style changes)
   - New tracks or instruments (for arrangement changes)
   - Modified velocity values (for dynamic changes)
   - Altered note timing (for rhythmic changes)
   - Additional notes (for harmony enrichment)

3. **Metadata Changes**: The LLM might also modify track names, instrument names, or other metadata to reflect the requested changes.

To analyze the changes made by the LLM, you can:
- Use the `MidiUtils.getMidiFileInfo()` method to get information about the transformed file
- Compare the serialized text representations of the original and transformed files
- Listen to both files to hear the musical differences

## Command-Line Interface

The library provides a command-line interface for processing MIDI files and transforming them using LLMs.

### Standard Conversion Mode

```bash
java -jar midi-processor.jar <input-file>
```

The tool automatically detects the file type and performs the appropriate conversion:
- If the input is a MIDI file, it will be converted to a serialized text format
- If the input is a serialized text file, it will be converted to a MIDI file

Example:
```bash
java -jar midi-processor.jar input.mid
```

### LLM-based Transformation Mode

```bash
java -jar midi-processor.jar transform <input-file> <output-file> <llm-name> <api-key> <prompt>
```

Parameters:
- `<input-file>`: Path to a MIDI file (.mid)
- `<output-file>`: Path where the transformed MIDI file will be saved
- `<llm-name>`: Name of the LLM service to use (e.g., openai)
- `<api-key>`: API key for the LLM service
- `<prompt>`: Natural language prompt explaining the desired transformation

Examples:

1. Basic style transformation:
```bash
java -jar midi-processor.jar transform input.mid output.mid openai sk-your-api-key "Make this melody more jazzy by adding swing rhythm and seventh chords"
```

2. Change instrumentation:
```bash
java -jar midi-processor.jar transform piano.mid orchestra.mid openai sk-your-api-key "Convert this piano piece into a full orchestral arrangement with strings, brass, and woodwinds"
```

3. Fix technical issues:
```bash
java -jar midi-processor.jar transform rough.mid clean.mid openai sk-your-api-key "Clean up overlapping notes, normalize velocities to 70-90 range, and quantize rhythm to 16th notes"
```

The output MIDI file will contain the transformed music according to your prompt. You can analyze the changes by comparing the original and transformed files or by using the `MidiUtils.getMidiFileInfo()` method.

## Java API Usage Example

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

## Developer Guide for IntelliJ IDEA

This section provides guidance for developers who want to work with the codebase in IntelliJ IDEA.

### Setting Up the Project

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd rubberduck
   ```

2. **Import the Project**:
   - Open IntelliJ IDEA
   - Select "Open" or "Import Project"
   - Navigate to the project directory and select the `build.gradle` file
   - Choose "Open as Project"
   - Wait for the Gradle sync to complete

3. **Verify Project Structure**:
   - Ensure that the project is recognized as a Gradle project
   - Verify that the source directories are correctly marked:
     - `src` should be marked as sources root
     - `src/test` should be marked as test sources root

### Running the Application

1. **Run the Main Class**:
   - Navigate to `src/Main.java` in the Project view
   - Right-click on the file and select "Run 'Main.main()'"
   - Alternatively, open the file and click the green "Run" icon in the gutter next to the `main` method

2. **Configure Run Configurations**:
   - To run with command-line arguments:
     - Click "Run" > "Edit Configurations..."
     - Select the "Main" configuration or create a new "Application" configuration
     - In the "Program arguments" field, enter your arguments (e.g., `path/to/input.mid`)
     - Click "Apply" and "OK"
     - Run the configuration using the "Run" button in the toolbar

### Debugging the Application

1. **Set Breakpoints**:
   - Click in the gutter next to the line where you want to set a breakpoint
   - A red dot will appear, indicating a breakpoint is set

2. **Start Debugging**:
   - Instead of running the application, click the "Debug" icon or use "Run" > "Debug 'Main'"
   - The application will start and pause at your breakpoints
   - Use the Debug window to:
     - Step through code (F8 or Step Over, F7 or Step Into)
     - Examine variables (Variables tab)
     - Evaluate expressions (Evaluate Expression, Alt+F8)

3. **Debug with Arguments**:
   - Use the same configuration as for running with arguments, but click the "Debug" button instead

### Working with MIDI Files

1. **Viewing MIDI Files**:
   - IntelliJ IDEA doesn't have a built-in MIDI viewer
   - Use external tools like GarageBand, MuseScore, or MIDI players to view and listen to MIDI files

2. **Testing MIDI Processing**:
   - Run the tests in `src/test` to verify MIDI processing functionality
   - Create custom test cases for specific scenarios

### Troubleshooting

1. **Gradle Sync Issues**:
   - If Gradle sync fails, try:
     - "File" > "Invalidate Caches / Restart..."
     - Updating Gradle in the Gradle wrapper properties
     - Checking your JDK configuration in "File" > "Project Structure"

2. **Run/Debug Configuration Issues**:
   - Ensure the correct main class is selected (`Main`)
   - Verify the working directory is set to the project root
   - Check that the JDK is properly configured

3. **MIDI-Related Issues**:
   - If MIDI playback doesn't work, ensure your system has proper MIDI support
   - For serialization/deserialization issues, check the format of your input files

### Using the CLI Tool

The application can be used as a command-line tool in two modes: standard conversion mode and LLM-based transformation mode.

#### Standard Conversion Mode

To convert between MIDI files and serialized text format:

```bash
./gradlew run --args="<input-file>"
```

Where `<input-file>` is the path to either a MIDI file or a serialized text file. The tool automatically detects the file type and performs the appropriate conversion:

- If the input is a MIDI file, it will be converted to a serialized text format (with a `.txt` extension)
- If the input is a serialized text file, it will be converted to a MIDI file (with a `.mid` extension)

The output file will be created in the same directory as the input file, with the same base name but a different extension.

Examples:

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

#### LLM-based Transformation Mode

To transform a MIDI file using an LLM:

```bash
./gradlew run --args="transform <input-file> <output-file> <llm-name> <api-key> <prompt>"
```

Parameters:
- `<input-file>`: Path to a MIDI file (.mid)
- `<output-file>`: Path where the transformed MIDI file will be saved
- `<llm-name>`: Name of the LLM service to use (e.g., openai)
- `<api-key>`: API key for the LLM service
- `<prompt>`: Natural language prompt explaining the desired transformation

Examples:

Transform a melody to a jazz style:

```bash
./gradlew run --args="transform path/to/input.mid path/to/output.mid openai sk-your-api-key \"Make this melody more jazzy with swing rhythm\""
```

Change the key and add harmonization:

```bash
./gradlew run --args="transform path/to/melody.mid path/to/harmonized.mid openai sk-your-api-key \"Transpose to D minor and add a harmonized countermelody\""
```

Note: When using Gradle to run the application with complex arguments containing spaces and special characters, make sure to properly escape quotes and other special characters according to your operating system's shell requirements.

#### Building a Standalone JAR

To create a standalone JAR file that can be run directly with Java:

```bash
./gradlew jar
```

Then you can run the CLI tool with:

```bash
java -jar build/libs/rubberduck-1.0-SNAPSHOT.jar <input-file>
```
