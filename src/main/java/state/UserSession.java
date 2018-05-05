package state;

import org.eclipse.jetty.websocket.api.Session;

import models.User;

public class UserSession implements Comparable<UserSession> {
	private User user;
	private Session session;
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public int compareTo(UserSession second) {
		return user.getId() - second.getUser().getId();
	}
}
