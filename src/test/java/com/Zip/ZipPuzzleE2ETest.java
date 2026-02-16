package com.Zip;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ZipPuzzleE2ETest {

    @Autowired
    private ZipPuzzleWebInterface zipPuzzleWebInterface;

    @Test
    void solveAndSubmitZipPuzzle() {
        zipPuzzleWebInterface.fetchAndSubmit();
    }

    @Test
    void visualizeAlgorithm() {
        zipPuzzleWebInterface.visualizeAlgorithm(false);
    }

    @Test
    void visualizeMultiThreadAlgorithm() {
        zipPuzzleWebInterface.visualizeAlgorithm(true);
    }

}
