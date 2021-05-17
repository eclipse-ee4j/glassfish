/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node;

import com.sun.enterprise.deployment.util.DOLUtils;
import org.jvnet.hk2.annotations.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

/**
 * Provides access to schemas and DTDs to Java Web Start-launched app clients
 * that do not have an app server product installation at hand.
 * <p/>
 * The DTDs and schemas are assumed to be in the classpath so that
 * schemas are at /schemas/<schema-name> and DTDs at /dtds/<dtd-name>
 *
 * This ParserHandler is used by Embedded GlassFish as well.
 *
 * @author tjquinn
 */
@Service
public class SaxParserHandlerBundled extends SaxParserHandler {

    /**
     * prefixes for the paths to use for looking up schemas and dtds as resources
     */
    private static final String BUNDLED_SCHEMA_ROOT = "/schemas";
    private static final String BUNDLED_DTD_ROOT = "/dtds";

    /**
     * Creates a new instance of SaxParserHandlerBundled
     */
    public SaxParserHandlerBundled() {
    }

    /**
     * Returns an InputSource for the requested DTD or schema.
     * <p/>
     * This implementation returns an InputSource that wraps the result
     * of getResourceAsStream, having
     * located the requested schema in the classpath.
     *
     * @param publicID public ID of the requested entity
     * @param systemID system ID of the requested entity
     * @return InputSource for the requested entity; null if not available
     * @throws SAXException in case of errors resolving the entity
     */
    public InputSource resolveEntity(String publicID, String systemID) throws SAXException {
        InputSource result = null;

        /*
         *This logic was patterned after that in the superclass.
         */
        try {
            if(DOLUtils.getDefaultLogger().isLoggable(Level.FINE)) {
                DOLUtils.getDefaultLogger().fine("Asked to resolve  " + publicID +
                        " system id = " + systemID);
            }
            if (publicID==null) {
                // unspecified schema
                if (systemID==null || systemID.lastIndexOf('/')==systemID.length()) {
                    return null;
                }

                /*
                *Attempt to open a stream to the requested resource as a schema.
                */
                result = openSchemaSource(systemID);

                if (result == null) {
                    /*
                     *The entity was not found, so wrap an InputSource around the system ID string instead.
                     */
                    result = new InputSource(systemID);
                }
            } else {
                /*
                 *Try to find a DTD for the entity.
                 */
                if ( getMapping().containsKey(publicID)) {
                    this.publicID = publicID;
                    result = openDTDSource(publicID);
                } else if (systemID != null) {
                    /*
                     *The DTD lookup failed but a system ID was also specified.  Try
                     *looking up the DTD in the schemas path, because some reside
                     *there and were not registered in the DTD map by SaxParserHandler.
                     */
                    result = openSchemaSource(systemID);

                    if (result == null) {
                        /*
                        * As a last resort, try opening the DTD without going
                        * through the mapping table.
                        */
                        result = openInputSource(BUNDLED_DTD_ROOT, systemID);
                    }
                }
            }
        } catch (Exception exc) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, "Error occurred", exc);
            throw new SAXException(exc);
        }
        return result;
    }

    /**
     * Returns an InputStream for the schema with the requested system ID.
     *
     * @param systemID system ID of the schema
     * @return an InputStream to the selected schema; null if the schema is not available
     */
    private InputSource openSchemaSource(String systemID) {
        return openInputSource(BUNDLED_SCHEMA_ROOT, systemID);
    }

    /**
     * Returns an InputStream for the DTD with the requested public ID.
     *
     * @param publicID public ID of the DTD requested
     * @return an InputStream to the selected DTD; null if the DTD is not available
     */
    private InputSource openDTDSource(String publicID) {
        return openInputSource(BUNDLED_DTD_ROOT, getMapping().get(publicID));
    }

    private InputSource openInputSource(final String localRoot, final String systemID) {
        String targetID = localRoot + "/" + systemID.substring(systemID.lastIndexOf("/") + 1);
        URL url = this.getClass().getResource(targetID);
        InputStream stream;
        try {
            stream = url != null ? url.openStream() : null;
        } catch (IOException e) {
            stream = null;
        }
        InputSource source = null;
        if (stream != null) {
            source = new InputSource(stream);
            source.setSystemId(url.toString());
        }
        return source;
    }
}
