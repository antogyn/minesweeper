package aginiers.minesweeper;

import java.io.IOException;

import javax.websocket.Session;

/**
 * 
 * @author aginiers
 *
 */
public class User {
	
	private final int id;
	private final Session session;
	private final String nickname;
	
	public User(int id, Session session, String nickname) {
		this.id = id;
		this.session = session;
		this.nickname = nickname;
	}
	
	public int getId() {
		return id;
	}
	
	public Session getSession() {
		return session;
	}
	
	public String getNickname() {
		return nickname;
	}

	public void sendMessage(String message) {
		try {
			if (session.isOpen()) {
				session.getBasicRemote().sendText(message);
			}
		} catch (IOException e) {
			try {
				session.close();
			} catch (IOException e1) {
			}
		}
	}
	
}

