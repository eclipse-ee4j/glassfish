/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.util.DOLUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.embeddable.client.UserError;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.xml.sax.SAXException;


/**
 *
 * @author tjquinn
 */
public class UndeployedLaunchable implements Launchable {

    private static final LocalStringsImpl localStrings = new LocalStringsImpl(UndeployedLaunchable.class);

    private final String callerSuppliedMainClassName;

    private ApplicationClientDescriptor acDesc;

    private AppClientArchivist archivist;

    private final ReadableArchive clientRA;

    private ClassLoader classLoader;

    static UndeployedLaunchable newUndeployedLaunchable(
            final ServiceLocator habitat,
            final ReadableArchive ra,
            final String callerSuppliedMainClassName,
            final String callerSuppliedAppName,
            final ClassLoader classLoader) throws IOException, SAXException, UserError {

        ArchivistFactory af = Util.getArchivistFactory();

        /*
         * Try letting the factory decide what type of archive this is.  That
         * will often allow an app client or an EAR archive to be detected
         * automatically.
         */
        Archivist<?> archivist = af.getArchivist("car", classLoader);
        if (archivist == null) {
            throw new UserError(localStrings.get("appclient.invalidArchive", ra.getURI().toASCIIString()));
        }

        final ArchiveType moduleType = archivist.getModuleType();
        if (moduleType != null && moduleType.equals(DOLUtils.carType())) {
            return new UndeployedLaunchable(habitat, ra,
                    (AppClientArchivist) archivist, callerSuppliedMainClassName);
        } else if (moduleType != null && moduleType.equals(DOLUtils.earType())) {
            /*
             * Locate the app client submodule that matches the main class name
             * or the app client name.
             */

            Application app = (Application) archivist.open(ra);
            for (ModuleDescriptor<BundleDescriptor> md : app.getModules()) {
                if ( ! md.getModuleType().equals(DOLUtils.carType())) {
                    continue;
                }

                ApplicationClientDescriptor acd = (ApplicationClientDescriptor) md.getDescriptor();

                final String displayName = acd.getDisplayName();
                final String appName = acd.getModuleID();

                ArchiveFactory archiveFactory = Util.getArchiveFactory();
                ReadableArchive clientRA = archiveFactory.openArchive(ra.getURI().resolve(md.getArchiveUri()));

                /*
                 * Choose this nested app client if the caller-supplied name
                 * matches, or if the caller-supplied main class matches, or
                 * if neither was provided.
                 */
                final boolean useThisClient =
                        (displayName != null && displayName.equals(callerSuppliedAppName))
                     || (appName != null && appName.equals(callerSuppliedAppName))
                     || (callerSuppliedMainClassName != null && clientRA.exists(classToResource(callerSuppliedMainClassName))
                     || (callerSuppliedAppName == null && callerSuppliedMainClassName == null));

                if (useThisClient) {
                    return new UndeployedLaunchable(habitat, clientRA, acd,
                            callerSuppliedMainClassName);
                }
                clientRA.close();
            }

            throw new UserError(localStrings.get("appclient.noMatchingClientInEAR",
                    ra.getURI(), callerSuppliedMainClassName, callerSuppliedAppName));
        } else {
            /*
             * There is a possibility that the user is trying to launch an
             * archive that is more than one type of archive: such as an EJB
             * but also an app client (because the manifest identifies a main
             * class, for example).
             *
             * Earlier the archivist factory might have returned the other type
             * of archivist - such as the EJB archivist.  Now see if the app
             * client archivist will work when selected directly.
             */
            archivist = af.getArchivist(DOLUtils.carType());

            /*
             * Try to open the archive as an app client archive just to see
             * if it works.
             */
            RootDeploymentDescriptor tempACD = archivist.open(ra);
            if (tempACD != null && tempACD instanceof ApplicationClientDescriptor) {
                /*
                 * Start with a fresh archivist - unopened - so we can request
                 * anno processing, etc. before opening it for real.
                 */
                archivist = af.getArchivist(DOLUtils.carType());
                return new UndeployedLaunchable(habitat, ra, (AppClientArchivist) archivist,
                                callerSuppliedMainClassName);
            }
            throw new UserError(
                    localStrings.get("appclient.unexpectedArchive", ra.getURI()));
        }
    }

    @Override
    public URI getURI() {
        return clientRA.getURI();
    }

    @Override
    public String getAnchorDir() {
        return null;
    }

    private static String classToResource(final String className) {
        return className.replace('.', '/') + ".class";
    }

    private UndeployedLaunchable(final ServiceLocator habitat,
            final ReadableArchive clientRA,
            final String callerSuppliedMainClass) throws IOException {
        this.callerSuppliedMainClassName = callerSuppliedMainClass;
        this.clientRA = clientRA;
    }
    private UndeployedLaunchable(final ServiceLocator habitat,
            final ReadableArchive clientRA,
            final ApplicationClientDescriptor acd,
            final String callerSuppliedMainClass) throws IOException {
        this(habitat, clientRA, callerSuppliedMainClass);
        this.acDesc = acd;
    }

    private UndeployedLaunchable(final ServiceLocator habitat,
            final ReadableArchive clientRA,
            final AppClientArchivist archivist,
            final String callerSuppliedMainClass) throws IOException {
        this(habitat, clientRA, callerSuppliedMainClass);
        this.archivist = completeInit(archivist);
    }

    @Override
    public Class getMainClass() throws ClassNotFoundException {
        try {
            String mainClassName = mainClassNameToLaunch();
            return Class.forName(mainClassName, true, getClassLoader());
        } catch (Exception e) {
            throw new ClassNotFoundException("<mainclass>");
        }
    }

    private ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    private String mainClassNameToLaunch() throws IOException {
        return (callerSuppliedMainClassName != null ? callerSuppliedMainClassName :
            extractMainClassFromArchive(clientRA));
    }

    private String extractMainClassFromArchive(final ReadableArchive clientRA) throws IOException  {
        final Manifest mf = clientRA.getManifest();
        if (mf == null) {
            return null;
        }
        return mf.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
    }

    @Override
    public ApplicationClientDescriptor getDescriptor(final URLClassLoader loader) throws IOException, SAXException {
        this.classLoader = loader;
        if (acDesc == null) {
            PrivilegedAction<TransformingClassLoader> action = () -> new TransformingClassLoader(loader.getURLs(), loader.getParent());
            final AppClientArchivist _archivist = getArchivist(AccessController.doPrivileged(action));

            _archivist.setAnnotationProcessingRequested(true);
            acDesc = _archivist.open(clientRA);

            ModuleDescriptor<BundleDescriptor> moduleDescriptor = acDesc.getModuleDescriptor();
            Application.createVirtualApplication(null, moduleDescriptor);
            acDesc.getApplication().setAppName(getDefaultApplicationName(clientRA));
        }
        return acDesc;
    }

    public String getDefaultApplicationName(ReadableArchive archive) {
        String appName = archive.getName();
        int lastDot = appName.lastIndexOf('.');
        if (lastDot != -1) {
            if (appName.substring(lastDot).equalsIgnoreCase(".ear") ||
                appName.substring(lastDot).equalsIgnoreCase(".jar")) {
                appName = appName.substring(0, lastDot);
            }
        }
        return appName;
    }


    private AppClientArchivist completeInit(final AppClientArchivist arch) {
        arch.setDescriptor(acDesc);
        arch.setAnnotationProcessingRequested(true);
        return arch;
    }

    private AppClientArchivist getArchivist(final ClassLoader classLoader) throws IOException {
        if (archivist == null) {
            ArchivistFactory af = Util.getArchivistFactory();
            archivist = completeInit(af.getArchivist("car"));
        }
        archivist.setClassLoader(classLoader);
        return archivist;
    }

    @Override
    public void validateDescriptor() {
        try {
            getArchivist(classLoader).validate(classLoader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
