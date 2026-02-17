package com.Sudoku;

import com.Utils.PuzzleType;
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.BiConsumer;

@Component
@AllArgsConstructor
public class SudokuPuzzleWebInterface {

    private static final String URL = PuzzleType.SUDOKU.getUrl();
    private static final int SIZE = 6;

    private final SudokuPuzzleParser sudokuPuzzleParser;

    public void withPuzzle(BiConsumer<SudokuPuzzle, Actions> task) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--start-maximized");
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

            // Switch into the game iframe
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    By.cssSelector("iframe.game-launch-page__iframe")));

            // Wait for the sudoku grid to render
            WebElement board = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".grid-game-board")));

            // Parse the puzzle from the live board
            String html = board.getAttribute("outerHTML");
            int[][] puzzle = sudokuPuzzleParser.parse(html);
            final SudokuPuzzle sudokuPuzzle = new SudokuPuzzle(puzzle);

            // Use Actions to send keys directly to the browser
            Actions actions = new Actions(driver);

            // Press Tab then Enter to get past the landing page and start the puzzle
            actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
            actions.sendKeys(Keys.ENTER).perform();

            task.accept(sudokuPuzzle, actions);

            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            driver.quit();
        }
    }

    public void fetchAndSubmit() {
        withPuzzle((sudokuPuzzle, actions) -> {
            sudokuPuzzle.solve();
            int[][] solution = sudokuPuzzle.getBoard();

            // Press right arrow to highlight the top-left cell
            actions.sendKeys(Keys.ARROW_RIGHT).perform();

            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    // Enter the solution value for every cell
                    actions.sendKeys(String.valueOf(solution[row][col])).perform();

                    // Navigate to next cell (right), except at end of row
                    if (col < SIZE - 1) {
                        actions.sendKeys(Keys.ARROW_RIGHT).perform();
                    }
                }
                // Move down to next row and back to the left edge
                if (row < SIZE - 1) {
                    actions.sendKeys(Keys.ARROW_DOWN).perform();
                    for (int i = 0; i < SIZE - 1; i++) {
                        actions.sendKeys(Keys.ARROW_LEFT).perform();
                    }
                }
            }
        });
    }

    public void visualizeAlgorithm() {
        withPuzzle((sudokuPuzzle, actions) -> {
            // Press right arrow to highlight the top-left cell
            actions.sendKeys(Keys.ARROW_RIGHT).perform();

            final int[] currentPos = {0, 0};
            final boolean[][] filled = new boolean[SIZE][SIZE];

            sudokuPuzzle.solve(cell -> {
                int targetRow = cell[0];
                int targetCol = cell[1];
                int value = cell[2];

                // Navigate from current position to the target cell
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

                // Only type the value if this cell was newly solved
                if (value != 0 && !filled[targetRow][targetCol]) {
                    actions.sendKeys(String.valueOf(value)).perform();
                    filled[targetRow][targetCol] = true;
                }

                currentPos[0] = targetRow;
                currentPos[1] = targetCol;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}
