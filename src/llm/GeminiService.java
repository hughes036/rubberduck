package llm;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiService implements LlmService {

    @Override
    public String generate(String prompt, String apiKey) throws Exception {
        // The client gets the API key from the environment variable `GEMINI_API_KEY`.
        // export GEMINI_API_KEY=$(cat ./apikey.txt)
        Client client = new Client();

        GenerateContentResponse response =
            client.models.generateContent(
                "gemini-1.5-flash",
                prompt,
                null);

        return response.text();
    }
}
