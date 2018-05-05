package messages;

public class BroadcastMessage extends UserMessage {
	
	private String player_action;
	private String song_stream_url;
	private String album_image_url;
	private String song_name;
	private String artist_name;
	private String datetime;
	private int seek_time;
	
	public int getSeek_time() {
		return seek_time;
	}
	public void setSeek_time(int seek_time) {
		this.seek_time = seek_time;
	}
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	public String getPlayer_action() {
		return player_action;
	}
	public void setPlayer_action(String player_action) {
		this.player_action = player_action;
	}
	public String getSong_stream_url() {
		return song_stream_url;
	}
	public void setSong_stream_url(String song_stream_url) {
		this.song_stream_url = song_stream_url;
	}
	public String getAlbum_image_url() {
		return album_image_url;
	}
	public void setAlbum_image_url(String album_image_url) {
		this.album_image_url = album_image_url;
	}
	public String getSong_name() {
		return song_name;
	}
	public void setSong_name(String song_name) {
		this.song_name = song_name;
	}
	public String getArtist_name() {
		return artist_name;
	}
	public void setArtist_name(String artist_name) {
		this.artist_name = artist_name;
	}

}
