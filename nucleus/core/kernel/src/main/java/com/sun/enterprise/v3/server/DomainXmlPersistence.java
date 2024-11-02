/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.config.modularity.ConfigModularityUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.glassfish.config.support.ConfigurationPersistence;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.IndentingXMLStreamWriter;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * domain.xml persistence.
 *
 * @author Jerome Dochez
 */
@Service
@Singleton
public class DomainXmlPersistence implements ConfigurationPersistence {

    @Inject
    ServerEnvironmentImpl env;
    @Inject
    protected Logger logger;
    @Inject
    ConfigModularityUtils modularityUtils;

    DomDocument skippedDoc;

    final XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();

    final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DomainXmlPersistence.class);

    @Override
    public synchronized void save(DomDocument doc) throws IOException {
        if (modularityUtils.isIgnorePersisting() && !modularityUtils.isCommandInvocation()) {
            skippedDoc = doc;
            return;
        }
        File destination = getDestination();
        if (destination == null) {
            throw new IOException("The domain.xml cannot be persisted, null destination");
        }

        // write to the temporary file
        final File domainXmlTmp = File.createTempFile("domain", ".xml", destination.getParentFile());
        if (!domainXmlTmp.exists()) {
            throw new IOException("Cannot create temporary file when saving domain.xml");
        }
        try (OutputStream fos = new FileOutputStream(domainXmlTmp);
            IndentingXMLStreamWriter writer = new IndentingXMLStreamWriter(
                xmlFactory.createXMLStreamWriter(new BufferedOutputStream(fos)))) {
            doc.writeTo(writer);
        } catch (XMLStreamException e) {
            throw new IOException("Configuration could not be saved to temporary file " + domainXmlTmp, e);
        }

        // backup the current file
        final File backup = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kConfigXMLFileNameBackup);
        if (destination.exists()) {
            Files.move(destination.toPath(), backup.toPath(), REPLACE_EXISTING);
        }
        // save the temp file to domain.xml
        try {
            Files.move(domainXmlTmp.toPath(), destination.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            try {
                Files.move(backup.toPath(), destination.toPath(), REPLACE_EXISTING);
            } catch (IOException e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }

        skippedDoc = null;
        saved(destination);
    }

    /**
     * Update the modified time of the persisted domain.xml so that
     * instances will detect it as changed.
     * This is for triggering instance synchronization to occur.
     */
    public void touch() throws IOException {
        getDestination().setLastModified(System.currentTimeMillis());
    }

    protected void saved(File destination) {
        logger.fine("Configuration saved at " + destination);
    }

    protected File getDestination() throws IOException {
        return new File(env.getConfigDirPath(), "domain.xml");
    }

    /*
     * The purpose of this service is to write out the domain.xml if any writes
     * were skipped during startup of the server.
     */
    @Service
    @RunLevel(PostStartupRunLevel.VAL)
    static class SkippedWriteWriter implements PostConstruct {

        @Inject DomainXmlPersistence domPersist;
        @Inject Logger logger;

        @Override
        public void postConstruct() {
            DomDocument doc = domPersist.skippedDoc;
            if (doc != null) {
                try {
                    domPersist.save(doc);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, localStrings.getLocalString("ioexception",
                        "IOException while saving the configuration, changes not persisted"), e);
                }
            }
        }
    }
}
