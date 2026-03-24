package com.Tango;

import com.Utils.PuzzleType;
import com.Utils.PuzzleWebInterface;
import lombok.AllArgsConstructor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TangoPuzzleWebInterface extends PuzzleWebInterface<TangoPuzzle> {

    private final TangoPuzzleParser tangoPuzzleParser;

    @Override
    protected PuzzleType getPuzzleType() { return PuzzleType.TANGO; }

    @Override
    protected String getBoardSelector() { return ".lotka-grid"; }

    @Override
    protected TangoPuzzle parsePuzzle(String html) {
        return tangoPuzzleParser.parse(html);
    }

    @Override
    protected void setupAfterParse(Actions actions) {
        // Press Tab then Enter twice to get past the landing page and start the puzzle
        actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
        actions.sendKeys(Keys.ENTER).perform();
        actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
        actions.sendKeys(Keys.ENTER).perform();
    }

    public void fetchAndSubmit() {
        withPuzzle((puzzle, actions, driver, recorder) -> {
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

    public void visualizeAlgorithm(boolean record) {
        withPuzzle((puzzle, actions, driver, recorder) -> {
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
                if (recorder != null) {
                    recorder.captureFrame();
                } else {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            if (recorder == null) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, record);
    }

    private void pressEnter(Actions actions, int value) {
        // Each Enter cycles: empty -> sun -> moon -> empty
        // SUN = 1 Enter, MOON = 2 Enters
        int presses = value == TangoPuzzle.SUN ? 1 : 2;
        for (int i = 0; i < presses; i++) {
            actions.sendKeys(Keys.ENTER).perform();
        }
    }
}
