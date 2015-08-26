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

import org.apache.log4j.Logger;

import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * This is the Container for holding InternetAddresses. It is self-sorting.
 * This is what is retrieved from the AddressBkDAO, and all InternetAddress
 * objects are accessed through here.
 *
 * @author drakee
 * @version $Revision: 1.2 $
 */
public final class AddressList implements List, java.io.Serializable {
    private static final Logger logger = Logger.getLogger(AddressList.class.getName());
    private static Comparator addressSort = new AddressSort();

    private String permId;
    private List addressList;
    private transient AddressBkDAO addressBkDAO;

    // constructor
    public AddressList(final String permId) throws AddressBkDAOException {
        this.permId = permId;
        //this.addressSort = new AddressSort();
        this.addressList = new ArrayList();
    }

    /**
     * Checks if an email address is already present in this List
     */
    public boolean containsEmail(final String email) {
        final int size = addressList.size();
        for (int i = 0; i < size; i++) {
            final InternetAddress internetAddress = (InternetAddress)addressList.get(i);
            if (internetAddress.getAddress().equals(email))
                return true;
        }
        return false;
    }

    /**
     * Adds Email entry into both in-memory list and backend storage.
     */
    public boolean addAddress(final InternetAddress internetAddress) throws AddressBkDAOException {
        getAddressBkDAO().addEntry(permId, internetAddress);
        return add(internetAddress);
    }

    /**
     * Removes Email entry from both in-memory list and backend storage.
     */
    public boolean removeAddress(final InternetAddress internetAddress) throws AddressBkDAOException {
        getAddressBkDAO().removeEntry(permId, internetAddress);
        return remove(internetAddress);
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        final int size = addressList.size();
        for (int i = 0; i < size; i++) {
            final InternetAddress internetAddress = (InternetAddress)addressList.get(i);
            buff.append(internetAddress.toString());
            if (i < size - 1)
                buff.append(", ");
        }
        return buff.toString();
    }

    private AddressBkDAO getAddressBkDAO() throws AddressBkDAOException {
        if (addressBkDAO == null) {
	    addressBkDAO = DAOFactory.getInstance().getAddressBkDAO();
	}
	return addressBkDAO;
    }

    ////////////////////////////////////////////////////////////
    // satisfy the List interface:
    ////////////////////////////////////////////////////////////
    public void add(final int index, final Object element) {
        if (!(element instanceof InternetAddress)) return;
        addressList.add(index, element);
        sort();
    }

    public boolean add(final Object o) {
        if (!(o instanceof InternetAddress)) return false;
        addressList.add(o);
        sort();
        return true;
    }

    public boolean addAll(final Collection c) {
        final Iterator it = c.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (!(o instanceof InternetAddress)) return false;
        }
        addressList.addAll(c);
        sort();
        return true;
    }

    public boolean addAll(final int index, final Collection c) {
        final Iterator it = c.iterator();
        while (it.hasNext()) {
            final Object o = it.next();
            if (!(o instanceof InternetAddress)) return false;
        }
        addressList.addAll(index, c);
        sort();
        return true;
    }

    public void clear() {
        addressList.clear();
    }

    public boolean contains(final Object o) {
        return addressList.contains(o);
    }

    public boolean containsAll(final Collection c) {
        return addressList.containsAll(c);
    }

    public boolean equals(final Object o) {
        return addressList.equals(o);
    }

    public Object get(final int index) {
        return addressList.get(index);
    }

    public int hashCode() {
        return addressList.hashCode();
    }

    public int indexOf(final Object o) {
        return addressList.indexOf(o);
    }

    public boolean isEmpty() {
        return addressList.isEmpty();
    }

    public Iterator iterator() {
        return addressList.iterator();
    }

    public int lastIndexOf(final Object o) {
        return addressList.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return addressList.listIterator();
    }

    public ListIterator listIterator(final int index) {
        return addressList.listIterator(index);
    }

    public Object remove(final int index) {
        return addressList.remove(index);
    }

    public boolean remove(final Object o) {
        return addressList.remove(o);
    }

    public boolean removeAll(final Collection c) {
        return addressList.removeAll(c);
    }

    public boolean retainAll(final Collection c) {
        return addressList.retainAll(c);
    }

    public Object set(final int index, final Object element) {
        if (!(element instanceof InternetAddress)) return null;
        return addressList.set(index, element);
    }

    public int size() {
        return addressList.size();
    }

    public List subList(final int fromIndex, final int toIndex) {
        return addressList.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
        return addressList.toArray();
    }

    public Object[] toArray(final Object[] a) {
        return addressList.toArray();
    }

    ////////////////////////////////////////////////////////////
    // private utility methods
    ////////////////////////////////////////////////////////////

    private void sort() {
        Collections.sort(addressList, addressSort);
    }
}
