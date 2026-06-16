/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.jsftemplating.layout.xml;

import com.sun.jsftemplating.annotation.FormatDefinition;
import com.sun.jsftemplating.layout.LayoutDefinitionException;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.template.TemplateParser;
import com.sun.jsftemplating.util.ClasspathEntityResolver;
import com.sun.jsftemplating.util.FileUtil;

import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

/**
 * <p>
 * This class is a concrete implmentation of the abstract class {@link LayoutDefinitionManager}. It obtains
 * {@link LayoutDefinition} objects by interpreting the <code>key</code> passed to {@link #getLayoutDefinition(String)}
 * as a path to an XML file describing the {@link LayoutDefinition}. It will first attempt to resolve this path from the
 * document root of the ServletContext or PortletCotnext. If that fails, it will attempt to use the Classloader to
 * resolve it.
 * </p>
 *
 * <p>
 * Locating the dtd for the XML file is done in a similar manner. It will first attempt to locate the dtd relative to
 * the ServletContext (or PortletContext) root. If that fails it will attempt to use the ClassLoader to resolve it.
 * Optionally a different EntityResolver may be supplied to provide a custom way of locating the dtd, this is done via
 * {@link #setEntityResolver}.
 * </p>
 *
 * <p>
 * This class is a singleton. This means modifications to this class effect all threads using this class. This includes
 * setting EntityResolvers and ErrorHandlers. These values only need to be set once to remain in effect as long as the
 * JVM is running.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@FormatDefinition
public class XMLLayoutDefinitionManager extends LayoutDefinitionManager {

    /**
     * <p>
     * Constructor.
     * </p>
     */
    protected XMLLayoutDefinitionManager() {
        super();

        // Set the default XMLError Handler
        try {
            setErrorHandler(new XMLErrorHandler(new PrintWriter(new OutputStreamWriter(System.err, "UTF-8"), true)));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

        // Set the default EntityResolver
        setEntityResolver(new ClasspathEntityResolver());
    }

    /**
     * <p>
     * This method returns an instance of this LayoutDefinitionManager. The object returned is a singleton (only 1 instance
     * will be created per JVM).
     * </p>
     *
     * @return <code>XMLLayoutDefinitionManager</code> instance
     */
    public static LayoutDefinitionManager getInstance() {
        return getInstance(FacesContext.getCurrentInstance());
    }

    /**
     * <p>
     * This method returns an instance of this LayoutDefinitionManager. The object returned is a singleton (only 1 instance
     * will be created per application).
     * </p>
     *
     * @return <code>XMLLayoutDefinitionManager</code> instance
     */
    public static LayoutDefinitionManager getInstance(FacesContext ctx) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        XMLLayoutDefinitionManager instance = null;
        if (ctx != null) {
            instance = (XMLLayoutDefinitionManager) ctx.getExternalContext().getApplicationMap().get(XLDM_INSTANCE);
        }
        if (instance == null) {
            instance = new XMLLayoutDefinitionManager();
            if (ctx != null) {
                ctx.getExternalContext().getApplicationMap().put(XLDM_INSTANCE, instance);
            }
        }
        return instance;
    }

    /**
     * <p>
     * This method uses the key to determine if this {@link LayoutDefinitionManager} is responsible for handling the key.
     * </p>
     */
    @Override
    public boolean accepts(String key) {
        URL url = null;
        try {
            url = FileUtil.searchForFile(key, ".jsf");
        } catch (IOException ex) {
            // Ignore this b/c we're just trying to detect if we're the right
            // LDM... if we're here, probably we're not.
        }
        if (url == null) {
            return false;
        }

        // Use the TemplateParser to help us read the file to see if it is a
        // valid XML-format file
        TemplateParser parser = new TemplateParser(url);
        try {
            parser.open();
            parser.readUntil("<layoutDefinition>", true);
        } catch (Exception ex) {
            // Didn't work...
            return false;
        } finally {
            parser.close();
        }
        return true;
    }

    /**
     * <p>
     * This method is responsible for finding the requested {@link LayoutDefinition} for the given <code>key</code>.
     * </p>
     *
     * @param key Key identifying the desired {@link LayoutDefinition}
     *
     * @return The requested {@link LayoutDefinition}.
     */
    @Override
    public LayoutDefinition getLayoutDefinition(String key) throws LayoutDefinitionException {
        // Make sure we found the url
        URL url = null;
        try {
            url = FileUtil.searchForFile(key, ".jsf");
        } catch (IOException ex) {
            throw new LayoutDefinitionException("Unable to locate '" + key + "'", ex);
        }
        if (url == null) {
            throw new LayoutDefinitionException("Unable to locate '" + key + "'");
        }

        // Read the XML file
        LayoutDefinition ld = null;
        String baseURI = getBaseURI();
        try {
            ld = new XMLLayoutDefinitionReader(url, getEntityResolver(), getErrorHandler(), baseURI).read();
        } catch (IOException ex) {
            throw new LayoutDefinitionException("Unable to process '" + url + "'.  EntityResolver: '" + getEntityResolver() + "'.  ErrorHandler: '"
                    + getErrorHandler() + "'.  baseURI: '" + baseURI + "'.", ex);
        }

        // Dispatch "initPage" handlers
        ld.dispatchInitPageHandlers(FacesContext.getCurrentInstance(), ld);

        // Return the LayoutDefinition
        return ld;
    }

    /**
     * <p>
     * This returns the LDM's entity resolver, null if not set.
     * </p>
     *
     * @return EntityResolver
     */
    public EntityResolver getEntityResolver() {
        return (EntityResolver) getAttribute(ENTITY_RESOLVER);
    }

    /**
     * <p>
     * This method sets the LDM's entity resolver.
     * </p>
     *
     * @param entityResolver The EntityResolver to use.
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        setAttribute(ENTITY_RESOLVER, entityResolver);
    }

    /**
     * <p>
     * This returns the LDM's XML parser ErrorHandler, null if not set.
     * </p>
     *
     * @return ErrorHandler
     */
    public ErrorHandler getErrorHandler() {
        return (ErrorHandler) getAttribute(ERROR_HANDLER);
    }

    /**
     * <p>
     * This method sets the LDM's ErrorHandler.
     * </p>
     *
     * @param errorHandler The ErrorHandler to use.
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        setAttribute(ERROR_HANDLER, errorHandler);
    }

    /**
     * <p>
     * This returns the LDM's XML parser baseURI which will be used to resolve relative URI's, null if not set.
     * </p>
     *
     * @return The base URI as a String.
     */
    public String getBaseURI() {
        String baseURI = (String) getAttribute(BASE_URI);
        if (baseURI == null) {
            // Use docroot for the baseURI.
            baseURI = FileUtil.getResource("").toString();
        }
        return baseURI;
    }

    /**
     * <p>
     * This method sets the LDM's BaseURI.
     * </p>
     *
     * @param baseURI The BaseURI to use.
     */
    public void setBaseURI(String baseURI) {
        setAttribute(BASE_URI, baseURI);
    }

    /**
     * <p>
     * Application scope key for an instance of this class.
     * </p>
     */
    private static final String XLDM_INSTANCE = "__jsft_XML_LDM";

    /**
     * <p>
     * This is an attribute key which can be used to provide an EntityResolver to the XML parser.
     * </p>
     */
    public static final String ENTITY_RESOLVER = "entityResolver";

    /**
     *
     */
    public static final String ERROR_HANDLER = "errorHandler";

    /**
     *
     */
    public static final String BASE_URI = "baseURI";
}
