package com.tango;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class TangoPuzzleParser {

    public TangoPuzzle parse(String html) {
        Document document = Jsoup.parse(html);
        Element grid = document.selectFirst(".lotka-grid");
        if (grid == null) {
            throw new RuntimeException("Could not find lotka-grid in HTML");
        }

        int rows = Integer.parseInt(grid.attr("style").replaceAll(".*--rows:\\s*(\\d+).*", "$1"));
        int cols = Integer.parseInt(grid.attr("style").replaceAll(".*--cols:\\s*(\\d+).*", "$1"));

        int[][] board = new int[rows][cols];
        HashMap<String, String> constraints = new HashMap<>();

        Elements cells = grid.select(".lotka-cell");
        for (Element cell : cells) {
            int idx = Integer.parseInt(cell.attr("data-cell-idx"));
            int row = idx / cols;
            int col = idx % cols;

            Element svg = cell.selectFirst(".lotka-cell-content svg[aria-label]");
            if (svg != null) {
                String label = svg.attr("aria-label");
                if (label.equals("Sun")) {
                    board[row][col] = TangoPuzzle.SUN;
                } else if (label.equals("Moon")) {
                    board[row][col] = TangoPuzzle.MOON;
                }
            }

            parseConstraints(cell, constraints, row, col);
        }

        return new TangoPuzzle(board, constraints);
    }

    private void parseConstraints(Element cell, HashMap<String, String> constraints, int row, int col) {
        for (Element edge : cell.select(".lotka-cell-edge")) {
            String classes = edge.className();
            Element svg = edge.selectFirst("svg[aria-label]");
            if (svg == null) continue;

            String constraintType = svg.attr("aria-label").equals("Equal") ? TangoPuzzle.EQUAL : TangoPuzzle.OPPOSITE;

            if (classes.contains("lotka-cell-edge--right")) {
                constraints.put(constraintKey(row, col, row, col + 1), constraintType);
                constraints.put(constraintKey(row, col + 1, row, col), constraintType);
            } else if (classes.contains("lotka-cell-edge--down")) {
                constraints.put(constraintKey(row, col, row + 1, col), constraintType);
                constraints.put(constraintKey(row + 1, col, row, col), constraintType);
            }
        }
    }

    private String constraintKey(int r1, int c1, int r2, int c2) {
        return r1 + "," + c1 + "-" + r2 + "," + c2;
    }
}
