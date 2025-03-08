// Sudoku.java
import java.util.Random;

public class Sudoku {
    private int[][] board;
    private int[][] solution;
    private Random random;

    public Sudoku() {
        board = new int[9][9];
        solution = new int[9][9];
        random = new Random();
    }

    public void generatePuzzle(int emptyCells) {
        // First generate a complete solution
        solveSudoku(true);
        // Copy to solution array
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, solution[i], 0, 9);
        }
        // Remove numbers to create puzzle
        removeCells(emptyCells);
    }

    private void removeCells(int count) {
        int removed = 0;
        while (removed < count) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                removed++;
            }
        }
    }

    private boolean solveSudoku(boolean random) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
                    if (random) {
                        shuffleArray(numbers);
                    }
                    for (int num : numbers) {
                        if (isValidMove(row, col, num)) {
                            board[row][col] = num;
                            if (solveSudoku(random)) {
                                return true;
                            }
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private void shuffleArray(int[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = arr[index];
            arr[index] = arr[i];
            arr[i] = temp;
        }
    }

    public boolean isValidMove(int row, int col, int num) {
        // Check row
        for (int x = 0; x < 9; x++) {
            if (board[row][x] == num) {
                return false;
            }
        }

        // Check column
        for (int x = 0; x < 9; x++) {
            if (board[x][col] == num) {
                return false;
            }
        }

        // Check 3x3 box
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i + startRow][j + startCol] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] getBoard() {
        return board.clone();
    }

    public int[][] getSolution() {
        return solution.clone();
    }
}