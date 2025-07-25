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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.xml.stream.XMLStreamException;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.embeddable.client.UserError;
import org.glassfish.hk2.api.ServiceLocator;
import org.xml.sax.SAXException;

/**
 * Something launchable by the ACC - an app client archive or a class.
 *
 * @author tjquinn
 */
interface Launchable {

    /**
     * Returns the main class for this Launchable.
     * @return the main class
     *
     * @throws java.lang.ClassNotFoundException
     */
    Class getMainClass() throws ClassNotFoundException;

    ApplicationClientDescriptor getDescriptor(URLClassLoader loader) throws IOException, SAXException;

    void validateDescriptor();

    URI getURI();

    String getAnchorDir();

    static class LaunchableUtil {

        private static final LocalStringManager localStrings = new LocalStringManagerImpl(Launchable.LaunchableUtil.class);
        static Launchable newLaunchable(final URI uri,
                final String callerSuppliedMainClassName,
                final String callerSuppliedAppName,
                final ServiceLocator habitat) throws IOException, BootException, URISyntaxException, XMLStreamException, SAXException, UserError {
            /*
             * Make sure the requested URI exists and is readable.
             */
            ArchiveFactory af = ACCModulesManager.getService(ArchiveFactory.class);
            ReadableArchive ra = null;
            try {
                ra = af.openArchive(uri);
            } catch (IOException e) {
                final String msg = localStrings.getLocalString(
                        Launchable.class,
                        "appclient.cannotFindJarFile",
                        "Could not locate the requested client JAR file {0}; please try again with an existing, valid client JAR",
                        new Object[] {uri});
                throw new UserError(msg);
            }

            Launchable result = FacadeLaunchable.newFacade(
                    habitat, ra, callerSuppliedMainClassName, callerSuppliedAppName);
            if (result == null) {
                /*
                 * If newFacade found a facade JAR but could not find a suitable
                 * client it will have thrown a UserError.  If we're here, then
                 * newFacade did not have a facade to work with.  So the caller-
                 * provided URI should refer to an undeployed EAR or an undeployed
                 * app client JAR.
                 */
                result = UndeployedLaunchable.newUndeployedLaunchable(habitat, ra,
                        callerSuppliedMainClassName, callerSuppliedAppName,
                        Thread.currentThread().getContextClassLoader());
            }
            if ( ! (result instanceof JWSFacadeLaunchable)) {
                URL clientOrFacadeURL = new URL("file:" + result.getURI().getSchemeSpecificPart());
                /*
                 * For the embedded case especially there might not be an
                 * ACCClassLoader instance yet.  Create one if needed
                 * before proceeding.
                 */
                TransformingClassLoader cl = TransformingClassLoader.instance();
                if (cl == null) {
                    cl = TransformingClassLoader.newInstance(Thread.currentThread().getContextClassLoader(), false);
                }
                cl.appendURL(clientOrFacadeURL);
            }
            return result;
        }

        static Launchable newLaunchable(final ServiceLocator habitat, final Class mainClass) {
            return new MainClassLaunchable(habitat, mainClass);
        }

        static ApplicationClientDescriptor openWithAnnoProcessingAndTempLoader(
                final AppClientArchivist archivist, final URLClassLoader loader,
                final ReadableArchive facadeRA,
                final ReadableArchive clientRA) throws IOException, SAXException {
            archivist.setAnnotationProcessingRequested(true);
            PrivilegedAction<TransformingClassLoader> action = () -> new TransformingClassLoader(loader.getURLs(), loader.getParent());
            final TransformingClassLoader tempLoader = AccessController.doPrivileged(action);
            archivist.setClassLoader(tempLoader);

            final ApplicationClientDescriptor acDesc = archivist.open(facadeRA, clientRA);
            archivist.setDescriptor(acDesc);
            return acDesc;

        }
        static boolean matchesAnyClass(final ReadableArchive archive, final String callerSpecifiedMainClassName) throws IOException {
            return (callerSpecifiedMainClassName != null) &&
                            archive.exists(classNameToArchivePath(callerSpecifiedMainClassName));
        }

        static String moduleID(
                final URI groupFacadeURI,
                final URI clientURI,
                final ApplicationClientDescriptor clientFacadeDescriptor) {
            String moduleID = clientFacadeDescriptor.getModuleID();
            /*
             * If the moduleID was never set explicitly in the descriptor then
             * it will fall back to  the URI of the archive...ending in .jar we
             * presume.  In that case, change the module ID to be the path
             * relative to the downloaded root directory.
             */
            if (moduleID.endsWith(".jar")) {
                moduleID = deriveModuleID(groupFacadeURI, clientURI);
            }
            return moduleID;
        }

        static boolean matchesName(
                final String moduleID,
                final URI groupFacadeURI,
                final ApplicationClientDescriptor clientFacadeDescriptor,
                final String appClientName) throws IOException {

            /*
             * The ReadableArchive argument should be the facade archive and
             * not the developer's original one, because when we try to open it
             * the archivist needs to have the augmented descriptor (which is
             * in the client facade) not the minimal or non-existent
             * descriptor (which could be in the developer's original client).
             */
            final String displayName = clientFacadeDescriptor.getDisplayName();
            return (   (moduleID != null && moduleID.equals(appClientName))
                    || (displayName != null && displayName.equals(appClientName)));
        }

        private static String classNameToArchivePath(final String className) {
            return new StringBuilder(className.replace('.', '/'))
                    .append(".class").toString();
        }

        private static String deriveModuleID(final URI groupFacadeURI,
                final URI clientArchiveURI) {
            /*
             * The groupFacadeURI will be something like x/y/appName.jar and
             * the clientArchiveURI will be something like x/y/appName/a/b/clientName.jar.
             * The derived moduleID should be the client archive's URI relative
             * to the x/y/appName directory with no file type: in this example a/b/clientName
             */
            URI dirURI = stripDotJar(groupFacadeURI);
            URI clientArchiveRelativeURI = stripDotJar(
                    dirURI.relativize(URI.create("file:" + clientArchiveURI.getRawSchemeSpecificPart())));
            return clientArchiveRelativeURI.getRawSchemeSpecificPart();
        }

        private static URI stripDotJar(final URI uri) {
            String pathWithoutDotJar = uri.getRawSchemeSpecificPart();
            pathWithoutDotJar = pathWithoutDotJar.substring(0,
                    pathWithoutDotJar.length() - ".jar".length());
            return URI.create("file:" + pathWithoutDotJar);
        }
    }
}
