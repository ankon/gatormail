/*
 * This file is part of GatorMail, a servlet based webmail.
 * Copyright (C) 2005 The Open Systems Group / University of Florida
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

import com.sun.mail.imap.Quota;

import java.util.Set;
import java.util.TreeSet;
import java.io.Serializable;

/**
 * JavaBean to hold {@link Quota} info.
 *
 * @author sandymac
 * @since Sep 22, 2005
 * @version $Revision: 1.2 $
 */
public final class QuotaBean implements Comparable, Serializable {

    /**
     * Quota Root name.
     * @serial
     */
    private String quotaRoot;

    /**
     * Set of {@link ResourceBean}s.
     * @serial
     */
    private Set resources;

    public QuotaBean() {
    }

    public QuotaBean(final String quotaRoot) {
        this.quotaRoot = quotaRoot;
    }

    public QuotaBean(final String quotaRoot, final Set resources) {
        this.quotaRoot = quotaRoot;
        this.resources = resources;
    }

    public String getQuotaRoot() {
        return quotaRoot;
    }

    public void setQuotaRoot(final String quotaRoot) {
        this.quotaRoot = quotaRoot;
    }

    /**
     * Getter for resources of this quota root.
     * @return a {@link Set} of {@link ResourceBean}s.
     */
    public Set getResources() {
        return resources;
    }

    /**
     * Setter for resources of this quota root.
     * @param resources a {@link Set} containing only {@link ResourceBean}s.
     */
    public void setResources(final Set resources) {
        this.resources = resources;
    }

    public void addResource(final ResourceBean rb) {
        if (resources == null) {
            resources = new TreeSet();
        }
        resources.add(rb);
    }

    public int compareTo(final Object o) {
        final QuotaBean qb = (QuotaBean)o;
        return quotaRoot.compareTo(qb.quotaRoot);
    }

    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof QuotaBean)) return false;

        final QuotaBean quotaBean = (QuotaBean)o;

        return !(quotaRoot != null ? !quotaRoot.equals(quotaBean.quotaRoot) : quotaBean.quotaRoot != null);
    }

    public int hashCode() {
        return (quotaRoot != null ? quotaRoot.hashCode() : 0);
    }

    public String toString() {
        return "QuotaBean{" + "quotaRoot='" + quotaRoot + "'" + ", resources=" + resources + "}";
    }

    /**
     * JavaBean to hold {@link Quota.Resource} info.
     */
    public final static class ResourceBean implements Comparable, Serializable {

        /**
         * @serial
         */
        private String name;

        /**
         * @serial
         */
        private long usage;

        /**
         * @serial
         */
        private long limit;

        public ResourceBean() {
        }

        public ResourceBean(final String name, final long usage, final long limit) {
            this.name = name;
            this.usage = usage;
            this.limit = limit;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public long getUsage() {
            return usage;
        }

        public void setUsage(final long usage) {
            this.usage = usage;
        }

        public long getLimit() {
            return limit;
        }

        public void setLimit(final long limit) {
            this.limit = limit;
        }

        public byte getPercentUsed() {
            return (byte)(100D * ((double)getUsage() / (double)getLimit()));
        }

        public byte getPercentFree() {
            return (byte)(100 - getPercentUsed());
        }

        public int compareTo(final Object o) {
            final ResourceBean rb = (ResourceBean)o;
            int r;

            r = name.compareTo(rb.name);
            if (r == 0) {
                r = (int)(usage - rb.usage);
                if (r == 0) {
                    r = (int)(limit - rb.limit);
                }
            }
            return r;
        }

        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof ResourceBean)) return false;

            final ResourceBean resourceBean = (ResourceBean)o;

            return !(name != null ? !name.equals(resourceBean.name) : resourceBean.name != null);
        }

        public int hashCode() {
            return (name != null ? name.hashCode() : 0);
        }

        public String toString() {
            return "QuotaBean.ResourceBean{" + "name='" + name + "'" + ", usage=" + usage + ", limit=" + limit + "}";
        }
    }
}
