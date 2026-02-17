package com.Sudoku;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@AllArgsConstructor
@Getter
public class SudokuPuzzle {

    private final int[][] board;

    private final static Integer[][] firstQuadrant = {{0,0}, {0,1}, {0,2}, {1,0}, {1,1}, {1,2}};
    private final static Integer[][] secondQuadrant = {{0,3}, {0,4}, {0,5}, {1,3}, {1,4}, {1,5}};
    private final static Integer[][] thirdQuadrant = {{2,0}, {2,1}, {2,2}, {3,0}, {3,1}, {3,2}};
    private final static Integer[][] fourthQuadrant = {{2,3}, {2,4}, {2,5}, {3,3}, {3,4}, {3,5}};
    private final static Integer[][] fifthQuadrant = {{4,0}, {4,1}, {4,2}, {5,0}, {5,1}, {5,2}};
    private final static Integer[][] sixthQuadrant = {{4,3}, {4,4}, {4,5}, {5,3}, {5,4}, {5,5}};

    public void solve() {
        solve(null);
    }

    public void solve(Consumer<int[]> onCellVisited) {
        boolean isSolved = true;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != 0) {
                    if (onCellVisited != null) {
                        onCellVisited.accept(new int[]{row, col, 0});
                    }
                    continue;
                }
                final Set<Integer> existingValues = new HashSet<>();
                existingValues.addAll(getAllValuesFromRow(row));
                existingValues.addAll(getAllValuesFromCol(col));
                existingValues.addAll(getAllValuesFromBlock(row, col));

                final Set<Integer> possibleValues = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));
                possibleValues.removeAll(existingValues);
                if (possibleValues.size() == 1) {
                    board[row][col] = possibleValues.stream().findFirst().get();
                    if (onCellVisited != null) {
                        onCellVisited.accept(new int[]{row, col, board[row][col]});
                    }
                } else {
                    isSolved = false;
                    if (onCellVisited != null) {
                        onCellVisited.accept(new int[]{row, col, 0});
                    }
                }
            }
        }
        if (!isSolved) {
            solve(onCellVisited);
        }
    }

    private Set<Integer> getAllValuesFromCol(int col) {
        final Set<Integer> values = new HashSet<>();
        for (int i = 0; i < board.length; i++) {
            if (board[i][col] != 0) {
                values.add(board[i][col]);
            }
        }
        return values;
    }

    private Set<Integer> getAllValuesFromRow(int row) {
        final Set<Integer> values = new HashSet<>();
        for (int i = 0; i < board[row].length; i++) {
            if (board[row][i] != 0) {
                values.add(board[row][i]);
            }
        }
        return values;
    }


    private Set<Integer> getAllValuesFromBlock(int row, int col) {
        Integer[][] blockCoords;
        if (row < 2) {
            if (col < 3) {
                blockCoords = firstQuadrant;
            } else {
                blockCoords = secondQuadrant;
            }
        } else if (row < 4) {
            if (col < 3) {
                blockCoords = thirdQuadrant;
            } else {
                blockCoords = fourthQuadrant;
            }
        } else {
            if (col < 3) {
                blockCoords = fifthQuadrant;
            } else {
                blockCoords = sixthQuadrant;
            }
        }
        Set<Integer> values = new HashSet<>();
        for (Integer[] coord : blockCoords) {
            if (board[coord[0]][coord[1]] != 0) {
                values.add(board[coord[0]][coord[1]]);
            }
        }
        return values;
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
