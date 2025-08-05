#!/bin/bash

# Set JAVA_HOME to Java 21
export JAVA_HOME=/Users/hughes/.sdkman/candidates/java/current

# Check if apikey.txt exists
if [ ! -f "./apikey.txt" ]; then
    echo "Error: apikey.txt file not found!"
    echo "Please create apikey.txt with your Gemini API key"
    echo "You can copy it from ../ai-sandbox/apikey.txt if it exists"
    exit 1
fi

# Export the API key from file
export GEMINI_API_KEY=$(cat ./apikey.txt)
export GOOGLE_API_KEY=$(cat ./apikey.txt)

echo "Building project with Maven..."
mvn clean compile

echo "Running Gemini Hello World example..."
mvn exec:java

echo "Application finished running"