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

package org.glassfish.webservices;

import com.sun.xml.ws.api.server.Adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry of JAXWS Adapter of endpoints.
 */
public class JAXWSAdapterRegistry {

    private static JAXWSAdapterRegistry registry = null;
    private final Map<String, ContextAdapter> store;

    private static final Logger logger = LogUtils.getLogger();

    /** Creates a new instance of JAXWSServletUtil */
    private JAXWSAdapterRegistry() {
        store = Collections.synchronizedMap(new HashMap<String, ContextAdapter>());
    }

    public static synchronized JAXWSAdapterRegistry getInstance() {
        if(registry == null)
            registry = new JAXWSAdapterRegistry();
        return registry;
    }

    public void addAdapter(String contextRoot, String urlPattern,
            Adapter info) {
        if (contextRoot == null)
            contextRoot = "";
        synchronized (store) {
            ContextAdapter contextRtInfo = store.get(contextRoot);
            if(contextRtInfo == null) {
                contextRtInfo = new ContextAdapter();
            }
            contextRtInfo.addAdapter(urlPattern, info);
            store.put(contextRoot, contextRtInfo);
        }
    }

     public Adapter getAdapter(String contextRoot,
             String path, String urlPattern ) {
            ContextAdapter serviceInfo = store.get(contextRoot);
            if(serviceInfo == null) {
                return null;
            }
            return serviceInfo.getAdapter(path, urlPattern);
     }

     public void removeAdapter(String contextRoot) {
         if(contextRoot == null)
             contextRoot = "";
         synchronized (store) {
            ContextAdapter serviceInfo = store.get(contextRoot);
            if(serviceInfo == null) {
                return ;
            }
            store.remove(contextRoot);
         }
     }

    static class ContextAdapter {

        final Map<String, Adapter> fixedUrlPatternEndpoints;
        final List<Adapter> pathUrlPatternEndpoints;

        ContextAdapter() {

            fixedUrlPatternEndpoints = Collections.synchronizedMap(new HashMap<String, Adapter>());
            pathUrlPatternEndpoints = Collections.synchronizedList(new ArrayList<Adapter>());
        }

        void addAdapter(String urlPattern, Adapter info) {
            if (urlPattern.indexOf("*.") != -1) {
                // cannot deal with implicit mapping right now
                logger.log(Level.SEVERE, LogUtils.ENTERPRISE_WEBSERVICE_IMPLICIT_MAPPING_NOT_SUPPORTED);
            } else if (urlPattern.endsWith("/*")) {
                pathUrlPatternEndpoints.add(info);
            } else {
                synchronized (fixedUrlPatternEndpoints) {
                    if (fixedUrlPatternEndpoints.containsKey(urlPattern)) {
                        logger.log(Level.SEVERE, LogUtils.ENTERPRISE_WEBSERVICE_DUPLICATE_SERVICE, urlPattern);
                    }
                    fixedUrlPatternEndpoints.put(urlPattern, info);
                }
            }
        }

        Adapter getAdapter(String path, String urlPattern) {
            Adapter result = fixedUrlPatternEndpoints.get(path);
            if (result == null) {
                // This loop is unnecessary.Essentially what it is doing to always
                // return the first element from pathUrlPatternEndpoints
                // TO DO clean up after SCF required
                synchronized (pathUrlPatternEndpoints) {
                    for (Iterator<Adapter> iter = pathUrlPatternEndpoints.iterator(); iter.hasNext();) {
                        Adapter candidate = iter.next();
                        if (path.startsWith(getValidPathForEndpoint(urlPattern))) {
                            result = candidate;
                            break;
                        }
                    }
                }
            }
            return result;
        }

         private String getValidPathForEndpoint(String s) {
            if (s.endsWith("/*")) {
                return s.substring(0, s.length() - 2);
            } else {
                return s;
            }
        }
    }
}
