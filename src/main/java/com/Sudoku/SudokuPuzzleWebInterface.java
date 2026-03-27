package com.Sudoku;

import com.Utils.PuzzleType;
import com.Utils.PuzzleWebInterface;
import lombok.AllArgsConstructor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SudokuPuzzleWebInterface extends PuzzleWebInterface<SudokuPuzzle> {

    private static final int SIZE = 6;

    private final SudokuPuzzleParser sudokuPuzzleParser;

    @Override
    protected PuzzleType getPuzzleType() { return PuzzleType.SUDOKU; }

    @Override
    protected String getBoardSelector() { return ".grid-game-board"; }

    @Override
    protected SudokuPuzzle parsePuzzle(String html) {
        return new SudokuPuzzle(sudokuPuzzleParser.parse(html));
    }

    @Override
    protected void setupAfterParse(Actions actions) {
        // Press Tab then Enter to get past the landing page and start the puzzle
        actions.sendKeys(Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB).perform();
        actions.sendKeys(Keys.ENTER).perform();
    }

    public void fetchAndSubmit() {
        withPuzzle((sudokuPuzzle, actions, driver, recorder) -> {
            sudokuPuzzle.solve();
            int[][] solution = sudokuPuzzle.getBoard();

            // Press right arrow to highlight the top-left cell
            actions.sendKeys(Keys.ARROW_RIGHT).perform();

            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    actions.sendKeys(String.valueOf(solution[row][col])).perform();
                    if (col < SIZE - 1) {
                        actions.sendKeys(Keys.ARROW_RIGHT).perform();
                    }
                }
                if (row < SIZE - 1) {
                    actions.sendKeys(Keys.ARROW_DOWN).perform();
                    for (int i = 0; i < SIZE - 1; i++) {
                        actions.sendKeys(Keys.ARROW_LEFT).perform();
                    }
                }
            }
        });
    }

    public void visualizeAlgorithm(boolean record) {
        withPuzzle((sudokuPuzzle, actions, driver, recorder) -> {
            // Press right arrow to highlight the top-left cell
            actions.sendKeys(Keys.ARROW_RIGHT).perform();

            final int[] currentPos = {0, 0};
            final boolean[][] filled = new boolean[SIZE][SIZE];

            sudokuPuzzle.solve(cell -> {
                int targetRow = cell[0];
                int targetCol = cell[1];
                int value = cell[2];

                navigateTo(actions, currentPos, targetRow, targetCol);

                if (value != 0 && !filled[targetRow][targetCol]) {
                    actions.sendKeys(String.valueOf(value)).perform();
                    filled[targetRow][targetCol] = true;
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
        }, record);
    }
}
