package messages;

import java.util.List;

import models.User;

public class UserListMessage extends Message {

	private List<User> list;

	public List<User> getList() {
		return list;
	}

	public void setList(List<User> list) {
		this.list = list;
	}
}
