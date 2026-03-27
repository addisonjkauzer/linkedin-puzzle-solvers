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
        final String guess = claude.ask("You are playing LinkedIn's Pinpoint puzzle. Each clue is a word that belongs to a " +
                "hidden category. It's possible that the clues are a prefix or suffix to the category word. Given " +
                "these clues: " + String.join(", ", clues) + " — respond with only " +
                "the single category word. No explanation. Do not guess these words: " + alreadyGuessed);
        alreadyGuessed.add(guess);
        return guess;
    }
}
