package messages;

import models.User;

public class UserMessage extends Message { 

	private User user;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
