package com.queens;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class QueensPuzzleParser {

    public QueensPuzzle parse(String html) {
        Document document = Jsoup.parse(html);
        Element grid = document.selectFirst("#queens-grid");
        if (grid == null) {
            throw new RuntimeException("Could not find queens-grid in HTML");
        }

        int rows = Integer.parseInt(grid.attr("style").replaceAll(".*--rows:\\s*(\\d+).*", "$1"));
        int cols = Integer.parseInt(grid.attr("style").replaceAll(".*--cols:\\s*(\\d+).*", "$1"));

        int[][] colorGrid = new int[rows][cols];
        int[][] solution = new int[rows][cols];

        Elements cells = grid.select(".queens-cell-with-border");
        for (Element cell : cells) {
            int idx = Integer.parseInt(cell.attr("data-cell-idx"));
            int row = idx / cols;
            int col = idx % cols;

            String colorClass = cell.classNames().stream()
                    .filter(c -> c.startsWith("cell-color-"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No color class found for cell " + idx));
            colorGrid[row][col] = Integer.parseInt(colorClass.replace("cell-color-", ""));

            String ariaLabel = cell.attr("aria-label");
            if (ariaLabel.startsWith("Queen of color")) {
                solution[row][col] = 1;
            }
        }

        return new QueensPuzzle(colorGrid, solution);
    }
}
