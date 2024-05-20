/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.deploy.shared;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Utility logic.
 *
 */
public class Util {

    private static final String SPACE = " ";

    private static final String ENCODED_SPACE = "%20";

   /**
    * Returns the name portion of the specified URI.  This is defined as the
    * part of the URI's path after the final slash (if any).  If the URI ends
    * with a slash that final slash is ignored in finding the name.
    *
    * @param uri the URI from which to extract the name
    * @return the name portion of the URI
    */
    public static String getURIName(URI uri) {
        String path = uri.getSchemeSpecificPart();
         // Strip the path up to and including the last slash, if there is one.
         // A directory URI may end in a slash, so be sure to remove it if it
         // is there.
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        // correct whether a / appears or not
        int startOfName = path.lastIndexOf('/') + 1;
        return path.substring(startOfName);
    }

   /**
    * Returns URI for the specified URL.  This method will take care of
    * the space in URL.
    *
    * @param url the URL to convert to URI
    * @return the URI
    */
    public static URI toURI(URL url) throws URISyntaxException {
        return new URI(url.toString().replaceAll(SPACE, ENCODED_SPACE));
    }

   /**
    * Constructs a new URI by parsing the given string and then resolving it
    * against the base URI.  This method will take care of the space in String.
    *
    * @param baseUri the base URI to resolve against
    * @param uriString the String to construct URI and resolve
    * @return the resulting URI
    */
    public static URI resolve(URI baseUri, String uriString) {
        return baseUri.resolve(uriString.replaceAll(SPACE, ENCODED_SPACE));
    }
}
