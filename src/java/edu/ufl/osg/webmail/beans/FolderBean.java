/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2002, 2003 William A. McArthur, Jr.
 * Copyright (C) 2003 The Open Systems Group / University of Florida
 * Copyright (C) 2004 Allison Moore
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

package edu.ufl.osg.webmail.beans;

/**
 * Holds folder information.
 * Lightweight representation of a folder, for storing in a Collection.
 *
 * @author sandymac
 * @version $Revision: 1.3 $
 */
public class FolderBean {

    private String name;
    private String fullName;
    private long size;
    private int numMessages;
    private boolean isSubscribed;
    private String sort;

    public FolderBean() {
    }

    public FolderBean(final String name, final String fullName) {
        this.name = name;
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public int getNumMessages() {
        return numMessages;
    }

    public void setNumMessages(final int numMessages) {
        this.numMessages = numMessages;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(final String sort) {
        this.sort = sort;
    }

    public String getIsSubscribed() {
        return String.valueOf(isSubscribed);
    }

    public void setSubscribed(final boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    /**
     * @return full name of the folder this represents
     */
    public String toString() {
        return fullName;
    }
}
