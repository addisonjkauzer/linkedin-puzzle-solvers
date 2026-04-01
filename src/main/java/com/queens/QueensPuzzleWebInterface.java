package com.queens;

import com.utils.PuzzleType;
import com.utils.PuzzleWebInterface;
import lombok.AllArgsConstructor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class QueensPuzzleWebInterface extends PuzzleWebInterface<QueensPuzzle> {

    private final QueensPuzzleParser queensPuzzleParser;

    @Override
    protected PuzzleType getPuzzleType() { return PuzzleType.QUEENS; }

    @Override
    protected String getBoardSelector() { return "#queens-grid"; }

    @Override
    protected QueensPuzzle parsePuzzle(String html) {
        return queensPuzzleParser.parse(html);
    }

    @Override
    protected void setupAfterParse(Actions actions) {
        actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
        actions.sendKeys(Keys.ENTER, Keys.ENTER).perform();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        actions.sendKeys(Keys.TAB, Keys.TAB).perform();
        actions.sendKeys(Keys.ENTER).perform();
    }

    public void fetchAndSubmit() {
        withPuzzle((puzzle, actions, driver, recorder) -> {
            int rows = puzzle.getRows();
            int cols = puzzle.getCols();

            boolean[][] wasEmpty = new boolean[rows][cols];
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    wasEmpty[row][col] = puzzle.getSolution()[row][col] == 0;
                }
            }

            puzzle.solve();

            final int[] currentPos = {0, 0};
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (wasEmpty[row][col] && puzzle.getSolution()[row][col] == 1) {
                        navigateTo(actions, currentPos, row, col);
                        placeQueen(actions);
                        currentPos[0] = row;
                        currentPos[1] = col;
                    }
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
                if (value == 1) {
                    placeQueen(actions);
                } else if (value == 0) {
                    undoQueen(actions);
                }
                currentPos[0] = targetRow;
                currentPos[1] = targetCol;
                if (recorder != null) {
                    recorder.captureFrame();
                } else {
                    try {
                        Thread.sleep(100);
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

    private void placeQueen(Actions actions) {
        // Each Enter cycles: empty -> X mark -> queen -> empty
        // Queen requires 2 Enter presses
        actions.sendKeys(Keys.ENTER).perform();
        actions.sendKeys(Keys.ENTER).perform();
    }

    private void undoQueen(Actions actions) {
        // Each Enter cycles: empty -> X mark -> queen -> empty
        // Undo Queen requires 1 Enter press
        actions.sendKeys(Keys.ENTER).perform();
    }
}
