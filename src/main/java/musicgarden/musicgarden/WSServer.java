package musicgarden.musicgarden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import messages.BroadcastMessage;
import messages.CreateRoomMessage;
import messages.DisplayMessage;
import messages.Message;
import messages.UserListMessage;
import messages.UserMessage;
import models.User;
import state.Room;
import state.UserSession;

import static musicgarden.musicgarden.Constants.*;

@WebSocket
public class WSServer {
	
	private static HashMap<Integer, Integer> ownerMap = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Room> rooms = new HashMap<Integer, Room>();
	
	@OnWebSocketMessage
	public void onText(Session session, String message) throws IOException {
		System.out.println("Message received:" + message);
		if (session.isOpen()) {
			handleMessage(session, message);
		}
	}
	
	private synchronized void handleMessage(Session session, String response) {

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();

		Message message = gson.fromJson(response, Message.class);
		
		switch(message.getType())
		{
		case CREATE:
			CreateRoomMessage createMessage = gson.fromJson(response, CreateRoomMessage.class);
			createRoom(session, createMessage);
			break;
		case BROADCAST:
			BroadcastMessage broadcastMessage = gson.fromJson(response, BroadcastMessage.class);
			broadcast(session, broadcastMessage);
			break;
		case DESTROY:
			UserMessage destroyMessage = gson.fromJson(response, UserMessage.class);
			destroyRoom(session, destroyMessage);
			break;
		case CONNECT:
			UserMessage connectMessage = gson.fromJson(response, UserMessage.class);
			joinRoom(session, connectMessage);
			break;
		case HEARTBEAT:
			send(session, message);
			break;
		case GET_USER_LIST:
			String url = session.getUpgradeRequest().getRequestURI().toString();
			int roomID = Integer.parseInt(url.substring(url.lastIndexOf('/') + 1));
			notifyUserListChange(roomID);
			break;
		}

	}

	
	private synchronized void broadcast(Session session, BroadcastMessage bm) {
		String url = session.getUpgradeRequest().getRequestURI().toString();
		try {
			int roomID = Integer.parseInt(url.substring(url.lastIndexOf('/') + 1));
			Room room = rooms.get(roomID);
			room.setMediaData(bm);
			
			sendToAll(roomID, room.getBroadcastMessage());
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private synchronized List<User> generateUserList(int roomID) {
		List<User> list = new ArrayList<User>();
		Room room = rooms.get(roomID);
		HashMap<Integer, UserSession> users = room.getUserSessions();
		Iterator<Integer> iterator = users.keySet().iterator();
		while(iterator.hasNext()) {
			int key = iterator.next();
			UserSession us = users.get(key);
			list.add(us.getUser());
		}
		return list;
	}
	
	
	private synchronized void createRoom(Session session, CreateRoomMessage message) {
		int roomID;
		do
		{
			roomID = (int)(Math.random()*10000) + 1;
		}while(rooms.get(roomID) != null);
		
		ownerMap.put(message.getUser().getId(), roomID);
		
		Room room = new Room();
		room.setRoomID(roomID);
		room.setRoomName(message.getName());
		room.setState(CREATED);
		HashMap<Integer, UserSession> users = new HashMap<Integer, UserSession>();
		room.setUserSessions(users);
		
		rooms.put(roomID, room);
		
		DisplayMessage displayMessage = new DisplayMessage();
		displayMessage.setType(NOTIFY_ROOM_CREATED);
		displayMessage.setResponse(String.valueOf(roomID));
		
		send(session, displayMessage);
		session.close();
		
	}
	
	
	private synchronized <T> void send(Session session, T message) {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String response = gson.toJson(message);
		
		try {
			if(session.isOpen()) {
				session.getRemote().sendString(response);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private synchronized <T> void sendToAll(int roomID, T message) {
		HashMap<Integer, UserSession> users = rooms.get(roomID).getUserSessions();
		Iterator<Integer> iterator = users.keySet().iterator();
		while(iterator.hasNext()) {
			int key = iterator.next();
			UserSession s = users.get(key);
			send(s.getSession(), message);
		}
	}
	
	
	private synchronized void destroyRoom(Session session, UserMessage message) {
		
		Integer roomID = ownerMap.get(message.getUser().getId());
		if(roomID == null) {
			DisplayMessage displayMessage = new DisplayMessage();
			displayMessage.setType(DISPLAY);
			displayMessage.setResponse("Unauthorized");
			send(session, displayMessage);
			return;
		}
		
		DisplayMessage destroyMessage = new DisplayMessage();
		destroyMessage.setType(DISPLAY);
		destroyMessage.setResponse("Owner closed the room");
		sendToAll(roomID, destroyMessage);
		
		HashMap<Integer, UserSession> users = rooms.get(roomID).getUserSessions();
		Iterator<Integer> iterator = users.keySet().iterator();
		while(iterator.hasNext()) {
			int key = iterator.next();
			UserSession userSession = users.get(key);
			userSession.getSession().close();
		}
		
		rooms.remove(roomID);
		
		ownerMap.remove(message.getUser().getId());
		
	}
	
	
	private synchronized void joinRoom(Session session, UserMessage message) {
		String url = session.getUpgradeRequest().getRequestURI().toString();
		try {
			int roomID = Integer.parseInt(url.substring(url.lastIndexOf('/') + 1));
			
			if(rooms.get(roomID) == null) {
				DisplayMessage displayMessage = new DisplayMessage();
				displayMessage.setType(DISPLAY);
				displayMessage.setResponse("No such room exists");
				send(session, displayMessage);
				return;
			}
			
			UserSession us = new UserSession();
			us.setSession(session);
			us.setUser(message.getUser());
			
			HashMap<Integer, UserSession> users = rooms.get(roomID).getUserSessions();
			users.put(message.getUser().getId(), us);
			
		
			Room room = rooms.get(roomID);
			room.setUserSessions(users);
			rooms.put(roomID, room);
			

			notifyUserListChange(roomID);
			
			if(room.getState() != CREATED) {
				
				BroadcastMessage bm = room.getBroadcastMessage();
				send(session, bm);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@OnWebSocketConnect
	public void onConnect(Session session) throws IOException {
		System.out.println(session.getRemoteAddress().getHostString() + " connected!");
		String url = session.getUpgradeRequest().getRequestURI().toString();
		System.out.println(url);
		
		try {
			int roomID = Integer.parseInt(url.substring(url.lastIndexOf('/') + 1));
			
			if(rooms.get(roomID) == null) {
				DisplayMessage displayMessage = new DisplayMessage();
				displayMessage.setType(DISPLAY);
				displayMessage.setResponse("No such room exists");
				send(session, displayMessage);
				session.close();
				return;
			}
		}
		catch(Exception e) {
			// do nothing
		}
	
	}

	
	@OnWebSocketClose
	public synchronized void onClose(Session session, int status, String reason) {
		System.out.println(session.getRemoteAddress().getHostString() + " closed!");
		String url = session.getUpgradeRequest().getRequestURI().toString();
		try {
			int roomID = Integer.parseInt(url.substring(url.lastIndexOf('/') + 1));
			if(rooms.get(roomID) != null) {
				
				Room room = rooms.get(roomID);
				HashMap<Integer, UserSession> users = room.getUserSessions();
				Iterator<Integer> iterator = users.keySet().iterator();
				while(iterator.hasNext()) {
					int key = iterator.next();
					UserSession us = users.get(key);
					if(us.getSession().equals(session)) {
						users.remove(key);
						break;
					}
				}
				room.setUserSessions(users);
				
				notifyUserListChange(roomID);
				
			}
		}
		catch(Exception e) {
		}
	}
	
	
	private synchronized void notifyUserListChange(int roomID) {

		UserListMessage userListMessage = new UserListMessage();
		userListMessage.setList(generateUserList(roomID));
		userListMessage.setType(NOTIFY_USER_LIST);
		sendToAll(roomID, userListMessage);
		
	}

}