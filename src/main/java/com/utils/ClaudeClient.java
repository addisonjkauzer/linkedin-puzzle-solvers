package com.utils;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.InternalServerException;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

public class ClaudeClient {

    private static final long DEFAULT_MAX_TOKENS = 16000L;
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 5000L;

    private final AnthropicClient client;

    public ClaudeClient() {
        String paramName = System.getenv("ANTHROPIC_API_KEY_PARAM");
        String apiKey = SsmClient.create()
                .getParameter(GetParameterRequest.builder()
                        .name(paramName)
                        .withDecryption(true)
                        .build())
                .parameter()
                .value();
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public ClaudeClient(String apiKey) {
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public String ask(String prompt) {
        return withRetry(() -> client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_OPUS_4_6)
                        .maxTokens(DEFAULT_MAX_TOKENS)
                        .addUserMessage(prompt)
                        .build()));
    }

    public String ask(String systemPrompt, String userPrompt) {
        return withRetry(() -> client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_OPUS_4_6)
                        .maxTokens(DEFAULT_MAX_TOKENS)
                        .system(systemPrompt)
                        .addUserMessage(userPrompt)
                        .build()));
    }

    private String withRetry(java.util.function.Supplier<Message> call) {
        long backoff = INITIAL_BACKOFF_MS;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                Message response = call.get();
                return response.content().stream()
                        .flatMap(block -> block.text().stream())
                        .map(textBlock -> textBlock.text())
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No text content in Claude response"));
            } catch (InternalServerException e) {
                if (attempt == MAX_RETRIES) throw e;
                System.err.println("Claude API overloaded (attempt " + (attempt + 1) + "), retrying in " + backoff + "ms: " + e.getMessage());
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                backoff *= 2;
            }
        }
        throw new RuntimeException("Unreachable");
    }
}
