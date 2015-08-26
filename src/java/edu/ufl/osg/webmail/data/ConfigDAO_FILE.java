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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Implementation for initializing and retrieving configuration data.
 *
 * @author drakee
 * @version $Revision: 1.3 $
 */
public class ConfigDAO_FILE implements ConfigDAO {
    private static final Logger logger = Logger.getLogger(ConfigDAO_FILE.class.getName());
    private static final String propPath = "/gatormail.properties";
    private Properties props;

    protected ConfigDAO_FILE() throws ConfigDAOException {
        props = new Properties();
        try {
            props.load(ConfigDAO_FILE.class.getResourceAsStream(propPath));
        } catch (IOException ioe) {
            logger.error(ioe.toString());
            throw new ConfigDAOException(ioe.toString());
        }
    }

    public Properties getProperties() {
        return props;
    }

    public String getProperty(final String key) {
        return props.getProperty(key);
    }
}
