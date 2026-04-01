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

    protected int lastGuessCount = 0;

    protected abstract PuzzleType getPuzzleType();

    protected abstract String getBoardSelector();

    protected abstract P parsePuzzle(String html);

    /** Called after parsing and recorder start, before handing control to the task. Override for puzzle-specific setup. */
    protected void setupAfterParse(Actions actions) {}

    public void withPuzzle(PuzzleTask<P> task) {
        withPuzzle(task, false);
    }

    public void withPuzzle(PuzzleTask<P> task, boolean record) {
        final String puzzleType = getPuzzleType().name();
        final long globalStart = System.currentTimeMillis();
        log(puzzleType, "Starting lambda execution", globalStart);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--ozone-platform=headless", "--window-size=1920,1080", "--disable-dev-shm-usage", "--disable-gpu", "--no-zygote");
        WebDriver driver = new ChromeDriver(options);
        log(puzzleType, "Chrome driver created", globalStart);

        ScreenRecorder recorder = null;
        try {
            driver.get(getPuzzleType().getUrl());
            log(puzzleType, "Navigated to URL", globalStart);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

            // Switch into the game iframe
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    By.cssSelector("iframe.game-launch-page__iframe")));
            log(puzzleType, "Switched into game iframe", globalStart);

            // Wait for the puzzle board to render
            WebElement board = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(getBoardSelector())));
            log(puzzleType, "Board element found", globalStart);

            P puzzle = parsePuzzle(board.getAttribute("outerHTML"));
            log(puzzleType, "Puzzle parsed", globalStart);

            Actions actions = new Actions(driver);

            if (record) {
                try {
                    recorder = ScreenRecorder.start(getPuzzleType().name(), driver);
                    log(puzzleType, "Screen recorder started", globalStart);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            setupAfterParse(actions);
            log(puzzleType, "Setup after parse complete", globalStart);

            long startTime = System.currentTimeMillis();
            task.accept(puzzle, actions, driver, recorder);
            long solveTimeMs = System.currentTimeMillis() - startTime;
            log(puzzleType, "Task complete, solveTimeMs=" + solveTimeMs, globalStart);

            MetricsPublisher.publishSolveTime(getPuzzleType().name(), solveTimeMs);
            if (lastGuessCount > 0) {
                MetricsPublisher.publishGuessCount(getPuzzleType().name(), lastGuessCount);
            }
            MetadataWriter.appendEntry(getPuzzleType().name(), solveTimeMs, lastGuessCount);
            log(puzzleType, "Metrics published", globalStart);
        } finally {
            if (recorder != null) {
                long t = System.currentTimeMillis();
                recorder.finalizeCapture();
                log(puzzleType, "Recording finalized, finalizeMs=" + (System.currentTimeMillis() - t), globalStart);
            }
            driver.quit();
            log(puzzleType, "Driver quit", globalStart);
            if (recorder != null) {
                long t = System.currentTimeMillis();
                recorder.stopAndUpload();
                log(puzzleType, "Recording uploaded, uploadMs=" + (System.currentTimeMillis() - t), globalStart);
            }
            log(puzzleType, "Lambda execution complete, totalMs=" + (System.currentTimeMillis() - globalStart), globalStart);
        }
    }

    private static void log(String puzzleType, String message, long globalStart) {
        System.out.printf("[%s] +%dms | %s%n", puzzleType, System.currentTimeMillis() - globalStart, message);
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
