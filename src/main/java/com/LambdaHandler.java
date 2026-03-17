package com;

import com.Sudoku.SudokuPuzzleParser;
import com.Sudoku.SudokuPuzzleWebInterface;
import com.Tango.TangoPuzzleParser;
import com.Tango.TangoPuzzleWebInterface;
import com.Zip.ZipPuzzleParser;
import com.Zip.ZipPuzzleWebInterface;

public class LambdaHandler {

    public static void main(String[] args) {
        new SudokuPuzzleWebInterface(new SudokuPuzzleParser()).visualizeAlgorithm(true);
        new TangoPuzzleWebInterface(new TangoPuzzleParser()).visualizeAlgorithm(true);
        new ZipPuzzleWebInterface(new ZipPuzzleParser()).visualizeAlgorithm(true, true);
    }
}
