// Main class to run
public class Minesweeper {
	public static void main(String[] args) {
		// Pass the command line argument to Game, used to select debug mode
		if (args.length == 1) {
			Game minesweeper = new Game(args[0]);
		}
	}
}
