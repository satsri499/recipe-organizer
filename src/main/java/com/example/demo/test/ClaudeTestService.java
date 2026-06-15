package com.example.demo.test;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ClaudeTestService implements CommandLineRunner {

    private final AnthropicClient anthropicClient;

    public ClaudeTestService(AnthropicClient anthropicClient) {
        this.anthropicClient = anthropicClient;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Testing Claude API connection...");

        Message response = anthropicClient.messages().create(
                MessageCreateParams.builder()
                        .model("claude-haiku-4-5")
                        .maxTokens(100L)
                        .addUserMessage("Say hello in one sentence")
                        .build()
        );
        String text = response.content().get(0).text().get().text();
        System.out.println("Claude says: " + text);
    }
}
