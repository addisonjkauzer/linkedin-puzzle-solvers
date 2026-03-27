package com.Zip;

import com.Utils.PuzzleType;
import com.Utils.PuzzleWebInterface;
import lombok.AllArgsConstructor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class ZipPuzzleWebInterface extends PuzzleWebInterface<ZipPuzzle> {

    private final ZipPuzzleParser zipPuzzleParser;

    @Override
    protected PuzzleType getPuzzleType() { return PuzzleType.ZIP; }

    @Override
    protected String getBoardSelector() { return ".grid-game-board"; }

    @Override
    protected ZipPuzzle parsePuzzle(String html) {
        return zipPuzzleParser.parse(html);
    }

    @Override
    protected void setupAfterParse(Actions actions) {
        // Press Tab then Enter to get past the landing page and start the puzzle
        actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
        actions.sendKeys(Keys.ENTER).perform();
    }

    public void fetchAndSubmit() {
        withPuzzle((puzzle, actions, driver, recorder) -> {
            List<Integer[]> solution = puzzle.getSolution();

            // Send arrow keys for each step in the solution path
            for (int i = 1; i < solution.size(); i++) {
                int rowDiff = solution.get(i)[0] - solution.get(i - 1)[0];
                int colDiff = solution.get(i)[1] - solution.get(i - 1)[1];
                actions.sendKeys(toArrowKey(rowDiff, colDiff)).perform();
            }
        });
    }

    public void visualizeAlgorithm(boolean enableOptimizations, boolean record) {
        withPuzzle((puzzle, actions, driver, recorder) -> {
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
                        if (recorder != null) recorder.captureFrame();
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
                        if (recorder != null) recorder.captureFrame();
                    }
                    return removed;
                }
            };

            puzzle.visualizeSolution(observablePath, enableOptimizations);
            if (recorder == null) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, record);
    }
}
