package com.queens;

import com.utils.MetricsPublisher;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class QueensPuzzle {

    private final static Integer[][] NEIGHBORS = new Integer[][]{{0,1},{1,0},{0,-1},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}};

    private final int[][] colorGrid; // region/color index for each cell
    private final HashMap<Integer, Set<Integer[]>> colorToCells = new HashMap<>();
    private final HashMap<List<Integer>, Integer> unavailableMap = new HashMap<>();
    private final int[][] solution;  // 0 = empty, 1 = queen
    private final int rows;
    private final int cols;

    private final int startingQueens;

    public QueensPuzzle(int[][] colorGrid, int[][] solution) {
        this.colorGrid = colorGrid;
        this.solution = solution;
        this.rows = colorGrid.length;
        this.cols = colorGrid[0].length;
        int foundQueens = 0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                colorToCells.computeIfAbsent(colorGrid[row][col], a -> new HashSet<>()).add(new Integer[]{row,col});
            }
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (solution[row][col] == 1) {
                    placeQueen(row, col, null);
                    foundQueens++;
                }
            }
        }
        this.startingQueens = foundQueens;
    }

    public void solve() {
        solve(null);
    }

    public void solve(Consumer<int[]> onCellVisited) {
        final long start = System.currentTimeMillis();
        for (int nextRow = 0; nextRow < rows; nextRow++) {
            for (int nextCol = 0; nextCol < cols; nextCol++) {
                List<Integer> key = Arrays.asList(nextRow, nextCol);
                if (!unavailableMap.containsKey(key)) {
                    if (dfsFillGridFromSquare(nextRow, nextCol, startingQueens, onCellVisited)) {
                        MetricsPublisher.publishSolveTime("Queens", System.currentTimeMillis() - start);
                        return;
                    }
                }
            }
        }
    }

    public boolean dfsFillGridFromSquare(int row, int col, int placedQueens, Consumer<int[]> onCellVisited) {

        placeQueen(row, col, onCellVisited);

        if (placedQueens + 1 == rows) {
            return true;
        }

        if (shouldPrune()) {
            removeQueen(row, col, onCellVisited);
            return false;
        }

        for (int nextRow = 0; nextRow < rows; nextRow++) {
            for (int nextCol = 0; nextCol < cols; nextCol++) {
                final List<Integer> key = Arrays.asList(nextRow, nextCol);
                if (!unavailableMap.containsKey(key)) {
                    if (dfsFillGridFromSquare(nextRow, nextCol, placedQueens + 1, onCellVisited)) {
                        return true;
                    }
                }
            }
        }

        removeQueen(row, col, onCellVisited);
        return false;
    }

    private boolean shouldPrune() {
        for (int row = 0; row < rows; row++) {
            boolean allCellsBlocked = true;
            for (int col = 0; col < cols; col++) {
                final List<Integer> key = Arrays.asList(row, col);
                if (!unavailableMap.containsKey(key) || solution[row][col] == 1) {
                    allCellsBlocked = false;
                    break;
                }
            }
            if (allCellsBlocked) {
                return true;
            }
        }
        for (int col = 0; col < cols; col++) {
            boolean allCellsBlocked = true;
            for (int row = 0; row < rows; row++) {
                final List<Integer> key = Arrays.asList(row, col);
                if (!unavailableMap.containsKey(key) || solution[row][col] == 1) {
                    allCellsBlocked = false;
                    break;
                }
            }
            if (allCellsBlocked) {
                return true;
            }
        }
        for (Set<Integer[]> cellsPerColor : colorToCells.values()) {
            boolean allCellsBlocked = true;
            for (Integer[] cell : cellsPerColor) {
                final List<Integer> key = Arrays.asList(cell[0], cell[1]);
                if (!unavailableMap.containsKey(key) || solution[cell[0]][cell[1]] == 1) {
                    allCellsBlocked = false;
                    break;
                }
            }
            if (allCellsBlocked) {
                return true;
            }
        }
        return false;
    }

    public void placeQueen(int row, int col, Consumer<int[]> onCellVisited) {
        makeLessAvailable(row, col, unavailableMap);
        solution[row][col] = 1;
        if (onCellVisited != null) {
            onCellVisited.accept(new int[]{row, col, solution[row][col]});
        }
        for (int i = 0; i < rows; i++) {
            makeLessAvailable(i, col, unavailableMap);
            makeLessAvailable(row, i, unavailableMap);
        }
        for (Integer[] coord : colorToCells.get(colorGrid[row][col])) {
            makeLessAvailable(coord[0], coord[1], unavailableMap);
        }
        for (Integer[] neighbor : NEIGHBORS) {
            final int newRow = row + neighbor[0];
            final int newCol = col + neighbor[1];
            if (newRow >=0 && newRow < rows && newCol >= 0 && newCol < cols) {
                makeLessAvailable(newRow, newCol, unavailableMap);
            }
        }
    }

    public void removeQueen(int row, int col, Consumer<int[]> onCellVisited) {
        makeMoreAvailable(row, col, unavailableMap);
        solution[row][col] = 0;
        if (onCellVisited != null) {
            onCellVisited.accept(new int[]{row, col, solution[row][col]});
        }
        for (int i = 0; i < rows; i++) {
            makeMoreAvailable(i, col, unavailableMap);
            makeMoreAvailable(row, i, unavailableMap);
        }
        for (Integer[] coord : colorToCells.get(colorGrid[row][col])) {
            makeMoreAvailable(coord[0], coord[1], unavailableMap);
        }
        for (Integer[] neighbor : NEIGHBORS) {
            final int newRow = row + neighbor[0];
            final int newCol = col + neighbor[1];
            if (newRow >=0 && newRow < rows && newCol >= 0 && newCol < cols) {
                makeMoreAvailable(newRow, newCol, unavailableMap);
            }
        }
    }

    public void makeLessAvailable(int row, int col, HashMap<List<Integer>, Integer> unavailableMap) {
        List<Integer> key = Arrays.asList(row, col);
        unavailableMap.put(key, unavailableMap.getOrDefault(key, 0) + 1);
    }

    public void makeMoreAvailable(int row, int col, HashMap<List<Integer>, Integer> unavailableMap) {
        List<Integer> key = Arrays.asList(row, col);
        int newAvailability = unavailableMap.get(key) - 1;
        if (newAvailability == 0) {
            unavailableMap.remove(key);
        } else {
            unavailableMap.put(key, newAvailability);
        }
    }
}
