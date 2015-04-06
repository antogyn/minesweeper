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
			this.interrupt();
		}
	}

    public void run() {
    	try {
	    	while (true) {
	    		String message = "";
				message = messages.take();
	    		Session session = user.getSession();
	    		synchronized (session) {
	    			try {
	    				if (session.isOpen()) {
	    					session.getBasicRemote().sendText(message);
	    				}
	    				if (user.getNickname().equals("blocktest")) {
							Thread.sleep(1500);
	    				}
	    			} catch (IOException e) {
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
    	} catch (InterruptedException e) {
    		System.out.println("SendMessagesThread closed for user " + this.user.getNickname());
    	}
    }
}
