package aginiers.minesweeper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Each user has a minefieldhandler
 * Should be used to take care of messages (asynchronous)
 * 
 * @author aginiers
 *
 */
public class MinefieldHandler {
	
	private static Minefield minefield;
	private boolean gameOn;
	
	public MinefieldHandler() {
		minefield = new Minefield(12, 8, 10, 3);
		this.gameOn = false;
	}
	
	public MinefieldHandler(int width, int height, int mines, int lives) {
		minefield = new Minefield(width, height, mines, lives);
		this.gameOn = false;
	}
	
	private String getMinefieldJson() {
		return minefield.toString();
	}
	
	public void startGame(int id) {
		// FIXME
		MinefieldSize size = MinefieldSize.MEDIUM;
		if (!gameOn) {
			minefield.newGame(size.getWidth(), size.getHeight(), size.getMines(), size.getLives());
			this.gameOn = true;
		}
		MinefieldEndpoint.sendToUser(getMinefieldJson(), id);
	}
	
	public void startGame(int id, MinefieldSize size) {
		// FIXME
		if (!gameOn) {
			minefield.newGame(size.getWidth(), size.getHeight(), size.getMines(), size.getLives());
			this.gameOn = true;
		}
		MinefieldEndpoint.sendToUser(getMinefieldJson(), id);
	}
	
	
	public void stopGame() {
		if (gameOn) {
			this.gameOn = false;
		}
	}

	/**
	 * data object = 
	 * x : coord
	 * y : coord
	 * click : "left"/"right"/"start"/"stop" 
	 * @param data
	 * @param id
	 */
	public void onMessage(JSONObject data, int id) {
		long deb = System.currentTimeMillis();
		
		JSONArray boxesJson = null;
		if (data.get("click").equals("left")) {
			boxesJson = minefield.leftClickBox((Integer) data.get("x"), (Integer) data.get("y"));
		} else if (data.get("click").equals("right")) {
			boxesJson = minefield.rightClickBox((Integer) data.get("x"), (Integer) data.get("y"));
		} else if (data.get("click").equals("start")) {
			startGame(id, MinefieldSize.valueOf((String) data.get("size")));	
		} else if (data.get("click").equals("stop")) {
			stopGame();
		}
		
		long totalSec = System.currentTimeMillis() - deb;
		System.out.println("time to compute " + totalSec);
		
		if (boxesJson != null && boxesJson.length() > 0) {
			sendNewBoxes(boxesJson);
		}
		
	}
	
	private void sendNewBoxes(JSONArray boxesJson) {
		JSONObject messageJson = new JSONObject();
		messageJson.put("type", "minefield");	
		messageJson.put("data", JsonHelper.getDataJson(boxesJson, minefield.getFlags(), minefield.getCurrentLives(), minefield.isWin()));
		new JsonBroadcasterThread(messageJson).run();
	}


}
