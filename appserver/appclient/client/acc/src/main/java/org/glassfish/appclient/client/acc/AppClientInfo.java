/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.PersistenceUnitDescriptor;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.util.AnnotationDetector;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.persistence.EntityManagerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotationProcessorException;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Represents information about the app client, regardless of what type of
 * archive (jar or directory) it is stored in or what type of module
 * (app client or nested within an ear) that archive holds.
 *
 * @author tjquinn
 */
@Service
public abstract class AppClientInfo {

    public static final String USER_CODE_IS_SIGNED_PROPERTYNAME = "com.sun.aas.user.code.signed";

    private static final String SIGNED_USER_CODE_PERMISSION_TEMPLATE_NAME = "jwsclientSigned.policy";

    private static final String UNSIGNED_USER_CODE_PERMISSION_TEMPLATE_NAME = "jwsclientUnsigned.policy";

    private static final String CODE_BASE_PLACEHOLDER_NAME = "com.sun.aas.jws.client.codeBase";

    /** access to the localizable strings */
    private static final LocalStringManager localStrings = new LocalStringManagerImpl(AppClientInfo.class);

    // for debug purpose
    protected static final boolean _keepExplodedDir = Boolean.getBoolean("appclient.keep.exploded.dir");

    /** logger */
    protected Logger _logger;

    /** main class name as the user specified it on the command line */
    protected String mainClassFromCommandLine;

    /**
     *main class to be used - could come from the command line or from the
     *manifest of the selected app client archive
     */
    protected String mainClassNameToRun;

    /** indicates if the app client has been launched using Java Web Start */
    protected boolean isJWS;

    /**
     * descriptor from the app client module or a default one for the
     * .class file case and the regular java launch with main class case
     */
    private ApplicationClientDescriptor acDesc;

    /**
     * Creates a new AppClientInfo for a main class file.
     *
     * @param isJWS
     * @param logger
     * @param mainClassFromCommandLine
     */
    public AppClientInfo(boolean isJWS, Logger logger, String mainClassFromCommandLine) {
        this.isJWS = isJWS;
        _logger = logger;
        this.mainClassFromCommandLine = mainClassFromCommandLine;
    }

    protected void setDescriptor(ApplicationClientDescriptor acDesc) {
        this.acDesc = acDesc;
    }

    protected ApplicationClientDescriptor getDescriptor() {
        return acDesc;
    }

    protected void completeInit() throws Exception {

    }

    /**
     *Returns the app client descriptor to be run.
     *@return the descriptor for the selected app client
     */
    protected ApplicationClientDescriptor getAppClient() {
        return getAppClient(null);
    }

    protected ClassLoader getClassLoader() {
        return null;
    }

    protected void close() throws IOException {

    }
    protected boolean deleteAppClientDir() {
        return !_keepExplodedDir;
    }

    protected String getLocalString(final String key, final String defaultMessage,
            final Object... args) {
        String result = localStrings.getLocalString(this.getClass(),
                key, defaultMessage, args);
        return result;
    }


    /**
     * Processes persistence unit handling for the ACC.
     */
    protected void handlePersistenceUnitDependency() throws URISyntaxException, MalformedURLException {
    }

    /**
     * implementation of
     * {@link com.sun.enterprise.server.PersistenceUnitLoader.ApplicationInfo}.
     */
    private static class ApplicationInfoImpl {

        private final AppClientInfo outer; // the outer object we are associated with

        private final ApplicationClientDescriptor appClient;

        public ApplicationInfoImpl(AppClientInfo outer) {
            this.outer = outer;
            appClient = outer.getAppClient();
        }

        //TODO: This method does not appear to be used -- can it be deleted?
        public Application getApplication(ServiceLocator habitat) {

            Application application = appClient.getApplication();
            if (application == null) {
                application = Application.createVirtualApplication(
                        appClient.getModuleID(),
                        appClient.getModuleDescriptor());
            }
            return application;
        }

        public InstrumentableClassLoader getClassLoader() {
            return (InstrumentableClassLoader) outer.getClassLoader();
        }

        /**
         * @return list of PU that are actually referenced by the
         *         appclient.
         */
        public Collection<? extends PersistenceUnitDescriptor>
                getReferencedPUs() {
            return appClient.findReferencedPUs();
        }

        /**
         * @return list of EMFs that have been loaded for this appclient.
         */
        public Collection<? extends EntityManagerFactory> getEntityManagerFactories() {
            Collection<EntityManagerFactory> emfs =
                    new HashSet<>();

            if (appClient.getApplication() != null) {
                emfs.addAll(appClient.getApplication()
                        .getEntityManagerFactories());
            }
            emfs.addAll(appClient.getEntityManagerFactories());
            return emfs;
        }
    } // end of class ApplicationInfoImpl

    /**
     *Reports whether the app client's descriptor shows a dependence on a
     *persistence unit.
     *@param descr the descriptor for the app client in question
     *@returns true if the descriptor shows such a dependency
     */
    protected  boolean descriptorContainsPURefcs(
        ApplicationClientDescriptor descr) {
        return ! descr.getEntityManagerFactoryReferenceDescriptors().isEmpty();
    }

    /**
     *Reports whether the main class in the archive contains annotations that
     *refer to persistence units.
     *@return boolean if the main class contains annotations that refer to a pers. unit
     */

    protected static URL getEntryAsUrl(File moduleLocation, String uri)
        throws MalformedURLException, IOException {
        URL url = null;
        try {
            url = new URL(uri);
        } catch(java.net.MalformedURLException e) {
            // ignore
            url = null;
        }
        if (url!=null) {
            return url;
        }
        if( moduleLocation != null ) {
            if( moduleLocation.isFile() ) {
                url = createJarUrl(moduleLocation, uri);
            } else {
                String path = uri.replace('/', File.separatorChar);
                url = new File(moduleLocation, path).toURI().toURL();
            }
        }
        return url;
    }

    private static URL createJarUrl(File jarFile, String entry)
        throws MalformedURLException, IOException {
        return new URL("jar:" + jarFile.toURI().toURL() + "!/" + entry);
    }

    protected ApplicationClientDescriptor getAppClient(
        Archivist archivist) {
        return ApplicationClientDescriptor.class.cast(
                    archivist.getDescriptor());
    }

    protected String getAppClientRoot(
        ReadableArchive archive, ApplicationClientDescriptor descriptor) {
        return archive.getURI().toASCIIString();
    }

    protected void massageDescriptor()
            throws IOException, AnnotationProcessorException {
        //default behavor: no op
    }

    protected List<String> getClassPaths(ReadableArchive archive) {
        List<String> paths = new ArrayList();
        paths.add(archive.getURI().toASCIIString());
        return paths;
    }

    /**
     *Returns the main class that should be executed.
     *@return the name of the main class to execute when the client starts
     */
    protected String getMainClassNameToRun(ApplicationClientDescriptor acDescr) {
         if (mainClassNameToRun == null) {
             if (mainClassFromCommandLine != null) {
                 mainClassNameToRun = mainClassFromCommandLine;
                 _logger.fine("Main class is " + mainClassNameToRun + " from command line");
             } else {
                 /*
                  *Find out which class to execute from the descriptor.
                  */
                 mainClassNameToRun = getAppClient().getMainClassName();
                 _logger.fine("Main class is " + mainClassNameToRun + " from descriptor");
             }
         }
         return mainClassNameToRun;
    }

    protected boolean classContainsAnnotation(
            String entry, AnnotationDetector detector,
            ReadableArchive archive, ApplicationClientDescriptor descriptor)
            throws FileNotFoundException, IOException {
        return detector.containsAnnotation(archive, entry);
    }

    @Override
    public String toString() {
        String lineSep = System.getProperty("line.separator");
        StringBuilder result = new StringBuilder();
        result.append(this.getClass().getName() + ": " + lineSep);
        result.append("  isJWS: " + isJWS);
        result.append("  main class to be run: " + mainClassNameToRun + lineSep);
        return result.toString();

    }

}
