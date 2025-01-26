/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.internal.deployment;

import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;

import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.XMLEvent;

import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.ServiceLocator;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Generic implementation of the Sniffer service that can be programmatically instantiated
 *
 * @author Jerome Dochez, Sanjeeb Sahoo
 */
public abstract class GenericSniffer implements Sniffer {

    @Inject
    protected ModulesRegistry modulesRegistry;

    @Inject
    protected ServiceLocator habitat;

    final private String containerName;
    final private String appStigma;
    final private String urlPattern;

    final private static XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
    private HK2Module[] modules;

    public GenericSniffer(String containerName, String appStigma, String urlPattern) {
        this.containerName = containerName;
        this.appStigma = appStigma;
        this.urlPattern = urlPattern;
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * composite sniffer.
     * @param context deployment context
     * @return true if the location is recognized by this sniffer
     */
    @Override
    public boolean handles(DeploymentContext context) {
        ArchiveType archiveType = habitat.getService(ArchiveType.class, context.getArchiveHandler().getArchiveType());
        if (archiveType != null && !supportsArchiveType(archiveType)) {
            return false;
        }
        return handles(context.getSource());
    }

    /**
     * Returns the list of annotation names that this sniffer is interested in.
     * If an application bundle contains at least one class annotated with
     * one of the returned annotations, the deployment process will not
     * call the handles method but will invoke the containers deployers as if
     * the handles method had been called and returned true.
     *
     * @param context deployment context
     * @return list of annotations this sniffer is interested in or an empty array
     */
    @Override
    public String[] getAnnotationNames(DeploymentContext context) {
        List<String> annotationNames = new ArrayList<>();
        for (Class<? extends Annotation> annotationType : getAnnotationTypes())  {
            annotationNames.add(annotationType.getName());
        }
        return annotationNames.toArray(new String[annotationNames.size()]);
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * instance.
     *
     * @param location the file or directory to explore
     * @return true if this sniffer handles this application type
     */
    @Override
    public boolean handles(ReadableArchive location) {
        if (appStigma != null) {
            try {
                if (location.exists(appStigma)) {
                    return true;
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return false;
    }

    /**
     * Returns the pattern to apply against the request URL
     * If the pattern matches the URL, the service method of the associated
     * container will be invoked
     *
     * @return pattern instance
     */
    @Override
    public String[] getURLPatterns() {
        if (urlPattern!=null) {
            return new String[] {urlPattern};
        } else {
            return null;
        }
    }

    /**
     * Returns the container name associated with this sniffer.
     * <p>
     * WARNING: This is different from {@link ArchiveType}!
     *
     * @return the container name
     */
    @Override
    public String getModuleType() {
        return containerName;
    }

   /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     *
     * This method returns a {@link ModuleDefinition} for the module containing
     * the core implementation of the container. That means that this module
     * will be locked as long as there is at least one module loaded in the
     * associated container.
     *
     * @param containerHome is where the container implementation resides (Not used anymore)
     * @param logger the logger to use
     * @return the module definition of the core container implementation.
     *
     * @throws java.io.IOException exception if something goes sour
     */
    @Override
    public synchronized HK2Module[] setup(String containerHome, Logger logger) throws IOException {   // TODO(Sahoo): Change signature to not accept containerHome or logger
        if (modules != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINE, "GenericSniffer", "setup", "{0} has already setup {1} container, so just returning.", new Object[]{this, containerName});
            }
            return modules;
        }
        List<HK2Module> tmp = new ArrayList<>();
        for (String moduleName : getContainerModuleNames()) {
            HK2Module m = modulesRegistry.makeModuleFor(moduleName, null);
            if (m != null) {
                tmp.add(m);
            } else {
                throw new RuntimeException("Unable to set up module " + moduleName);
            }
        }
        modules = tmp.toArray(new HK2Module[tmp.size()]);
        return modules;
    }

    protected String[] getContainerModuleNames() {
        return new String[0];
    }
    /**
     * Tears down a container, remove all imported libraries from the module
     * subsystem.
     *
     */
    @Override
    public void tearDown() {
        // It is not safe to uninstall modules in a running server as there might be existing
        // references to objects loaded from those modules, so we don't uninstall modules at this point of time.
    }

    /**
     * Returns the list of annotations types that this sniffer is interested in.
     * If an application bundle contains at least one class annotated with
     * one of the returned annotations, the deployment process will not
     * call the handles method but will invoke the containers deployers as if
     * the handles method had been called and returned true.
     *
     * @return list of annotations this sniffer is interested in.
     */
    @Override
    public Class<? extends Annotation>[] getAnnotationTypes() {
        return new Class[0];
    }

    /**
     * @return false
     */
    @Override
    public boolean isUserVisible() {
        return false;
    }

    /**
     * @return false
     */
    @Override
    public boolean isJakartaEE() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Sniffer) {
            Sniffer otherSniffer = (Sniffer)other;
            return getModuleType().equals(otherSniffer.getModuleType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (getModuleType() != null ? getModuleType().hashCode() : 0);
        return hash;
    }

    /**
     * Returns a map of deployment configurations composed by reading from a
     * list of paths in the readable archive.  (For Jakarta EE applications the
     * deployment configurations correspond to the deployment descriptors.)  The
     * {@link #getDeploymentConfigurationPaths} method returns this list of paths
     * which might exist in archives that this sniffer handles.
     * <p>
     * In each returned map entry the key is a path and the value is the
     * contents of the archive entry at that path.  This method creates a map
     * entry only if the path exists in the readable archive.
     * <p>
     * Sniffers for applications that do not store their configurations as
     * deployment descriptors at predictable paths within an archive are free
     * to override this implementation to return whatever information is
     * appropriate to that application type.  A key usage of the returned
     * Map is in the application type's GUI plug-in (if desired) to allow
     * users to customize the deployment configuration after the application
     * has been deployed.  The concrete Sniffer implementation and the
     * GUI plug-in must agree on the conventions for storing deployment
     * configuration inforation in the Map.
     *
     * @param location the readable archive for the application of interest
     * @return a map from path names to the contents of the archive entries
     * at those paths
     * @throws java.io.IOException in case of errors retrieving an entry or
     * reading the archive contents at an entry
     */
    @Override
    public Map<String,String> getDeploymentConfigurations(final ReadableArchive location) throws IOException {
        final Map<String,String> deploymentConfigs = new HashMap<>();

        for (String path : getDeploymentConfigurationPaths()) {
            InputStream is = null;
            try {
                is = location.getEntry(path);
                if (is != null) {
                    String dc = readDeploymentConfig(is);
                    deploymentConfigs.put(path, dc);
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return deploymentConfigs;
    }

    /**
     * Returns a list of paths within an archive that represents deployment
     * configuration files.
     * <p>
     * Sniffers that recognize Jakarta EE applications typically override this
     * default implementation to return a list of the deployment descriptors
     * that might appear in the type of Jakarta EE application which the sniffer
     * recognizes.  For example, the WebSniffer implementation of this method
     * returns WEB-INF/web.xml, WEB-INF/glassfish-web.xml and
     * WEB-INF/sun-web.xml.
     *
     * @return list of paths in the archive where deployment configuration
     * archive entries might exist
     */
    protected List<String> getDeploymentConfigurationPaths() {
        return Collections.EMPTY_LIST;
    }

    /**
     * @return the set of the sniffers that should not co-exist for the
     * same module. For example, ejb and appclient sniffers should not
     * be returned in the sniffer list for a certain module.
     * This method will be used to validate and filter the retrieved sniffer
     * lists for a certain module
     *
     */
    @Override
    public String[] getIncompatibleSnifferTypes() {
        return null;
    }

    private String readDeploymentConfig(final InputStream is) throws IOException {
        String encoding = null;
        XMLEventReader rdr = null;
        try {
            is.mark(Integer.MAX_VALUE);
            rdr = xmlInputFactory.createXMLEventReader(new InputStreamReader(is, UTF_8));
            while (rdr.hasNext()) {
                final XMLEvent ev = rdr.nextEvent();
                if (ev.isStartDocument()) {
                    final StartDocument sd = (StartDocument) ev;
                    encoding = sd.getCharacterEncodingScheme();
                    break;
                }
            }
        } catch (XMLStreamException e) {
            if (rdr != null) {
                try {
                    rdr.close();
                } catch (XMLStreamException inner) {
                    throw new IOException(e);
                }
            }
            throw new IOException(e);
        }
        if (encoding == null) {
            encoding = "UTF-8";
        }
        is.reset();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead;
        final byte [] buffer = new byte[1024];
        while (( bytesRead = is.read(buffer)) != -1 ) {
            baos.write(buffer, 0, bytesRead);
        }
        try {
            rdr.close();
        } catch (XMLStreamException ex) {
            throw new IOException(ex);
        }
        is.close();
        return new String(baos.toByteArray(), encoding);
    }
}
