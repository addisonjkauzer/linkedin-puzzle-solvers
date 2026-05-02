package com.tango;

import com.utils.MetricsPublisher;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

@Getter
public class TangoPuzzle {
    public static final int SUN = 1;
    public static final int MOON = -1;

    public static final String EQUAL = "EQUAL";
    public static final String OPPOSITE = "OPPOSITE";

    private final int[][] board;

    // Key: "row1,col1-row2,col2" (adjacent cells, smaller index first)
    // Value: EQUAL or OPPOSITE
    private final HashMap<String, String> constraints;

    public final int[] remainingSunRow;
    public final int[] remainingSunCol;
    public final int[] remainingMoonRow;
    public final int[] remainingMoonCol;

    public TangoPuzzle(int[][] board, HashMap<String, String> constraints) {
        this.board = board;
        this.constraints = constraints;
        this.remainingSunRow = new int[board.length];
        this.remainingSunCol = new int[board[0].length];
        this.remainingMoonRow = new int[board.length];
        this.remainingMoonCol = new int[board[0].length];
        Arrays.fill(this.remainingSunRow, 3);
        Arrays.fill(this.remainingSunCol, 3);
        Arrays.fill(this.remainingMoonRow, 3);
        Arrays.fill(this.remainingMoonCol, 3);
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == SUN) {
                    remainingSunRow[row]--;
                    remainingSunCol[col]--;
                } else if (board[row][col] == MOON) {
                    remainingMoonRow[row]--;
                    remainingMoonCol[col]--;
                }
            }
        }
    }

    public void solve() {
        solve(null);
    }

    public void solve(Consumer<int[]> onCellVisited) {
        final long start = System.currentTimeMillis();
        boolean isSolved;
        do {
            isSolved = true;
            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[row].length; col++) {
                    if (board[row][col] != 0) {
                        continue;
                    }
                    int upTwo = board[(row + board.length - 2) % board.length][col];
                    int upOne = board[(row + board.length - 1) % board.length][col];
                    int downTwo = board[(row + 2) % board.length][col];
                    int downOne = board[(row + 1) % board.length][col];
                    int leftTwo = board[row][(col + board[row].length - 2) % board[row].length];
                    int leftOne = board[row][(col + board[row].length - 1) % board[row].length];
                    int rightTwo = board[row][(col + 2) % board[row].length];
                    int rightOne = board[row][(col + 1) % board[row].length];
                    String constraintKeyLeft = constraintKey(row, col, row, (col + board[row].length - 1) % board[row].length);
                    String constraintKeyRight = constraintKey(row, col, row, (col + 1) % board[row].length);
                    String constraintKeyUp = constraintKey(row, col, (row + board.length - 1) % board.length, col);
                    String constraintKeyDown = constraintKey(row, col, (row + 1) % board.length, col);

                    if (upTwo == upOne && upOne != 0) {
                        submitCell(row, col, upOne * -1, onCellVisited);
                        continue;
                    }
                    if (downTwo == downOne && downOne != 0) {
                        submitCell(row, col, downOne * -1, onCellVisited);
                        continue;
                    }
                    if (leftTwo == leftOne && leftOne != 0) {
                        submitCell(row, col, leftOne * -1, onCellVisited);
                        continue;
                    }
                    if (rightTwo == rightOne && rightOne != 0) {
                        submitCell(row, col, rightOne * -1, onCellVisited);
                        continue;
                    }
                    if (rightOne == leftOne && leftOne != 0) {
                        submitCell(row, col, leftOne * -1, onCellVisited);
                        continue;
                    }
                    if (upOne == downOne && downOne != 0) {
                        submitCell(row, col, downOne * -1, onCellVisited);
                        continue;
                    }
                    if (constraints.containsKey(constraintKeyLeft)){
                        if (leftOne != 0) {
                            int newValue = EQUAL.equals(constraints.get(constraintKeyLeft)) ? leftOne : leftOne * -1;
                            submitCell(row, col, newValue, onCellVisited);
                            continue;
                        } else if (EQUAL.equals(constraints.get(constraintKeyLeft)) && rightOne != 0) {
                            submitCell(row, col, rightOne * -1, onCellVisited);
                            continue;
                        }
                    }
                    if (constraints.containsKey(constraintKeyRight)) {
                        if (rightOne != 0) {
                            int newValue = EQUAL.equals(constraints.get(constraintKeyRight)) ? rightOne : rightOne * -1;
                            submitCell(row, col, newValue, onCellVisited);
                            continue;
                        } else if (EQUAL.equals(constraints.get(constraintKeyRight)) && leftOne != 0) {
                            submitCell(row, col, leftOne * -1, onCellVisited);
                            continue;
                        }
                    }
                    if (constraints.containsKey(constraintKeyUp)){
                        if (upOne != 0) {
                            int newValue = EQUAL.equals(constraints.get(constraintKeyUp)) ? upOne : upOne * -1;
                            submitCell(row, col, newValue, onCellVisited);
                            continue;
                        } else if (EQUAL.equals(constraints.get(constraintKeyUp)) && downOne != 0) {
                            submitCell(row, col, downOne * -1, onCellVisited);
                            continue;
                        }
                    }
                    if (constraints.containsKey(constraintKeyDown)){
                        if (downOne != 0) {
                            int newValue = EQUAL.equals(constraints.get(constraintKeyDown)) ? downOne : downOne * -1;
                            submitCell(row, col, newValue, onCellVisited);
                            continue;
                        } else if (EQUAL.equals(constraints.get(constraintKeyDown)) && upOne != 0) {
                            submitCell(row, col, upOne * -1, onCellVisited);
                            continue;
                        }
                    }
                    int defaultFromCol = checkRemainingCol(col);
                    if (defaultFromCol != 0) {
                        submitCell(row, col, defaultFromCol, onCellVisited);
                        continue;
                    }
                    int defaultFromRow = checkRemainingRow(row);
                    if (defaultFromRow != 0) {
                        submitCell(row, col, defaultFromRow, onCellVisited);
                        continue;
                    }

                    isSolved = false;
                    if (onCellVisited != null) {
                        onCellVisited.accept(new int[]{row, col, 0});
                    }

                }
            }
        } while (!isSolved);
        MetricsPublisher.publishSolveTime("Tango", System.currentTimeMillis() - start);
    }

    private String constraintKey(int r1, int c1, int r2, int c2) {
        return r1 + "," + c1 + "-" + r2 + "," + c2;
    }

    private int checkRemainingCol(int col) {
        int remainingSun = 3;
        int remainingMoon = 3;
        for (int row = 0; row < board.length; row++) {
            if (board[row][col] == SUN) {
                remainingSun--;
            } else if (board[row][col] == MOON) {
                remainingMoon--;
            } else {
                int downNeighbor = (row + 1) % board.length;
                String constraintKeyDown = constraintKey(row, col, downNeighbor, col);
                if (board[downNeighbor][col] == 0 && constraints.containsKey(constraintKeyDown)) {
                    if (OPPOSITE.equals(constraints.get(constraintKeyDown))) {
                        remainingSun--;
                        remainingMoon--;
                        row++;
                    }
                }
            }
        }
        if (remainingSun == 0) {
            return MOON;
        } else if (remainingMoon == 0) {
            return SUN;
        } else {
            return 0;
        }
    }

    private int checkRemainingRow(int row) {
        int remainingSun = 3;
        int remainingMoon = 3;
        for (int col = 0; col < board.length; col++) {
            if (board[row][col] == SUN) {
                remainingSun--;
            } else if (board[row][col] == MOON) {
                remainingMoon--;
            } else {
                int rightNeighbor = (col + 1) % board[row].length;
                String constraintKeyRight = constraintKey(row, col, row, rightNeighbor);
                if (board[row][rightNeighbor] == 0 && constraints.containsKey(constraintKeyRight)) {
                    if (OPPOSITE.equals(constraints.get(constraintKeyRight))) {
                        remainingSun--;
                        remainingMoon--;
                        col++;
                    }
                }
            }
        }
        if (remainingSun == 0) {
            return MOON;
        } else if (remainingMoon == 0) {
            return SUN;
        } else {
            return 0;
        }
    }


    private void submitCell(int row, int col, int value, Consumer<int[]> onCellVisited) {
        board[row][col] = value;
        if (value == SUN) {
            remainingSunRow[row]--;
            remainingSunCol[col]--;
        } else if (value == MOON) {
            remainingMoonRow[row]--;
            remainingMoonCol[col]--;
        }
        if (onCellVisited != null) {
            onCellVisited.accept(new int[]{row, col, board[row][col]});
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            // Cell row with horizontal constraints between cells
            for (int j = 0; j < board[i].length; j++) {
                if (j > 0) {
                    String h = constraints.get(i + "," + (j - 1) + "-" + i + "," + j);
                    sb.append(EQUAL.equals(h) ? '=' : OPPOSITE.equals(h) ? 'x' : ' ');
                }
                sb.append(switch (board[i][j]) {
                    case SUN -> 'S';
                    case MOON -> 'M';
                    default -> '.';
                });
            }
            sb.append('\n');

            // Vertical constraint row between this row and the next
            if (i < board.length - 1) {
                for (int j = 0; j < board[i].length; j++) {
                    if (j > 0) sb.append(' ');
                    String v = constraints.get(i + "," + j + "-" + (i + 1) + "," + j);
                    sb.append(EQUAL.equals(v) ? '=' : OPPOSITE.equals(v) ? 'x' : ' ');
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
