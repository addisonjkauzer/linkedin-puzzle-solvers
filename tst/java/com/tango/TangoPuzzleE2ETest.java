package com.tango;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TangoPuzzleE2ETest {

    @Autowired
    private TangoPuzzleWebInterface tangoPuzzleWebInterface;

    @Test
    void solvePuzzle() {
        tangoPuzzleWebInterface.fetchAndSubmit();
    }

    @Test
    void visualizeAlgorithm() {
        tangoPuzzleWebInterface.visualizeAlgorithm(false);
    }
}
