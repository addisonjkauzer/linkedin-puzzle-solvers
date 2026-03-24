package com.Pinpoint;

import com.Utils.PuzzleType;
import com.Utils.PuzzleWebInterface;
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@AllArgsConstructor
public class PinpointPuzzleWebInterface extends PuzzleWebInterface<PinpointPuzzle> {

    private final PinpointPuzzleParser pinpointPuzzleParser;

    @Override
    protected PuzzleType getPuzzleType() { return PuzzleType.PINPOINT; }

    @Override
    protected String getBoardSelector() { return ".pinpoint__board"; }

    @Override
    protected PinpointPuzzle parsePuzzle(String html) {
        return pinpointPuzzleParser.parse(html);
    }

    @Override
    protected void setupAfterParse(Actions actions) {
        actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
        actions.sendKeys(Keys.ENTER).perform();
    }

    private PinpointPuzzle reParse(org.openqa.selenium.WebDriver driver) {
        WebElement board = driver.findElement(By.cssSelector(getBoardSelector()));
        return parsePuzzle(board.getAttribute("outerHTML"));
    }

    private boolean isSolved(org.openqa.selenium.WebDriver driver) {
        return !driver.findElements(By.cssSelector(".pr-game-results__components")).isEmpty();
    }

    public void visualizeAlgorithm(boolean record) {
        withPuzzle((puzzle, actions, driver, recorder) -> {
            if (recorder != null) recorder.captureFramesFor(5000);
            while (!isSolved(driver)) {
                PinpointPuzzle current = reParse(driver);
                String answer = current.solve();
                String boardBefore = driver.findElement(By.cssSelector(getBoardSelector())).getAttribute("outerHTML");
                typeAnswer(actions, answer);
                if (recorder != null) recorder.captureFramesFor(5000);
                new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                        isSolved(d) || !d.findElement(By.cssSelector(getBoardSelector())).getAttribute("outerHTML").equals(boardBefore)
                );
            }
            if (recorder == null) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, record);
    }

    private void typeAnswer(Actions actions, String answer) {
        actions.sendKeys(answer).perform();
        actions.sendKeys(Keys.ENTER).perform();
    }
}
