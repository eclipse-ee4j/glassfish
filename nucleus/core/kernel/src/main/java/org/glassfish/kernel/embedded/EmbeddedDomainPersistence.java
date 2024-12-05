/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.kernel.embedded;

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.server.DomainXmlPersistence;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import org.jvnet.hk2.config.DomDocument;

/**
 * Configuration file persistence handler for embedded
 *
 * @author Jerome Dochez
 * @author bhavanishankar@dev.java.net
 */
public class EmbeddedDomainPersistence extends DomainXmlPersistence {

    @Inject
    StartupContext startupContext;

    final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DomainXmlPersistence.class);

    /**
     * Returns the destination file for saving the embedded configuration file,
     * when set.
     *
     * @return the embedded configuration file if set in read-write mode.
     * @throws IOException
     */
    @Override
    protected File getDestination() throws IOException {
        String configFileReadOnly = startupContext.getArguments().getProperty(
                "org.glassfish.embeddable.configFileReadOnly");
        if (configFileReadOnly != null &&
                !Boolean.valueOf(configFileReadOnly).booleanValue()) {
            try {
                URI uri = EmbeddedDomainXml.getDomainXml(startupContext).toURI();
                if ("file".equalsIgnoreCase(uri.getScheme())) {
                    return new File(uri);
                }
                throw new IOException("configurationFile is writable but is not a file");
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
        }
        return null; // Don't persist domain.xml anywhere.
    }

    @Override
    public void save(DomDocument doc) throws IOException {
        File destination = getDestination();
        if (destination == null) {
            logger.finer("domain.xml cannot be persisted, null destination");
            return;
        }
        super.save(doc);
    }

    @Override
    protected void saved(File destination) {
        logger.log(Level.INFO, "Configuration saved at {0}", destination);
    }
}
