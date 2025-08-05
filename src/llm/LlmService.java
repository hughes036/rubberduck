package llm;

public interface LlmService {
    String generate(String prompt, String apiKey) throws Exception;
}
