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

package org.glassfish.web;

import com.sun.enterprise.util.MapBuilder;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.catalina.startup.Constants;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * {@link EntityResolver} that recognizes known public IDs of JavaEE DTDs/schemas
 * and return a local copy.
 *
 * <p>
 * This implementation assumes that those files are available in
 * <tt>$INSTALL_ROOT/lib/schemas</tt> and <tt>$INSTALL_ROOT/lib/dtds</tt>,
 * but in different environment, different implementation can be plugged in
 * to perform entirely different resolution.
 *
 * @author Kohsuke Kawaguchi
 */
@Service(name="web")
@ContractsProvided({WebEntityResolver.class, EntityResolver.class})
public class WebEntityResolver implements EntityResolver, PostConstruct {
    @Inject
    ServerContext serverContext;

    private File dtdDir;

    /**
     * Known DTDs.
     *
     * Expose the map so that interested party can introspect the table value and modify them.
     */
    public final Map<String/*public id*/,String/*bare file name*/> knownDTDs = new MapBuilder<String,String>()
            .put(Constants.TldDtdPublicId_11,"web-jsptaglibrary_1_1.dtd")
            .put(Constants.TldDtdPublicId_12,"web-jsptaglibrary_1_2.dtd")
            .put(Constants.WebDtdPublicId_22,"web-app_2_2.dtd")
            .put(Constants.WebDtdPublicId_23,"web-app_2_3.dtd")
            .build();

    public void postConstruct() {
        File root = serverContext.getInstallRoot();
        File libRoot = new File(root, "lib");
        dtdDir = new File(libRoot, "dtds");
    }

    /**
     * If the parser hits one of the well-known DTDs, parse local copies instead of hitting
     * the remote server.
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String fileName = knownDTDs.get(publicId);
        if(fileName!=null) {
            File dtd = new File(dtdDir,fileName);
            if(dtd.exists())
                return new InputSource(dtd.toURI().toString());
        }

        return null;
    }
}
