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

import javax.mail.internet.InternetAddress;

/**
 * Interface for interacting with the addressbook store.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public interface AddressBkDAO {

    public AddressList getAddressList(String permId) throws AddressBkDAOException;

    public void addEntry(String permId, InternetAddress internetAddress) throws AddressBkDAOException;

    public void removeEntry(String permId, InternetAddress internetAddress) throws AddressBkDAOException;
}
