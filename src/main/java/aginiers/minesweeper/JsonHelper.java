package aginiers.minesweeper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author aginiers
 *
 */
public class JsonHelper {

	public static JSONObject getDataJson(JSONArray boxesJson, int flags, int lives, boolean win) {
		JSONObject data = new JSONObject();	
		data.put("boxes", boxesJson);
		data.put("flags", flags);
		data.put("lives", lives);
		data.put("win", win);	
		return data;
	}

}
