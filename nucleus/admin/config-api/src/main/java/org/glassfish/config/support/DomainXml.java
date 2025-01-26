/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;

import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ConfigurationCleanup;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.config.support.DomainXmlPreParser.DomainXmlPreParserException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.ConfigPopulatorException;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.Populator;

import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.badEnv;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.cleaningDomainXmlFailed;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.failedUpgrade;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.noBackupFile;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.noConfigFile;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.startupClass;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.successfulCleanupWith;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.successfulUpgrade;
import static com.sun.enterprise.config.util.ConfigApiLoggerInfo.totalTimeToParseDomain;
import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

/**
 * Locates and parses the portion of <tt>domain.xml</tt> that we care.
 *
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 * @author Byron Nevins
 */
public abstract class DomainXml implements Populator {

    private static final Logger LOG = ConfigApiLoggerInfo.getLogger();

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


    protected abstract DomDocument getDomDocument();

    @Override
    public void run(ConfigParser parser) throws ConfigPopulatorException {
        LOG.log(FINE, startupClass, this.getClass().getName());
        ClassLoader parentClassLoader = registry == null ? getClass().getClassLoader() : registry.getParentClassLoader();
        if (parentClassLoader == null) {
            parentClassLoader = getClass().getClassLoader();
        }

        ServiceLocatorUtilities.addOneConstant(habitat, parentClassLoader, null, ClassLoader.class);

        String instance = env.getInstanceName();
        URL domainURL = null;
        try {
            domainURL = getDomainXml(env);
            parseDomainXml(parser, domainURL, instance);
        } catch (NoBackupException ex) {
            /* Both files do not exists or are empty */
            throw new ConfigPopulatorException("Failed to parse domain.xml", ex);
        } catch (Throwable ex) {
            if (domainURL == null || isBackupFile(domainURL)) {
                /* Already tried backup file */
                throw new ConfigPopulatorException("Failed to parse domain.xml", ex);
            }
            /* Retry with backup file*/
            try {
                domainURL = getAlternativeDomainXml(env);
                parseDomainXml(parser, getAlternativeDomainXml(env), instance);
            } catch (Throwable e) {
                e.addSuppressed(ex);
                throw new ConfigPopulatorException("Failed to parse domain.xml", e);
            }
        }
        if (isBackupFile(domainURL)) {
            Path destination = env.getConfigDirPath().toPath().resolve(ServerEnvironmentImpl.kConfigXMLFileName);
            Path backup = env.getConfigDirPath().toPath().resolve(ServerEnvironmentImpl.kConfigXMLFileNameBackup);
            try {
                Files.move(backup, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Restoring backup failed!", e);
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
                LOG.log(FINE, successfulCleanupWith, cc.getClass());
            } catch (Exception e) {
                LOG.log(SEVERE, cleaningDomainXmlFailed, new Object[] {cc.getClass(), e});
                LOG.log(Level.FINE, "Cleaning the domain.xml failed!", e);
            }
        }

        decorate();
    }

    protected boolean isBackupFile(URL url) {
        return url.getPath().endsWith(ServerEnvironmentImpl.kConfigXMLFileNameBackup);
    }

    protected void decorate() {

        Server server = habitat.getService(Server.class, env.getInstanceName());
        if (server == null) {
            LOG.log(SEVERE, badEnv, env.getInstanceName());
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
                LOG.log(FINE, successfulUpgrade, cu.getClass());
            } catch (Exception e) {
                LOG.log(Level.SEVERE, failedUpgrade, new Object[] {cu.getClass(), e});
                LOG.log(Level.FINE, "Upgrade of the domain.xml failed!", e);
            }
        }
    }

    private boolean checkDomainFile(File domainFile, Supplier<String> errorMessage) {
        if (domainFile.exists() && domainFile.length() > 0) {
            return true;
        }
        LOG.log(SEVERE, errorMessage.get());
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

        private static final long serialVersionUID = 1L;

        private NoBackupException(File configDirectory) {
            super("No usable configuration file at " + configDirectory.getAbsolutePath());
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
            // Set the resolver so that any external entity references, such
            // as a reference to a DTD, return an empty file.  The domain.xml
            // file doesn't support entity references.
            xif.setXMLResolver(new XMLResolver() {
                @Override
                public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException {
                    return new ByteArrayInputStream(new byte[0]);
                }
            });

            try (ServerReaderFilter readerFilter = createReaderFilter(domainXml)) {
                parser.parse(readerFilter, getDomDocument());
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse " + domainXml, e);
        }
        LOG.log(CONFIG, totalTimeToParseDomain, System.nanoTime() - startNano);
    }


    private ServerReaderFilter createReaderFilter(final URL domainXml)
        throws XMLStreamException, DomainXmlPreParserException {
        if (env.getRuntimeType() == RuntimeType.DAS || env.getRuntimeType() == RuntimeType.EMBEDDED) {
            return new DasReaderFilter(domainXml, xif);
        } else if (env.getRuntimeType() == RuntimeType.INSTANCE) {
            return new InstanceReaderFilter(env.getInstanceName(), domainXml, xif);
        } else {
            throw new RuntimeException("Unknown server type: " + env.getRuntimeType());
        }
    }
}
