package edu.ufl.osg.webmail.data;

import edu.ufl.osg.webmail.User;

public class UserInfoDAO_Transient implements UserInfoDAO {
	@Override
	public String getDisplayName(String username) throws UserInfoDAOException {
		return username;
	}

	@Override
	public String getDisplayName(String username, String password) throws UserInfoDAOException {
		return getDisplayName(username);
	}

	@Override
	public String getPermId(String username) throws UserInfoDAOException {
		return username;
	}

	@Override
	public String getPermId(String username, String password) throws UserInfoDAOException {
		return getPermId(username);
	}

	@Override
	public void setUserInfo(User user) throws UserInfoDAOException {
		user.setPermId(user.getUsername());
		user.setDisplayName(user.getUsername());
	}
}
