package com.zip;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ZipPuzzleE2ETest {

    @Autowired
    private ZipPuzzleWebInterface zipPuzzleWebInterface;

    @Test
    void solvePuzzle() {
        zipPuzzleWebInterface.fetchAndSubmit();
    }

    @Test
    void visualizeAlgorithm() {
        zipPuzzleWebInterface.visualizeAlgorithm(false, false);
    }

    @Test
    void visualizeOptimizedAlgorithm() {
        zipPuzzleWebInterface.visualizeAlgorithm(true, false);
    }

}
