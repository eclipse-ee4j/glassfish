/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.embed.impl;

import org.apache.catalina.startup.Constants;
import org.glassfish.internal.api.ServerContext;
import jakarta.inject.Inject;
import org.glassfish.hk2.api.PostConstruct;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * For Embedded GlassFish, override loading of known DTDs via
 * getClass().getResource() whenever there is no installRoot/lib/dtds
 * to avoid fetching the DTDs via HttpUrlConnection.
 *
 * @author bhavanishankar@dev.java.net
 * @see org.glassfish.web.WebEntityResolver#resolveEntity(String, String)
 */
//@Service(name="web")
//@ContractsProvided({EmbeddedWebEntityResolver.class, EntityResolver.class})
public class EmbeddedWebEntityResolver implements EntityResolver, PostConstruct {

    @Inject
    ServerContext serverContext;

    private File dtdDir;

    public static final Map<String/*public id*/, String/*bare file name*/> knownDTDs =
            new HashMap<String, String>();

    static {
        knownDTDs.put(Constants.TldDtdPublicId_11, "web-jsptaglibrary_1_1.dtd");
        knownDTDs.put(Constants.TldDtdPublicId_12, "web-jsptaglibrary_1_2.dtd");
        knownDTDs.put(Constants.WebDtdPublicId_22, "web-app_2_2.dtd");
        knownDTDs.put(Constants.WebDtdPublicId_23, "web-app_2_3.dtd");
    }

    public void postConstruct() {
        if (serverContext != null) {
            File root = serverContext.getInstallRoot();
            File libRoot = new File(root, "lib");
            dtdDir = new File(libRoot, "dtds");
        }
    }

    /**
     * Fetch the DTD via getClass().getResource() if the DTD is not
     *
     * @param publicId
     * @param systemId
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        InputSource resolvedEntity = __resolveEntity(publicId, systemId);
        if (resolvedEntity == null) {
            String fileName = knownDTDs.get(publicId);
            URL url = this.getClass().getResource("/dtds/" + fileName);
            InputStream stream = url != null ? url.openStream() : null;
            if (stream != null) {
                resolvedEntity = new InputSource(stream);
                resolvedEntity.setSystemId(url.toString());
            }
        }
        return resolvedEntity;
    }

    /**
     * Try to fetch DTD from installRoot. Copied from org.glassfish.web.WebEntityResolver
     */
    public InputSource __resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        String fileName = knownDTDs.get(publicId);
        if (fileName != null && dtdDir != null) {
            File dtd = new File(dtdDir, fileName);
            if (dtd.exists()) {
                return new InputSource(dtd.toURI().toString());
            }
        }
        return null;
    }

}
