package com.Zip;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Component
public class ZipPuzzleParser {

    public ZipPuzzle parse(String html) {
        Document document = Jsoup.parse(html);
        Element grid = document.selectFirst(".grid-game-board");
        if (grid == null) {
            throw new RuntimeException("Could not find grid-game-board in HTML");
        }

        int rows = Integer.parseInt(grid.attr("style").replaceAll(".*--rows:\\s*(\\d+).*", "$1"));
        int cols = Integer.parseInt(grid.attr("style").replaceAll(".*--cols:\\s*(\\d+).*", "$1"));

        int[][] board = new int[rows][cols];
        final HashMap<String, Set<String>> bannedMoves = new HashMap<>();
        final HashMap<Integer, Integer[]> nodeLocations = new HashMap<>();
        int maxNode = 0;

        Elements cells = grid.select(".trail-cell");
        for (Element cell : cells) {
            int idx = Integer.parseInt(cell.attr("data-cell-idx"));
            int row = idx / cols;
            int col = idx % cols;

            Element content = cell.selectFirst(".trail-cell-content");
            if (content != null) {
                int nodeValue = Integer.parseInt(content.text().trim());
                board[row][col] = nodeValue;
                if (nodeValue > 0) {
                    nodeLocations.put(nodeValue, new Integer[]{row, col});
                    maxNode = Math.max(maxNode, Integer.parseInt(content.text().trim()));
                }
            }

            parseWalls(cell, bannedMoves, row, col);
        }

        return new ZipPuzzle(board, bannedMoves, nodeLocations, maxNode);
    }

    private void parseWalls(Element cell, HashMap<String, Set<String>> bannedMoves, int row, int col) {
        String startingSquareKey = row + "," + col;
        Elements wallDivs = cell.select(".trail-cell-wall");
        for (Element wall : wallDivs) {
            String classes = wall.className();
            if (classes.contains("trail-cell-wall--right")) {
                addBidirectionalBlock(bannedMoves, startingSquareKey, row + "," + (col + 1));
            } else if (classes.contains("trail-cell-wall--down")) {
                addBidirectionalBlock(bannedMoves, startingSquareKey, (row + 1) + "," + col);
            } else if (classes.contains("trail-cell-wall--left")) {
                addBidirectionalBlock(bannedMoves, startingSquareKey, row + "," + (col - 1));
            } else if (classes.contains("trail-cell-wall--top")) {
                addBidirectionalBlock(bannedMoves, startingSquareKey, (row - 1) + "," + col);
            }
        }
    }

    private void addBidirectionalBlock(HashMap<String, Set<String>> bannedMoves, String key1, String key2) {
        bannedMoves.computeIfAbsent(key1, k -> new HashSet<>()).add(key2);
        bannedMoves.computeIfAbsent(key2, k -> new HashSet<>()).add(key1);
    }
}
