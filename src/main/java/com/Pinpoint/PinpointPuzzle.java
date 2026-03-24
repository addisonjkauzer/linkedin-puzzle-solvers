package com.Pinpoint;

import com.Utils.ClaudeClient;
import lombok.Getter;

import java.util.List;

@Getter
public class PinpointPuzzle {

    private final List<String> clues;
    private final ClaudeClient claude;

    public PinpointPuzzle(List<String> clues) {
        // For local testing via E2E test add your claude API key here
        this.claude = new ClaudeClient();
        this.clues = clues;
    }

    public String solve() {
        return claude.ask("You are playing LinkedIn's Pinpoint puzzle. Each clue is a word that belongs to a " +
                "hidden category. Given these clues: " + String.join(", ", clues) + " — respond with only " +
                "the single category word. No explanation.");

    }
}
