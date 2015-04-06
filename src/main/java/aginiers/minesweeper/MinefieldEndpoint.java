package aginiers.minesweeper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author aginiers
 *
 */
@ServerEndpoint("/websocket/{nickname}")
public class MinefieldEndpoint {
	
	private static final AtomicInteger idIncrementer = new AtomicInteger(0);
	private static final ConcurrentHashMap<Integer, User> users = new ConcurrentHashMap<Integer, User>();
	private MinefieldHandler minefieldHandler = new MinefieldHandler();
	private final int id;
	private User user;
	
	public MinefieldEndpoint() {
		this.id = MinefieldEndpoint.idIncrementer.getAndIncrement();
	}
	
	@OnOpen
	public void onOpen(@PathParam("nickname") String nickname, Session session) {
		this.user = new User(this.id, session, this.identicalNickname(nickname));
		MinefieldEndpoint.users.put(this.id, this.user);
		broadcast(createLogJson(this.user.getNickname() + " has joined."));
		broadcast(createUserlistJson());
	}
	
	@OnClose
	public void onClose(Session session) {
		MinefieldEndpoint.users.remove(this.id);
		this.user.disconnect();
		broadcast(createLogJson(this.user.getNickname() + " has left."));
		broadcast(createUserlistJson());
	}

	/**
	 * Called when the server receives a message from this user
	 * @param message
	 */
	@OnMessage
	public void onTextMessage(String message) {
		
		JSONObject messageJson = new JSONObject(message);
		
		if (messageJson.getString("type").equals("chat")) {
			
			broadcast(createLogJson(this.user.getNickname() + " : " + messageJson.get("data")));
		} else if (messageJson.getString("type").equals("minefield")) {
			minefieldHandler.onMessage(messageJson.getJSONObject("data"), id);
		}
	}
	
	/**
	 * Sends a message to all users
	 * @param message
	 */
	public static void broadcast(String message) {
		JsonBroadcasterThread jsonBroadcasterThread = new JsonBroadcasterThread(MinefieldEndpoint.users, message);
		jsonBroadcasterThread.start();
	}

	/**
	 * Sends a message to a user based on his id
	 * @param message
	 * @param id
	 */
	public static void sendToUser(String message, int id) {
		MinefieldEndpoint.users.get(id).sendMessage(message);
	}
	
	/**
	 * 
	 * @param nickname
	 * @return
	 */
	private String identicalNickname(String nickname) {
		String result = nickname;
		for (User user : MinefieldEndpoint.users.values()) {
			if (nickname.equals(user.getNickname())) {
				result = identicalNickname(result + "_bis");
				break;
			}
		}
		return result;
	}
	
	/**
	 * Creates a JSON string, used for the log
	 * {"type": "log", "data": message}
	 * @param message
	 * @return
	 */
	private String createLogJson(String message) {
		JSONObject messageJson = new JSONObject();
		messageJson.put("type", "chat");
		messageJson.put("data", message);
		return messageJson.toString();
	}
	
	/**
	 * Creates a JSON string, used for the userlist
	 * {"type": "userlist", "data": [userlist]}
	 * @param message
	 * @return
	 */
	private String createUserlistJson() {
		JSONArray userList = new JSONArray();
		for (User user : MinefieldEndpoint.users.values()) {
			userList.put(user.getNickname());
		}
		JSONObject messageJson = new JSONObject();
		messageJson.put("type", "userlist");
		messageJson.put("data", userList);
		return messageJson.toString();
	}

}
