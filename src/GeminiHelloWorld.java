package com.example.demo;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

/**
 * Hello world!
 */
public class GeminiHelloWorld {

  public static void main(String[] args) {
    // The client gets the API key from the environment variable `GEMINI_API_KEY`.
    // export GEMINI_API_KEY=$(cat ./apikey.txt)
    Client client = new Client();

    GenerateContentResponse response =
        client.models.generateContent(
            "gemini-1.5-flash",
            "What name is mack short for.  what famous people were named mack.  who used the nickname mackie?",
            null);

    System.out.println(response.text());
  }
}
