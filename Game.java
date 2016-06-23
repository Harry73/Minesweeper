import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer;
import java.text.DecimalFormat;
import java.io.*;

public class Game
{
	// Drawing components
	private JButton newGameButton = new JButton("New Game");
	private JTextField size_textfield = new JTextField(5);
	private JTextField mines_textfield = new JTextField(5);
	private JLabel size_label = new JLabel("Size:", JLabel.CENTER);
	private JLabel mines_label = new JLabel("Mines:", JLabel.CENTER);
	private JLabel time_label = new JLabel("Time: ", JLabel.LEFT);
	private JLabel minesLeft_label = new JLabel("Remaining: ", JLabel.RIGHT);
	private GamePanel mainPicture = new GamePanel(); 				// The playing grid is drawn to this panel
	private JPanel inputs = new JPanel(new FlowLayout()); 			// Controls are added to this panel
	private JFrame frame = new JFrame("Minesweeper"); 				// Window
	
	private boolean firstClickOccured = false;
	
	private int gridSize; 						// Size! Grid will be square for now
	private int gridScale;				 		// Scale for drawing grid is based on size
	private int xoffset;
	private int yoffset;
	private int numberMines;				// number of mines
	private int pressedX; 					// Record current position
	private int pressedY;
	private boolean mouseDown = false;
	
	// the minefield, not going to use "0" position cause I'm too lazy to think about it
	private Cell[][] grid;
	
	// Picture time
	private Image zero_pic;
	private Image one_pic;
	private Image two_pic;
	private Image three_pic;
	private Image four_pic;
	private Image five_pic;
	private Image six_pic;
	private Image seven_pic;
	private Image eight_pic;
	private Image press_pic;
	private Image mine_pic;
	private Image hidden_pic;
	private Image flag_pic;
	private Image mistake_pic;
	
	private boolean debugMode;
	
	private boolean done;
	private int numberMinesLeft;
	private int seconds;				// counting time
	private int milliseconds;			// doing time
	private Timer time = new Timer();	// counting time
	private DecimalFormat formant1 = new DecimalFormat("0");
	private DecimalFormat formant2 = new DecimalFormat("000");
	
	KeyListener keys = new KeyAdapter() {
		public void keyPressed(KeyEvent e)
		{
			// Same as pressing the "New Game" button again
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				startNewGame(false);
			}
		}
	};

	MouseListener mousey = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			if (!done) {
				pressedX = (int)Math.floor((e.getX()*1.0 - (gridScale+xoffset))/gridScale + 1);
				pressedY = (int)Math.floor((e.getY()*1.0 - (gridScale+yoffset+70))/gridScale + 1);
				
				// Enable a visual effect on left mouse clicks
				if (pressedX > 0 && pressedX <= gridSize && pressedY > 0 && pressedY <= gridSize) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						mouseDown = true;
						repainting();
					}
				}
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			mouseDown = false;
			
			// Keep it in the grid please
			if (pressedX > 0 && pressedX <= gridSize && pressedY > 0 && pressedY <= gridSize) {				
				// Generate the maze on the first click
				if (!firstClickOccured) {
					generateGame();
					// Ensure that location clicked doesn't have a mine. Regenerate if it does
					while (grid[pressedX][pressedY].getValue() == 9)
						generateGame();
					
					firstClickOccured = true;
					
					// Start the timer
				    time.scheduleAtFixedRate(new task(), 1, 1);
					time_label.setText("Time: " + formant1.format(seconds) + "." + formant2.format(milliseconds));
				}
			
				if (SwingUtilities.isLeftMouseButton(e)) {
					// You can only press hidden squares
					if (grid[pressedX][pressedY].getState().compareTo("hidden") == 0)
						pressSquare(pressedX, pressedY);
				}
				else if (SwingUtilities.isRightMouseButton(e)) {
					// You can mark hidden/flagged squares
					if (grid[pressedX][pressedY].getState().compareTo("hidden") == 0 || grid[pressedX][pressedY].getState().compareTo("flagged") == 0)
						markSquare(pressedX, pressedY);
				}
				
				repainting();
			}
		}
	};
	
	public Game(String debugMode) {
		this.debugMode = Boolean.parseBoolean(debugMode);
		
		// Lots of preferences
		// Main container
		frame.getContentPane().setBackground(Color.white);
		frame.setForeground(Color.white);
		frame.setLayout(new FlowLayout());
		// Labels
		size_label.setForeground(Color.black);
		mines_label.setForeground(Color.black);
		size_textfield.setPreferredSize(new Dimension(66, 20));
		mines_textfield.setPreferredSize(new Dimension(66, 20));
		size_label.setPreferredSize(new Dimension(41, 20));
		mines_label.setPreferredSize(new Dimension(41, 20));
		time_label.setPreferredSize(new Dimension(200, 20));
		minesLeft_label.setPreferredSize(new Dimension(200, 20));
		// Buttons
		newGameButton.setPreferredSize(new Dimension(100, 20));
		newGameButton.addActionListener(mainPicture); 		//adding listener for button presses
		// Textfields
		size_textfield.setToolTipText("Size can be from 2-30.");
		mines_textfield.setToolTipText("How many mines are in the game?");
		// Panels
		mainPicture.setBackground(Color.white);
		mainPicture.setPreferredSize(new Dimension(900, 900));
		inputs.setBackground(Color.white);
		
		// Add all the controls to the input panel
		inputs.add(time_label);
		inputs.add(size_label);
		inputs.add(size_textfield);
		inputs.add(mines_label);
		inputs.add(mines_textfield);
		inputs.add(newGameButton);
		inputs.add(minesLeft_label);
		inputs.setBackground(Color.white);
		
		// Put it all in the window
		frame.add(inputs);
		frame.addKeyListener(keys);
		frame.addMouseListener(mousey);
		frame.add(mainPicture);
		frame.setSize(900, 730); 	// window size
		frame.setVisible(true); 	// show window please
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // apparently important
		
		// Fetch all the pictures
		zero_pic = Toolkit.getDefaultToolkit().getImage("Images/0.png");
		one_pic = Toolkit.getDefaultToolkit().getImage("Images/1.png");
		two_pic = Toolkit.getDefaultToolkit().getImage("Images/2.png");
		three_pic = Toolkit.getDefaultToolkit().getImage("Images/3.png");
		four_pic = Toolkit.getDefaultToolkit().getImage("Images/4.png");
		five_pic = Toolkit.getDefaultToolkit().getImage("Images/5.png");
		six_pic = Toolkit.getDefaultToolkit().getImage("Images/6.png");
		seven_pic = Toolkit.getDefaultToolkit().getImage("Images/7.png");
		eight_pic = Toolkit.getDefaultToolkit().getImage("Images/8.png");
		hidden_pic = Toolkit.getDefaultToolkit().getImage("Images/hidden.png");
		mine_pic = Toolkit.getDefaultToolkit().getImage("Images/mine.png");
		flag_pic = Toolkit.getDefaultToolkit().getImage("Images/flag.png");
		press_pic = Toolkit.getDefaultToolkit().getImage("Images/press.png");
		mistake_pic = Toolkit.getDefaultToolkit().getImage("Images/mistake.png");

		startNewGame(true);
	}

	// You clicked a square, I show my hand
	public void pressSquare(int x, int y) {
		// If we get here, the square pressed is hidden. Show it
		grid[x][y].setState("shown");
		
		// You hit a mine
		if (grid[x][y].getValue() == 9)	{
			lose();
		}
		// You hit something else
		else {
			// Call this method again on neighbors for flooding
			if (grid[x][y].getValue() == 0) {
				// Of course, keep it in the grid though, and only do it to hidden friends
				if (x-1 > 0 && grid[x-1][y].getState().compareTo("hidden") == 0)
					pressSquare(x-1, y);
				if (y-1 > 0 && grid[x][y-1].getState().compareTo("hidden") == 0)
					pressSquare(x, y-1);
				if (x+1 <= gridSize && grid[x+1][y].getState().compareTo("hidden") == 0)
					pressSquare(x+1, y);
				if (y+1 <= gridSize && grid[x][y+1].getState().compareTo("hidden") == 0)
					pressSquare(x, y+1);
				if (x-1 > 0 && y-1 > 0 && grid[x-1][y-1].getState().compareTo("hidden") == 0)
					pressSquare(x-1, y-1);
				if (x-1 > 0 && y+1 <= gridSize && grid[x-1][y+1].getState().compareTo("hidden") == 0)
					pressSquare(x-1, y+1);
				if (x+1 <= gridSize && y-1 > 0 && grid[x+1][y-1].getState().compareTo("hidden") == 0)
					pressSquare(x+1, y-1);
				if (x+1 <= gridSize && y+1 <= gridSize && grid[x+1][y+1].getState().compareTo("hidden") == 0)
					pressSquare(x+1, y+1);
			}	
			
			checkSolved();		
		}
	}
	
	// Checks if the current game is solved and displays a happy message if it is
	public void checkSolved() {
		if (!done) {
			// Game is done when the only things still hidden are mines
			boolean allValuesShown = true;
			for (int i = 1; i <= gridSize; i++) {
				for (int j = 1; j <= gridSize; j++) {
					if (grid[i][j].getState().compareTo("hidden") == 0 && grid[i][j].getValue() != 9)
						allValuesShown = false;
				}
			}
			if (allValuesShown) {
				done = true;
				time.cancel();
				repainting();
				
				// Win condition. Check highscores list and write new highscores to file
				PrintWriter out;
				Scanner scan;
				double oldHighscore = 0;
				String highscores = "";
				try {
					scan = new Scanner(new File("highscores.txt"));
					boolean found = false;
					while (scan.hasNext()) {
						String line = scan.nextLine();
						if (line.contains("" + gridSize + ", " + numberMines + ", ")) {
							int index = line.lastIndexOf(',');
							oldHighscore = Double.valueOf(line.substring(index+1));
							found = true;
						}
						else {
							highscores = highscores + line + "\n";
						}
					}
					if (!found)
						oldHighscore = 1000000;
				}
				catch (FileNotFoundException e) {
					oldHighscore = 1000000;
				}
				try {
					out = new PrintWriter(new File("highscores.txt"));
					if ((seconds + milliseconds/1000.0) < oldHighscore) {
						JOptionPane.showMessageDialog(frame, "New highscore! Time: " + formant1.format(seconds) + "." + formant2.format(milliseconds));
						if (highscores.compareTo("\n") != 0)
							out.print(highscores);
						out.println(gridSize + ", " + numberMines + ", " + (seconds+milliseconds/1000.0));
					}
					else {
						JOptionPane.showMessageDialog(frame, "You won! Time: " + formant1.format(seconds) + "." + formant2.format(milliseconds));
						out.print(highscores);
						out.println(gridSize + ", " + numberMines + ", " + oldHighscore);
					}
					out.close();
				}
				catch (IOException E) {
					System.out.println("death.");
				}
			}
		}
	}
	
	// When a mine is hit, deal with it here
	public void lose() {
		// Search for a reveal all mines
		for (int i = 1; i <= gridSize; i++) {
			for (int j = 1; j <= gridSize; j++) {
				if (grid[i][j].getValue() == 9)
					grid[i][j].setState("shown");
				else if (grid[i][j].getState().compareTo("flagged") == 0)
					grid[i][j].setState("mistake");
			}
		}
		
		done = true;
		time.cancel();
	}
	
	// Flag or unflag a square
	public void markSquare(int x, int y) {
		if (grid[x][y].getState().compareTo("hidden") == 0) {
			grid[x][y].setState("flagged");
			numberMinesLeft--;
			minesLeft_label.setText("Remaining: " + numberMinesLeft);
		}
		else if (grid[x][y].getState().compareTo("flagged") == 0) {
			grid[x][y].setState("hidden");
			numberMinesLeft++;
			minesLeft_label.setText("Remaining: " + numberMinesLeft);
		}
		else
			JOptionPane.showMessageDialog(frame, "Fatal error occurred, please kill the stupid designer.");
	}
	
	// Start a new game
	public void startNewGame(boolean firstTime) {
		// Reset time
		time.cancel();
		time = new Timer();
		seconds = 0;
		milliseconds = 0;
		time_label.setText("Time: " + formant1.format(seconds) + "." + formant2.format(milliseconds));

		// Called by constructor, use default values
		if (firstTime) {
			gridSize = 9;
			gridScale = 550/gridSize;
			numberMines = 10;
			yoffset = 0;
			xoffset = 100 + gridSize/2;
		}
		// Called by button press, get values from user
		else {
			try {
				gridSize = Integer.parseInt(size_textfield.getText()); 		// get maze size from text box
				numberMines = Integer.parseInt(mines_textfield.getText()); 	// get number of mines from text box
				
				// Place limits of gridSize
				if (gridSize > 30) {
					gridSize = 30;
					size_textfield.setText("30");
				}
				else if (gridSize < 2) {
					gridSize = 2;
					size_textfield.setText("2");
				}
				
				if (numberMines > gridSize*gridSize/2) {
					numberMines = gridSize*gridSize/2;
					JOptionPane.showMessageDialog(frame, "You desire too many explosions. Will use " + numberMines + " instead.");
					mines_textfield.setText(numberMines + "");
				}	

				// sets a good scale for drawing the grid
				if (gridSize >= 20)
					gridScale = 630/gridSize;
				else if (gridSize < 20 && gridSize > 5)
					gridScale = 550/gridSize;
				else
					gridScale = 400/gridSize;
				
				// Set values for drawing 
				yoffset = 0;
				xoffset = 100 + gridSize/2;
			}
			// Don't screw with me now with those textfields
			catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(frame, "Enter a number for the size and number of mines please.");
			}
		}

		// Actual game will be generated on the first mouse click, for now make something nice to show player
		grid = new Cell[gridSize+1][gridSize+1];		// make a new minefield
		for (int i = 1; i <= gridSize; i++) {
			for (int j = 1; j <= gridSize; j++) {
				grid[i][j] = new Cell(i, j, 0);
				grid[i][j].setState("hidden");
			}
		}
		
		firstClickOccured = false;
		done = false;
		numberMinesLeft = numberMines;
		minesLeft_label.setText("Remaining: " + numberMines);
		
		repainting();
	}
	
	// Random place the mines and calculate the rest of the board
	public void generateGame() {
		grid = new Cell[gridSize+1][gridSize+1];		// make a new minefield
		
		// Select numberMines random locations for a mine to be placed. Make sure there are no repeats
		LinkedList<Point> mineLocations = new LinkedList<Point>();
		Random rn = new Random();
		for (int i = 0; i < numberMines; i++) {
			int xLoc = rn.nextInt(gridSize) + 1;
			int yLoc = rn.nextInt(gridSize) + 1;
			Point mineLoc = new Point(xLoc, yLoc);
			while (mineLocations.contains(mineLoc)) {
				xLoc = rn.nextInt(gridSize) + 1;
				yLoc = rn.nextInt(gridSize) + 1;
				mineLoc = new Point(xLoc, yLoc);
			}
			mineLocations.add(mineLoc);
		}
				
		// Initialize board
		for (int i = 1; i <= gridSize; i++) {
			for (int j = 1; j <= gridSize; j++) {
				grid[i][j] = new Cell(i, j);
			}
		}
		
		// Actually place mines based on locations in list
		for (int i = 0; i < mineLocations.size(); i++) {
			Point current = mineLocations.get(i);
			grid[(int)current.getX()][(int)current.getY()].setValue(9);
		}
		
		// Fill out rest of board based on mine locations
		for (int x = 1; x <= gridSize; x++) {
			for (int y = 1; y <= gridSize; y++) {
				// Count number of surrounding mines and use that to set value of square
				if (grid[x][y].getValue() == 0) {
					int sum = 0;
					if (x-1 > 0 && grid[x-1][y].getValue() == 9)
						sum++;
					if (y-1 > 0 && grid[x][y-1].getValue() == 9)
						sum++;
					if (x+1 <= gridSize && grid[x+1][y].getValue() == 9)
						sum++;
					if (y+1 <= gridSize && grid[x][y+1].getValue() == 9)
						sum++;
					if (x-1 > 0 && y-1 > 0 && grid[x-1][y-1].getValue() == 9)
						sum++;
					if (x-1 > 0 && y+1 <= gridSize && grid[x-1][y+1].getValue() == 9)
						sum++;
					if (x+1 <= gridSize && y-1 > 0 && grid[x+1][y-1].getValue() == 9)
						sum++;
					if (x+1 <= gridSize && y+1 <= gridSize && grid[x+1][y+1].getValue() == 9)
						sum++;
					
					grid[x][y].setValue(sum);
				}
			}
		}
		
		// Print out the game in a lame fashion for debugging
		if (debugMode) {
			for (int i = 1; i <= gridSize; i++) {
				for (int j = 1; j <= gridSize; j++) {
					if (grid[i][j].getValue() == 9)
						System.out.print("M ");
					else
						System.out.print(grid[i][j].getValue() + " ");
				}
				System.out.println();
			}
		}
	}
	
	//Small method to update the main frame
	public void repainting() {
		frame.repaint(1, -100, -100, 1000, 1000);
	}
	
	//Closes the frame and ends the program
	public void quit() {
		frame.dispose();
		System.exit(0);
	}
	
	// Back to the drawing board
	public class GamePanel extends JPanel implements ActionListener {
		public Dimension getPreferredSize() {
			return new Dimension(900, 900);
		}
		
		public void actionPerformed(ActionEvent e) {
			 // Create a new game when button is pressed
			if (e.getSource() == newGameButton)
			{
				startNewGame(false);
			}
		}
		
		// Draw the screen	
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			// Drawing borders for the grid
			g.setColor(Color.black);
			g.drawLine(gridScale+xoffset, gridScale+yoffset, gridScale+xoffset, gridScale*gridSize+gridScale+yoffset);
			g.drawLine(gridScale+gridScale*gridSize+xoffset, gridScale+yoffset, gridScale+gridScale*gridSize+xoffset, gridScale*gridSize+gridScale+yoffset);
			g.drawLine(gridScale+xoffset, gridScale+yoffset, gridScale*gridSize+gridScale+xoffset,gridScale+yoffset);
			g.drawLine(gridScale+xoffset, gridScale+gridScale*gridSize+yoffset, gridScale*gridSize+gridScale+xoffset,gridScale+gridScale*gridSize+yoffset);

			// Draw the cells
			for (int i = 1; i <= gridSize; i++) {
				for (int j = 1; j <= gridSize; j++) {
					// I seem to have issues with the "grid = new Cell[][]" request in the startNewGame method not completing before the repainting here
					// tries to occur. This if statment seems to fix it, I don't really trust it as a solution. 
					if (gridSize != grid.length - 1)
						break;
					
					if (grid[i][j].getState().compareTo("flagged") == 0) {
						g.drawImage(flag_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);
					}
					else if (grid[i][j].getState().compareTo("shown") == 0) {
						if (grid[i][j].getValue() == 0)
							g.drawImage(zero_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);
						else if (grid[i][j].getValue() == 1)
							g.drawImage(one_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);							
						else if (grid[i][j].getValue() == 2)
							g.drawImage(two_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);							
						else if (grid[i][j].getValue() == 3)
							g.drawImage(three_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);							
						else if (grid[i][j].getValue() == 4)
							g.drawImage(four_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);							
						else if (grid[i][j].getValue() == 5)
							g.drawImage(five_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);							
						else if (grid[i][j].getValue() == 6)
							g.drawImage(six_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);							
						else if (grid[i][j].getValue() == 7)
							g.drawImage(seven_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);							
						else if (grid[i][j].getValue() == 8)
							g.drawImage(eight_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);						
						else if (grid[i][j].getValue() == 9)
							g.drawImage(mine_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);							
					}
					else if (grid[i][j].getState().compareTo("hidden") == 0) {
						g.drawImage(hidden_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);
					}
					else if (grid[i][j].getState().compareTo("mistake") == 0) {
						g.drawImage(mistake_pic, i*gridScale+xoffset, j*gridScale+yoffset, gridScale, gridScale, this);
					}
				}
			}
			
			// Visual effect for mouse pressed
			if (mouseDown && grid[pressedX][pressedY].getState().compareTo("hidden") == 0) {
				g.drawImage(press_pic, pressedX*gridScale+xoffset, pressedY*gridScale+yoffset, gridScale, gridScale, this);
			}
		}
	}

	public class task extends TimerTask {
		public void run() {
			milliseconds++;
			if (milliseconds == 1000) {
				milliseconds = 0;
				seconds++;
			}
			time_label.setText("Time: " + formant1.format(seconds) + "." + formant2.format(milliseconds));
		}
	}
}