package aginiers.minesweeper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author aginiers
 *
 */
public class JsonBroadcasterThread extends Thread {
	
	ConcurrentHashMap<Integer, User> users;
	String message;
    
	JsonBroadcasterThread(ConcurrentHashMap<Integer, User> users, String message) {
		this.users = users;
		this.message = message;
    }

    public void run() {
    	for (Map.Entry<Integer,User> mapEntry : users.entrySet()) {
			mapEntry.getValue().sendMessage(message);
		}
    }
}
