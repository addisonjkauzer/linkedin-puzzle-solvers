package com;

import com.Pinpoint.PinpointPuzzleParser;
import com.Pinpoint.PinpointPuzzleWebInterface;
import com.Queens.QueensPuzzleParser;
import com.Queens.QueensPuzzleWebInterface;
import com.Sudoku.SudokuPuzzleParser;
import com.Sudoku.SudokuPuzzleWebInterface;
import com.Tango.TangoPuzzleParser;
import com.Tango.TangoPuzzleWebInterface;
import com.Zip.ZipPuzzleParser;
import com.Zip.ZipPuzzleWebInterface;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LambdaHandler {

    public static void main(String[] args) throws Exception {
        String event = System.getProperty("LAMBDA_EVENT", "");
        JsonNode node = new ObjectMapper().readTree(event);

        if (node.path("warmup").asBoolean(false)) {
            return;
        }

        String puzzle = node.get("puzzle").asText();

        switch (puzzle.toUpperCase()) {
            case "ZIP"      -> new ZipPuzzleWebInterface(new ZipPuzzleParser()).visualizeAlgorithm(true, true);
            case "SUDOKU"   -> new SudokuPuzzleWebInterface(new SudokuPuzzleParser()).visualizeAlgorithm(true);
            case "TANGO"    -> new TangoPuzzleWebInterface(new TangoPuzzleParser()).visualizeAlgorithm(true);
            case "QUEENS"   -> new QueensPuzzleWebInterface(new QueensPuzzleParser()).visualizeAlgorithm(true);
            case "PINPOINT" -> new PinpointPuzzleWebInterface(new PinpointPuzzleParser()).visualizeAlgorithm(true);
            default         -> throw new IllegalArgumentException("Unknown puzzle type: " + puzzle);
        }
    }

}
