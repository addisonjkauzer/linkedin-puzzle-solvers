package com.Sudoku;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class SudokuPuzzle {

    private final int[][] board;

    private final HashMap<String, Set<Integer>> possibleValuesGrid = new HashMap<>();

    private final static Integer[][] firstQuadrant = {{0,0}, {0,1}, {0,2}, {1,0}, {1,1}, {1,2}};
    private final static Integer[][] secondQuadrant = {{0,3}, {0,4}, {0,5}, {1,3}, {1,4}, {1,5}};
    private final static Integer[][] thirdQuadrant = {{2,0}, {2,1}, {2,2}, {3,0}, {3,1}, {3,2}};
    private final static Integer[][] fourthQuadrant = {{2,3}, {2,4}, {2,5}, {3,3}, {3,4}, {3,5}};
    private final static Integer[][] fifthQuadrant = {{4,0}, {4,1}, {4,2}, {5,0}, {5,1}, {5,2}};
    private final static Integer[][] sixthQuadrant = {{4,3}, {4,4}, {4,5}, {5,3}, {5,4}, {5,5}};


    public SudokuPuzzle(final int[][] board) {
        this.board = board;
        for (int row = 0; row < this.board.length; row++) {
            for (int col = 0; col < this.board[row].length; col++) {
                if (this.board[row][col] == 0) {
                    updatePossibleValues(row, col);
                } else {
                    possibleValuesGrid.put(row + "," + col, new HashSet<>(Set.of(this.board[row][col])));
                }
            }
        }
    }

    public void solve() {
        solve(null);
    }

    public void solve(Consumer<int[]> onCellVisited) {
        boolean isSolved = true;
        do {
            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[row].length; col++) {
                    if (board[row][col] != 0) {
                        if (onCellVisited != null) {
                            onCellVisited.accept(new int[]{row, col, 0});
                        }
                        continue;
                    }
                    final Set<Integer> possibleValues = updatePossibleValues(row, col);
                    if (possibleValues.size() == 1) {
                        submitCell(row, col, possibleValues.stream().findFirst().get(), onCellVisited);
                        continue;
                    }

                    final Set<Integer> remainingPossibleInRow = getRemainingPossibleInRow(row, col);
                    if (remainingPossibleInRow.size() == 1) {
                        submitCell(row, col, remainingPossibleInRow.stream().findFirst().get(), onCellVisited);
                        continue;
                    }

                    final Set<Integer> remainingPossibleInCol = getRemainingPossibleInCol(row, col);
                    if (remainingPossibleInCol.size() == 1) {
                        submitCell(row, col, remainingPossibleInCol.stream().findFirst().get(), onCellVisited);
                        continue;
                    }

                    final Set<Integer> remainingPossibleInBlock = getRemainingPossibleInBlock(row, col);
                    if (remainingPossibleInBlock.size() == 1) {
                        submitCell(row, col, remainingPossibleInBlock.stream().findFirst().get(), onCellVisited);
                        continue;
                    }

                    isSolved = false;
                    if (onCellVisited != null) {
                        onCellVisited.accept(new int[]{row, col, 0});
                    }
                }
            }
        } while (!isSolved);
    }

    private Set<Integer> updatePossibleValues(int row, int col) {

        final Set<Integer> values = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));
        for (int i = 0; i < board.length; i++) {
            if (board[i][col] != 0) {
                values.remove(board[i][col]);
            }
        }
        for (int i = 0; i < board[row].length; i++) {
            if (board[row][i] != 0) {
                values.remove(board[row][i]);
            }
        }
        final Integer[][] blockCoords = getBlockCoords(row, col);
        for (Integer[] coord : blockCoords) {
            if (board[coord[0]][coord[1]] != 0) {
                values.remove(board[coord[0]][coord[1]]);
            }
        }
        possibleValuesGrid.put(row + "," + col, values);
        return values;
    }

    private Set<Integer> getRemainingPossibleInCol(int row, int col) {
        final Set<Integer> values = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));
        for (int i = 0; i < board.length; i++) {
            if (i != row) {
                values.removeAll(possibleValuesGrid.get(i + "," + col));
            }
        }
        return values;
    }

    private Set<Integer> getRemainingPossibleInRow(int row, int col) {
        final Set<Integer> values = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));
        for (int i = 0; i < board[row].length; i++) {
            if (i != col) {
                values.removeAll(possibleValuesGrid.get(row + "," + i));
            }
        }
        return values;
    }

    private Set<Integer> getRemainingPossibleInBlock(int row, int col) {
        final Set<Integer> values = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));
        final Integer[][] blockCoords = getBlockCoords(row, col);
        for (Integer[] coord : blockCoords) {
            if (coord[0] == row && coord[1] == col) {
                continue;
            }
            values.removeAll(possibleValuesGrid.get(coord[0] + "," + coord[1]));
        }
        return values;
    }

    private void submitCell(int row, int col, int value, Consumer<int[]> onCellVisited) {
        board[row][col] = value;
        possibleValuesGrid.put(row + "," + col, Set.of(value));
        if (onCellVisited != null) {
            onCellVisited.accept(new int[]{row, col, board[row][col]});
        }
    }

    private Integer[][] getBlockCoords(int row, int col) {
        if (row < 2) {
            if (col < 3) {
                return firstQuadrant;
            } else {
                return secondQuadrant;
            }
        } else if (row < 4) {
            if (col < 3) {
                return thirdQuadrant;
            } else {
                return fourthQuadrant;
            }
        } else {
            if (col < 3) {
                return fifthQuadrant;
            } else {
                return sixthQuadrant;
            }
        }
    }



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int j = 0; j < row.length; j++) {
                if (j > 0) sb.append('\t');
                sb.append(row[j] == 0 ? "." : row[j]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
