package com.Pinpoint;

import com.Utils.ClaudeClient;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class PinpointPuzzle {

    private final List<String> clues;
    private final ClaudeClient claude;
    private final Set<String> alreadyGuessed;

    public PinpointPuzzle(List<String> clues) {
        this.claude = new ClaudeClient(); // For local testing via E2E test add your claude API key here
        this.clues = clues;
        this.alreadyGuessed = new HashSet<>();
    }

    public String solve() {
        final String guess = claude.ask("You are playing LinkedIn's Pinpoint puzzle. Each clue is a word " +
                "that belongs to a hidden category." +
                "\n * The category can be a word that can precede or follow every clue word. " +
                "\n * These are the current clues: " + String.join(", ", clues) +
                "\n * Respond with only a single category word. No explanation. " +
                "\n * Do not guess these words: " + alreadyGuessed + ", " + String.join(", ", clues));
        alreadyGuessed.add(guess);
        return guess;
    }
}
