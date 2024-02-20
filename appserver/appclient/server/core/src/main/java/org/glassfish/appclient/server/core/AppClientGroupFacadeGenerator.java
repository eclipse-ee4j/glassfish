/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.server.core;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.appclient.server.connector.CarType;
import org.glassfish.appclient.server.core.jws.JavaWebStartInfo;
import org.glassfish.deployment.common.ClientArtifactsManager;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.versioning.VersioningSyntaxException;
import org.glassfish.deployment.versioning.VersioningUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Generates the app client group (EAR-level) facade JAR.
 * <p>
 * Because an EAR can contain multiple clients, this might be run multiple
 * times.  To avoid extra work the class stores a flag that it has done its
 * work in the deployment context's transient app data.
 *
 * @author tjquinn
 */
@Service
@PerLookup
public class AppClientGroupFacadeGenerator {

    private static final String GLASSFISH_APPCLIENT_GROUP_FACADE_CLASS_NAME =
            "org.glassfish.appclient.client.AppClientGroupFacade";

    private static final Attributes.Name GLASSFISH_APPCLIENT_GROUP = new Attributes.Name("GlassFish-AppClient-Group");
    private static final String GROUP_FACADE_ALREADY_GENERATED = "groupFacadeAlreadyGenerated";
    private static final String PERMISSIONS_XML_PATH = "META-INF/permissions.xml";

    private DeploymentContext dc;
    private AppClientDeployerHelper helper;

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private CarType carType;

    void run(final AppClientDeployerHelper helper) {
        dc = helper.dc();
        this.helper = helper;
        if ( ! groupFacadeAlreadyGenerated().get()) {
            generateGroupFacade();
        }
    }

    private AtomicBoolean groupFacadeAlreadyGenerated() {
        AtomicBoolean groupFacadeAlreadyGenerated =
                dc.getTransientAppMetaData(GROUP_FACADE_ALREADY_GENERATED, AtomicBoolean.class);
        if (groupFacadeAlreadyGenerated == null) {
            groupFacadeAlreadyGenerated = new AtomicBoolean(false);
            dc.addTransientAppMetaData(GROUP_FACADE_ALREADY_GENERATED,
                    groupFacadeAlreadyGenerated);
        }
        return groupFacadeAlreadyGenerated;
    }

    private void recordGroupFacadeGeneration() {
        dc.getTransientAppMetaData(GROUP_FACADE_ALREADY_GENERATED, AtomicBoolean.class).set(true);
    }

    private void generateGroupFacade() {

        final Application application = dc.getModuleMetaData(Application.class);
        final Collection<ModuleDescriptor<BundleDescriptor>> appClients =
                application.getModuleDescriptorsByType(carType);

        final StringBuilder appClientGroupListSB = new StringBuilder();

        /*
        /*
         * For each app client, get its facade's URI to include in the
         * generated EAR facade's client group listing.
         */
        for (ModuleDescriptor<BundleDescriptor> md : appClients) {
            appClientGroupListSB.append((appClientGroupListSB.length() > 0) ? " " : "")
                    .append(earDirUserURIText(dc)).append(appClientFacadeUserURI(md.getArchiveUri()));
        }

        try {
            addTopLevelContentToGroupFacade();

            /*
             * Pass the EAR's generated/xml directory for where to generated the
             * group facade.  Because the directories are flattened, even if the
             * client is actually x/y/z.jar its expanded directory will be just
             * one level lower than the EAR's directory.
             */
            generateAndRecordEARFacadeContents(
                    dc,
                    appClientGroupListSB.toString());
            recordGroupFacadeGeneration();
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private void addTopLevelContentToGroupFacade() throws IOException {
        helper.addClientPolicyFiles(null);
    }

    private String earDirUserURIText(final DeploymentContext dc)  {
        final DeployCommandParameters deployParams = dc.getCommandParameters(DeployCommandParameters.class);
        final String appName = deployParams.name();
        try {
            return VersioningUtils.getUntaggedName(appName) + "Client/";
        } catch (VersioningSyntaxException ex) {
            Logger.getLogger(JavaWebStartInfo.APPCLIENT_SERVER_MAIN_LOGGER,
                JavaWebStartInfo.APPCLIENT_SERVER_LOGMESSAGE_RESOURCE).log(Level.SEVERE, null, ex);
        }
        return appName;

    }

    private String appClientFacadeUserURI(String appClientModuleURIText) {
        if (appClientModuleURIText.endsWith("_jar")) {
            appClientModuleURIText = appClientModuleURIText.substring(0, appClientModuleURIText.lastIndexOf("_jar")) + ".jar";
        }
        final int dotJar = appClientModuleURIText.lastIndexOf(".jar");
        String appClientFacadePath = appClientModuleURIText.substring(0, dotJar) + "Client.jar";
        return appClientFacadePath;
    }

    /**
     * Generates content for the top-level generated client JAR from the
     * app clients in this app.
     * <p>
     * Higher-level logic will actually create the client JAR, because the need
     * for a client JAR can be triggered by other deployers (EJB for generated
     * stubs and web services), not only app clients.
     * @param dc
     * @param appScratchDir
     * @param facadeFileName
     * @param appClientGroupList
     * @throws IOException
     */
    private void generateAndRecordEARFacadeContents(
            final DeploymentContext dc,
            final String appClientGroupList) throws IOException {

        final ClientArtifactsManager clientArtifactsManager = ClientArtifactsManager.get(dc);

        final Manifest manifest = new Manifest();
        Attributes mainAttrs = manifest.getMainAttributes();

        mainAttrs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mainAttrs.put(Attributes.Name.MAIN_CLASS, GLASSFISH_APPCLIENT_GROUP_FACADE_CLASS_NAME);
        mainAttrs.put(GLASSFISH_APPCLIENT_GROUP, appClientGroupList);


        //Now manifest is ready to be written.
        final File manifestFile = File.createTempFile("groupMF", ".MF");
        try (OutputStream manifestOutputStream = new BufferedOutputStream(new FileOutputStream(manifestFile))) {
            manifest.write(manifestOutputStream);
        }
        clientArtifactsManager.add(manifestFile, JarFile.MANIFEST_NAME, true /* isTemp */);

        writeMainClass(clientArtifactsManager);

        /*
         * If the EAR contains a permissions file we need to make sure it's added
         * to the group-level generated facade JAR.
         */
        final File permissionsFile = getPermissionsFile();
        if (permissionsFile.canRead()) {
            clientArtifactsManager.add(permissionsFile, PERMISSIONS_XML_PATH, false /* isTemp */);
        }

        /*
         * Higher-level code will copy the files generated here plus other deployers'
         * artifacts - such as generated stubs - into the generated client JAR
         * which the app client deployer views as the group facade.
         * Each client's individual facade JARs then refer
         * to the group facade in their Class-Path so they can see the stubs.
         * This also allows Java SE clients to add the group facade JAR to
         * the runtime class path and see the stubs.  (This allows users who
         * did this in v2 to use the same technique.)
         */

    }

    private File getPermissionsFile() {
        return new File(new File(dc.getSource().getParentArchive().getURI()), PERMISSIONS_XML_PATH);
    }

    private void writeMainClass(final ClientArtifactsManager clientArtifactsManager) throws IOException {
        final String mainClassResourceName = GLASSFISH_APPCLIENT_GROUP_FACADE_CLASS_NAME.replace('.', '/') + ".class";
        final File mainClassJAR = new File(AppClientDeployerHelper.getModulesDir(serviceLocator),
            AppClientDeployerHelper.GF_CLIENT_MODULE_PATH);
        final File mainClassFile = File.createTempFile("main", ".class");
        try (OutputStream os = new FileOutputStream(mainClassFile);
            JarFile jf = new JarFile(mainClassJAR)) {
            JarEntry entry = jf.getJarEntry(mainClassResourceName);
            try (InputStream is = jf.getInputStream(entry)) {
                FileUtils.copy(is, os);
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }
        clientArtifactsManager.add(mainClassFile, mainClassResourceName, true);
    }
}
