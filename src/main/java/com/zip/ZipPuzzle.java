package com.zip;

import com.utils.MetricsPublisher;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@Getter
public class ZipPuzzle {

    private final int[][] board;

    private final HashMap<String, Set<String>> bannedMoves;

    private final HashMap<Integer, Integer[]> nodeLocations;

    private final int maxNode;

    private final HashMap<Integer, Set<String>> cachedPaths = new HashMap<>();

    private final AtomicInteger cacheHits = new AtomicInteger(0);
    private final AtomicInteger cacheMisses = new AtomicInteger(0);

    private final static int[][] DIRECTIONS = new int[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}};

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public List<Integer[]> getSolution() {
        final List<Integer[]> solution = new ArrayList<>();
        dfsFindPath(getStartLocation(), 1, new ArrayList<>(), new HashSet<>(), solution, true);
        executor.shutdown();
        return solution;
    }

    public void visualizeSolution(final List<Integer[]> path,
                                  final boolean enableOptimizations) {
        try {
            final long start = System.currentTimeMillis();
            final List<Integer[]> solution = new ArrayList<>();
            dfsFindPath(getStartLocation(), 1, path, new HashSet<>(), solution, enableOptimizations);
            MetricsPublisher.publishSolveTime("Zip", System.currentTimeMillis() - start);
        } finally {
            executor.shutdownNow();
        }
    }

    private void dfsFindPath(final Integer[] currentLocation,
                            Integer nextNode,
                            final List<Integer[]> path,
                            final Set<String> seen,
                            final List<Integer[]> solution,
                            final boolean enableOptimizations) {
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
        if (enableOptimizations && (!allNodesConnectable(nextNode, seen) || numBlankIslands(seen, nextNode) > 1 || hasDeadEndPath(seen, row, col))) {
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
            dfsFindPath(newLocation, nextNode, path, seen, solution, enableOptimizations);
        }
        if (!solution.isEmpty()) return;
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
                    cacheHits.incrementAndGet();
                    continue;
                }
            }
            cacheMisses.incrementAndGet();
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

    private int numBlankIslands(final Set<String> currentPath, int nextNode) {
        boolean[][] seen = new boolean[board.length][board[0].length];
        int numIslands = 0;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                int value = board[row][col];
                if ((value == 0 || value >= nextNode) && !seen[row][col] && !currentPath.contains(row + "," + col)) {
                    final Queue<Integer[]> bfsQueue = new LinkedList<>();
                    bfsQueue.add(new Integer[]{row, col});
                    while (!bfsQueue.isEmpty()) {
                        final Integer[] current = bfsQueue.poll();
                        seen[current[0]][current[1]] = true;
                        for (int[] direction : DIRECTIONS) {
                            int newRow = current[0] + direction[0];
                            int newCol = current[1] + direction[1];
                            if (newRow < 0 || newRow >= board.length || newCol < 0 || newCol >= board[0].length) {
                                continue;
                            }
                            int newValue = board[newRow][newCol];
                            if ((newValue == 0 || newValue >= nextNode) && !seen[newRow][newCol] && !currentPath.contains(newRow + "," + newCol)) {
                                bfsQueue.add(new Integer[]{newRow, newCol});
                            }
                        }
                    }
                    numIslands++;
                }
            }
        }
        return numIslands;
    }

    private boolean hasDeadEndPath(final Set<String> seen, int toBePlacedRow, int toBePlacedCol) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if ((row == toBePlacedRow && col == toBePlacedCol) || seen.contains(row + "," + col) ||board[row][col] == maxNode) {
                    continue;
                }
                int openSides = 4;
                for (final int[] direction : DIRECTIONS) {
                    int newRow = row + direction[0];
                    int newCol = col + direction[1];
                    if (newRow < 0 || newRow >= board.length || newCol < 0 || newCol >= board[0].length || seen.contains(newRow + "," + newCol)) {
                        openSides--;
                    }
                }
                if (openSides < 2) {
                    return true;
                }
            }
        }
        return false;
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
