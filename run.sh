#!/bin/bash

# Set JAVA_HOME to Java 21
export JAVA_HOME=/Users/hughes/.sdkman/candidates/java/current

echo "Building project with Gradle..."
./gradlew clean build

echo "Running MIDI Processing CLI..."
./gradlew run

echo "Application finished running"