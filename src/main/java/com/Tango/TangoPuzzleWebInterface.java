package com.Tango;

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
public class TangoPuzzleWebInterface {

    private static final String URL = PuzzleType.TANGO.getUrl();

    private final TangoPuzzleParser tangoPuzzleParser;

    public void fetchAndSubmit() {
        withPuzzle((puzzle, actions) -> {
            int rows = puzzle.getBoard().length;
            int cols = puzzle.getBoard()[0].length;

            boolean[][] wasEmpty = new boolean[rows][cols];
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    wasEmpty[row][col] = puzzle.getBoard()[row][col] == 0;
                }
            }

            puzzle.solve();

            final int[] currentPos = {0, 0};
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (wasEmpty[row][col] && puzzle.getBoard()[row][col] != 0) {
                        navigateTo(actions, currentPos, row, col);
                        pressEnter(actions, puzzle.getBoard()[row][col]);
                        currentPos[0] = row;
                        currentPos[1] = col;
                    }
                }
            }
        });
    }

    public void visualizeAlgorithm() {
        withPuzzle((puzzle, actions) -> {
            final int[] currentPos = {0, 0};
            puzzle.solve(cell -> {
                int targetRow = cell[0];
                int targetCol = cell[1];
                int value = cell[2];

                navigateTo(actions, currentPos, targetRow, targetCol);
                if (value != 0) {
                    pressEnter(actions, value);
                }
                currentPos[0] = targetRow;
                currentPos[1] = targetCol;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private void navigateTo(Actions actions, int[] currentPos, int targetRow, int targetCol) {
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

    private void pressEnter(Actions actions, int value) {
        // Each Enter cycles: empty -> sun -> moon -> empty
        // SUN = 1 Enter, MOON = 2 Enters
        int presses = value == TangoPuzzle.SUN ? 1 : 2;
        for (int i = 0; i < presses; i++) {
            actions.sendKeys(Keys.ENTER).perform();
        }
    }

    public void withPuzzle(BiConsumer<TangoPuzzle, Actions> task) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--start-maximized");
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

            // Switch into the game iframe
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    By.cssSelector("iframe.game-launch-page__iframe")));

            // Wait for the tango board to render
            WebElement board = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".lotka-grid")));

            // Parse the puzzle from the live board
            String html = board.getAttribute("outerHTML");
            TangoPuzzle puzzle = tangoPuzzleParser.parse(html);

            // Press Tab then Enter to get past the landing page and start the puzzle
            Actions actions = new Actions(driver);
            actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
            actions.sendKeys(Keys.ENTER).perform();
            actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
            actions.sendKeys(Keys.ENTER).perform();

            task.accept(puzzle, actions);

            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            driver.quit();
        }
    }
}
