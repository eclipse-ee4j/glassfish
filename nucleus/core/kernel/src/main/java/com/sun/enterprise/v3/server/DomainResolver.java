/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.common_impl.LogHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * DTD resolver used when parsing the domain.xml and resolve to local DTD copies
 *
 * @author Jerome Dochez
 * @Deprecated
 */
@Deprecated
public class DomainResolver implements EntityResolver {
    @Override
    public InputSource resolveEntity(String publicId, String systemId) {

        if (systemId.startsWith("http://www.sun.com/software/appserver/")) {
            // return a special input source
            String fileName = systemId.substring("http://www.sun.com/software/appserver/".length());
            File f = new File(System.getProperty(INSTALL_ROOT.getSystemPropertyName()));
            f = new File(f, "lib");
            f = new File(f, fileName.replace('/', File.separatorChar));
            if (f.exists()) {
                try {
                    return new InputSource(new BufferedInputStream(new FileInputStream(f)));
                } catch (IOException e) {
                    LogHelper.getDefaultLogger().log(Level.SEVERE, "Exception while getting " + fileName + " : ", e);
                    return null;
                }
            } else {
                System.out.println("Cannot find " + f.getAbsolutePath());
                return null;
            }
        } else {
            // use the default behaviour
            return null;
        }
    }
}
