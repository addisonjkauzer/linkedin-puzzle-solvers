package com.Utils;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;

public abstract class PuzzleWebInterface<P> {

    protected abstract PuzzleType getPuzzleType();

    protected abstract String getBoardSelector();

    protected abstract P parsePuzzle(String html);

    /** Called after parsing and recorder start, before handing control to the task. Override for puzzle-specific setup. */
    protected void setupAfterParse(Actions actions) {}

    public void withPuzzle(PuzzleTask<P> task) {
        withPuzzle(task, false);
    }

    public void withPuzzle(PuzzleTask<P> task, boolean record) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--ozone-platform=headless", "--window-size=1920,1080", "--disable-dev-shm-usage", "--disable-gpu", "--no-zygote");
        WebDriver driver = new ChromeDriver(options);
        ScreenRecorder recorder = null;
        try {
            driver.get(getPuzzleType().getUrl());

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

            // Switch into the game iframe
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    By.cssSelector("iframe.game-launch-page__iframe")));

            // Wait for the puzzle board to render
            WebElement board = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(getBoardSelector())));

            P puzzle = parsePuzzle(board.getAttribute("outerHTML"));

            Actions actions = new Actions(driver);

            if (record) {
                try {
                    recorder = ScreenRecorder.start(getPuzzleType().name(), driver);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            setupAfterParse(actions);

            task.accept(puzzle, actions, driver, recorder);
        } finally {
            if (recorder != null) recorder.stopAndUpload();
            driver.quit();
        }
    }

    protected void navigateTo(Actions actions, int[] currentPos, int targetRow, int targetCol) {
        int rowDiff = targetRow - currentPos[0];
        int colDiff = targetCol - currentPos[1];

        Keys verticalKey = rowDiff > 0 ? Keys.ARROW_DOWN : Keys.ARROW_UP;
        for (int i = 0; i < Math.abs(rowDiff); i++) {
            actions.sendKeys(verticalKey).perform();
        }

        Keys horizontalKey = colDiff > 0 ? Keys.ARROW_RIGHT : Keys.ARROW_LEFT;
        for (int i = 0; i < Math.abs(colDiff); i++) {
            actions.sendKeys(horizontalKey).perform();
        }
    }

    protected Keys toArrowKey(int rowDiff, int colDiff) {
        if (rowDiff == -1) return Keys.ARROW_UP;
        if (rowDiff == 1) return Keys.ARROW_DOWN;
        if (colDiff == -1) return Keys.ARROW_LEFT;
        if (colDiff == 1) return Keys.ARROW_RIGHT;
        throw new IllegalArgumentException("Invalid move: rowDiff=" + rowDiff + ", colDiff=" + colDiff);
    }
}
