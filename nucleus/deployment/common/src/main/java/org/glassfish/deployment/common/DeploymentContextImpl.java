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

package org.glassfish.deployment.common;

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.versioning.VersioningUtils;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;

import static com.sun.enterprise.config.serverbeans.ServerTags.IS_COMPOSITE;
import static com.sun.enterprise.util.io.FileUtils.whack;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;

/**
 *
 * @author dochez
 */
public class DeploymentContextImpl implements ExtendedDeploymentContext, PreDestroy {

    @LogMessagesResourceBundle
    private static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.deployment.LogMessages";

    // Reserve this range [NCLS-DEPLOYMENT-00001, NCLS-DEPLOYMENT-02000]
    // for message ids used in this deployment common module
    @LoggerInfo(subsystem = "DEPLOYMENT", description = "Deployment logger for common module", publish = true)
    private static final String DEPLOYMENT_LOGGER = "jakarta.enterprise.system.tools.deployment.common";

    public static final Logger deplLogger = Logger.getLogger(DEPLOYMENT_LOGGER, SHARED_LOGMESSAGE_RESOURCE);

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeploymentContextImpl.class);
    private static final String INTERNAL_DIR_NAME = "__internal";
    private static final String APP_TENANTS_SUBDIR_NAME = "__app-tenants";

    ReadableArchive source;
    ReadableArchive originalSource;
    final OpsParams parameters;
    ActionReport actionReport;
    final ServerEnvironment env;
    ClassLoader cloader;
    ArchiveHandler archiveHandler;
    Properties props;
    Map<String, Object> modulesMetaData = new HashMap<>();
    Phase phase = Phase.UNKNOWN;
    ClassLoader sharableTemp;
    Map<String, Properties> modulePropsMap = new HashMap<>();
    Map<String, Object> transientAppMetaData = new HashMap<>();
    Map<String, ArchiveHandler> moduleArchiveHandlers = new HashMap<>();
    Map<String, ExtendedDeploymentContext> moduleDeploymentContexts = new HashMap<>();
    ExtendedDeploymentContext parentContext;
    String moduleUri;
    private String tenant;
    private String originalAppName;
    private File tenantDir;

    /** Creates a new instance of DeploymentContext */
    public DeploymentContextImpl(Deployment.DeploymentContextBuilder builder, ServerEnvironment env) {
        this(builder.report(), builder.sourceAsArchive(), builder.params(), env);
    }

    public DeploymentContextImpl(ActionReport actionReport, Logger logger, ReadableArchive source, OpsParams params,
            ServerEnvironment env) {
        this(actionReport, source, params, env);
    }

    public DeploymentContextImpl(ActionReport actionReport, ReadableArchive source, OpsParams params, ServerEnvironment env) {
        this.originalSource = source;
        this.source = source;
        this.actionReport = actionReport;
        this.parameters = params;
        this.env = env;
    }

    @Override
    public Phase getPhase() {
        return phase;
    }

    @Override
    public void setPhase(Phase newPhase) {
        this.phase = newPhase;
    }

    @Override
    public ReadableArchive getSource() {
        return source;
    }

    @Override
    public void setSource(ReadableArchive source) {
        this.source = source;
    }

    @Override
    public <U extends OpsParams> U getCommandParameters(Class<U> commandParametersType) {
        try {
            return commandParametersType.cast(parameters);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public Logger getLogger() {
        return deplLogger;
    }

    @Override
    public synchronized void preDestroy() {
        try {
            PreDestroy.class.cast(sharableTemp).preDestroy();
        } catch (Exception e) {
            // ignore, the classloader does not need to be destroyed
        }
        try {
            PreDestroy.class.cast(cloader).preDestroy();
        } catch (Exception e) {
            // ignore, the classloader does not need to be destroyed
        }
    }

    /**
     * Returns the class loader associated to this deployment request. ClassLoader instances are usually obtained by the
     * getClassLoader API on the associated ArchiveHandler for the archive type being deployed.
     * <p/>
     * This can return null and the container should allocate a ClassLoader while loading the application.
     *
     * @return a class loader capable of loading classes and resources from the source
     * @link {org.jvnet.glassfish.apu.deployment.archive.ArchiveHandler.getClassLoader()}
     */
    @Override
    public ClassLoader getFinalClassLoader() {
        return cloader;
    }

    /**
     * Returns the class loader associated to this deployment request. ClassLoader instances are usually obtained by the
     * getClassLoader API on the associated ArchiveHandler for the archive type being deployed.
     * <p/>
     * This can return null and the container should allocate a ClassLoader while loading the application.
     *
     * @return a class loader capable of loading classes and resources from the source
     * @link {org.jvnet.glassfish.apu.deployment.archive.ArchiveHandler.getClassLoader()}
     */
    @Override
    public ClassLoader getClassLoader() {
        /*
         * TODO -- Replace this method with another that does not imply it is an accessor and conveys that the result may change
         * depending on the current lifecycle. For instance contemporaryClassLoader() Problem was reported by findbug
         */
        return getClassLoader(true);
    }

    @Override
    public synchronized void setClassLoader(ClassLoader cloader) {
        this.cloader = cloader;
    }

    // This classloader will be used for sniffer retrieval, metadata parsing
    // and the prepare
    @Override
    public synchronized void createDeploymentClassLoader(ClassLoaderHierarchy clh, ArchiveHandler handler) throws URISyntaxException, MalformedURLException {
        this.addTransientAppMetaData(IS_TEMP_CLASSLOADER, TRUE);
        this.sharableTemp = createClassLoader(clh, handler, null);
    }

    // This classloader will used to load and start the application
    @Override
    public void createApplicationClassLoader(ClassLoaderHierarchy classLoaderHierarchy, ArchiveHandler handler) throws URISyntaxException, MalformedURLException {
        addTransientAppMetaData(IS_TEMP_CLASSLOADER, FALSE);

        if (cloader == null) {
            cloader = createClassLoader(classLoaderHierarchy, handler, parameters.name());
        }
    }

    private ClassLoader createClassLoader(ClassLoaderHierarchy classLoaderHierarchy, ArchiveHandler handler, String appName) throws URISyntaxException, MalformedURLException {
        // first we create the appLib class loader, this is non shared libraries class loader
        ClassLoader applibCL = classLoaderHierarchy.getAppLibClassLoader(appName, getAppLibs());
        ClassLoader parentCL = classLoaderHierarchy.createApplicationParentCL(applibCL, this);

        return handler.getClassLoader(parentCL, this);
    }

    public synchronized ClassLoader getClassLoader(boolean sharable) {
        // If we are in prepare phase, we need to return our sharable temporary class loader
        // otherwise, we return the final one.
        if (phase == Phase.PREPARE) {
            if (sharable) {
                return sharableTemp;
            }

            return InstrumentableClassLoader.class.cast(sharableTemp).copy();
        }

        // we are out of the prepare phase, destroy the shareableTemp and
        // return the final classloader
        if (sharableTemp != null) {
            try {
                PreDestroy.class.cast(sharableTemp).preDestroy();
            } catch (Exception e) {
                // ignore, the classloader does not need to be destroyed
            }
            sharableTemp = null;
        }

        return cloader;
    }

    /**
     * Returns a scratch directory that can be used to store things in. The scratch directory will be persisted accross
     * server restart but not accross redeployment of the same application
     *
     * @param subDirName the sub directory name of the scratch dir
     * @return the scratch directory for this application based on passed in subDirName. Returns the root scratch dir if the
     * passed in value is null.
     */
    @Override
    public File getScratchDir(String subDirName) {
        File rootScratchDir = env.getApplicationStubPath();

        if (tenant != null && originalAppName != null) {
            // Multi-tenant case
            rootScratchDir = getRootScratchTenantDirForApp(originalAppName);
            rootScratchDir = new File(rootScratchDir, tenant);
            if (subDirName != null) {
                rootScratchDir = new File(rootScratchDir, subDirName);
            }

            return rootScratchDir;
        }

        // Regular case
        if (subDirName != null) {
            rootScratchDir = new File(rootScratchDir, subDirName);
        }

        return new File(rootScratchDir, VersioningUtils.getRepositoryName(parameters.name()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getSourceDir() {
        return new File(getSource().getURI());
    }

    @Override
    public void addModuleMetaData(Object metaData) {
        if (metaData != null) {
            modulesMetaData.put(metaData.getClass().getName(), metaData);
        }
    }

    @Override
    public <T> T getModuleMetaData(Class<T> metadataType) {
        Object moduleMetaData = modulesMetaData.get(metadataType.getName());
        if (moduleMetaData != null) {
            return metadataType.cast(moduleMetaData);
        }

        for (Object metadata : modulesMetaData.values()) {
            try {
                return metadataType.cast(metadata);
            } catch (ClassCastException e) {
            }
        }

        return null;
    }

    @Override
    public Collection<Object> getModuleMetadata() {
        return new ArrayList<>(modulesMetaData.values());
    }

    @Override
    public Map<String, Object> getTransientAppMetadata() {
        return new HashMap<>(transientAppMetaData);
    }

    @Override
    public void addTransientAppMetaData(String metaDataKey, Object metaData) {
        if (metaData != null) {
            transientAppMetaData.put(metaDataKey, metaData);
        }
    }

    @Override
    public <T> T getTransientAppMetaData(String key, Class<T> metadataType) {
        Object metaData = transientAppMetaData.get(key);
        if (metaData == null) {
            return null;
        }

        return metadataType.cast(metaData);
    }

    /**
     * Returns the application level properties that will be persisted as a key value pair at then end of deployment. That
     * allows individual Deployers implementation to store some information at the application level that should be
     * available upon server restart. Application level propertries are shared by all the modules.
     *
     * @return the application's properties.
     */
    @Override
    public Properties getAppProps() {
        if (props == null) {
            props = new Properties();
        }

        return props;
    }

    /**
     * Returns the module level properties that will be persisted as a key value pair at then end of deployment. That allows
     * individual Deployers implementation to store some information at the module level that should be available upon
     * server restart. Module level properties are only visible to the current module.
     *
     * @return the module's properties.
     */
    @Override
    public Properties getModuleProps() {
        // For standalone case, it would return the same as application level properties
        // For composite case, the composite deployer will return proper module level properties
        if (props == null) {
            props = new Properties();
        }

        return props;
    }

    /**
     * Add a new ClassFileTransformer to the context
     *
     * @param transformer the new class file transformer to register to the new application class loader
     * @throws UnsupportedOperationException if the class loader we use does not support the registration of a
     * ClassFileTransformer. In such case, the deployer should either fail deployment or revert to a mode without the
     * byteocode enhancement feature.
     */
    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        InstrumentableClassLoader instrumentableClassLoader = InstrumentableClassLoader.class.cast(getFinalClassLoader());
        ClassFileTransformer reentrantTransformer = new ReentrantClassFileTransformer(transformer);

        String isComposite = getAppProps().getProperty(IS_COMPOSITE);
        if (Boolean.valueOf(isComposite) && instrumentableClassLoader instanceof URLClassLoader) {
            @SuppressWarnings("resource")
            URLClassLoader urlClassLoader = (URLClassLoader) instrumentableClassLoader;
            boolean isAppLevel = (getParentContext() == null);
            if (isAppLevel) {
                // For ear lib PUs, let's install the tranformers with the EarLibClassLoader
                instrumentableClassLoader = InstrumentableClassLoader.class.cast(urlClassLoader.getParent().getParent());
            } else {
                // For modules inside the ear, let's install the transformers with the EarLibClassLoader in
                // addition to installing them to module classloader
                ClassLoader libClassLoader = urlClassLoader.getParent().getParent();
                if (!(libClassLoader instanceof InstrumentableClassLoader)) {
                    // Web module
                    libClassLoader = libClassLoader.getParent();
                }
                if (libClassLoader instanceof InstrumentableClassLoader) {
                    InstrumentableClassLoader.class.cast(libClassLoader).addTransformer(reentrantTransformer);
                }

            }
        }

        instrumentableClassLoader.addTransformer(reentrantTransformer);
    }

    @Override
    public List<URI> getAppLibs() throws URISyntaxException {
        List<URI> libURIs = new ArrayList<>();
        if (parameters.libraries() != null) {
            URL[] urls = ASClassLoaderUtil.getDeployParamLibrariesAsURLs(parameters.libraries(), env);
            for (URL url : urls) {
                File file = new File(url.getFile());
                deplLogger.log(FINE, "Specified library jar: " + file.getAbsolutePath());
                if (file.exists()) {
                    libURIs.add(url.toURI());
                } else {
                    throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.nonexist.libraries",
                            "Specified library jar {0} does not exist: {1}", file.getName(), file.getAbsolutePath()));
                }
            }
        }

        Set<String> extensionList = null;
        try {
            extensionList = InstalledLibrariesResolver.getInstalledLibraries(source);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        URL[] extensionListLibraries = ASClassLoaderUtil.getLibrariesAsURLs(extensionList, env);
        for (URL url : extensionListLibraries) {
            libURIs.add(url.toURI());
            if (deplLogger.isLoggable(FINEST)) {
                deplLogger.log(FINEST,
                        "Detected [EXTENSION_LIST]" + " installed-library [ " + url + " ] for archive [ " + source.getName() + "]");
            }
        }

        return libURIs;
    }

    @Override
    public void clean() {
        if (parameters.origin == OpsParams.Origin.undeploy || parameters.origin == OpsParams.Origin.deploy) {
            // For undeploy or deploy failure roll back we need to remove generated/xml, generated/ejb, generated/jsp,

            // Remove generated/xml
            whack(getScratchDir("xml"));

            // Remove generated/ejb
            whack(getScratchDir("ejb"));

            // Remove generated/jsp
            whack(getScratchDir("jsp"));

            // Remove the internal archive directory which holds the original
            // archive (and possibly deployment plan) that cluster sync can use
            whack(getAppInternalDir());

            whack(getAppAltDDDir());

            // Remove the root tenant dir for this application
            whack(getRootTenantDirForApp(parameters.name()));

            // Remove the root tenant generated dir root for this application
            whack(getRootScratchTenantDirForApp(parameters.name()));
        } else if (parameters.origin == OpsParams.Origin.mt_unprovision) {
            // for unprovision application, remove the tenant dir
            whack(tenantDir);

            // And remove the generated dir
            whack(getScratchDir(null));
        }
    }

    @Override
    public ArchiveHandler getArchiveHandler() {
        return archiveHandler;
    }

    @Override
    public void setArchiveHandler(ArchiveHandler archiveHandler) {
        this.archiveHandler = archiveHandler;
    }

    @Override
    public ReadableArchive getOriginalSource() {
        return originalSource;
    }

    /**
     * Gets the module properties for modules
     *
     * @return a map containing module properties
     */
    @Override
    public Map<String, Properties> getModulePropsMap() {
        return modulePropsMap;
    }

    /**
     * Sets the module properties for modules
     *
     * @param modulePropsMap
     */
    @Override
    public void setModulePropsMap(Map<String, Properties> modulePropsMap) {
        this.modulePropsMap = modulePropsMap;
    }

    /**
     * Sets the parent context for the module
     *
     * @param parentContext
     */
    @Override
    public void setParentContext(ExtendedDeploymentContext parentContext) {
        this.parentContext = parentContext;
    }

    /**
     * Gets the parent context of the module
     *
     *
     * @return the parent context
     */
    @Override
    public ExtendedDeploymentContext getParentContext() {
        return parentContext;
    }

    /**
     * Gets the module uri for this module context
     *
     * @return the module uri
     */
    @Override
    public String getModuleUri() {
        return moduleUri;
    }

    /**
     * Sets the module uri for this module context
     *
     * @param moduleUri
     */
    @Override
    public void setModuleUri(String moduleUri) {
        this.moduleUri = moduleUri;
    }

    /**
     * Gets the archive handlers for modules
     *
     * @return a map containing module archive handlers
     */
    @Override
    public Map<String, ArchiveHandler> getModuleArchiveHandlers() {
        return moduleArchiveHandlers;
    }

    /**
     * Gets the deployment context for modules
     *
     * @return a map containing module deployment contexts
     */
    @Override
    public Map<String, ExtendedDeploymentContext> getModuleDeploymentContexts() {
        return moduleDeploymentContexts;
    }

    /**
     * Gets the action report for this context
     *
     * @return an action report
     */
    @Override
    public ActionReport getActionReport() {
        return actionReport;
    }

    @Override
    public File getAppInternalDir() {
        final File internalDir = new File(env.getApplicationRepositoryPath(), INTERNAL_DIR_NAME);
        return new File(internalDir, VersioningUtils.getRepositoryName(parameters.name()));
    }

    @Override
    public File getAppAltDDDir() {
        final File altDDDir = env.getApplicationAltDDPath();
        return new File(altDDDir, VersioningUtils.getRepositoryName(parameters.name()));
    }

    @Override
    public void setTenant(final String tenant, final String appName) {
        this.tenant = tenant;
        this.originalAppName = appName;
        tenantDir = initTenantDir();
    }

    @Override
    public String getTenant() {
        return tenant;
    }

    @Override
    public File getTenantDir() {
        return tenantDir;
    }

    @Override
    public void postDeployClean(boolean isFinalClean) {
        if (transientAppMetaData != null) {
            if (isFinalClean) {
                transientAppMetaData.clear();
            } else {
                final String[] classNamesToClean = { Types.class.getName(), Parser.class.getName() };

                for (String className : classNamesToClean) {
                    transientAppMetaData.remove(className);
                }
            }
        }

        actionReport = null;
    }

    @Override
    public String toString() {
        return (source == null ? "" : source.toString()) + " " + (originalSource == null ? "" : originalSource.getURI());
    }

    /**
     * Prepare the scratch directories, creating the directories if they do not exist
     */
    @Override
    public void prepareScratchDirs() throws IOException {
        prepareScratchDir(getScratchDir("ejb"));
        prepareScratchDir(getScratchDir("xml"));
        prepareScratchDir(getScratchDir("jsp"));
    }


    // ### Private methods

    private File initTenantDir() {
        if (tenant == null || originalAppName == null) {
            return null;
        }

        File tenantDir = new File(getRootTenantDirForApp(originalAppName), tenant);
        if (!tenantDir.exists() && !tenantDir.mkdirs()) {
            if (deplLogger.isLoggable(FINEST)) {
                deplLogger.log(FINEST, "Unable to create directory " + tenantDir.getAbsolutePath());
            }

        }

        return tenantDir;
    }

    private File getRootTenantDirForApp(String appName) {
        return new File(new File(env.getApplicationRepositoryPath(), APP_TENANTS_SUBDIR_NAME), appName);
    }

    private File getRootScratchTenantDirForApp(String appName) {
        return new File(new File(env.getApplicationStubPath(), APP_TENANTS_SUBDIR_NAME), appName);
    }

    private void prepareScratchDir(File scratchDir) throws IOException {
        if (!scratchDir.isDirectory() && !scratchDir.mkdirs()) {
            throw new IOException("Cannot create scratch directory : " + scratchDir.getAbsolutePath());
        }
    }

    static final class ReentrantClassFileTransformer implements ClassFileTransformer {

        private final ThreadLocal<Boolean> bytecodeTransforming = ThreadLocal.withInitial(() -> false);
        private final ClassFileTransformer transformer;

        ReentrantClassFileTransformer(ClassFileTransformer transformer) {
            this.transformer = transformer;
        }

        @Override
        public byte[] transform(ClassLoader loader,
                                String className,
                                Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain,
                                byte[] classfileBuffer) throws IllegalClassFormatException {
            // Skip if already transformation in progress
            if (bytecodeTransforming.get()) {
                return null;
            }

            bytecodeTransforming.set(true);
            try {
                return transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            } finally {
                bytecodeTransforming.remove();
            }
        }
    }
}
