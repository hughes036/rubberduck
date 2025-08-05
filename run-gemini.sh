#!/bin/bash

# Set JAVA_HOME to Java 21
export JAVA_HOME=/Users/hughes/.sdkman/candidates/java/current

# Check if apikey.txt exists
if [ ! -f "./apikey.txt" ]; then
    echo "Error: apikey.txt file not found!"
    echo "Please create apikey.txt with your Gemini API key"
    exit 1
fi

# Export the API key from file
export GOOGLE_API_KEY=$(cat ./apikey.txt)

echo "Running Gemini Hello World example..."
./gradlew runGemini
