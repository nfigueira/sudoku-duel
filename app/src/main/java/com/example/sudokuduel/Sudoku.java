package com.example.sudokuduel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

class Sudoku {

    private class Square {
        int box;
        int square;
        Square(int b, int s) {
            box = b;
            square = s;
        }
    }

    /* Each block, then which square within the block. */
    int[][] puzzle;
    int[][] solution;
    int length;
    int height;
    int numLeft;

    Sudoku(int length, int height) {
        this.length = length;
        this.height = height;
        int nums = length * height;
        puzzle = new int[nums][nums];
        solution = new int[nums][nums];

        fillInPuzzle();

        removeNums(64);
    }

    Sudoku(List<Long> puzzleFlat, List<Long> solutionFlat) {
        //TODO: make height and length changeable?
        this.length = 3;
        this.height = 3;
        this.numLeft = 0;
        int nums = length * height;
        puzzle = new int[nums][nums];
        solution = new int[nums][nums];
        for (int b = 0; b < length * height; b++) {
            for (int s = 0; s < length * height; s++) {
                puzzle[b][s] = puzzleFlat.get(b * nums + s).intValue();
                solution[b][s] = solutionFlat.get(b * nums + s).intValue();
                if (puzzle[b][s] == 0) {
                    numLeft += 1;
                }
            }
        }
    }

    List<Integer> getPuzzleFlat() {
        int nums = length * height;
        List<Integer> puzzleFlat = new ArrayList<>();
        for (int b = 0; b < length * height; b++) {
            for (int s = 0; s < length * height; s++) {
                puzzleFlat.add(puzzle[b][s]);
            }
        }
        return puzzleFlat;
    }

    List<Integer> getSolutionFlat() {
        int nums = length * height;
        List<Integer> solutionFlat = new ArrayList<>();
        for (int b = 0; b < length * height; b++) {
            for (int s = 0; s < length * height; s++) {
                solutionFlat.add(solution[b][s]);
            }
        }
        return solutionFlat;
    }

    private void removeNums(int max) {
        List<Square> removable = new ArrayList<>();
        for (int b = 0; b < length * height; b++) {
            for (int s = 0; s < length * height; s++) {
                removable.add(new Square(b, s));
            }
        }

        // Randomly remove max numbers
        Collections.shuffle(removable);
        for (int i = 0; i < max; i++) {
            Square toRemove = removable.get(i);
            puzzle[toRemove.box][toRemove.square] = 0;
        }
        Set<Integer>[] boxesAvailable = new HashSet[length * height];
        Set<Integer>[] rowsAvailable = new HashSet[length * height];
        Set<Integer>[] colsAvailable = new HashSet[length * height];
        getAllAvailable(boxesAvailable, rowsAvailable, colsAvailable);
        while (hasOneSolution(0, 0, boxesAvailable, rowsAvailable, colsAvailable) != 1) {
            Square toRemove = removable.get(--max);
            puzzle[toRemove.box][toRemove.square] = solution[toRemove.box][toRemove.square];
            getAllAvailable(boxesAvailable, rowsAvailable, colsAvailable);
        }
        numLeft = max;
    }

    private void getAllAvailable(Set<Integer>[] boxesAvailable, Set<Integer>[] rowsAvailable,
                                 Set<Integer>[] colsAvailable) {
        for (int i = 0; i < length * height; i++) {
            boxesAvailable[i] = fullSet();
            for (int square = 0; square < length * height; square++) {
                boxesAvailable[i].remove(puzzle[i][square]);
            }
            rowsAvailable[i] = fullSet();
            for (int box = i / height * height; box < i / height * height + height; box++) {
                for (int square = (i % length) * length; square < (i % length) * length + length; square++) {
                    rowsAvailable[i].remove(puzzle[box][square]);
                }
            }
            colsAvailable[i] = fullSet();
            for (int box = i / length; box < length * height; box += height) {
                for (int square = i % length; square < length * height; square += length) {
                    colsAvailable[i].remove(puzzle[box][square]);
                }
            }
        }
    }

    private int hasOneSolution(int box, int square, Set<Integer>[] boxesAvailable,
                               Set<Integer>[] rowsAvailable, Set<Integer>[] colsAvailable) {
        // Find the next empty square
        while (box < length * height) {
            while (square < length * height && puzzle[box][square] != 0) {
                square++;
            }
            if (square < length * height && puzzle[box][square] == 0) {
                break;
            }
            square = 0;
            box++;
        }

        // If we've reached the end, then we've found a solution
        if (box == length * height) {
            return 1;
        }

        int numSolutions = 0;

        // Get which numbers are available
        Set<Integer> col = colsAvailable[box % height * length + square % length];
        Set<Integer> row = rowsAvailable[(box / length) * height + square / length];
        Set<Integer> boxAvailable = boxesAvailable[box];
        Set<Integer> available = new HashSet<>(boxAvailable);
        available.retainAll(col);
        available.retainAll(row);

        // Iterate through them to find solutions recursively
        for (int num: available) {
            col.remove(num);
            row.remove(num);
            boxAvailable.remove(num);
            int nextBox = box + (square + 1) / (length * height);
            int nextSquare = (square + 1) % (length * height);
            numSolutions += hasOneSolution(nextBox, nextSquare, boxesAvailable, rowsAvailable, colsAvailable);
            if (numSolutions > 1) {
                break;
            }
            col.add(num);
            row.add(num);
            boxAvailable.add(num);
        }

        return numSolutions;
    }

    private void fillInPuzzle() {
        int nums = length * height;

        // Remaining numbers in the columns
        Set<Integer>[] columns = new HashSet[nums];

        Set<Integer>[] rows = new HashSet[height];
        for (int i = 0; i < nums; i++) {
            // Remaining numbers in the relevant rows
            if (i % height == 0) {
                for (int j = 0; j < height; j++) {
                    rows[j] = fullSet();
                }
            }

            if (i == 0) {
                for (int j = 0; j < nums; j++) {
                    columns[j] = fullSet();
                }
            }

            // Remaining numbers in the box
            Set<Integer> box = fullSet();
            Set<Integer> column;
            Set<Integer> row = rows[0];
            // For the current box
            for (int j = 0; j < nums; j++) {
                column = columns[i % height * length + j % length];
                if (j % length == 0) {
                    row = rows[j / length];
                }

                Set<Integer> available = new HashSet<>(box);
                available.retainAll(column);
                available.retainAll(row);

                Iterator<Integer> availableIter = available.iterator();
                if (available.size() == 0) {
                    i = -1;
                    break;
                }
                for (int k = 0; k < (int) (Math.random() * available.size()); k++) {
                    availableIter.next();
                }
                int num = availableIter.next();
                puzzle[i][j] = num;
                solution[i][j] = num;
                box.remove(num);
                column.remove(num);
                row.remove(num);
            }
        }
    }

    private Set<Integer> fullSet() {
        Set<Integer> fullSet = new HashSet<>();
        for (int i = 1; i <= this.length * this.height; i++) {
            fullSet.add(i);
        }
        return fullSet;
    }

    int getPuzzle(int outerBox, int innerBox) {
        return puzzle[outerBox - 1][innerBox - 1];
    }

    boolean setPuzzle(int outerBox, int innerBox, int num) {
        if (puzzle[outerBox - 1][innerBox - 1] == solution[outerBox - 1][innerBox - 1]) {
            numLeft++;
        }
        puzzle[outerBox - 1][innerBox - 1] = num;
        if (puzzle[outerBox - 1][innerBox - 1] == solution[outerBox - 1][innerBox - 1]) {
            numLeft--;
        }
        return numLeft == 0;
    }
}
