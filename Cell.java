import java.awt.image.*;
import java.awt.*;

// Represents one box on the Minesweeper grid
public class Cell {
	private int x;
	private int y;
	private String state;
	private int value;		// 10 is a mine

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
		this.state = "hidden";
		this.value = 0;
	}
	
	public Cell(int x, int y, int value) {
		this.x = x;
		this.y = y;
		state = "hidden";
		if (value >= 0 && value <= 9)
			this.value = value;
		else
			this.value = 0;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public String getState() {
		return state;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int newValue) {
		if (newValue >= 0 && newValue <= 10)
			value = newValue;
	}
	
	public void setState(String newState) {
		if (newState.compareTo("hidden") == 0 || newState.compareTo("shown") == 0 || newState.compareTo("flagged") == 0 || newState.compareTo("mistake") == 0)
			state = newState;
	}
}