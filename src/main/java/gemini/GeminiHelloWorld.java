package gemini;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

/**
 * Hello world example using Gemini AI!
 * Based on the example from ai-sandbox repo.
 */
public class GeminiHelloWorld {

    public static void main(String[] args) {
        // The client gets the API key from the environment variable `GEMINI_API_KEY`.
        // export GEMINI_API_KEY=$(cat ./apikey.txt)
        Client client = new Client();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash",
                        "What name is mack short for. What famous people were named mack. Who used the nickname mackie?",
                        null);

        System.out.println(response.text());
    }
}
