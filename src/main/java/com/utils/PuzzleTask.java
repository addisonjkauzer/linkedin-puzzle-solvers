package com.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

@FunctionalInterface
public interface PuzzleTask<P> {
    void accept(P puzzle, Actions actions, WebDriver driver, ScreenRecorder recorder);
}
