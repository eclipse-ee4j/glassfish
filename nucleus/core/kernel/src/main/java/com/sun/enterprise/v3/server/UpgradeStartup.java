/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.common.DoNothingActionReporter;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.deployment.archive.WritableArchiveEntry;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.internal.api.DomainUpgrade;
import org.glassfish.internal.api.InternalSystemAdministrator;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Very simple ModuleStartup that basically force an immediate shutdown.
 * When start() is invoked, the upgrade of the domain.xml has already been
 * performed.
 *
 * @author Jerome Dochez
 */
@Service(name="upgrade")
public class UpgradeStartup implements ModuleStartup {

    @Inject
    CommandRunner runner;

    @Inject
    AppServerStartup appservStartup;

    @Inject
    Applications applications;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    ServerEnvironment env;

    @Inject @Named( ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    @Inject
    Domain domain;

    @Inject
    CommandRunner commandRunner;

    @Inject @Optional
    IterableProvider<DomainUpgrade> upgrades;

    // we need to refine, a better logger should be used.
    @Inject
    Logger logger;

    @Inject
    private InternalSystemAdministrator kernelIdentity;

    private final static String MODULE_TYPE = "moduleType";

    private final static String J2EE_APPS = "j2ee-apps";
    private final static String J2EE_MODULES = "j2ee-modules";

    private final static String DOMAIN_TARGET = "domain";

    private final static String SIGNATURE_TYPES_PARAM = "-signatureTypes";

    private List<String> sigTypeList = new ArrayList<>();

    @Override
    public void setStartupContext(StartupContext startupContext) {
        appservStartup.setStartupContext(startupContext);
    }

    // do nothing, just return, at the time the upgrade service has
    // run correctly.
    @Override
    public void start() {

        // we need to disable all the applications before starting server
        // so the applications will not get loaded before redeployment
        // store the list of previous enabled applications
        // so we can reset these applications back to enabled after
        // redeployment
        List<Application> enabledApps = new ArrayList<>();
        List<String> enabledAppNames = new ArrayList<>();

        for (Application app : domain.getApplications().getApplications()) {
            logger.log(Level.INFO, "app " + app.getName() + " is " + app.getEnabled() + " resulting in " + Boolean.parseBoolean(app.getEnabled()));
            if (Boolean.parseBoolean(app.getEnabled())) {
                logger.log(Level.INFO, "Disabling application " + app.getName());
                enabledApps.add(app);
                enabledAppNames.add(app.getName());
            }
        }

        if (enabledApps.size()>0) {
            try  {
                ConfigSupport.apply(new ConfigCode() {
                    @Override
                    public Object run(ConfigBeanProxy... configBeanProxies) throws PropertyVetoException, TransactionFailure {
                        for (ConfigBeanProxy proxy : configBeanProxies) {
                            Application app = (Application) proxy;
                            app.setEnabled(Boolean.FALSE.toString());
                        }
                        return null;
                    }
                }, enabledApps.toArray(new Application[enabledApps.size()]));
            } catch(TransactionFailure tf) {
                logger.log(Level.SEVERE, "Exception while disabling applications", tf);
                return;
            }
        }

        // start the application server
        appservStartup.start();

        initializeSigTypeList();

        // redeploy all existing applications
        for (Application app : applications.getApplications()) {
            // we don't need to redeploy lifecycle modules
            if (Boolean.parseBoolean(app.getDeployProperties().getProperty
                (ServerTags.IS_LIFECYCLE))) {
                continue;
            }
            logger.log(Level.INFO, "Redeploy application " + app.getName() + " located at " + app.getLocation());
            // we let upgrade proceed even if one application
            // failed to redeploy
            redeployApp(app);
        }

        // re-enables all applications.
        // we need to use the names in the enabledAppNames to find all
        // the application refs that need to be re-enabled
        // as the previous application collected not longer exist
        // after redeployment
        if (enabledAppNames.size()>0) {
            for (Application app : domain.getApplications().getApplications()) {
                if (enabledAppNames.contains(app.getName())) {
                    logger.log(Level.INFO, "Enabling application " + app.getName());
                    try {
                        ConfigSupport.apply(new SingleConfigCode<Application>() {
                            @Override
                            public Object run(Application param) throws PropertyVetoException, TransactionFailure {
                                if (!Boolean.parseBoolean(param.getEnabled())) {
                                    param.setEnabled(Boolean.TRUE.toString());
                                }
                                return null;
                            }
                        }, app);
                    } catch(TransactionFailure tf) {
                        logger.log(Level.SEVERE, "Exception while disabling applications", tf);
                        return;
                    }
                }
            }
        }

        // clean up leftover directories
        cleanupLeftOverDirectories();

        // stop-the server.
        KernelLoggerInfo.getLogger().info(KernelLoggerInfo.exitUpgrade);
        try {
            Thread.sleep(3000);
            if (runner!=null) {
                runner.getCommandInvocation("stop-domain", new DoNothingActionReporter(), kernelIdentity.getSubject()).execute();
            }

        } catch (InterruptedException e) {
            KernelLoggerInfo.getLogger().log(Level.SEVERE, KernelLoggerInfo.exceptionUpgrade, e);
        }

    }

    @Override
    public void stop() {
        appservStartup.stop();
    }

    private void cleanupLeftOverDirectories() {
        // 1. remove applications/j2ee-apps(modules) directory
        File oldJ2eeAppsRepository = new File(
            env.getApplicationRepositoryPath(), J2EE_APPS);
        FileUtils.whack(oldJ2eeAppsRepository);
        File oldJ2eeModulesRepository = new File(
            env.getApplicationRepositoryPath(), J2EE_MODULES);
        FileUtils.whack(oldJ2eeModulesRepository);

        // 2. remove generated/xml/j2ee-apps(modules) directory
        File oldJ2eeAppsGeneratedXMLDir = new File(
           env.getApplicationGeneratedXMLPath(), J2EE_APPS);
        FileUtils.whack(oldJ2eeAppsGeneratedXMLDir);
        File oldJ2eeModulesGeneratedXMLDir = new File(
           env.getApplicationGeneratedXMLPath(), J2EE_MODULES);
        FileUtils.whack(oldJ2eeModulesGeneratedXMLDir);

        // 3. remove generated/ejb/j2ee-apps(modules) directory
        File oldJ2eeAppsEJBStubDir = new File(
           env.getApplicationEJBStubPath(), J2EE_APPS);
        FileUtils.whack(oldJ2eeAppsEJBStubDir);
        File oldJ2eeModulesEJBStubDir = new File(
           env.getApplicationEJBStubPath(), J2EE_MODULES);
        FileUtils.whack(oldJ2eeModulesEJBStubDir);

        // 4. clean up generated/jsp/j2ee-apps(modules) directory
        File oldJ2eeAppsJSPCompileDir = new File(
           env.getApplicationCompileJspPath(), J2EE_APPS);
        FileUtils.whack(oldJ2eeAppsJSPCompileDir);
        File oldJ2eeModulesJSPCompileDir = new File(
           env.getApplicationCompileJspPath(), J2EE_MODULES);
        FileUtils.whack(oldJ2eeModulesJSPCompileDir);

        // 5. clean up old system apps policy files
        File policyRootDir = env.getApplicationPolicyFilePath();
        File adminapp = new File(policyRootDir, "adminapp");
        FileUtils.whack(adminapp);
        File admingui = new File(policyRootDir, "admingui");
        FileUtils.whack(admingui);
        File ejbtimer = new File(policyRootDir, "__ejb_container_timer_app");
        FileUtils.whack(ejbtimer);
        File mejbapp = new File(policyRootDir, "MEjbApp");
        FileUtils.whack(mejbapp);
        File wstx = new File(policyRootDir, "WSTXServices");
        FileUtils.whack(wstx);
        File jwsappclient = new File(policyRootDir, "__JWSappclients");
        FileUtils.whack(jwsappclient);
    }

    private boolean redeployApp(Application app) {
        // we don't need to redeploy any v3 type application
        if (app.getModule().size() > 0 ) {
            logger.log(Level.INFO, "Skip redeploying v3 type application " +
                app.getName());
            return true;
        }

        // populate the params and properties from application element first
        DeployCommandParameters deployParams = app.getDeployParameters(null);

        // for archive deployment, let's repackage the archive and redeploy
        // that way
        // we cannot just directory redeploy the archive deployed apps in
        // v2->v3 upgrade as the repository layout was different in v2
        // we should not have to repackage for any upgrade from v3
        if (! Boolean.valueOf(app.getDirectoryDeployed())) {
            File repackagedFile = null;
            try {
                repackagedFile = repackageArchive(app);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Repackaging of application " + app.getName() + " failed: " + ioe.getMessage(), ioe);
                return false;
            }
            if (repackagedFile == null) {
                logger.log(Level.SEVERE, "Repackaging of application " + app.getName() + " failed.");
                return false;
            }
            logger.log(Level.INFO, "Repackaged application " + app.getName()
                + " at " + repackagedFile.getPath());
            deployParams.path = repackagedFile;
        }

        deployParams.properties = app.getDeployProperties();
        // remove the marker properties so they don't get carried over
        // through redeployment
        deployParams.properties.remove(MODULE_TYPE);
        // add the compatibility property so the applications are
        // upgraded/redeployed in a backward compatible way
        deployParams.properties.setProperty(DeploymentProperties.COMPATIBILITY, "v2");

        // now override the ones needed for the upgrade
        deployParams.enabled = null;
        deployParams.force = true;
        deployParams.dropandcreatetables = false;
        deployParams.createtables = false;
        deployParams.target = DOMAIN_TARGET;

        ActionReport report = new DoNothingActionReporter();
        commandRunner.getCommandInvocation("deploy", report, kernelIdentity.getSubject()).parameters(deployParams).execute();

        // should we delete the temp file after we are done
        // it seems it might be useful to keep it around for debugging purpose

        if (report.getActionExitCode().equals(ActionReport.ExitCode.FAILURE)) {
            logger.log(Level.SEVERE, "Redeployment of application " + app.getName() + " failed: " + report.getMessage() + "\nPlease redeploy " + app.getName() + " manually.", report.getFailureCause());
            return false;
        }
        return true;
    }

    private File repackageArchive(Application app) throws IOException {
        URI uri = null;
        try {
            uri = new URI(app.getLocation());
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        if (uri == null) {
            return null;
        }

        Properties appProperties = app.getDeployProperties();
        String moduleType = appProperties.getProperty(MODULE_TYPE);
        String suffix = getSuffixFromType(moduleType);
        if (suffix == null) {
            suffix = ".jar";
        }
        File repositoryDir = new File(uri);

        // get temporary file directory of the system and set targetDir to it
        File tmp = File.createTempFile("upgrade", null);
        String targetParentDir = tmp.getParent();
        boolean isDeleted = tmp.delete();
        if (!isDeleted) {
            logger.log(Level.WARNING, "Error in deleting file " + tmp.getAbsolutePath());
        }

        if (moduleType.equals(ServerTags.J2EE_APPLICATION)) {
            return repackageApplication(repositoryDir, targetParentDir, suffix);
        } else {
            return repackageStandaloneModule(repositoryDir, targetParentDir, suffix);
        }
    }

    private File repackageApplication(File appDir,
        String targetParentDir, String suffix) throws IOException {
        String appName = appDir.getName();

        File tempEar = new File(targetParentDir, appName + suffix);
        if (tempEar.exists()) {
            boolean isDeleted = tempEar.delete();
            if (!isDeleted) {
                logger.log(Level.WARNING, "Error in deleting file " + tempEar.getAbsolutePath());
            }
        }
        try (ReadableArchive source = archiveFactory.openArchive(appDir);
            WritableArchive target = archiveFactory.createArchive("jar", tempEar)) {

            Collection<String> directoryEntries = source.getDirectories();
            List<String> subModuleEntries = new ArrayList<>();
            List<String> entriesToExclude = new ArrayList<>();

            // first put all the sub module jars to the target archive
            for (String directoryEntry : directoryEntries) {
                if (directoryEntry.endsWith("_jar") || directoryEntry.endsWith("_war")
                    || directoryEntry.endsWith("_rar")) {
                    subModuleEntries.add(directoryEntry);
                    File moduleJar = processModule(new File(appDir, directoryEntry), targetParentDir, null);
                    try (InputStream is = new FileInputStream(moduleJar);
                        WritableArchiveEntry os = target.putNextEntry(moduleJar.getName())) {
                        FileUtils.copy(is, os);
                    }
                }
            }

            // now find all the entries we should exclude to copy to the target
            // basically all sub module entries should be excluded
            for (String subModuleEntry : subModuleEntries) {
                Enumeration<String> ee = source.entries(subModuleEntry);
                while (ee.hasMoreElements()) {
                    String eeEntryName = ee.nextElement();
                    entriesToExclude.add(eeEntryName);
                }
            }

            // now copy the rest of the entries
            Enumeration<String> e = source.entries();
            while (e.hasMoreElements()) {
                String entryName = e.nextElement();
                if (entriesToExclude.contains(entryName)) {
                    continue;
                }
                if (isSigFile(entryName)) {
                    logger.log(Level.INFO, "Excluding signature file: {0} from repackaged application: {1}",
                        new Object[] {entryName, appName});
                    continue;
                }
                try (InputStream sis = source.getEntry(entryName)) {
                    if (sis == null) {
                        continue;
                    }
                    try (WritableArchiveEntry os = target.putNextEntry(entryName)) {
                        FileUtils.copy(sis, os);
                    }
                }
            }

            // last is manifest if existing.
            Manifest m = source.getManifest();
            if (m != null) {
                processManifest(m, appName);
                try (WritableArchiveEntry os = target.putNextEntry(JarFile.MANIFEST_NAME)) {
                    m.write(os);
                }
            }
        }
        return tempEar;
    }


    private File repackageStandaloneModule(File moduleDirName,
        String targetParentDir, String suffix) throws IOException {
        return processModule(moduleDirName, targetParentDir, suffix);
    }

    // repackage a module and return it as a jar file
    private File processModule(File moduleDir, String targetParentDir, String suffix) throws IOException {
        String moduleName = moduleDir.getName();

        // sub module in ear case
        if (moduleName.endsWith("_jar") || moduleName.endsWith("_war") || moduleName.endsWith("_rar")) {
            suffix = "." + moduleName.substring(moduleName.length() - 3);
            moduleName = moduleName.substring(0, moduleName.lastIndexOf('_'));
        }

        File tempJar = new File(targetParentDir, moduleName + suffix);
        if (tempJar.exists()) {
            boolean isDeleted = tempJar.delete();
            if (!isDeleted) {
                logger.log(Level.WARNING, "Error in deleting file " + tempJar.getAbsolutePath());
            }
        }
        try (ReadableArchive source = archiveFactory.openArchive(moduleDir);
            WritableArchive target = archiveFactory.createArchive("jar", tempJar)) {
            Enumeration<String> e = source.entries();
            while (e.hasMoreElements()) {
                String entryName = e.nextElement();
                if (isSigFile(entryName)) {
                    logger.log(Level.INFO,
                        "Excluding signature file: " + entryName + " from repackaged module: " + moduleName + "\n");
                    continue;
                }
                try (InputStream sis = source.getEntry(entryName)) {
                    if (sis == null) {
                        continue;
                    }
                    try (OutputStream os = target.putNextEntry(entryName)) {
                        FileUtils.copy(sis, os);
                    }
                }
            }

            // last is manifest if existing.
            Manifest manifest = source.getManifest();
            if (manifest != null) {
                processManifest(manifest, moduleName);
                try (OutputStream os = target.putNextEntry(JarFile.MANIFEST_NAME)) {
                    manifest.write(os);
                }
            }
        }

        return tempJar;
    }

    private String getSuffixFromType(String moduleType) {
        if (moduleType == null) {
            return null;
        }
        if (moduleType.equals(ServerTags.CONNECTOR_MODULE)) {
            return ".rar";
        }
        if (moduleType.equals(ServerTags.EJB_MODULE)) {
            return ".jar";
        }
        if (moduleType.equals(ServerTags.WEB_MODULE)) {
            return ".war";
        }
        if (moduleType.equals(ServerTags.APPCLIENT_MODULE)) {
            return ".jar";
        }
        if (moduleType.equals(ServerTags.J2EE_APPLICATION)) {
            return ".ear";
        }
        return null;
    }

    private void initializeSigTypeList() {
        String sigTypesParam = env.getStartupContext().getArguments().getProperty(SIGNATURE_TYPES_PARAM);
        if (sigTypesParam != null) {
            sigTypeList = StringUtils.parseStringList(sigTypesParam, ",");
        }
        sigTypeList.add(".SF");
        sigTypeList.add(".sf");
        sigTypeList.add(".RSA");
        sigTypeList.add(".rsa");
        sigTypeList.add(".DSA");
        sigTypeList.add(".dsa");
        sigTypeList.add(".PGP");
        sigTypeList.add(".pgp");
    }

    private boolean isSigFile(String entryName) {
        for (String sigType : sigTypeList) {
            if (entryName.endsWith(sigType)) {
                return true;
            }
        }
        return false;
    }

    private void processManifest(Manifest m, String moduleName) {
        // remove signature related entries from the file
        Map<String, Attributes> entries = m.getEntries();
        Iterator<Map.Entry<String, Attributes>> entryItr = entries.entrySet().iterator();
        while (entryItr.hasNext()) {
            Attributes attr = entryItr.next().getValue();
            Iterator<Map.Entry<Object, Object>> attrItr  = attr.entrySet().iterator();
            while (attrItr.hasNext()) {
                Object attrKey = attrItr.next().getKey();
                if (attrKey instanceof Attributes.Name) {
                    Attributes.Name attrKey2 = (Attributes.Name) attrKey;
                    if (attrKey2.toString().trim().equals("Digest-Algorithms")
                        || attrKey2.toString().indexOf("-Digest") != -1) {
                        logger.log(Level.INFO, "Removing signature attribute "
                            + attrKey2 + " from manifest in "  +
                            moduleName + "\n");
                        attrItr.remove();
                    }
                }
            }
            if (attr.size() == 0) {
                entryItr.remove();
            }
        }
    }
}
