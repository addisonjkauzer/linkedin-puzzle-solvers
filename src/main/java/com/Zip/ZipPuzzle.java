package com.Zip;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@AllArgsConstructor
@Getter
public class ZipPuzzle {

    private final int[][] board;

    private final HashMap<String, Set<String>> bannedMoves;

    private final HashMap<Integer, Integer[]> nodeLocations;

    private final int maxNode;

    private final HashMap<Integer, Set<String>> cachedPaths = new HashMap<>();

    private final static int[][] DIRECTIONS = new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

    private final static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public List<Integer[]> getSolution() {
        final List<Integer[]> solution = new ArrayList<>();
        dfsFindPath(getStartLocation(), 1, new ArrayList<>(), new HashSet<>(), solution, true);
        return solution;
    }

    public void visualizeSolution(final List<Integer[]> path,
                                  final boolean multiThreaded) {
        final List<Integer[]> solution = new ArrayList<>();
        dfsFindPath(getStartLocation(), 1, path, new HashSet<>(), solution, multiThreaded);
    }

    private void dfsFindPath(final Integer[] currentLocation,
                            Integer nextNode,
                            final List<Integer[]> path,
                            final Set<String> seen,
                            final List<Integer[]> solution,
                            final boolean multiThreaded) {
        if (!solution.isEmpty()) {
            return;
        }
        if (path.size() == board.length * board[0].length) {
            solution.addAll(path);
            return;
        }
        final int row = currentLocation[0];
        final int col = currentLocation[1];
        final String seenKey = row + "," + col;
        if (row < 0 || row == board.length ||
                col < 0 || col == board[0].length ||
                seen.contains(seenKey) ||
                (board[row][col] != nextNode && board[row][col] != 0)) {
            return;
        }
        if (board[row][col] == nextNode) {
            nextNode++;
        }
        if (multiThreaded && !allNodesConnectable(nextNode, seen)) {
            return;
        }
        path.add(new Integer[]{row, col});
        seen.add(seenKey);
        for (int[] direction : DIRECTIONS) {
            final Integer[] newLocation = new Integer[]{row + direction[0], col + direction[1]};
            final String newLocationKey = newLocation[0] + "," + newLocation[1];
            if (bannedMoves.getOrDefault(seenKey, new HashSet<>()).contains(newLocationKey)) {
                continue;
            }
            dfsFindPath(newLocation, nextNode, path, seen, solution, multiThreaded);
        }
        seen.remove(seenKey);
        path.removeLast();
    }

    private boolean allNodesConnectable(final int nextNode,
                                        final Set<String> seen) {
        final HashSet<Future<Boolean>> futures = new HashSet<>();
        for (int i = nextNode; i < maxNode; i++) {
            final Integer[] nodeLocation = new Integer[]{nodeLocations.get(i)[0], nodeLocations.get(i)[1]};
            final int endNode = i + 1;
            if (cachedPaths.containsKey(endNode)) {
                final Set<String> cachedPath = cachedPaths.get(endNode);
                if (cachedPath.stream().anyMatch(seen::contains)) {
                    cachedPaths.remove(endNode);
                } else {
                    continue;
                }
            }
            final Future<Boolean> future = executor.submit(() -> pathExists(nodeLocation, endNode, new HashSet<>(seen), new HashSet<>()));
            futures.add(future);
        }
        try {
            for (Future<Boolean> future : futures) {
                if (!future.get()) {
                    futures.forEach(a -> a.cancel(true));
                    return false;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Multithreading error");
        }
        return true;
    }

    private boolean pathExists(final Integer[] currentLocation,
                               final Integer endNode,
                               final Set<String> seen,
                               final Set<String> path) {
        final int row = currentLocation[0];
        final int col = currentLocation[1];
        final String seenKey = row + "," + col;
        if (row < 0 || row == board.length ||
                col < 0 || col == board[0].length ||
                seen.contains(seenKey) ||
                !Set.of(endNode, endNode - 1, 0).contains(board[row][col])) {
            return false;
        }
        if (board[row][col] == endNode) {
            cachedPaths.put(endNode, path);
            return true;
        }
        seen.add(seenKey);
        path.add(seenKey);
        for (int[] direction : DIRECTIONS) {
            final Integer[] newLocation = new Integer[]{row + direction[0], col + direction[1]};
            final String newLocationKey = newLocation[0] + "," + newLocation[1];
            if (bannedMoves.getOrDefault(seenKey, new HashSet<>()).contains(newLocationKey)) {
                continue;
            }
            if (pathExists(newLocation, endNode, seen, path)) return true;
        }
        path.remove(seenKey);
        return false;
    }


    private Integer[] getStartLocation() {
        for (int i = 0; i < board.length; i++) {
            for (int j =0; j < board[i].length; j++) {
                if (board[i][j] == 1) {
                    return new Integer[]{i, j};
                }
            }
        }
        throw new RuntimeException("Start location of ZIP could not be found");
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
