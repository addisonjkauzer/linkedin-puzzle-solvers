package com.Utils;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;

public class ClaudeClient {

    private static final long DEFAULT_MAX_TOKENS = 16000L;

    private final AnthropicClient client;

    public ClaudeClient() {
        this.client = AnthropicOkHttpClient.fromEnv();
    }

    public ClaudeClient(String apiKey) {
        this.client = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public String ask(String prompt) {
        Message response = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_OPUS_4_6)
                        .maxTokens(DEFAULT_MAX_TOKENS)
                        .addUserMessage(prompt)
                        .build());

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No text content in Claude response"));
    }

    public String ask(String systemPrompt, String userPrompt) {
        Message response = client.messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_OPUS_4_6)
                        .maxTokens(DEFAULT_MAX_TOKENS)
                        .system(systemPrompt)
                        .addUserMessage(userPrompt)
                        .build());

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No text content in Claude response"));
    }
}
