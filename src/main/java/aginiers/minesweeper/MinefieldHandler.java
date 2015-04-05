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
	
	private static Minefield minefield = new Minefield(12, 8, 10, 3);
	private static volatile boolean gameOn = false;
	
	public MinefieldHandler() {
//		minefield = new Minefield(12, 8, 10, 3);
//		gameOn = false;
	}
	
	public MinefieldHandler(int width, int height, int mines, int lives) {
//		minefield = new Minefield(width, height, mines, lives);
//		gameOn = false;
	}
	
	private String getMinefieldJson() {
		return minefield.toString();
	}
	
	public void startGame(int id) {
		// FIXME
		MinefieldSize size = MinefieldSize.MEDIUM;
		if (!gameOn) {
			minefield.newGame(size.getWidth(), size.getHeight(), size.getMines(), size.getLives());
			MinefieldEndpoint.broadcast(getMinefieldJson());
			gameOn = true;
		} else {
			MinefieldEndpoint.sendToUser(getMinefieldJson(), id);
		}
	}
	
	public void startGame(int id, MinefieldSize size) {
		// FIXME
		if (!gameOn) {
			minefield.newGame(size.getWidth(), size.getHeight(), size.getMines(), size.getLives());
			MinefieldEndpoint.broadcast(getMinefieldJson());
			gameOn = true;
		} else {
			MinefieldEndpoint.sendToUser(getMinefieldJson(), id);
		}
	}
	
	
	public void stopGame() {
		if (gameOn) {
			gameOn = false;
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
			boxesJson = minefield.leftClickBox(Integer.parseInt((String) data.get("x")), Integer.parseInt((String) data.get("y")));
		} else if (data.get("click").equals("right")) {
			boxesJson = minefield.rightClickBox(Integer.parseInt((String) data.get("x")), Integer.parseInt((String) data.get("y")));
		} else if (data.get("click").equals("start")) {
			startGame(id, MinefieldSize.valueOf((String) data.get("size")));	
		} else if (data.get("click").equals("stop")) {
			stopGame();
		} else if (data.get("click").equals("restart")) {
			stopGame();
			startGame(id, MinefieldSize.valueOf((String) data.get("size")));	
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
