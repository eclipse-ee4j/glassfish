/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.resources.custom.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;


public class PropertiesFactory implements Serializable, ObjectFactory {

    public static final String filePropertyName = "org.glassfish.resources.custom.factory.PropertiesFactory.fileName";

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
        throws Exception {
        Reference ref = (Reference) obj;
        Enumeration<RefAddr> refAddrs = ref.getAll();

        String fileName = null;
        Properties fileProperties = new Properties();
        Properties properties = new Properties();

        while(refAddrs.hasMoreElements()){
            RefAddr addr = refAddrs.nextElement();
            String type = addr.getType();
            String value = (String)addr.getContent();

            if(type.equalsIgnoreCase(filePropertyName)){
                fileName = value;
            }else{
                properties.put(type, value);
            }
        }

        if (fileName != null) {
            File file = new File(fileName);
            if (!file.isAbsolute()) {
                file = new File(
                    System.getProperty(INSTALL_ROOT.getSystemPropertyName()) + File.separator + fileName);
            }
            try {
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        if (fileName.toUpperCase(Locale.getDefault()).endsWith("XML")) {
                            fileProperties.loadFromXML(fis);
                        } else {
                            fileProperties.load(fis);
                        }

                    } catch (IOException ioe) {
                        throw new IOException("IO Exception during properties load : " + file.getAbsolutePath());
                    }
                } else {
                    throw new FileNotFoundException("File not found : " + file.getAbsolutePath());
                }
            } catch (FileNotFoundException fnfe) {
                throw new FileNotFoundException("File not found : " + file.getAbsolutePath());
            }
        }
        fileProperties.putAll(properties);

        return fileProperties;
    }
}
