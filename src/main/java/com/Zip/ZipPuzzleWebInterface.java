package com.Zip;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Component
@AllArgsConstructor
public class ZipPuzzleWebInterface {

    private static final String URL = PuzzleType.ZIP.getUrl();

    private final ZipPuzzleParser zipPuzzleParser;

    public void fetchAndSubmit() {
        withPuzzle((puzzle, actions) -> {
            List<Integer[]> solution = puzzle.getSolution();

            // Send arrow keys for each step in the solution path
            for (int i = 1; i < solution.size(); i++) {
                int rowDiff = solution.get(i)[0] - solution.get(i - 1)[0];
                int colDiff = solution.get(i)[1] - solution.get(i - 1)[1];
                actions.sendKeys(toArrowKey(rowDiff, colDiff)).perform();
            }
        });
    }

    public void visualizeAlgorithm(boolean multiThreaded) {
        withPuzzle((puzzle, actions) -> {
            // Observable path that sends arrow keys on add and reverse arrow keys on removeLast
            List<Integer[]> observablePath = new ArrayList<>() {
                @Override
                public boolean add(Integer[] element) {
                    boolean result = super.add(element);
                    if (size() > 1) {
                        Integer[] prev = get(size() - 2);
                        int rowDiff = element[0] - prev[0];
                        int colDiff = element[1] - prev[1];
                        actions.sendKeys(toArrowKey(rowDiff, colDiff)).perform();
                    }
                    return result;
                }

                @Override
                public Integer[] removeLast() {
                    Integer[] removed = super.removeLast();
                    if (!isEmpty()) {
                        Integer[] target = getLast();
                        int rowDiff = target[0] - removed[0];
                        int colDiff = target[1] - removed[1];
                        actions.sendKeys(toArrowKey(rowDiff, colDiff)).perform();
                    }
                    return removed;
                }
            };

            // Run the DFS with the observable path so the browser mirrors every step
            puzzle.visualizeSolution(observablePath, multiThreaded);
        });
    }

    private void withPuzzle(BiConsumer<ZipPuzzle, Actions> task) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--start-maximized");
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

            // Switch into the game iframe
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(
                    By.cssSelector("iframe.game-launch-page__iframe")));

            // Wait for the grid-game-board to render
            WebElement board = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".grid-game-board")));

            // Parse the puzzle from the live board
            String html = board.getAttribute("outerHTML");
            ZipPuzzle puzzle = zipPuzzleParser.parse(html);

            // Use Actions to send keys directly to the browser
            Actions actions = new Actions(driver);

            // Press Tab then Enter to get past the landing page and start the puzzle
            actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
            actions.sendKeys(Keys.ENTER).perform();

            task.accept(puzzle, actions);

            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            driver.quit();
        }
    }

    private Keys toArrowKey(int rowDiff, int colDiff) {
        if (rowDiff == -1) return Keys.ARROW_UP;
        if (rowDiff == 1) return Keys.ARROW_DOWN;
        if (colDiff == -1) return Keys.ARROW_LEFT;
        if (colDiff == 1) return Keys.ARROW_RIGHT;
        throw new IllegalArgumentException("Invalid move: rowDiff=" + rowDiff + ", colDiff=" + colDiff);
    }
}
