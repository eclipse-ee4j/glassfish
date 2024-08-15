/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.impl.authorization;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.security.services.api.authorization.AzResource;
import org.glassfish.security.services.api.common.Attributes;
import org.glassfish.security.services.impl.ServiceLogging;

public final class AzResourceImpl extends AzAttributesImpl implements AzResource {


    private static final Logger logger = Logger.getLogger(ServiceLogging.SEC_SVCS_LOGGER,ServiceLogging.SHARED_LOGMESSAGE_RESOURCE);

    private static final boolean REPLACE = true;


    private final URI uri;


    /**
     * Constructor
     *
     * @param resource The represented resource
     * @throws IllegalArgumentException Given resource was null
     */
    public AzResourceImpl( URI resource )  {
        super(NAME);

        if ( null == resource ) {
            throw new IllegalArgumentException("Illegal null resource URI.");
        }
        this.uri = resource;

        // Dump the query parameters into the attribute map.
        addAttributesFromUriQuery( uri, this, !REPLACE );
    }


    /**
     * Determines the URI representing this resource.
     * @return The URI representing this resource., never null.
     */
    @Override
    public URI getUri() {
        return uri;
    }


    /**
     * Determines the URI used to initialize this resource.
     * @return The URI used to initialize this resource.
     */
    @Override
    public String toString() {
        return uri.toString();
    }


    /**
     * Yet another URI query parser, but this one knows how to populate
     * <code>{@link org.glassfish.security.services.api.common.Attributes}</code>.
     *
     * @param uri The URI from which the query will be derived.
     * @param attributes Attributes collection to populate
     * @param replace true to replace entire attribute, false to append value to attribute.
     * See <code>{@link org.glassfish.security.services.api.common.Attributes#addAttribute}</code>.
     * @throws IllegalArgumentException URI or Attributes is null.
     */
    static void addAttributesFromUriQuery( URI uri, Attributes attributes, boolean replace ) {
        if ( null == uri ) {
            throw new IllegalArgumentException( "Illegal null URI." );
        }
        if ( null == attributes ) {
            throw new IllegalArgumentException( "Illegal null Attributes." );
        }

        String query = uri.getRawQuery();
        if ( ( null != query ) && ( query.length() > 0 ) ) {
            String[] params = query.split( "&" );
            if ( ( null != params ) && ( params.length > 0 ) ) {
                for ( String nv : params ) {
                    if ( (null == nv) || (nv.length() <= 0) )  {
                        continue;
                    }

                    String name, value;
                    int equalsPos = nv.indexOf( "=" );
                    if ( -1 == equalsPos ) {
                        name = decodeURI( nv );
                        value = "";
                    } else {
                        name = decodeURI( nv.substring( 0, equalsPos ) );
                        value = decodeURI( nv.substring( equalsPos + 1 ) );
                    }

                    attributes.addAttribute( name, value, replace );
                }
            }
        }
    }


    /**
     * URI decode the input, assumes UTF-8 encoding.
     *
     * @param input The input to decode.
     * @return The decoded input, null returns null.
     */
    static String decodeURI( String input ) {
        if ( null == input ) {
            return null;
        }

        String output = input;
        try {
            output = URLDecoder.decode(input, "UTF-8");
        } catch ( UnsupportedEncodingException e ) {
            if ( logger.isLoggable( Level.WARNING ) ) {
                logger.log( Level.WARNING, URI_DECODING_ERROR, e.getLocalizedMessage() );
            }
        }

        return output;
    }

    @LogMessageInfo(message = "Unable to decode URI: {0}.", level = "WARNING")
    private static final String URI_DECODING_ERROR = "SEC-SVCS-00102";
}
