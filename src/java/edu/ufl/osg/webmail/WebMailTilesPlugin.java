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

package edu.ufl.osg.webmail;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsFactory;
import org.apache.struts.tiles.DefinitionsFactoryConfig;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.NoSuchDefinitionException;
import org.apache.struts.tiles.TilesPlugin;
import org.apache.struts.tiles.TilesUtilImpl;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

/**
 * The extends {@link org.apache.struts.tiles.TilesPlugin} to wrap the
 * definition factory that would normally be loaded in a custom one that will
 * try decreasingly complex mime types encoded in the tiles definition name.
 *
 * <p>When using this plugin you need to set the <code>mimeTilesBase</code>
 * property to the {@link java.lang.String#startsWith(String)} of what is used
 * in out message view.</p>
 *
 * @author sandymac
 * @version $Revision: 1.2 $
 */
public class WebMailTilesPlugin extends TilesPlugin {
    private String mimeTilesBase = "SoMe_BoGuS_vAlUe";

    /**
     * Calls <code>super.init(ActionServlet,ModuleConfig)</code> and then wraps
     * the <code>definitionFactory</code> in a custom {@link
     * org.apache.struts.tiles.DefinitionsFactory} that retries on less specific
     * mime types.
     *
     * @param  actionServlet
     * @param  moduleConfig
     * @throws ServletException
     */
    public void init(final ActionServlet actionServlet, final ModuleConfig moduleConfig) throws ServletException {
        super.init(actionServlet, moduleConfig);

        // Wrap the Definition Factory
        definitionFactory = new DefinitionsFactoryWrapper(definitionFactory);
        actionServlet.getServletContext().setAttribute(TilesUtilImpl.DEFINITIONS_FACTORY, definitionFactory);
    }

    /**
     * Accessor for the MimeTilesBase property.
     *
     * @return                     the mimeTilesBase proerty.
     */
    public String getMimeTilesBase() {
        return mimeTilesBase;
    }

    /**
     * Setter for the MimeTilesBase property.
     *
     * @param mimeTilesBase       The base of the defination for mime types.
     */
    public void setMimeTilesBase(final String mimeTilesBase) {
        this.mimeTilesBase = mimeTilesBase;
    }


    /**
     * Wrapper for {@link org.apache.struts.tiles.DefinitionsFactory} that
     * retries to find definitions if they match a mime type pattern.
     */
    private class DefinitionsFactoryWrapper implements DefinitionsFactory {
        DefinitionsFactory definitionsFactory;

        DefinitionsFactoryWrapper(final DefinitionsFactory definitionsFactory) {
            this.definitionsFactory = definitionsFactory;
        }

        public ComponentDefinition getDefinition(final String name, final ServletRequest request, final ServletContext servletContext) throws NoSuchDefinitionException, DefinitionsFactoryException {
            ComponentDefinition definition = null;
            if (name.startsWith(mimeTilesBase)) {
                String tmpName = name.toLowerCase();
                try {
                    // eg: message.text/plain
                    definition = definitionsFactory.getDefinition(tmpName, request, servletContext);
                } catch (DefinitionsFactoryException e) {
                    // Do noting, We'll reproduce this later
                }

                if (definition == null) {
                    tmpName = tmpName.substring(0, tmpName.indexOf("/"));
                    try {
                        // eg: message.text
                        definition = definitionsFactory.getDefinition(tmpName, request, servletContext);
                    } catch (DefinitionsFactoryException e) {
                        // Do noting
                    }
                }

                if (definition == null) {
                    tmpName = tmpName.substring(0, tmpName.indexOf("."));
                    try {
                        // eg: message
                        definition = definitionsFactory.getDefinition(tmpName, request, servletContext);
                    } catch (DefinitionsFactoryException e) {
                        // Do noting
                    }
                }
            }

            if (definition == null) {
                // No mangling or this is a last to make the thrown exception have the right info.
                // eg: message.text/plain
                definition = definitionsFactory.getDefinition(name, request, servletContext);
            }
            return definition;
        }

        public void init(final DefinitionsFactoryConfig definitionsFactoryConfig, final ServletContext servletContext) throws DefinitionsFactoryException {
            definitionsFactory.init(definitionsFactoryConfig, servletContext);
        }

        public void destroy() {
            definitionsFactory.destroy();
        }

        public void setConfig(final DefinitionsFactoryConfig definitionsFactoryConfig, final ServletContext servletContext) throws DefinitionsFactoryException {
            definitionsFactory.setConfig(definitionsFactoryConfig, servletContext);
        }

        public DefinitionsFactoryConfig getConfig() {
            return definitionsFactory.getConfig();
        }
    }

}