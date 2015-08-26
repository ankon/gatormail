/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2006 The Open Systems Group / University of Florida
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
import java.util.Map;
import java.util.WeakHashMap;

/**
 * An Addressbook implementation that doesn't preserve information beyond the user's session.
 *
 * @author sandymac
 * @since Feb 2, 2006
 * @version $Revision: 1.1 $
 */
public class AddressBkDAO_Transient implements AddressBkDAO {
    Map books = new WeakHashMap();

    public AddressList getAddressList(final String permId) throws AddressBkDAOException {
        AddressList list = (AddressList)books.get(permId);
        if (list == null) {
            list = new AddressList(permId);
            books.put(permId, list);
        }
        return list;
    }

    public void addEntry(final String permId, final InternetAddress internetAddress) throws AddressBkDAOException {
        // ignored
    }

    public void removeEntry(final String permId, final InternetAddress internetAddress) throws AddressBkDAOException {
        // ignored
    }
}
