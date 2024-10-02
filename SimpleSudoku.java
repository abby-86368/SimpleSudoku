import java.awt.*;
import java.io.*; // Import for file I/O
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SimpleSudoku extends JFrame {
    private static final int SIZE = 9;
    private static final int SUBGRID = 3;
    private static final int EMPTY = 0;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private int[][] board = new int[SIZE][SIZE];
    private int[][] solutionBoard = new int[SIZE][SIZE];
    private JPanel confettiPanel;
    private JButton doneButton;
    private JLabel timerLabel;
    private Timer timer;
    private int elapsedTime;
    private int highScore = Integer.MAX_VALUE; // Initialize high score to max value
    private static final String EASY_HIGH_SCORE_FILE = "easy_highscore.txt"; // File for easy high score
    private static final String HARD_HIGH_SCORE_FILE = "hard_highscore.txt"; // File for hard high score
    private int numberOfRemovals; // Number of cells to remove based on difficulty

    public SimpleSudoku() {
        showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        JDialog welcomeDialog = new JDialog(this, "Welcome to Sudoku", true);
        welcomeDialog.setSize(400, 200);
        welcomeDialog.setLayout(new FlowLayout());

        JLabel welcomeLabel = new JLabel("Welcome to Sudoku! Enjoy.");
        welcomeDialog.add(welcomeLabel);

        JButton easyButton = new JButton("Easy");
        easyButton.addActionListener(e -> {
            numberOfRemovals = 20; // Easy level
            welcomeDialog.dispose();
            startGame();
        });
        welcomeDialog.add(easyButton);

        JButton hardButton = new JButton("Hard");
        hardButton.addActionListener(e -> {
            numberOfRemovals = 40; // Hard level
            welcomeDialog.dispose();
            startGame();
        });
        welcomeDialog.add(hardButton);

        welcomeDialog.setVisible(true);
    }

    private void startGame() {
        setTitle("Sudoku Game");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        timerLabel = new JLabel("Time: 0 seconds");
        add(timerLabel, BorderLayout.NORTH); // Add timer label to the top

        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        initializeBoard(gridPanel);
        add(gridPanel, BorderLayout.CENTER);

        doneButton = new JButton("Done");
        doneButton.addActionListener(e -> checkWin());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(doneButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadHighScore(); // Load the high score when the game starts
        generateSolvablePuzzle();
        startTimer();
        setVisible(true);
    }

    private void initializeBoard(JPanel gridPanel) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                cells[i][j] = new JTextField();
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setFont(new Font("Arial", Font.BOLD, 20));
                cells[i][j].addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        validateInput((JTextField) evt.getSource());
                    }
                });
                gridPanel.add(cells[i][j]);
            }
        }
    }

    private void validateInput(JTextField textField) {
        String text = textField.getText();
        if (text.isEmpty()) {
            return; // Allow empty input
        }
        try {
            int value = Integer.parseInt(text);
            if (value < 1 || value > 9) {
                showError("Please enter a number between 1 and 9.");
                textField.setText(""); // Clear invalid input
            } else {
                // Check if the number is valid for the current row, column, and subgrid
                int row = -1, col = -1;
                for (int i = 0; i < SIZE; i++) {
                    for (int j = 0; j < SIZE; j++) {
                        if (cells[i][j] == textField) {
                            row = i;
                            col = j;
                            break;
                        }
                    }
                }
                if (!isValidMove(row, col, value)) {
                    showError("Invalid number for this position.");
                    textField.setText(""); // Clear invalid input
                }
            }
        } catch (NumberFormatException e) {
            showError("Invalid input! Please enter a number between 1 and 9.");
            textField.setText(""); // Clear invalid input
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void generateSolvablePuzzle() {
        fillBoard();
        // Copy filled board to solution board
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(board[i], 0, solutionBoard[i], 0, SIZE);
        }
        removeNumbers();
    }

    private void fillBoard() {
        fillDiagonal();
        fillRemaining(0, SUBGRID);
    }

    private void fillDiagonal() {
        for (int i = 0; i < SIZE; i += SUBGRID) {
            fillSubGrid(i, i);
        }
    }

    private void fillSubGrid(int row, int col) {
        Random rand = new Random();
        for (int i = 0; i < SUBGRID; i++) {
            for (int j = 0; j < SUBGRID; j++) {
                int num;
                do {
                    num = rand.nextInt(SIZE) + 1;
                } while (!isValidInSubGrid(row, col, num));
                board[row + i][col + j] = num;
            }
        }
    }

    private boolean isValidInSubGrid(int row, int col, int num) {
        for (int i = 0; i < SUBGRID; i++) {
            for (int j = 0; j < SUBGRID; j++) {
                if (board[row + i][col + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean fillRemaining(int i, int j) {
        if (j >= SIZE && i < SIZE - 1) {
            i++;
            j = 0;
        }
        if (i >= SIZE && j >= SIZE) {
            return true;
        }
        if (i < SUBGRID) {
            if (j < SUBGRID) {
                j = SUBGRID;
            }
        } else if (i < SIZE - SUBGRID) {
            if (j == (i / SUBGRID) * SUBGRID) {
                j += SUBGRID;
            }
        } else {
            if (j == SIZE - SUBGRID) {
                i++;
                j = 0;
                if (i >= SIZE) {
                    return true;
                }
            }
        }
        for (int num = 1; num <= SIZE; num++) {
            if (isValidMove(i, j, num)) {
                board[i][j] = num;
                if (fillRemaining(i, j + 1)) {
                    return true;
                }
                board[i][j] = EMPTY;
            }
        }
        return false;
    }

    private void removeNumbers() {
        Random rand = new Random();
        int count = numberOfRemovals; // Use the selected difficulty level
        while (count != 0) {
            int cellId = rand.nextInt(SIZE * SIZE);
            int i = cellId / SIZE;
            int j = cellId % SIZE;
            if (board[i][j] != EMPTY) {
                board[i][j] = EMPTY;
                cells[i][j].setText("");
                count--;
            }
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != EMPTY) {
                    cells[i][j].setText(String.valueOf(board[i][j]));
                    cells[i][j].setEditable(false);
                }
            }
        }
    }

    private boolean isValidMove(int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num) {
                return false;
            }
        }
        int startRow = row / SUBGRID * SUBGRID;
        int startCol = col / SUBGRID * SUBGRID;
        for (int i = 0; i < SUBGRID; i++) {
            for (int j = 0; j < SUBGRID; j++) {
                if (board[startRow + i][startCol + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startTimer() {
        elapsedTime = 0;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime++;
                SwingUtilities.invokeLater(() -> timerLabel.setText("Time: " + elapsedTime + " seconds"));
            }
        }, 1000, 1000); // Update every second
    }

    private void stopTimer() {
        timer.cancel();
    }

    private void loadHighScore() {
        String fileName = (numberOfRemovals == 20) ? EASY_HIGH_SCORE_FILE : HARD_HIGH_SCORE_FILE;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException | NumberFormatException e) {
            highScore = Integer.MAX_VALUE; // Reset on error
        }
    }

    private void saveHighScore() {
        String fileName = (numberOfRemovals == 20) ? EASY_HIGH_SCORE_FILE : HARD_HIGH_SCORE_FILE;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkWin() {
        boolean isCorrect = true;

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (cells[i][j].getText().isEmpty()) {
                    isCorrect = false;  // Cell is empty
                    break;
                }
                int value;
                try {
                    value = Integer.parseInt(cells[i][j].getText());
                    if (value != solutionBoard[i][j]) { // Check against the solutionBoard
                        isCorrect = false;  // Value does not match
                        break;
                    }
                } catch (NumberFormatException e) {
                    isCorrect = false;  // Not a valid number
                    break;
                }
            }
            if (!isCorrect) break;
        }

        if (isCorrect) {
            stopTimer(); // Stop the timer when the puzzle is solved
            checkHighScore(); // Check for new high score
            showCongratulations();
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect solution! Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkHighScore() {
        if (elapsedTime < highScore) {
            highScore = elapsedTime; // Update high score
            saveHighScore(); // Save new high score to file
            JOptionPane.showMessageDialog(this, "New High Score! Time: " + highScore + " seconds", "High Score", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showCongratulations() {
        JOptionPane.showMessageDialog(this, "Congratulations! You solved the puzzle!", "Congratulations", JOptionPane.INFORMATION_MESSAGE);
        showConfetti();
    }

    private void showConfetti() {
        confettiPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Random rand = new Random();
                for (int i = 0; i < 100; i++) {
                    int x = rand.nextInt(getWidth());
                    int y = rand.nextInt(getHeight());
                    int size = rand.nextInt(10) + 5;
                    g.setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
                    g.fillOval(x, y, size, size);
                }
            }
        };

        confettiPanel.setOpaque(false);
        confettiPanel.setBounds(0, 0, getWidth(), getHeight());

        // Create a layered pane and add the confetti panel to it
        JLayeredPane layeredPane = getLayeredPane();
        layeredPane.add(confettiPanel, JLayeredPane.PALETTE_LAYER);

        // Set a Timer to repaint the confetti panel
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                confettiPanel.repaint();
            }
        }, 0, 100);

        // Ensure focus and repaint the main frame
        confettiPanel.requestFocus();
        revalidate();
        repaint();

        // Add a component listener to resize the confetti panel when the JFrame is resized
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                confettiPanel.setBounds(0, 0, getWidth(), getHeight());
                confettiPanel.repaint(); // Optional: repaint after resizing
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimpleSudoku::new);
    }
}
