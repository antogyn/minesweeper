package aginiers.minesweeper;

import org.json.JSONObject;

public class Box {
	
	private boolean exposed;
	private boolean flag;
	private boolean bomb;
	private int adjacentBombs;
	private int x;
	private int y;
	
	public Box(int x, int y) {
		this.exposed = false;
		this.flag = false;
		this.bomb = false;
		this.setAdjacentBombs(0);
		this.x = x;
		this.y = y;
	}
	
	public synchronized boolean isExposed() {
		return exposed;
	}
	
	public void setExposed(boolean exposed) {
		this.exposed = exposed;
	}
	
	public synchronized boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	public boolean isBomb() {
		return bomb;
	}
	public void setBomb(boolean bomb) {
		this.bomb = bomb;
	}

	public int getAdjacentBombs() {
		return adjacentBombs;
	}

	public void setAdjacentBombs(int adjacentBombs) {
		this.adjacentBombs = adjacentBombs;
	}
	
	public void incrementAdjacentBombs() {
		this.adjacentBombs++;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	/**
	 * The display attribute in the json sent to draw the minefield
	 * @return
	 */
	public synchronized String display() {
		if (!exposed) {
			if (flag) {
				return BoxType.FLAG.getCode();
			} else {
				return BoxType.UNEXPOSED.getCode();
			}
		} else if (bomb) {
			return BoxType.BOMB.getCode();
		} else {
			return BoxType.EXPOSED.getCode();
		}
	}
	
	/**
	 * Json representation of a box (String)
	 */
	@Override
	public String toString() {
		return toJson().toString();
	}
	
	/**
	 * Json representation of a box (JSONObject)
	 * @return
	 */
	public JSONObject toJson() {
		JSONObject boxJson = new JSONObject();
		String display;
		long timestamp;
		synchronized(this) {
			display = display();
			timestamp = System.currentTimeMillis();
		}
		boxJson.put("display", display);
		boxJson.put("adjacentBombs", this.getAdjacentBombs());
		boxJson.put("x", this.getX());
		boxJson.put("y", this.getY());
		boxJson.put("timestamp", timestamp);
		return boxJson;
	}
	
}
