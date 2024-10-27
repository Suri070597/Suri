/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameantsweeper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author admin
 */
public class GameAntsweeper extends javax.swing.JFrame implements ActionListener, KeyListener {

    // The number of bombs in the game.
    private int BOM;

    // Counter for the number of bombs encountered or flagged.
    private int countBom = 0;

    // The current difficulty level of the game.
    private int level;

    // The number of hints available to the player.
    private int hint;

    // Timer for the game duration.
    private int time;

    // Maximum size for the game grid (can be adjusted if needed).
    private int max = 100;

    // Indicates whether the game is over.
    private boolean isGameOver = false;

    // Flag to determine if the user is in flag mode (to mark bombs).
    private boolean flag = false;

    // Indicates if the game timer is currently running.
    private boolean isRunGame = true;

    // Thread to manage the game timer.
    private Thread timer;

    // Number of rows in the game grid.
    private int row;

    // Number of columns in the game grid.
    private int column;

    // Predefined row sizes for different levels of difficulty.
    private int getRow[] = {8, 10, 12}; // Number of rows for levels 1, 2, and 3 respectively

    // Predefined column sizes for different levels of difficulty.
    private int getColumn[] = {10, 12, 14}; // Number of columns for levels 1, 2, and 3 respectively

    // Predefined number of mines for each difficulty level.
    private int mines[] = {10, 20, 30}; // Number of mines for levels 1, 2, and 3 respectively

    // Array representing difficulty levels for easy management.
    private int levels[] = {1, 2, 3}; // Difficulty levels 

    // Array representing the number of hints for each difficulty level.
    private int hints[] = {5, 10, 15}; // Number of hints for levels 1, 2, and 3 respectively

    // Filenames for saving high scores or game statistics for each difficulty level.
    private String[] fileNames = {"top10__easy.txt", "top10__medium.txt", "top10__hard.txt"};

    // 2D array to store the game state, where each cell represents a game cell's value.
    private int values[][] = new int[max][max]; // Holds values like -1 for bombs, 0 for empty, and numbers for nearby bombs

    // 2D array of JButton objects representing the game grid, allowing for user interaction.
    private JButton button[][] = new JButton[max][max]; // Buttons corresponding to each cell in the grid

    // 2D boolean array to track which cells have been revealed or flagged.
    private boolean isTick[][] = new boolean[max][max]; // Tracks whether a cell has been revealed or not

    // List to store game-related strings, which might include names or scores.
    private List<String> listGame = new ArrayList<>(); // List to store player names or scores

    // Filename to be used for saving/loading game data, such as scores or settings.
    private String FILE_NAME;

    public void initGame(int lvl) {
        // Initialize the number of bombs based on the selected level.
        BOM = mines[lvl];

        // Display the number of bombs in the label for flags.
        lbl_Flag.setText("" + BOM);

        // Set the number of rows and columns based on the selected level.
        row = getRow[lvl];
        column = getColumn[lvl];

        // Set the game level and hint value based on the level.
        level = levels[lvl];
        hint = hints[lvl];

        // Set the file name for the data source based on the level.
        FILE_NAME = "src/datas/" + fileNames[lvl];

        // Initialize the bombs on the game board using the initBom() method.
        initBom();

        // Reset the game timer to 0.
        time = 0;

        // Clear all components from the panel and set the layout to a grid based on the row and column values.
        pnl_Body.removeAll();
        pnl_Body.setLayout(new GridLayout(row, column));

        // Initialize all buttons and flags for the game board.
        for (int i = 0; i <= row + 1; i++) {
            for (int j = 0; j <= column + 1; j++) {
                button[i][j] = new JButton();
                button[i][j].setText("");
                isTick[i][j] = true;
            }
        }

        // Add buttons to the grid panel, set action commands, and add event listeners for the buttons.
        for (int i = 1; i <= row; i++) {
            for (int j = 1; j <= column; j++) {
                button[i][j] = new JButton();
                button[i][j].setText("");
                pnl_Body.add(button[i][j]);
                button[i][j].setActionCommand(i + " " + j);
                button[i][j].addActionListener(this);
                button[i][j].addMouseListener(new MouseAdapter() {
                    // Change the icon when the mouse is pressed on a button (indicating a click action).
                    @Override
                    public void mousePressed(MouseEvent e) {
                        btn_Icon.setIcon(new ImageIcon(getClass().getResource("/imgs/oh.png")));
                    }

                    // Revert the icon back to the play icon when the mouse is released.
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        btn_Icon.setIcon(new ImageIcon(getClass().getResource("/imgs/play.png")));
                    }
                });
            }
        }

        // Refresh and repaint the panel to reflect the updated layout and buttons.
        pnl_Body.revalidate();
        pnl_Body.repaint();

        // Set up the "Question" button with an icon and add an event listener.
        btn_Question.setPreferredSize(new Dimension(55, 38));
        btn_Question.setIcon(new ImageIcon(getClass().getResource("/imgs/question.png")));
        btn_Question.setActionCommand("hint");
        btn_Question.addActionListener(this);

        // Set up the "Play" button with an icon and add an event listener.
        btn_Icon.setPreferredSize(new Dimension(55, 38));
        btn_Icon.setIcon(new ImageIcon(getClass().getResource("/imgs/play.png")));
        btn_Icon.setActionCommand("showSmileIcon");
        btn_Icon.addActionListener(this);

        // Set up the "Flag" button with an icon and add an event listener.
        btn_Flag.setPreferredSize(new Dimension(55, 38));
        btn_Flag.setIcon(new ImageIcon(getClass().getResource("/imgs/flag.png")));
        btn_Flag.setActionCommand("flag");
        btn_Flag.addActionListener(this);

        // Set up the "Level" button with an icon that corresponds to the current level, and add an event listener.
        btn_Level.setPreferredSize(new Dimension(55, 38));
        btn_Level.setIcon(new ImageIcon(getClass().getResource("/imgs/level_" + level + ".png")));
        btn_Level.setActionCommand(level + "");
        btn_Level.addActionListener(this);

        // Update the hint label with the number of available hints.
        lbl_Hint.setText(hint + "");

        // Add a mouse click listener to display the top 10 players when the "Top10" label is clicked.
        lbl_Top10.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ImageIcon icon = new ImageIcon(getClass().getResource("/imgs/cup.png"));
                JLabel messageLabel = new JLabel();
                String str = "";
                int count = 1;

                // Read the top player data from a file.
                readFile();

                // Display the top 3 players in bold, and others normally.
                for (String data : listGame) {
                    String[] cut = data.split(" ");
                    if (count <= 3) {
                        str += "<b>" + "Top" + count++ + " " + cut[0] + " " + cut[1] + "</b>" + "<br>";
                    } else {
                        str += "Top" + count++ + " " + cut[0] + " " + cut[1] + "<br>";
                    }
                }

                // If there are no players, display a message.
                if (str.isEmpty()) {
                    str = "There are no players.";
                }

                messageLabel.setFont(new Font("Arial", Font.PLAIN, 20));
                messageLabel.setText("<html>" + str + "</html>");
                JPanel panel = new JPanel();
                panel.add(messageLabel);

                // Show a dialog with the top 10 players.
                JOptionPane.showMessageDialog(null, panel, "Top 10 Players", JOptionPane.PLAIN_MESSAGE, icon);
                listGame.clear();
            }
        });

        // Add a mouse click listener to display the "About Game" message when the "Game" label is clicked.
        lbl_Game.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ImageIcon icon = new ImageIcon(getClass().getResource("/imgs/game.jpg"));
                JLabel messageLabel = new JLabel();
                String str = "";

                // Create a description of the game.
                str += "The game is a <b>Minesweeper</b> variant where players <br>";
                str += "uncover tiles on a grid, avoiding hidden mines. <br>";
                str += "The grid is made up of rows and columns, <br>";
                str += "and each tile can either be empty, contain a number <br>";
                str += "indicating nearby mines, or have a hidden mine. <br>";
                str += "Players can flag tiles they believe contain mines, and <br>";
                str += "the goal is to clear all non-mine tiles without hitting a mine.<br>";

                messageLabel.setFont(new Font("Arial", Font.PLAIN, 20));
                messageLabel.setText("<html>" + str + "</html>");
                JPanel panel = new JPanel();
                panel.add(messageLabel);

                // Show the "About Game" dialog.
                JOptionPane.showMessageDialog(null, panel, "About Game", JOptionPane.PLAIN_MESSAGE, icon);
            }
        });

        // Add a mouse click listener to display help instructions when the "Help" label is clicked.
        lbl_Help.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                ImageIcon icon = new ImageIcon(getClass().getResource("/imgs/help.png"));
                JLabel messageLabel = new JLabel();
                String str = "";

                // Create a detailed help description.
                str += "<h2 style='color:red;'>- How to Play: </h2><br>";
                str += "<b>+ Starting the Game: </b>Click on any tile to uncover it.<br>";
                str += "If you click on a mine, the game ends. <br>";
                str += "If you uncover an empty tile, it may reveal adjacent <br>";
                str += "tiles automatically if there are no mines nearby. <br>";
                str += "If a number appears, it shows how many mines are adjacent. <br>";
                str += "<b>+ Flagging Mines: </b>Click the flag icon to place a flag.<br>";
                str += "You can only place a flag if the tile is not already uncovered. <br>";
                str += "<b>+ Winning the Game: </b>The game is won when all non-mine tiles are uncovered. <br>";
                str += "If you uncover all safe tiles and have flagged all mines correctly, you win.<br>";
                str += "<h2 style='color:red;'>- Tips: </h2><br>";
                str += "+ Start with the corners or edges to uncover larger areas. <br>";
                str += "+ Use logic based on the numbers to deduce where mines are located.<br>";

                messageLabel.setFont(new Font("Arial", Font.PLAIN, 15));
                messageLabel.setText("<html>" + str + "</html>");
                JPanel panel = new JPanel();
                panel.add(messageLabel);

                // Show the "Help" dialog.
                JOptionPane.showMessageDialog(null, panel, "Help", JOptionPane.PLAIN_MESSAGE, icon);
            }
        });

        // Print the game board values (for debugging purposes).
        for (int i = 1; i <= row; i++) {
            for (int j = 1; j <= column; j++) {
                System.out.printf("%2s ", values[i][j]);
            }
            System.out.println("\n");
        }
    }

    // Initializes the bomb grid by placing bombs (mines) randomly and updating the grid values accordingly.
    public void initBom() {
        float ratio = (float) 0.05; // Probability ratio for bomb placement.
        int i, j;

        // Initialize the grid, setting all cells to 0 (no bombs).
        for (i = 0; i <= row + 1; i++) {
            for (j = 0; j <= column + 1; j++) {
                values[i][j] = 0; // 0 indicates no bomb in the cell.
            }
        }

        // Randomly place bombs on the grid until the number of bombs equals BOM.
        while (countBom < BOM) {
            do {
                Random rand = new Random();
                i = rand.nextInt(row - 1) + 1; // Random row index.
                j = rand.nextInt(column - 2) + 1; // Random column index.
            } while (values[i][j] != 0); // Ensure the selected cell does not already have a bomb.

            // If the cell is empty, initialize a bomb in that position.
            if (values[i][j] == 0) {
                init(i, j, ratio); // Call the init method to place the bomb and update surrounding cells.
            }
        }
    }

    // Places a bomb at the given grid location (i, j) and updates the adjacent cells' values.
    public void init(int i, int j, float ratio) {
        // If the random value is below the threshold, place a bomb.
        if (Math.random() < ratio) {
            values[i][j] = -1; // -1 indicates a bomb in the cell.

            // Update the values of the surrounding cells to indicate proximity to the bomb.
            for (int k = i - 1; k <= i + 1; k++) {
                for (int h = j - 1; h <= j + 1; h++) {
                    if (values[k][h] != -1) {
                        values[k][h]++; // Increment cell value to indicate nearby bomb.
                    }
                }
            }

            countBom++; // Increment bomb count.

            // Recursively initialize bombs in surrounding cells if they are empty and bomb count is below limit.
            for (int k = i - 1; k <= i + 1; k++) {
                for (int h = j - 1; h <= j + 1; h++) {
                    if (k > 0 && h > 0 && values[k][h] != -1 && countBom < BOM) {
                        init(k, h, ratio); // Recursive call to continue placing bombs.
                    }
                }
            }
        }
    }

    // Converts a given time in seconds to a formatted string (HH:mm:ss).
    public String int2time(int time) {
        return String.format("%02d:%02d:%02d", time / 3600, (time / 60) % 60, time % 60);
    }

    // Starts a timer in a new thread that increments the game time every second and updates the timer label.
    public void runTimer() {
        timer = new Thread() {
            public void run() {
                while (true) {
                    try {
                        time++; // Increment the time by 1 second.
                        lbl_Time.setText(int2time(time)); // Update the timer label with the formatted time.
                        Thread.sleep(1000); // Wait for 1 second.
                    } catch (InterruptedException ex) {
                        System.out.println("error: " + ex.getMessage()); // Print error if thread is interrupted.
                    }
                }
            }
        };

        timer.start(); // Start the timer thread.
    }

    // Opens the cell at the given coordinates (i, j) if it has not been previously opened and is not a bomb.
    public void open(int i, int j) {
        // Check if the cell has not been opened yet and is not a bomb.
        if (isTick[i][j] && values[i][j] != -1) {
            button[i][j].setText(String.valueOf(values[i][j])); // Display the number of surrounding bombs on the button.

            // Update the score if the cell is within valid bounds.
            if (i > 0 && i <= row && j > 0 && j <= column) {
                lbl_ScoreValue.setText(values[i][j] + Integer.parseInt(lbl_ScoreValue.getText()) + ""); // Increment score.
            }

            button[i][j].setBackground(Color.yellow); // Change the button's background color to yellow to indicate it's opened.
            isTick[i][j] = false; // Mark the cell as opened.
            checkWin(); // Check for a win condition.
        }
    }

    // Opens empty cells recursively around the given coordinates (i, j) until no more empty cells are found.
    public void openEmpty(int i, int j) {
        // Check if the current cell has not been opened yet.
        if (isTick[i][j]) {
            isTick[i][j] = false; // Mark the cell as opened.
            button[i][j].setBackground(Color.GRAY); // Change the button's background to gray to indicate it's opened.
            checkWin(); // Check for a win condition.

            // Loop through the surrounding cells to open them recursively.
            for (int h = i - 1; h <= i + 1; h++) {
                for (int k = j - 1; k <= j + 1; k++) {
                    // Check if the surrounding cell indices are within valid bounds.
                    if (h >= 0 && h <= row && k >= 0 && k <= column) {
                        // If the surrounding cell is empty, continue to open it recursively.
                        if (values[h][k] == 0 && isTick[h][k]) {
                            openEmpty(h, k);
                        } else {
                            open(h, k); // Otherwise, just open the cell.
                        }
                    }
                }
            }
        }
    }

    // Checks if the game has been won by ensuring all cells are opened.
    public void checkWin() {
        // Loop through all cells to check if any cell remains unopened.
        for (int i = 1; i <= row; i++) {
            for (int j = 1; j <= column; j++) {
                if (isTick[i][j]) {
                    return; // If any cell is still unopened, exit the method.
                }
            }
        }
        // If all cells are opened, set game over state.
        isGameOver = true;
        btn_Icon.setIcon(new ImageIcon(getClass().getResource("/imgs/win.png"))); // Change the button icon to a win image.
        timer.stop(); // Stop the game timer.
        JOptionPane.showMessageDialog(null, "You win!"); // Display a win message.
        saveGame(); // Save the game state.
    }

    // Saves the current game score and time to a file.
    public void saveGame() {
        readFile(); // Read the existing game scores from the file.

        // Get the current score from the label.
        int getCurrentScore = Integer.parseInt(lbl_ScoreValue.getText());

        // If the list already has 10 entries, remove the last one before adding the new score.
        if (listGame.size() == 10) {
            final int lastIndex = listGame.size() - 1;
            listGame.removeIf(item -> listGame.indexOf(item) == lastIndex);
            listGame.add("Score=" + getCurrentScore + " " + "Time=" + lbl_Time.getText()); // Add new score entry.
        } else {
            listGame.add("Score=" + getCurrentScore + " " + "Time=" + lbl_Time.getText()); // Add new score entry.
        }

        // Sort the scores in descending order based on score and time.
        Collections.sort(listGame, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] cut1 = o1.split(" "); // Split the first score entry.
                String[] cut2 = o2.split(" "); // Split the second score entry.
                int getScore1 = Integer.parseInt(cut1[0].substring(cut1[0].indexOf("=") + 1)); // Extract score.
                int getScore2 = Integer.parseInt(cut2[0].substring(cut2[0].indexOf("=") + 1)); // Extract score.
                String getTime1 = cut1[1].substring(cut1[1].indexOf("=") + 1); // Extract time.
                String getTime2 = cut2[1].substring(cut2[1].indexOf("=") + 1); // Extract time.
                if (getScore1 != getScore2) {
                    return getScore2 - getScore1; // Compare scores in descending order.
                } else {
                    return getTime1.compareTo(getTime2); // Compare times in ascending order if scores are equal.
                }
            }
        });

        // Write the sorted scores back to the file.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            int count = 1;
            for (String data : listGame) {
                writer.write("Top" + count++ + ": " + data); // Write each score entry.
                writer.newLine(); // New line for the next entry.
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage()); // Handle file writing errors.
            System.exit(0); // Exit the program if there's an error.
        }
        listGame.clear(); // Clear the list after saving.
    }

    // Reads the existing game scores from the file and populates the listGame.
    public void readFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cut = line.split("\\s+"); // Split the line by whitespace.
                listGame.add(cut[1] + " " + cut[2]); // Add the score and time to the list.
            }
        } catch (IOException e) {
            // Handle any errors that occur during file reading.
            System.out.println("Error: " + e.getMessage());
            System.exit(0); // Exit the program if there's an error.
        }
    }

    // Reveals all bombs on the board when the player loses the game.
    public void loss() {
        for (int i = 1; i <= row; i++) {
            for (int j = 1; j <= column; j++) {
                // Check for bombs and reveal them.
                if (values[i][j] == -1) {
                    button[i][j].setBackground(Color.WHITE); // Change the background to white for visibility.
                    ImageIcon icon = new ImageIcon(getClass().getResource("/imgs/ant.jpg")); // Load bomb image.
                    Image img = icon.getImage().getScaledInstance(30, 25, Image.SCALE_AREA_AVERAGING); // Resize the image.
                    button[i][j].setIcon(new ImageIcon(img)); // Set the bomb icon.
                }
            }
        }
    }

    // Adds or removes a flag on the cell at the given coordinates (i, j).
    public void addFlag(int i, int j) {
        // Check if the button text is empty (indicating the cell is not opened).
        if (button[i][j].getText() == " ") {
            isTick[i][j] = true; // Mark the cell as flagged.
            button[i][j].setIcon(null); // Remove any icon from the button.
            lbl_Flag.setText(Integer.parseInt(lbl_Flag.getText()) + 1 + ""); // Increment the flag count.
            button[i][j].setText(""); // Clear the button text.
        } else if (isTick[i][j] && Integer.parseInt(lbl_Flag.getText()) > 0) {
            // Remove the flag if the cell was already flagged and flags are available.
            isTick[i][j] = false; // Unmark the cell as flagged.
            button[i][j].setText(" "); // Set the button text to indicate it is unflagged.
            ImageIcon icon = new ImageIcon(getClass().getResource("/imgs/flags.png")); // Load the flag image.
            Image img = icon.getImage().getScaledInstance(30, 25, Image.SCALE_SMOOTH); // Resize the image.
            button[i][j].setIcon(new ImageIcon(img)); // Set the flag icon on the button.
            lbl_Flag.setText(Integer.parseInt(lbl_Flag.getText()) - 1 + ""); // Decrement the flag count.
            // Check if the flag count has reached zero, and toggle the flagging state.
            if (lbl_Flag.getText().equals("0")) {
                flag = !flag; // Toggle the flag state.
            }
        }
        checkWin(); // Check for win condition after adding/removing the flag.
    }

    /**
     * Creates new form GameMinesweeper
     *
     */
    public GameAntsweeper(String title, int level) {
        // Initialize the components of the frame (e.g., buttons, labels, etc.).
        initComponents();

        // Center the window on the screen.
        this.setLocationRelativeTo(null);

        // Set the title of the window to the provided title string.
        this.setTitle(title);

        // Automatically sizes the frame to fit the preferred size and layouts of its components.
        this.pack();

        // Set the size of the window based on the selected game level.
        if (level == 0) {
            this.setSize(850, 600); // Set size for level 0.
        }

        if (level == 1) {
            this.setSize(880, 600); // Set size for level 1.
        }

        if (level == 2) {
            this.setSize(880, 640); // Set size for level 2.
        }

        // Initialize the game with the specified level settings.
        initGame(level);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_GameInfor = new javax.swing.JPanel();
        btn_Question = new javax.swing.JButton();
        lbl_Game = new javax.swing.JLabel();
        lbl_Help = new javax.swing.JLabel();
        lbl_Top10 = new javax.swing.JLabel();
        lbl_Flag = new javax.swing.JLabel();
        lbl_Time = new javax.swing.JLabel();
        lbl_Hint = new javax.swing.JLabel();
        btn_Level = new javax.swing.JButton();
        btn_Icon = new javax.swing.JButton();
        btn_Flag = new javax.swing.JButton();
        lbl_Score = new javax.swing.JLabel();
        lbl_ScoreValue = new javax.swing.JLabel();
        pnl_Body = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setSize(new java.awt.Dimension(0, 0));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                none(evt);
            }
        });

        pnl_GameInfor.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Game Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 15))); // NOI18N

        btn_Question.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_Question.setPreferredSize(new java.awt.Dimension(5, 25));

        lbl_Game.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        lbl_Game.setText("Game");
        lbl_Game.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        lbl_Help.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        lbl_Help.setText("Help");
        lbl_Help.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        lbl_Top10.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        lbl_Top10.setText("Top10");
        lbl_Top10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        lbl_Flag.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        lbl_Flag.setText("10");

        lbl_Time.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        lbl_Time.setText("00:00:00");

        lbl_Hint.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        lbl_Hint.setText("3");

        btn_Level.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_Level.setPreferredSize(new java.awt.Dimension(5, 25));

        btn_Icon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_Icon.setPreferredSize(new java.awt.Dimension(5, 25));

        btn_Flag.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_Flag.setPreferredSize(new java.awt.Dimension(5, 26));

        lbl_Score.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        lbl_Score.setText("Score:");

        lbl_ScoreValue.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        lbl_ScoreValue.setText("0");

        javax.swing.GroupLayout pnl_GameInforLayout = new javax.swing.GroupLayout(pnl_GameInfor);
        pnl_GameInfor.setLayout(pnl_GameInforLayout);
        pnl_GameInforLayout.setHorizontalGroup(
            pnl_GameInforLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_GameInforLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_GameInforLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnl_GameInforLayout.createSequentialGroup()
                        .addComponent(lbl_Game)
                        .addGap(41, 41, 41)
                        .addComponent(lbl_Help))
                    .addComponent(lbl_Score))
                .addGroup(pnl_GameInforLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_GameInforLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(lbl_Top10))
                    .addGroup(pnl_GameInforLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbl_ScoreValue, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_Flag, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lbl_Flag, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(btn_Icon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(btn_Level, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(btn_Question, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lbl_Hint, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lbl_Time)
                .addGap(21, 21, 21))
        );
        pnl_GameInforLayout.setVerticalGroup(
            pnl_GameInforLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_GameInforLayout.createSequentialGroup()
                .addGroup(pnl_GameInforLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_GameInforLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(pnl_GameInforLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_Question, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pnl_GameInforLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lbl_Hint)
                                .addComponent(lbl_Time)
                                .addComponent(lbl_Flag))
                            .addComponent(btn_Level, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_Icon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_Flag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnl_GameInforLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnl_GameInforLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lbl_Game)
                            .addComponent(lbl_Help)
                            .addComponent(lbl_Top10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnl_GameInforLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lbl_Score)
                            .addComponent(lbl_ScoreValue))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnl_Body.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pnl_Body.setNextFocusableComponent(pnl_Body);
        pnl_Body.setPreferredSize(new java.awt.Dimension(800, 400));
        pnl_Body.setLayout(new java.awt.GridLayout(1, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnl_GameInfor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnl_Body, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_GameInfor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(pnl_Body, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void none(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_none
        // TODO add your handling code here:
    }//GEN-LAST:event_none

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameAntsweeper.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameAntsweeper.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameAntsweeper.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameAntsweeper.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GameAntsweeper("Group03 - Antsweeper", 0).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_Flag;
    private javax.swing.JButton btn_Icon;
    private javax.swing.JButton btn_Level;
    private javax.swing.JButton btn_Question;
    private javax.swing.JLabel lbl_Flag;
    private javax.swing.JLabel lbl_Game;
    private javax.swing.JLabel lbl_Help;
    private javax.swing.JLabel lbl_Hint;
    private javax.swing.JLabel lbl_Score;
    private javax.swing.JLabel lbl_ScoreValue;
    private javax.swing.JLabel lbl_Time;
    private javax.swing.JLabel lbl_Top10;
    private javax.swing.JPanel pnl_Body;
    private javax.swing.JPanel pnl_GameInfor;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        // Determine the action based on the command associated with the event.
        switch (e.getActionCommand()) {
            case "1": // When the user selects level 1
                // Start a new game at level 1 and make it visible.
                new GameAntsweeper("Group03 - Antsweeper", 1).setVisible(true);
                // Set the preferred size of the body panel.
                pnl_Body.setPreferredSize(new Dimension(55, 38));
                pack(); // Resize the frame to fit the components.
                this.dispose(); // Close the current window.
                btn_Level.setActionCommand("2"); // Set the button command for the next level.
                break;

            case "2": // When the user selects level 2
                // Start a new game at level 2 and make it visible.
                new GameAntsweeper("Group03 - Antsweeper", 2).setVisible(true);
                this.dispose(); // Close the current window.
                btn_Level.setActionCommand("3"); // Set the button command for the next level.
                break;

            case "3": // When the user selects level 0
                // Start a new game at level 0 and make it visible.
                new GameAntsweeper("Group03 - Antsweeper", 0).setVisible(true);
                this.dispose(); // Close the current window.
                // Change the button icon to indicate a change in level.
                btn_Level.setIcon(new ImageIcon(getClass().getResource("/imgs/level_1.png")));
                btn_Level.setActionCommand("2"); // Set the button command for the next level.
                break;

            case "hint": // When the user requests a hint
                if (!isGameOver) { // Check if the game is not over
                    int currentHint = Integer.parseInt(lbl_Hint.getText()); // Get the current hint count
                    if (currentHint == 0) { // If no hints left, do nothing
                        break;
                    } else if (currentHint > 0) { // If hints are available
                        Random random = new Random();
                        int x, y;
                        while (true) {
                            // Randomly select coordinates for a hint
                            x = random.nextInt(row) + 1;
                            y = random.nextInt(column) + 1;
                            // Check if the selected cell is safe (not a bomb) and has not been revealed
                            if (values[x][y] != -1 && isTick[x][y]) {
                                currentHint--; // Decrease the hint count
                                button[x][y].setBackground(Color.green); // Change the cell color to indicate a hint
                                break; // Exit the loop after revealing a hint
                            }
                        }
                        lbl_Hint.setText(currentHint + ""); // Update the hint display
                    }
                }
                break;

            case "flag": // When the user toggles the flag mode
                flag = !flag; // Toggle the flag state
                break;

            default: // Handle other actions (button clicks)
                // Check if the action is to show the smile icon
                if (e.getActionCommand().equals("showSmileIcon")) {
                    // Change the button icon to the play icon
                    btn_Icon.setIcon(new ImageIcon(getClass().getResource("/imgs/play.png")));
                    btn_Icon.setActionCommand("showSmileIcon"); // Update action command
                    // Start a new game at the previous level
                    new GameAntsweeper("Group03 - Antsweeper", level - 1).setVisible(true);
                    this.dispose(); // Close the current window
                } else if (!isGameOver) { // If the game is not over
                    if (isRunGame) { // If the game is running
                        runTimer(); // Start the timer
                        isRunGame = false; // Prevent restarting the timer
                    }

                    // Parse the action command to get the cell coordinates
                    String s = e.getActionCommand();
                    int k = s.indexOf(32); // Find the space separating coordinates
                    int i = Integer.parseInt(s.substring(0, k)); // Extract row index
                    int j = Integer.parseInt(s.substring(k + 1)); // Extract column index

                    if (!flag) { // If flag mode is off
                        // Check if the selected cell is a bomb
                        if (values[i][j] == -1) {
                            if (!button[i][j].getText().equals(" ")) { // If the cell has not been revealed
                                timer.stop(); // Stop the timer
                                loss(); // Handle the loss condition
                                btn_Icon.setIcon(new ImageIcon(getClass().getResource("/imgs/sad.png"))); // Change icon to sad
                                JOptionPane.showMessageDialog(null, "Hit ant nest. You lost!"); // Show loss message
                                isGameOver = true; // Set game over state
                            }
                        } else if (values[i][j] == 0) { // If the selected cell is empty
                            openEmpty(i, j); // Open the surrounding cells
                        } else { // If the cell has a number
                            open(i, j); // Open the selected cell
                        }
                    } else { // If flag mode is on
                        addFlag(i, j); // Add a flag to the selected cell
                    }
                }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
