import midi.MidiSerializer;
import midi.MidiDeserializer;
import midi.MidiUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

/**
 * JUnit tests for the MIDI processing library.
 * This class contains tests for the functionality of the MidiSerializer,
 * MidiDeserializer, and MidiUtils classes.
 */
public class MidiSerializerTest {

    private File testMidiFile;
    private File outputMidiFile;

    @BeforeEach
    public void setUp() throws Exception {
        // Create a test MIDI file with a single note for use in tests
        Sequence sequence = MidiUtils.createSingleNoteSequence(60, 100, 480);
        testMidiFile = File.createTempFile("test", ".mid");
        MidiSystem.write(sequence, 1, testMidiFile);

        // Create a temporary file for output
        outputMidiFile = File.createTempFile("output", ".mid");
    }

    @AfterEach
    public void tearDown() {
        // Clean up temporary files
        if (testMidiFile != null && testMidiFile.exists()) {
            testMidiFile.delete();
        }
        if (outputMidiFile != null && outputMidiFile.exists()) {
            outputMidiFile.delete();
        }
    }

    @Test
    @DisplayName("Test serializing a MIDI file and then deserializing it back")
    public void testSerializeAndDeserialize() throws Exception {
        // Serialize the MIDI file
        String serialized = MidiSerializer.serializeMidiFile(testMidiFile);

        // Print a sample of the serialized data
        System.out.println("Serialized MIDI data sample:");
        System.out.println(serialized.substring(0, Math.min(serialized.length(), 200)) + "...");

        // Deserialize back to a MIDI file
        MidiDeserializer.deserializeToMidiFile(serialized, outputMidiFile);

        // Verify the output file exists
        assertTrue(outputMidiFile.exists(), "Output MIDI file should exist");

        // Load both sequences for comparison
        Sequence originalSequence = MidiSystem.getSequence(testMidiFile);
        Sequence deserializedSequence = MidiSystem.getSequence(outputMidiFile);

        // Compare basic properties
        assertEquals(originalSequence.getDivisionType(), deserializedSequence.getDivisionType(), 0.001f,
                "Division type should match");
        assertEquals(originalSequence.getResolution(), deserializedSequence.getResolution(),
                "Resolution should match");

        // Compare track count
        assertEquals(originalSequence.getTracks().length, deserializedSequence.getTracks().length,
                "Track count should match");

        // Check that the deserialized sequence has the expected note events
        Track deserializedTrack = deserializedSequence.getTracks()[0];
        boolean foundNoteOn = false;
        boolean foundNoteOff = false;

        for (int i = 0; i < deserializedTrack.size(); i++) {
            MidiEvent event = deserializedTrack.get(i);
            MidiMessage message = event.getMessage();

            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;

                if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData1() == 60) {
                    foundNoteOn = true;
                } else if (sm.getCommand() == ShortMessage.NOTE_OFF && sm.getData1() == 60) {
                    foundNoteOff = true;
                }
            }
        }

        assertTrue(foundNoteOn, "Deserialized sequence should contain a NOTE_ON event for note 60");
        assertTrue(foundNoteOff, "Deserialized sequence should contain a NOTE_OFF event for note 60");
    }

    @Test
    @DisplayName("Test serializing a MIDI sequence")
    public void testSerializeSequence() throws Exception {
        // Create a sequence with a single note
        Sequence sequence = MidiUtils.createSingleNoteSequence(60, 100, 480);

        // Serialize the sequence
        String serialized = MidiSerializer.serializeSequence(sequence);

        // Verify the serialized data contains expected information
        assertTrue(serialized.contains("MIDI_HEADER"), "Serialized data should contain MIDI_HEADER");
        assertTrue(serialized.contains("TRACKS"), "Serialized data should contain TRACKS");
        assertTrue(serialized.contains("NOTE_ON"), "Serialized data should contain NOTE_ON");
        assertTrue(serialized.contains("note=60"), "Serialized data should contain note=60");
    }

    @Test
    @DisplayName("Test deserializing a serialized MIDI sequence")
    public void testDeserializeToSequence() throws Exception {
        // Serialize a simple sequence
        Sequence originalSequence = MidiUtils.createSingleNoteSequence(60, 100, 480);
        String serialized = MidiSerializer.serializeSequence(originalSequence);

        // Deserialize back to a sequence
        Sequence deserializedSequence = MidiDeserializer.deserializeToSequence(serialized);

        // Verify basic properties
        assertEquals(originalSequence.getDivisionType(), deserializedSequence.getDivisionType(), 0.001f,
                "Division type should match");
        assertEquals(originalSequence.getResolution(), deserializedSequence.getResolution(),
                "Resolution should match");

        // Verify the sequence contains the expected note events
        Track deserializedTrack = deserializedSequence.getTracks()[0];
        boolean foundNoteOn = false;

        for (int i = 0; i < deserializedTrack.size(); i++) {
            MidiEvent event = deserializedTrack.get(i);
            MidiMessage message = event.getMessage();

            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;

                if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData1() == 60) {
                    foundNoteOn = true;
                    break;
                }
            }
        }

        assertTrue(foundNoteOn, "Deserialized sequence should contain a NOTE_ON event for note 60");
    }

    @Test
    @DisplayName("Test deserializing an invalid format")
    public void testDeserializeInvalidFormat() {
        // Try to deserialize an invalid format
        Exception exception = assertThrows(InvalidMidiDataException.class, () -> {
            MidiDeserializer.deserializeToSequence("This is not a valid serialized MIDI format");
        }, "Should have thrown InvalidMidiDataException for invalid format");
    }

    @Test
    @DisplayName("Test MidiUtils methods")
    public void testMidiUtils() {
        // Test getNoteName
        assertEquals("C4", MidiUtils.getNoteName(60), "Note name for 60 should be C4");
        assertEquals("A4", MidiUtils.getNoteName(69), "Note name for 69 should be A4");

        // Test getNoteFrequency
        assertEquals(440.0, MidiUtils.getNoteFrequency(69), 0.01, "Frequency for A4 (69) should be 440Hz");

        // Test frequencyToNoteNumber
        assertEquals(69, MidiUtils.frequencyToNoteNumber(440.0), "Note number for 440Hz should be 69");
    }
}
