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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.config.modularity.ConfigModularityUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.glassfish.common.util.admin.ManagedFile;
import org.glassfish.config.support.ConfigurationAccess;
import org.glassfish.config.support.ConfigurationPersistence;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.IndentingXMLStreamWriter;

/**
 * domain.xml persistence.
 *
 * @author Jerome Dochez
 */
@Service
@Singleton
public class DomainXmlPersistence implements ConfigurationPersistence, ConfigurationAccess {

    @Inject
    ServerEnvironmentImpl env;
    @Inject
    protected Logger logger;
    @Inject
    ConfigModularityUtils modularityUtils;

    DomDocument skippedDoc = null;

    final XMLOutputFactory xmlFactory = XMLOutputFactory.newInstance();

    final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DomainXmlPersistence.class);


    private synchronized ManagedFile getPidFile() throws IOException {
        File location=null;
        try {
            // I am locking indefinitely with a 2 seconds timeOut.
            location = new File(env.getConfigDirPath(), "lockfile");
            if (!location.exists()) {
                if (!location.createNewFile()) {
                    if (!location.exists()) {
                        String message = localStrings.getLocalString("cannotCreateLockfile",
                                "Cannot create lock file at {0}, configuration changes will not be persisted",
                                location);
                        logger.log(Level.SEVERE, message);
                        throw new IOException(message);
                    }
                }
            }
            return new ManagedFile(location, 2000, -1);
        } catch (IOException e) {
            logger.log(Level.SEVERE,
                    localStrings.getLocalString("InvalidLocation",
                            "Cannot obtain lockfile location {0}, configuration changes will not be persisted",
                            location), e);
            throw e;
        }
    }

    @Override
    public Lock accessRead() throws IOException, TimeoutException {
        return getPidFile().accessRead();
    }

    @Override
    public Lock accessWrite() throws IOException, TimeoutException {
        return getPidFile().accessWrite();
    }

    @Override
    public void save(DomDocument doc) throws IOException {
        if (modularityUtils.isIgnorePersisting() && !modularityUtils.isCommandInvocation()) {
            assert skippedDoc == null || (doc == skippedDoc);
            skippedDoc = doc;
            return;
        }
        File destination = getDestination();
        if (destination == null) {
            String msg = localStrings.getLocalString("NoLocation",
                    "domain.xml cannot be persisted, null destination");
            logger.severe(msg);
            throw new IOException(msg);
        }
        Lock writeLock = null;
        try {
            try {
                writeLock = accessWrite();
            } catch (TimeoutException e) {
                String msg = localStrings.getLocalString("Timeout",
                        "Timed out when waiting for write lock on configuration file");
                logger.log(Level.SEVERE, msg);
                throw new IOException(msg, e);

            }

            // get a temporary file
            File f = File.createTempFile("domain", ".xml", destination.getParentFile());
            if (!f.exists()) {
                throw new IOException(localStrings.getLocalString("NoTmpFile",
                        "Cannot create temporary file when saving domain.xml"));
            }
            // write to the temporary file
            XMLStreamWriter writer = null;
            try (OutputStream fos = getOutputStream(f)) {
                writer = xmlFactory.createXMLStreamWriter(new BufferedOutputStream(fos));
                IndentingXMLStreamWriter indentingXMLStreamWriter = new IndentingXMLStreamWriter(writer);
                doc.writeTo(indentingXMLStreamWriter);
                indentingXMLStreamWriter.close();
            } catch (XMLStreamException e) {
                String msg = localStrings.getLocalString("TmpFileNotSaved",
                        "Configuration could not be saved to temporary file");
                logger.log(Level.SEVERE, msg, e);
                throw new IOException(e.getMessage(), e);
                // return after calling finally clause, because since temp file couldn't be saved,
                // renaming should not be attempted
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (XMLStreamException e) {
                        logger.log(Level.SEVERE, localStrings.getLocalString("CloseFailed",
                                "Cannot close configuration writer stream"), e);
                        throw new IOException(e.getMessage(), e);
                    }
                }
            }

            // backup the current file
            File backup = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kConfigXMLFileNameBackup);
            if (destination.exists() && backup.exists() && !backup.delete()) {
                String msg = localStrings.getLocalString("BackupDeleteFailed",
                        "Could not delete previous backup file at {0}" , backup.getAbsolutePath());
                logger.severe(msg);
                throw new IOException(msg);
            }
            if (destination.exists() && !FileUtils.renameFile(destination, backup)) {
                String msg = localStrings.getLocalString("TmpRenameFailed",
                        "Could not rename {0} to {1}",  destination.getAbsolutePath() , backup.getAbsolutePath());
                logger.severe(msg);
                throw new IOException(msg);
            }
            // save the temp file to domain.xml
            if (!FileUtils.renameFile(f, destination)) {
                String msg = localStrings.getLocalString("TmpRenameFailed",
                        "Could not rename {0} to {1}",  f.getAbsolutePath() , destination.getAbsolutePath());
                // try to rename backup to domain.xml (so that at least something is there)
                if (!FileUtils.renameFile(backup, destination)) {
                    msg += "\n" + localStrings.getLocalString("RenameFailed",
                            "Could not rename backup to {0}", destination.getAbsolutePath());
                }
                logger.severe(msg);
                throw new IOException(msg);
            }
        } catch(IOException e) {
            logger.log(Level.SEVERE, localStrings.getLocalString("ioexception",
                    "IOException while saving the configuration, changes not persisted"), e);
            throw e;
        } finally {
            if (writeLock != null) {
                writeLock.unlock();
            }
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

    protected OutputStream getOutputStream(File destination) throws IOException {
        return new FileOutputStream(destination);
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
