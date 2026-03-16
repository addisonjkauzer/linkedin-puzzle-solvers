package com.Utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PuzzleType {
    ZIP("https://www.linkedin.com/games/zip/"),
    SUDOKU("https://www.linkedin.com/games/mini-sudoku/"),
    TANGO("https://www.linkedin.com/games/tango");

    private final String url;
}

