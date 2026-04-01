package com.pinpoint;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PinpointPuzzleE2ETest {

    @Autowired
    private PinpointPuzzleWebInterface pinpointPuzzleWebInterface;

    @Test
    void visualizeAlgorithm() {
        pinpointPuzzleWebInterface.visualizeAlgorithm(false);
    }
}
