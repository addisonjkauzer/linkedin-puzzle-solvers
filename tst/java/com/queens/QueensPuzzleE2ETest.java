package com.queens;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QueensPuzzleE2ETest {

    @Autowired
    private QueensPuzzleWebInterface queensPuzzleWebInterface;

    @Test
    void solvePuzzle() {
        queensPuzzleWebInterface.fetchAndSubmit();
    }

    @Test
    void visualizeAlgorithm() {
        queensPuzzleWebInterface.visualizeAlgorithm(false);
    }
}
