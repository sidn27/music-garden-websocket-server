package state;

import static musicgarden.musicgarden.Constants.*;

import java.util.HashMap;

import messages.BroadcastMessage;
import models.MediaData;

public class Room {
	private String roomName;
	private int roomID;
	private HashMap<Integer, UserSession> userSessions;
	private MediaData mediaData;
	private int state;
	private String datetime;
	
	public HashMap<Integer, UserSession> getUserSessions() {
		return userSessions;
	}
	public void setUserSessions(HashMap<Integer, UserSession> userSessions) {
		this.userSessions = userSessions;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public int getRoomID() {
		return roomID;
	}
	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}
	public MediaData getMediaData() {
		return mediaData;
	}
	public void setMediaData(BroadcastMessage broadcastMessage) {
		MediaData mediaData = new MediaData();
		mediaData.setAlbum_image_url(broadcastMessage.getAlbum_image_url());
		mediaData.setArtist_name(broadcastMessage.getArtist_name());
		mediaData.setSeek_time(broadcastMessage.getSeek_time());
		mediaData.setSong_name(broadcastMessage.getSong_name());
		mediaData.setSong_stream_url(broadcastMessage.getSong_stream_url());
		this.datetime = broadcastMessage.getDatetime();
		this.state = broadcastMessage.getPlayer_action().equals("play") ? PLAYING : PAUSED;
		this.mediaData = mediaData;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	
	public BroadcastMessage getBroadcastMessage() {
		BroadcastMessage bm = new BroadcastMessage();
		bm.setAlbum_image_url(this.getMediaData().getAlbum_image_url());
		bm.setArtist_name(this.getMediaData().getArtist_name());
		bm.setSong_name(this.getMediaData().getSong_name());
		bm.setSong_stream_url(this.getMediaData().getSong_stream_url());
		bm.setSeek_time(this.getMediaData().getSeek_time());
		bm.setPlayer_action(this.getState() == PLAYING ? "play" : "pause");
		bm.setDatetime(this.getDatetime());
		bm.setType(BROADCAST);
		
		return bm;
		
	}
}
