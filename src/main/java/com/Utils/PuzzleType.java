package com.Utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PuzzleType {
    ZIP("https://www.linkedin.com/games/zip/");

    private final String url;
}

