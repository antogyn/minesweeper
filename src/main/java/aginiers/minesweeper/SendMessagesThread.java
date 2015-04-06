package aginiers.minesweeper;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.websocket.CloseReason;
import javax.websocket.Session;

/**
 * 
 * @author aginiers
 *
 */
public class SendMessagesThread extends Thread {
	
	private BlockingQueue<String> messages;
	private User user;
    
	SendMessagesThread(User user) {
        this.messages = new ArrayBlockingQueue<String>(50);
        this.user = user;
    }
	
	public void addMessage(String message) {
		if (messages.remainingCapacity() == 0) {
			try {
				user.getSession().close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Too many stored messages"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			this.messages.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

    public void run() {
    	while (true) {
    		String message = "";
			try {
				message = messages.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		Session session = user.getSession();
    		synchronized (session) {
    			try {
    				if (session.isOpen()) {
    					session.getBasicRemote().sendText(message);
    				}
    			} catch (Exception e) {
    				try {
    					e.printStackTrace();
    					if (session.isOpen()) {
    						session.close();
    					}
    				} catch (IOException e1) {
    				}
    			}
    		}

    	}
    }
}
