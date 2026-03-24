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

public class LambdaHandler {

    public static void main(String[] args) {
        new ZipPuzzleWebInterface(new ZipPuzzleParser()).visualizeAlgorithm(true, true);
        new SudokuPuzzleWebInterface(new SudokuPuzzleParser()).visualizeAlgorithm(true);
        new TangoPuzzleWebInterface(new TangoPuzzleParser()).visualizeAlgorithm(true);
        new QueensPuzzleWebInterface(new QueensPuzzleParser()).visualizeAlgorithm(true);
        new PinpointPuzzleWebInterface(new PinpointPuzzleParser()).visualizeAlgorithm(true);
    }
}
