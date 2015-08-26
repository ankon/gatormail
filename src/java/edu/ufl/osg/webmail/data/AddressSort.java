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
import java.util.Comparator;

/**
 * @author drakee
 * @version $Revision: 1.2 $
 */
class AddressSort implements Comparator {
    /**
     * Sorts by Entry name, alphabetically. If two entries have the same
     * value, it will resort to comparing the email addresses.
     */
    public int compare(final Object o1, final Object o2) {
        final InternetAddress a1 = (InternetAddress)o1;
        final InternetAddress a2 = (InternetAddress)o2;
        // entry comparisons get first priority
        if (a1.getPersonal().compareTo(a2.getPersonal()) < 0) {
            return -1;
        } else if (a1.getPersonal().compareTo(a2.getPersonal()) > 0) {
            return 1;
        }
        // entries are equal, so let's compare email addresses
        if (a1.getAddress().compareTo(a2.getAddress()) < 0) {
            return -1;
        } else if (a1.getAddress().compareTo(a2.getAddress()) > 0) {
            return 1;
        }
        return 0; // should never get here
    }
}
