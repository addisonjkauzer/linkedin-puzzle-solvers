package com.Sudoku;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class SudokuPuzzleParser {

    public int[][] parse(String html) {
        Document document = Jsoup.parse(html);
        Element grid = document.selectFirst(".sudoku-grid.grid-game-board");
        if (grid == null) {
            throw new RuntimeException("Could not find sudoku-grid in HTML");
        }

        int rows = Integer.parseInt(grid.attr("style").replaceAll(".*--rows:\\s*(\\d+).*", "$1"));
        int cols = Integer.parseInt(grid.attr("style").replaceAll(".*--cols:\\s*(\\d+).*", "$1"));

        int[][] board = new int[rows][cols];

        Elements cells = grid.select(".sudoku-cell");
        for (Element cell : cells) {
            int idx = Integer.parseInt(cell.attr("data-cell-idx"));
            int row = idx / cols;
            int col = idx % cols;

            Element content = cell.selectFirst(".sudoku-cell-content");
            if (content != null && cell.hasClass("sudoku-cell-prefilled")) {
                String text = content.text().trim();
                if (!text.isEmpty()) {
                    board[row][col] = Integer.parseInt(text);
                }
            }
        }

        return board;
    }
}
