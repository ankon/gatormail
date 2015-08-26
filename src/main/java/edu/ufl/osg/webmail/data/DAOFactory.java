/*
 * This file is part of GatorMail, a servlet based webmail.
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

import edu.ufl.osg.webmail.SessionProvider;
import org.apache.log4j.Logger;

/**
 * The factory class that maintains and presents the singleton DAOs.
 *
 * @author drakee
 * @version $Revision: 1.4 $
 * @deprecated Try to start using {@link org.apache.struts.action.PlugIn Struts PlugIns} and putting providers in the {@link javax.servlet.ServletContext application context}.
 */
public class DAOFactory {
    private static final Logger logger = Logger.getLogger(DAOFactory.class.getName());
    private static DAOFactory daoFactory;
    private AttachDAO attachDAO;
    private AddressBkDAO addressBkDAO;
    private ConfigDAO configDAO;
    private UserInfoDAO userInfoDAO;
    private SessionProvider sessionProvider;

    /** No other class can instantiate this factory */
    private DAOFactory() {
    }

    /** Singleton accessor */
    public static DAOFactory getInstance() {
        if (daoFactory == null)
            daoFactory = new DAOFactory();
        return daoFactory;
    }

    ////////////////////////////////////////////////////////////
    // DAOs provided by this factory.
    ////////////////////////////////////////////////////////////
    /**
     * The Data Access Object for storing and retrieving attachments.
     *
     * @return                     the concrete implementation of AttachDAO
     */
    public AttachDAO getAttachDAO() throws AttachDAOException {
        if (attachDAO == null)
            attachDAO = new AttachDAO_FILE();
        return attachDAO;
    }

    /**
     * The Data Access Object for a user's address book.
     *
     * @return                     the concrete implementation of AddressBkDAO
     */
    public AddressBkDAO getAddressBkDAO() throws AddressBkDAOException {
        if (addressBkDAO == null) {
            addressBkDAO = new AddressBkDAO_DB();
            //addressBkDAO = new AddressBkDAO_Transient();
        }
        return addressBkDAO;
    }

    /**
     * The Data Access Object for a user's address book.
     *
     * @return                     the concrete implementation of AddressBkDAO
     */
    public ConfigDAO getConfigDAO() throws ConfigDAOException {
        if (configDAO == null)
            configDAO = new ConfigDAO_FILE();
        return configDAO;
    }

    /**
     * The Data Access Object for obtaining user information.
     *
     * @return                     the concrete implementation of UserInfoDAO
     */
    public UserInfoDAO getUserInfoDAO() throws UserInfoDAOException {
        if (userInfoDAO == null)
            userInfoDAO = new UserInfoDAO_LDAP();
        return userInfoDAO;
    }

    ////////////////////////////////////////////////////////////
    // only one, global instance of SessionProvider should exist
    ////////////////////////////////////////////////////////////
    public SessionProvider getSessionProvider() {
	return this.sessionProvider;
    }

    public void setSessionProvider(final SessionProvider sessionProvider) {
	this.sessionProvider = sessionProvider;
    }
}
