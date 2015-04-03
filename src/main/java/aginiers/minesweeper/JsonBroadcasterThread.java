package aginiers.minesweeper;

import org.json.JSONObject;

/**
 * 
 * @author aginiers
 *
 */
public class JsonBroadcasterThread extends Thread {
	
	JSONObject json;
    
	JsonBroadcasterThread(JSONObject json) {
        this.json = json;
    }

    public void run() {
    	long deb = System.currentTimeMillis();
    	MinefieldEndpoint.broadcast(json.toString());
    	long totalSec = System.currentTimeMillis() - deb;
    	System.out.println("time to send " + totalSec);
    }
}
