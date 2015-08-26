/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003 The Open Systems Group / University of Florida
 *
 * GatorMail is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * GatorMail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GatorMail; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ufl.osg.webmail.data;

import edu.ufl.osg.webmail.User;

/**
 * Interface for obtaining and setting user information.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public interface UserInfoDAO {
    /**
     * Get the display name for the user.
     * Used when piercing privacy, since no password is passed in.
     * @param username the user ID whose display name we want.
     */
    public String getDisplayName(String username) throws UserInfoDAOException;

    /**
     * Get the display name for the user.
     * @param username the user ID whose display name we want.
     * @param password the credentials for username.
     */
    public String getDisplayName(String username, String password) throws UserInfoDAOException;

    /**
     * Get the permanent ID code to represent the username.
     * Used when piercing privacy, since no password is passed in.
     * @param username the user id whose permanent ID we want.
     */
    public String getPermId(String username) throws UserInfoDAOException;

    /**
     * Get the permanent ID code to represent the username.
     * @param username the user id whose permanent ID we want.
     * @param password the credentials for username.
     */
    public String getPermId(String username, String password) throws UserInfoDAOException;

    /**
     * Set the User object with all available information. When appropriate
     * (if privacy piercing is not set), uses the password set in user.
     * @param user the user object to set information in.
     */
    public void setUserInfo(User user) throws UserInfoDAOException;
}
