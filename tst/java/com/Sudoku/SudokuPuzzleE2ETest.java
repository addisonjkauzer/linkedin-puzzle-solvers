package com.Sudoku;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SudokuPuzzleE2ETest {

    @Autowired
    private SudokuPuzzleWebInterface sudokuPuzzleWebInterface;

    @Test
    void solvePuzzle() {
        sudokuPuzzleWebInterface.fetchAndSubmit();
    }
    @Test
    void visualizeAlgorithm() {
        sudokuPuzzleWebInterface.visualizeAlgorithm();
    }
}
