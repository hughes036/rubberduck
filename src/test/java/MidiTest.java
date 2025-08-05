package test;

import midi.MidiSerializer;
import midi.MidiDeserializer;
import midi.MidiUtils;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;

/**
 * Simple test class for the MIDI processing library.
 * This class contains methods to test the functionality of the MidiSerializer,
 * MidiDeserializer, and MidiUtils classes.
 */
public class MidiTest {
    
    private static boolean allTestsPassed = true;
    
    /**
     * Main method to run all tests.
     */
    public static void main(String[] args) {
        try {
            System.out.println("Running MIDI library tests...");
            
            testSerializeAndDeserialize();
            testSerializeSequence();
            testDeserializeToSequence();
            testDeserializeInvalidFormat();
            testMidiUtils();
            
            if (allTestsPassed) {
                System.out.println("All tests passed!");
            } else {
                System.out.println("Some tests failed. See above for details.");
            }
        } catch (Exception e) {
            System.err.println("Test execution failed with exception: " + e.getMessage());
            e.printStackTrace();
            allTestsPassed = false;
        }
    }
    
    /**
     * Test serializing a MIDI file and then deserializing it back.
     */
    private static void testSerializeAndDeserialize() throws Exception {
        System.out.println("\nTest: Serialize and Deserialize");
        
        // Create a test MIDI file with a single note
        Sequence sequence = MidiUtils.createSingleNoteSequence(60, 100, 480);
        File testMidiFile = File.createTempFile("test", ".mid");
        MidiSystem.write(sequence, 1, testMidiFile);
        
        // Create a temporary file for output
        File outputMidiFile = File.createTempFile("output", ".mid");
        
        try {
            // Serialize the MIDI file
            String serialized = MidiSerializer.serializeMidiFile(testMidiFile);
            
            // Print a sample of the serialized data
            System.out.println("Serialized MIDI data sample:");
            System.out.println(serialized.substring(0, Math.min(serialized.length(), 200)) + "...");
            
            // Deserialize back to a MIDI file
            MidiDeserializer.deserializeToMidiFile(serialized, outputMidiFile);
            
            // Verify the output file exists
            assertTrue("Output MIDI file should exist", outputMidiFile.exists());
            
            // Load both sequences for comparison
            Sequence originalSequence = MidiSystem.getSequence(testMidiFile);
            Sequence deserializedSequence = MidiSystem.getSequence(outputMidiFile);
            
            // Compare basic properties
            assertEquals("Division type should match", 
                    originalSequence.getDivisionType(), deserializedSequence.getDivisionType(), 0.001f);
            assertEquals("Resolution should match", 
                    originalSequence.getResolution(), deserializedSequence.getResolution());
            
            // Compare track count
            assertEquals("Track count should match", 
                    originalSequence.getTracks().length, deserializedSequence.getTracks().length);
            
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
            
            assertTrue("Deserialized sequence should contain a NOTE_ON event for note 60", foundNoteOn);
            assertTrue("Deserialized sequence should contain a NOTE_OFF event for note 60", foundNoteOff);
            
            System.out.println("Test passed: Serialize and Deserialize");
        } finally {
            // Clean up temporary files
            if (testMidiFile != null && testMidiFile.exists()) {
                testMidiFile.delete();
            }
            if (outputMidiFile != null && outputMidiFile.exists()) {
                outputMidiFile.delete();
            }
        }
    }
    
    /**
     * Test serializing a MIDI sequence.
     */
    private static void testSerializeSequence() throws Exception {
        System.out.println("\nTest: Serialize Sequence");
        
        // Create a sequence with a single note
        Sequence sequence = MidiUtils.createSingleNoteSequence(60, 100, 480);
        
        // Serialize the sequence
        String serialized = MidiSerializer.serializeSequence(sequence);
        
        // Verify the serialized data contains expected information
        assertTrue("Serialized data should contain MIDI_HEADER", serialized.contains("MIDI_HEADER"));
        assertTrue("Serialized data should contain TRACKS", serialized.contains("TRACKS"));
        assertTrue("Serialized data should contain NOTE_ON", serialized.contains("NOTE_ON"));
        assertTrue("Serialized data should contain note=60", serialized.contains("note=60"));
        
        System.out.println("Test passed: Serialize Sequence");
    }
    
    /**
     * Test deserializing a serialized MIDI sequence.
     */
    private static void testDeserializeToSequence() throws Exception {
        System.out.println("\nTest: Deserialize to Sequence");
        
        // Serialize a simple sequence
        Sequence originalSequence = MidiUtils.createSingleNoteSequence(60, 100, 480);
        String serialized = MidiSerializer.serializeSequence(originalSequence);
        
        // Deserialize back to a sequence
        Sequence deserializedSequence = MidiDeserializer.deserializeToSequence(serialized);
        
        // Verify basic properties
        assertEquals("Division type should match", 
                originalSequence.getDivisionType(), deserializedSequence.getDivisionType(), 0.001f);
        assertEquals("Resolution should match", 
                originalSequence.getResolution(), deserializedSequence.getResolution());
        
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
        
        assertTrue("Deserialized sequence should contain a NOTE_ON event for note 60", foundNoteOn);
        
        System.out.println("Test passed: Deserialize to Sequence");
    }
    
    /**
     * Test deserializing an invalid format.
     */
    private static void testDeserializeInvalidFormat() {
        System.out.println("\nTest: Deserialize Invalid Format");
        
        try {
            // Try to deserialize an invalid format
            MidiDeserializer.deserializeToSequence("This is not a valid serialized MIDI format");
            
            // If we get here, the test failed
            fail("Should have thrown InvalidMidiDataException for invalid format");
        } catch (InvalidMidiDataException e) {
            // Expected exception
            System.out.println("Test passed: Deserialize Invalid Format (caught expected exception)");
        } catch (Exception e) {
            // Unexpected exception
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    /**
     * Test MidiUtils methods.
     */
    private static void testMidiUtils() {
        System.out.println("\nTest: MidiUtils");
        
        // Test getNoteName
        assertEquals("Note name for 60 should be C4", "C4", MidiUtils.getNoteName(60));
        assertEquals("Note name for 69 should be A4", "A4", MidiUtils.getNoteName(69));
        
        // Test getNoteFrequency
        assertEquals("Frequency for A4 (69) should be 440Hz", 440.0, MidiUtils.getNoteFrequency(69), 0.01);
        
        // Test frequencyToNoteNumber
        assertEquals("Note number for 440Hz should be 69", 69, MidiUtils.frequencyToNoteNumber(440.0));
        
        System.out.println("Test passed: MidiUtils");
    }
    
    // Simple assertion methods
    
    private static void assertTrue(String message, boolean condition) {
        if (!condition) {
            fail(message);
        }
    }
    
    private static void assertEquals(String message, float expected, float actual, float delta) {
        if (Math.abs(expected - actual) > delta) {
            fail(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void assertEquals(String message, int expected, int actual) {
        if (expected != actual) {
            fail(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void assertEquals(String message, String expected, String actual) {
        if (!expected.equals(actual)) {
            fail(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void assertEquals(String message, double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) {
            fail(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    
    private static void fail(String message) {
        System.err.println("TEST FAILED: " + message);
        allTestsPassed = false;
    }
}