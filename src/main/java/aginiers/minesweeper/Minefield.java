package aginiers.minesweeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *	
 * @author aginiers
 *
 */
public class Minefield {
	
	private volatile int width; // safe
	private volatile int height; // safe
	private volatile Box[][] boxes; // the array of boxes
	private volatile int flags; // remaining flags
	private volatile int mines; // safe
	private volatile int currentLives; // current life total
	private volatile int lives; // life total given at start
	private volatile int boxesLeft; // the boxes left to reveal before winning 
	private volatile boolean win;
	
	public Minefield(int width, int height, int mines, int lives) {
		this.width = width;
		this.height = height;
		this.boxes = new Box[width][height];
		this.mines = mines;
		this.lives = lives;
	}

	/**
	 * resets the field
	 */
	public synchronized void newGame(int width, int height, int mines, int lives) {
		
		this.width = width;
		this.height = height;
		this.boxes = new Box[width][height];
		this.mines = mines;
		this.lives = lives;
		
		
		// give flags and lives back
		flags = mines;
		currentLives = lives;
		win = false;
		boxesLeft = width*height - mines;
		
		// init boxes
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				boxes[i][j] = new Box(i,j);
			}
		}
		
		// set up random mines and sets infos
		for (int i = 1; i <= mines; i++) {
			setAMine();  
		}
		
	}
	
	/**
	 * Sets a random mine on the field
	 * and increments the adjacent bombs on the
	 * adjacent boxes
	 * Lazy and naive.
	 * TODO: http://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
	 */
	private void setAMine() {
		Random rand = new Random();
	    int randomNum = rand.nextInt(height*width);
		int x = randomNum%width;
		int y = randomNum/width;
		if (boxes[x][y].isBomb()) {
			setAMine();
		} else {
			boxes[x][y].setBomb(true);
			// +1 bomb to adjacent boxes
			for (Box box : getAdjacentBoxes(x,y)) {
				box.incrementAdjacentBombs();
			}
		}	
	}
	
	/**
	 * List of adjacent boxes to a box given its coordinates
	 * @param x
	 * @param y
	 * @return
	 */
	private List<Box> getAdjacentBoxes(final int x, final int y) {
		ArrayList<Box> result = new ArrayList<Box>();
		for (int i = x-1; i <= x+1; i++) {
			if (i>=0 && i < width) {
				for (int j = y-1; j <= y+1; j++) {
					if ((j>=0 && j < height) && (i != x || j != y)) {
						result.add(boxes[i][j]);
					}
				}
			}
		}
		return result;
	}

	/**
	 * left click on a box
	 * @param x
	 * @param y
	 * @return a json array of the new boxes
	 */
	public synchronized JSONArray leftClickBox(final int x, final int y) {
		// FIXME handle x & y < or > min/max ?

		JSONArray boxesJson = new JSONArray();		
		
		if (currentLives > 0 && !win) {
			if (boxes[x][y].isExposed()) {
				boxesJson = leftClickExposedBox(x,y,boxesJson);
			} else {
				boxesJson = leftClickUnexposedBox(x,y,false,boxesJson);
			}
		}
		
		return boxesJson;
	}
	
	/** click on exposed -> if flags around = its number,
	 *  reveal all unexposed boxes that aren't flags
	 * @param x
	 * @param y
	 */
	private JSONArray leftClickExposedBox(final int x, final int y, JSONArray boxesJson) {
		int flagsAround = 0;
		// count flags around
		for (Box boxAround : getAdjacentBoxes(x,y)) {
			if (boxAround.isFlag()) {
				flagsAround++;
			}
		}
		// if flags around = number of adjacent bombs written on the box,
		// reveal boxes that aren't flags
		if (flagsAround == boxes[x][y].getAdjacentBombs()) {
			for (Box boxAround : getAdjacentBoxes(x,y)) {
				if (!boxAround.isExposed() && !boxAround.isFlag()) {
					boxesJson = leftClickUnexposedBox(boxAround.getX(),boxAround.getY(), false, boxesJson);
				}
			}
		}
		return boxesJson;
	}
	
	private JSONArray leftClickUnexposedBox(final int x, final int y, boolean force, JSONArray boxesJson) {
		// if it's a flag do nothing
		if (!boxes[x][y].isFlag() && !force) {
			if (boxes[x][y].isBomb()) {
				// if it's a bomb -> life-1
				currentLives--;
				if (currentLives == 0) {
//					MinefieldHandler.sendBoxChange(BoxType.BOMB.getCode(), boxes[x][y].getAdjacentBombs(), x, y);
					boxes[x][y].setExposed(true);
					boxesJson = handleGameOver(boxesJson);
					boxesJson.put(boxes[x][y].toJson());
				} else {
					// if still alive, make the bomb a flag
					boxes[x][y].setFlag(true);
					flags--;
//					MinefieldHandler.sendBoxChange(BoxType.FLAG.getCode(), boxes[x][y].getAdjacentBombs(), x, y);
					boxesJson.put(boxes[x][y].toJson());
					
				}
			} else {
				// if it's not a bomb, reveal it
				boxes[x][y].setExposed(true);
				boxesLeft--;
//				MinefieldHandler.sendBoxChange(BoxType.EXPOSED.getCode(), boxes[x][y].getAdjacentBombs(), x, y);
				boxesJson.put(boxes[x][y].toJson());
				// check victory
				boxesJson = checkVictory(boxesJson);
				
				if (boxes[x][y].getAdjacentBombs() == 0) {
					for (Box boxAround : getAdjacentBoxes(x,y)) {
						if (!boxAround.isExposed()) {
							boxesJson = leftClickUnexposedBox(boxAround.getX(), boxAround.getY(), true, boxesJson);
						}
					}
				}
			}
			
		} else if (force) {
			//TODO duplicate..
			if (boxes[x][y].isFlag()) {
				flags--;
				boxes[x][y].setFlag(false);
			}
			boxes[x][y].setExposed(true);
			boxesLeft--;
//			MinefieldHandler.sendBoxChange(BoxType.EXPOSED.getCode(), boxes[x][y].getAdjacentBombs(), x, y);
			boxesJson.put(boxes[x][y].toJson());
			// check victory
			boxesJson = checkVictory(boxesJson);
			
			if (boxes[x][y].getAdjacentBombs() == 0) {
				for (Box boxAround : getAdjacentBoxes(x,y)) {
					if (!boxAround.isExposed()) {
						boxesJson = leftClickUnexposedBox(boxAround.getX(), boxAround.getY(), true, boxesJson);
					}
				}
			}
		}
		
		return boxesJson;
		
	}
	
	private JSONArray checkVictory(JSONArray boxesJson) {
		if (boxesLeft <= 0) {
			boxesJson = handleVictory(boxesJson);
		}
		return boxesJson;
	}
	
	/**
	 * Sets flags
	 */
	public synchronized JSONArray rightClickBox(final int x, final int y) {
		JSONArray boxesJson = new JSONArray();	
		if (currentLives > 0 && !win) {
			if (!boxes[x][y].isExposed()) {
				if (!boxes[x][y].isFlag()) {
					if (flags > 0) {
						boxes[x][y].setFlag(true);
						flags--;
						boxesJson.put(boxes[x][y].toJson());
					}
				} else {
					boxes[x][y].setFlag(false);
					flags++;
					boxesJson.put(boxes[x][y].toJson());
				}
			}
		}
		return boxesJson;
	}
	
	private JSONArray handleGameOver(JSONArray boxesJson) {
		boxesJson = exposeBombs(boxesJson, false);
		//TODO ?
		return boxesJson;
	}
	
	private JSONArray handleVictory(JSONArray boxesJson) {
		//TODO ?
		win = true;
		boxesJson = exposeBombs(boxesJson, true);
		return boxesJson;
	}
	
	private JSONArray exposeBombs(JSONArray boxesJson, boolean asFlags) {
		for (int i = 0; i < this.getWidth(); i++) {
			for (int j = 0; j < this.getHeight(); j++) {
				if (boxes[i][j].isBomb()) {
					if (asFlags && !boxes[i][j].isFlag()) {
						boxes[i][j].setFlag(true);	
						boxesJson.put(boxes[i][j].toJson());
					} else if (!asFlags) {
						boxes[i][j].setExposed(true);
						boxes[i][j].setFlag(false);
						boxesJson.put(boxes[i][j].toJson());
					}
				}
			}
		}		
		return boxesJson;
	}
	
	/**
	 * @return the boxes
	 */
	public synchronized Box[][] getBoxes() {
		return boxes;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return the flags
	 */
	public synchronized int getFlags() {
		return flags;
	}

	/**
	 * @return the mines
	 */
	public int getMines() {
		return mines;
	}

	/**
	 * @return the currentLives
	 */
	public synchronized int getCurrentLives() {
		return currentLives;
	}

	/**
	 * @return the lives
	 */
	public int getLives() {
		return lives;
	}

	/**
	 * @return the win
	 */
	public synchronized boolean isWin() {
		return win;
	}
	
	/**
	 * Json representation of the minefield (String)
	 */
	@Override
	public String toString() {
		JSONObject messageJson = new JSONObject();
		messageJson.put("type", "minefield");
		JSONArray boxesJson = new JSONArray();
		
		for (int i = 0; i < this.getWidth(); i++) {
			for (int j = 0; j < this.getHeight(); j++) {
				boxesJson.put(boxes[i][j].toJson());					
			}
		}

		messageJson.put("data", JsonHelper.getDataJson(boxesJson, this.getFlags(), this.getCurrentLives(), this.isWin()));
		
		return messageJson.toString();
	}
	
	
	
	
}
