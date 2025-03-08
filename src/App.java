// App.java (Updated with Thicker 3x3 Grid Lines)
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.border.Border; // Added for BorderFactory

public class App extends JFrame {
    private Sudoku sudoku;
    private JTextField[][] cells;
    private Timer timer;
    private JLabel timerLabel;
    private JLabel errorLabel;
    private JLabel statusLabel;
    private JButton showSolutionButton;
    private int errorCount = 0;
    private int timeSeconds = 0;
    private boolean showingSolution = false;
    private JPanel gamePanel;
    private JPanel controlPanel;
    private JPanel levelPanel;

    public App() {
        setTitle("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);

        Container contentPane = getContentPane();
        contentPane.setLayout(new CardLayout());

        JPanel welcomeContainer = new JPanel(new BorderLayout());
        JPanel welcomePanel = new JPanel();
        welcomePanel.add(new JLabel("Welcome to Sudoku!"));

        levelPanel = new JPanel();
        JButton easyButton = new JButton("Easy");
        JButton mediumButton = new JButton("Medium");
        JButton hardButton = new JButton("Hard");

        levelPanel.add(easyButton);
        levelPanel.add(mediumButton);
        levelPanel.add(hardButton);

        welcomeContainer.add(welcomePanel, BorderLayout.NORTH);
        welcomeContainer.add(levelPanel, BorderLayout.CENTER);

        gamePanel = new JPanel(new GridLayout(9, 9));
        cells = new JTextField[9][9];
        sudoku = new Sudoku();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new JTextField();
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setFont(new Font("Arial", Font.PLAIN, 20));

                ((AbstractDocument)cells[i][j].getDocument()).setDocumentFilter(new DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                            throws BadLocationException {
                        String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                        String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
                        if (newText.length() <= 1 && (text.isEmpty() || text.matches("[1-9]"))) {
                            super.replace(fb, offset, length, text, attrs);
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                            showTemporaryStatus("Only single digits 1-9 allowed!");
                        }
                    }
                });

                // Add thicker 3x3 grid lines here
                int top = (i % 3 == 0) ? 2 : 1;
                int left = (j % 3 == 0) ? 2 : 1;
                int bottom = ((i + 1) % 3 == 0) ? 2 : 1;
                int right = ((j + 1) % 3 == 0) ? 2 : 1;
                cells[i][j].setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));

                gamePanel.add(cells[i][j]);

                int finalI = i;
                int finalJ = j;
                cells[i][j].addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        validateInput(finalI, finalJ);
                    }
                });
            }
        }

        controlPanel = new JPanel();
        timerLabel = new JLabel("Time: 00:00");
        errorLabel = new JLabel("Errors: 0");
        statusLabel = new JLabel("");
        JButton hintButton = new JButton("Hint");
        JButton resetButton = new JButton("Reset");
        showSolutionButton = new JButton("Show Solution");

        controlPanel.add(timerLabel);
        controlPanel.add(errorLabel);
        controlPanel.add(statusLabel);
        controlPanel.add(hintButton);
        controlPanel.add(resetButton);
        controlPanel.add(showSolutionButton);

        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.add(gamePanel, BorderLayout.CENTER);
        gameContainer.add(controlPanel, BorderLayout.SOUTH);

        contentPane.add(welcomeContainer, "Welcome");
        contentPane.add(gameContainer, "Game");

        timer = new Timer(1000, e -> updateTimer());

        easyButton.addActionListener(e -> startGame(30));
        mediumButton.addActionListener(e -> startGame(40));
        hardButton.addActionListener(e -> startGame(50));

        hintButton.addActionListener(e -> showHint());
        resetButton.addActionListener(e -> resetGame());
        showSolutionButton.addActionListener(e -> toggleSolution());

        ((CardLayout)contentPane.getLayout()).show(contentPane, "Welcome");
    }

    private void showTemporaryStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
        Timer statusTimer = new Timer(2000, e -> statusLabel.setText(""));
        statusTimer.setRepeats(false);
        statusTimer.start();
    }

    private void startGame(int emptyCells) {
        sudoku.generatePuzzle(emptyCells);
        updateBoard();
        CardLayout cl = (CardLayout)(getContentPane().getLayout());
        cl.show(getContentPane(), "Game");
        errorCount = 0;
        timeSeconds = 0;
        timerLabel.setText("Time: 00:00");
        errorLabel.setText("Errors: 0");
        statusLabel.setText("");
        if (timer.isRunning()) {
            timer.stop();
        }
        timer.restart();
        gamePanel.requestFocus();
    }

    private void updateBoard() {
        int[][] board = sudoku.getBoard();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != 0) {
                    cells[i][j].setText(String.valueOf(board[i][j]));
                    cells[i][j].setEditable(false);
                    cells[i][j].setBackground(Color.LIGHT_GRAY);
                } else {
                    cells[i][j].setText("");
                    cells[i][j].setEditable(true);
                    cells[i][j].setBackground(Color.WHITE);
                }
            }
        }
    }

    private void validateInput(int row, int col) {
        String text = cells[row][col].getText();
        if (!text.isEmpty()) {
            try {
                int value = Integer.parseInt(text);
                if (!sudoku.isValidMove(row, col, value)) {
                    errorCount++;
                    errorLabel.setText("Errors: " + errorCount);
                    cells[row][col].setBackground(Color.PINK);
                } else {
                    cells[row][col].setBackground(Color.WHITE);
                    checkWin();
                }
            } catch (NumberFormatException e) {
                // Shouldn't happen with filter
            }
        }
    }

    private void updateTimer() {
        timeSeconds++;
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    private void showHint() {
        int[][] solution = sudoku.getSolution();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].getText().isEmpty()) {
                    cells[i][j].setText(String.valueOf(solution[i][j]));
                    cells[i][j].setBackground(Color.YELLOW);
                    return;
                }
            }
        }
    }

    private void resetGame() {
        timer.stop();
        timeSeconds = 0;
        errorCount = 0;
        timerLabel.setText("Time: 00:00");
        errorLabel.setText("Errors: 0");
        statusLabel.setText("");
        updateBoard();
        timer.restart();
    }

    private void toggleSolution() {
        if (!showingSolution) {
            int[][] solution = sudoku.getSolution();
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    cells[i][j].setText(String.valueOf(solution[i][j]));
                }
            }
            showSolutionButton.setText("Hide Solution");
            showingSolution = true;
        } else {
            updateBoard();
            showSolutionButton.setText("Show Solution");
            showingSolution = false;
        }
    }

    private void checkWin() {
        int[][] solution = sudoku.getSolution();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String cellText = cells[i][j].getText();
                if (cellText.isEmpty() || Integer.parseInt(cellText) != solution[i][j]) {
                    return;
                }
            }
        }
        timer.stop();
        JOptionPane.showMessageDialog(this,
                "Congratulations! You won!\nTime: " + timerLabel.getText() +
                        "\nErrors: " + errorCount,
                "Victory!",
                JOptionPane.INFORMATION_MESSAGE);

        int playAgainChoice = JOptionPane.showConfirmDialog(this,
                "Would you like to play again?",
                "Play Again?",
                JOptionPane.YES_NO_OPTION);

        if (playAgainChoice == JOptionPane.YES_OPTION) {
            CardLayout cl = (CardLayout)(getContentPane().getLayout());
            cl.show(getContentPane(), "Welcome");
        } else if (playAgainChoice == JOptionPane.NO_OPTION) {
            int closeChoice = JOptionPane.showConfirmDialog(this,
                    "Would you like to close the game?",
                    "Close Game?",
                    JOptionPane.YES_NO_OPTION);

            if (closeChoice == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
            // If "No" is selected, stay on the current game screen
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}