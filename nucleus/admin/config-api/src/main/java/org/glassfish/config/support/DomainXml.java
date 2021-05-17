/*
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

package org.glassfish.config.support;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.EarlyLogHandler;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigurationCleanup;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.ConfigPopulatorException;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.Populator;

import jakarta.inject.Inject;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.*;
import static org.glassfish.config.support.GrizzlyConfigSchemaMigrator.logger;

/**
 * Locates and parses the portion of <tt>domain.xml</tt> that we care.
 *
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 * @author Byron Nevins
 */
public abstract class DomainXml implements Populator {

    @Inject
    StartupContext context;
    @Inject
    protected ServiceLocator habitat;
    @Inject
    @Optional
    private ModulesRegistry registry;
    @Inject
    XMLInputFactory xif;
    @Inject
    ServerEnvironmentImpl env;
    @Inject
    ConfigurationAccess configAccess;

    final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DomainXml.class);

    @Override
    public void run(ConfigParser parser) throws ConfigPopulatorException {
        LogRecord lr = new LogRecord(Level.FINE, startupClass + this.getClass().getName());
        lr.setLoggerName(getClass().getName());
        EarlyLogHandler.earlyMessages.add(lr);

        ClassLoader parentClassLoader = (registry == null) ? getClass().getClassLoader() : registry.getParentClassLoader();
        if (parentClassLoader == null)
            parentClassLoader = getClass().getClassLoader();

        ServiceLocatorUtilities.addOneConstant(habitat, parentClassLoader, null, ClassLoader.class);

        String instance = env.getInstanceName();
        URL domainURL = null;
        try {
            domainURL = getDomainXml(env);
            parseDomainXml(parser, domainURL, instance);
        } catch (NoBackupException ex) {
            /* Both files do not exists or are empty */
            throwParseError(ex);
        } catch (Throwable ex) {
            if (domainURL == null || isBackupFile(domainURL)) {
                /* Already tried backup file */
                throwParseError(ex);
            }
            /* Retry with backup file*/
            try {
                domainURL = getAlternativeDomainXml(env);
                parseDomainXml(parser, getAlternativeDomainXml(env), instance);
            } catch (Throwable e) {
                e.addSuppressed(ex);
                throwParseError(e);
            }
        }
        if (isBackupFile(domainURL)) {
            Lock writeLock = null;
            try {
                writeLock = configAccess.accessWrite();
                File destination = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kConfigXMLFileName);
                File backup = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kConfigXMLFileNameBackup);
                if (!destination.exists() || (destination.delete() && !destination.exists())) {
                    FileUtils.renameFile(backup, destination);
                }
            } catch (IOException | TimeoutException e) {
                /* We can safely ignore it as it is not so important */
            } finally {
                if (writeLock != null) {
                    writeLock.unlock();
                }
            }
        }

        // run the upgrades...
        if ("upgrade".equals(context.getPlatformMainServiceName())) {
            upgrade();
        }

        // run the cleanup.
        for (ServiceHandle<?> cc : habitat.getAllServiceHandles(ConfigurationCleanup.class)) {
            try {
                cc.getService(); // run the upgrade
                lr = new LogRecord(Level.FINE, successfulCleanupWith + cc.getClass());
                lr.setLoggerName(getClass().getName());
                EarlyLogHandler.earlyMessages.add(lr);
            } catch (Exception e) {
                lr = new LogRecord(Level.FINE, e.toString() + e);
                lr.setLoggerName(getClass().getName());
                EarlyLogHandler.earlyMessages.add(lr);

                lr = new LogRecord(Level.SEVERE, cc.getClass() + cleaningDomainXmlFailed + e);
                lr.setLoggerName(getClass().getName());
                EarlyLogHandler.earlyMessages.add(lr);
            }
        }

        decorate();
    }

    protected boolean isBackupFile(URL url) {
        return url.getPath().endsWith(ServerEnvironmentImpl.kConfigXMLFileNameBackup);
    }

    private void throwParseError(Throwable parent) {
        throw new ConfigPopulatorException(localStrings.getLocalString("ConfigParsingFailed", "Failed to parse domain.xml"), parent);
    }

    protected void decorate() {

        Server server = habitat.getService(Server.class, env.getInstanceName());
        if (server == null) {
            LogRecord lr = new LogRecord(Level.SEVERE, badEnv);
            lr.setLoggerName(getClass().getName());
            EarlyLogHandler.earlyMessages.add(lr);
            return;
        }
        ServiceLocatorUtilities.addOneConstant(habitat, server, ServerEnvironment.DEFAULT_INSTANCE_NAME, Server.class);

        server.getConfig().addIndex(habitat, ServerEnvironment.DEFAULT_INSTANCE_NAME);

        Cluster c = server.getCluster();
        if (c != null) {
            ServiceLocatorUtilities.addOneConstant(habitat, c, ServerEnvironment.DEFAULT_INSTANCE_NAME, Cluster.class);
        }
    }

    protected void upgrade() {

        // run the upgrades...
        for (ServiceHandle<?> cu : habitat.getAllServiceHandles(ConfigurationUpgrade.class)) {
            try {
                cu.getService(); // run the upgrade
                LogRecord lr = new LogRecord(Level.FINE, successfulUpgrade + cu.getClass());
                lr.setLoggerName(getClass().getName());
                EarlyLogHandler.earlyMessages.add(lr);
            } catch (Exception e) {
                LogRecord lr = new LogRecord(Level.FINE, e.toString() + e);
                lr.setLoggerName(getClass().getName());
                EarlyLogHandler.earlyMessages.add(lr);

                lr = new LogRecord(Level.SEVERE, cu.getClass() + failedUpgrade + e);
                lr.setLoggerName(getClass().getName());
                EarlyLogHandler.earlyMessages.add(lr);
            }
        }
    }

    private boolean checkDomainFile(File domainFile, Supplier<String> errorMessage) {
        if (domainFile.exists() && domainFile.length() > 0) {
            return true;
        }
        LogRecord lr = new LogRecord(Level.SEVERE, errorMessage.get());
        lr.setLoggerName(getClass().getName());
        EarlyLogHandler.earlyMessages.add(lr);
        return false;
    }

    /**
     * Determines the alternative location of the <tt>domain.xml</tt> file
     */
    protected URL getAlternativeDomainXml(ServerEnvironmentImpl env) throws IOException {
        File domainXml = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kConfigXMLFileNameBackup);
        if (checkDomainFile(domainXml, () -> noBackupFile)) {
            return domainXml.toURI().toURL();
        }

        throw new NoBackupException(env.getConfigDirPath());
    }

    private static class NoBackupException extends IOException {

        private NoBackupException(File configDirectory) {
            super(localStrings.getLocalString("NoUsableConfigFile", "No usable configuration file at {0}",
                    configDirectory.getAbsolutePath()));
        }

    }

    /**
     * Determines the location of <tt>domain.xml</tt> to be parsed.
     */
    protected URL getDomainXml(ServerEnvironmentImpl env) throws IOException {
        File domainXml = new File(env.getConfigDirPath(), ServerEnvironmentImpl.kConfigXMLFileName);
        if (checkDomainFile(domainXml, () -> noConfigFile)) {
            return domainXml.toURI().toURL();
        }
        return getAlternativeDomainXml(env);
    }

    /**
     * Parses <tt>domain.xml</tt>
     */
    protected void parseDomainXml(ConfigParser parser, final URL domainXml, final String serverName) {
        long startNano = System.nanoTime();

        try {
            ServerReaderFilter xsr = null;
            // Set the resolver so that any external entity references, such
            // as a reference to a DTD, return an empty file.  The domain.xml
            // file doesn't support entity references.
            xif.setXMLResolver(new XMLResolver() {
                @Override
                public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException {
                    return new ByteArrayInputStream(new byte[0]);
                }
            });

            if (env.getRuntimeType() == RuntimeType.DAS || env.getRuntimeType() == RuntimeType.EMBEDDED)
                xsr = new DasReaderFilter(domainXml, xif);
            else if (env.getRuntimeType() == RuntimeType.INSTANCE)
                xsr = new InstanceReaderFilter(env.getInstanceName(), domainXml, xif);
            else
                throw new RuntimeException("Internal Error: Unknown server type: " + env.getRuntimeType());

            Lock lock = null;
            try {
                // lock the domain.xml for reading if not embedded
                try {
                    lock = configAccess.accessRead();
                } catch (Exception e) {
                    // ignore
                }
                parser.parse(xsr, getDomDocument());
                xsr.close();
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
            String errorMessage = xsr.configWasFound();

            if (errorMessage != null) {
                LogRecord lr = new LogRecord(Level.WARNING, errorMessage);
                lr.setLoggerName(getClass().getName());
                EarlyLogHandler.earlyMessages.add(lr);
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw new RuntimeException("Fatal Error.  Unable to parse " + domainXml, e);
        }
        Long l = System.nanoTime() - startNano;
        LogRecord lr = new LogRecord(Level.FINE, totalTimeToParseDomain + l.toString());
        lr.setLoggerName(getClass().getName());
        EarlyLogHandler.earlyMessages.add(lr);

    }

    protected abstract DomDocument getDomDocument();

    private final static LocalStringsImpl strings = new LocalStringsImpl(DomainXml.class);
}
