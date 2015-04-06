package aginiers.minesweeper;

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
  private final SendMessagesThread sendMessagesThread;

  public User(int id, Session session, String nickname) {
    this.id = id;
    this.session = session;
    this.nickname = nickname;
    this.sendMessagesThread = new SendMessagesThread(this);
    sendMessagesThread.start();
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

  public void disconnect() {
    sendMessagesThread.interrupt();
  }

  public void sendMessage(String message) {
    sendMessagesThread.addMessage(message);
  }

}
