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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.deployment.ManagedBeanDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFileFor;
import com.sun.enterprise.deployment.io.DescriptorConstants;
import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.xml.TagNames;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.CarArchiveType;
import org.glassfish.api.deployment.archive.EarArchiveType;
import org.glassfish.api.deployment.archive.EjbArchiveType;
import org.glassfish.api.deployment.archive.RarArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ScatteredWarArchiveType;
import org.glassfish.api.deployment.archive.WarArchiveType;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.deployment.SnifferManager;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.xml.sax.SAXException;

import static com.sun.enterprise.deployment.deploy.shared.Util.toURI;
import static java.lang.System.Logger.Level.DEBUG;
import static java.util.Collections.emptyList;
import static org.glassfish.deployment.common.DeploymentUtils.getManifestLibraries;
import static org.glassfish.loader.util.ASClassLoaderUtil.getAppLibDirLibrariesAsList;

/**
 * Utility class for convenience methods
 *
 * @author Jerome Dochez
 */
public class DOLUtils {

    public final static String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    public final static String SCHEMA_LOCATION_TAG = "xsi:schemaLocation";

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DOLUtils.class);

    @LogMessagesResourceBundle
    private static final String SHARED_LOGMESSAGE_RESOURCE = "org.glassfish.deployment.LogMessages";

    // Reserve this range [AS-DEPLOYMENT-00001, AS-DEPLOYMENT-02000]
    // for message ids used in this deployment dol module
    @LoggerInfo(subsystem = "DEPLOYMENT", description = "Deployment logger for dol module", publish = true)
    private static final String DEPLOYMENT_LOGGER = "jakarta.enterprise.system.tools.deployment.dol";

    public static final Logger deplLogger = Logger.getLogger(DEPLOYMENT_LOGGER, SHARED_LOGMESSAGE_RESOURCE);
    private static final System.Logger LOGGER = System.getLogger(DEPLOYMENT_LOGGER, deplLogger.getResourceBundle());

    @LogMessageInfo(
        message = "Ignore {0} in archive {1}, as WLS counterpart runtime xml {2} is present in the same archive.",
        level = "WARNING")
    private static final String COUNTERPART_CONFIGDD_EXISTS = "AS-DEPLOYMENT-00001";

    @LogMessageInfo(message = "Exception caught:  {0}.", level = "WARNING")
    private static final String EXCEPTION_CAUGHT = "AS-DEPLOYMENT-00002";

    @LogMessageInfo(message = "{0} module [{1}] contains characteristics of other module type: {2}.", level = "WARNING")
    private static final String INCOMPATIBLE_TYPE = "AS-DEPLOYMENT-00003";

    @LogMessageInfo(
        message = "Unsupported deployment descriptors element {0} value {1} for descriptor {2}.",
        level = "WARNING")
    public static final String INVALID_DESC_MAPPING = "AS-DEPLOYMENT-00015";

    @LogMessageInfo(
        message = "DOLUtils: Invalid Deployment Descriptors in {0} \nLine {1} Column {2} -- {3}.",
        level = "SEVERE",
        cause = "Failed to find the resource specified in the deployment descriptor."
            + " May be because of wrong specification in the descriptor",
        action = "Ensure that the resource specified is present."
            + " Ensure that there is no typo in the resource specified in the descriptor")
    public static final String INVALILD_DESCRIPTOR_LONG = "AS-DEPLOYMENT-00118";

    @LogMessageInfo(
        message = "Deployment Descriptor parsing failure: {0}",
        cause = "Error while parsing the deployment descriptor."
            + " May be because of malformed descriptor or absence of all required descriptor elements.",
        action = "Ensure that the descriptor is well formed and as per specification."
            + " Ensure that the SAX parser configuration is correct and the descriptor has right permissions.")
    public static final String INVALILD_DESCRIPTOR_SHORT = "AS-DEPLOYMENT-00120";

    @LogMessageInfo(
        message = "DEP0003:The jndi-name is already used in the global tree failed for given jndi-name: {0}",
        level = "SEVERE",
        cause = "The JNDI name of the descriptor is already used in the global JNDI tree,"
            + " probably by a resource defined on the server",
        action = "Make sure that the JNDI name doesn't conflict with any global resource already defined on the server")
    public static final String JNDI_LOOKUP_FAILED = "enterprise.deployment.util.application.lookup";

    @LogMessageInfo(
        message = "DEP0004:Deployment failed because a conflict occured for jndi-name: {0} for application: {1}",
        level = "SEVERE",
        cause = "Unknown",
        action = "Unknown")
    public static final String INVALID_NAMESPACE = "enterprise.deployment.util.application.invalid.namespace";

    @LogMessageInfo(
        message = "DEP0005:Deployment failed due to the invalid scope {0} defined for jndi-name: {1}",
        level = "SEVERE",
        cause = "Unknown",
        action = "Unknown")
    public static final String INVALID_JNDI_SCOPE = "enterprise.deployment.util.application.invalid.jndiname.scope";

    @LogMessageInfo(
        message = "DPL8006: get/add descriptor failure : {0} TO {1}",
        level = "SEVERE",
        cause = "Adding or getting a descriptor failed. May be because the node / information to be added is not valid;"
            + " may be because of the descriptor was not registered",
        action = "Ensure that the node to be added is valid. Ensure that the permissions are set as expected.")
    public static final String ADD_DESCRIPTOR_FAILURE = "enterprise.deployment.backend.addDescriptorFailure";

    // The system property to control the precedence between GF DD
    // and WLS DD when they are both present. When this property is
    // set to true, GF DD will have higher precedence over WLS DD.
    private static final String GFDD_OVER_WLSDD = "gfdd.over.wlsdd";

    // The system property to control whether we should just ignore
    // WLS DD. When this property is set to true, WLS DD will be ignored.
    private static final String IGNORE_WLSDD = "ignore.wlsdd";

    private static final String ID_SEPARATOR = "_";


    /** no need to creates new DOLUtils */
    private DOLUtils() {
    }

    /**
     * @return a logger to use in the DOL implementation classes
     */
    public static Logger getDefaultLogger() {
        return deplLogger;
    }

    public static System.Logger getLogger() {
        return LOGGER;
    }

    public static boolean equals(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    public static List<URI> getLibraryJarURIs(BundleDescriptor bundleDesc, ReadableArchive archive) throws Exception {
        if (bundleDesc == null) {
            return emptyList();
        }

        ModuleDescriptor moduleDesc = bundleDesc.getModuleDescriptor();
        Application app = ((BundleDescriptor)moduleDesc.getDescriptor()).getApplication();
        return getLibraryJarURIs(app, archive);
    }

    public static List<URI> getLibraryJarURIs(Application app, ReadableArchive archive) throws Exception {
        List<URI> libraryURIs = new ArrayList<>();

        // Add libraries referenced through manifest
        List<URL> libraryURLs = new ArrayList<>(getManifestLibraries(archive));
        ReadableArchive parentArchive = archive.getParentArchive();
        if (parentArchive == null) {
            return emptyList();
        }

        File appRoot = new File(parentArchive.getURI());
        // Add libraries jars inside application lib directory
        libraryURLs.addAll(getAppLibDirLibrariesAsList(appRoot, app.getLibraryDirectory(), null));
        for (URL url : libraryURLs) {
            libraryURIs.add(toURI(url));
        }

        return libraryURIs;
    }

    public static BundleDescriptor getCurrentBundleForContext(DeploymentContext context) {
        ExtendedDeploymentContext ctx = (ExtendedDeploymentContext)context;
        Application application = context.getModuleMetaData(Application.class);
        if (application == null) {
            // This can happen for non-JavaEE type deployment. e.g., issue 15869
            return null;
        }

        if (ctx.getParentContext() == null) {
            if (application.isVirtual()) {
                // standalone module
                return application.getStandaloneBundleDescriptor();
            }
            // top level
            return application;
        }

        // a sub module of ear
        return application.getModuleByUri(ctx.getModuleUri());
    }

    public static boolean isRAConnectionFactory(ServiceLocator habitat, String type, Application thisApp) {
        // first check if this is a connection factory defined in a resource
        // adapter in this application
        if (isRAConnectionFactory(type, thisApp)) {
            return true;
        }

        // then check if this is a connection factory defined in a standalone
        // resource adapter
        Applications applications = habitat.getService(Applications.class);
        if (applications != null) {
            List<com.sun.enterprise.config.serverbeans.Application> raApps = applications.getApplicationsWithSnifferType(com.sun.enterprise.config.serverbeans.ServerTags.CONNECTOR, true);
            ApplicationRegistry appRegistry = habitat.getService(ApplicationRegistry.class);
            for (com.sun.enterprise.config.serverbeans.Application raApp : raApps) {
                ApplicationInfo appInfo = appRegistry.get(raApp.getName());
                if (appInfo == null) {
                    continue;
                }
                if (isRAConnectionFactory(type, appInfo.getMetaData(Application.class))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isRAConnectionFactory(String type, Application app) {
        if (app == null) {
            return false;
        }
        for (ConnectorDescriptor cd : app.getBundleDescriptors(ConnectorDescriptor.class)) {
            if (cd.getConnectionDefinitionByCFType(type) != null) {
                return true;
            }
        }
        return false;
    }

    public static ArchiveType earType() {
        return getModuleType("ear");
    }

    public static ArchiveType ejbType() {
        return getModuleType("ejb");
    }


    /**
     * @return Can return null when not executed on the server!!!
     * @see ProcessEnvironment#getProcessType()
     */
    // FIXME: Can return null when not executed on the server!!!
    public static ArchiveType carType() {
        return getModuleType("car");
    }

    public static ArchiveType warType() {
        return getModuleType("war");
    }

    public static ArchiveType rarType() {
        return getModuleType("rar");
    }

    public static ArchiveType scatteredWarType() {
        return getModuleType(ScatteredWarArchiveType.ARCHIVE_TYPE);
    }


    /**
     * Utility method to retrieve a {@link ArchiveType} from a stringified module type.
     * Since {@link ArchiveType} is an extensible abstraction and implementations are plugged
     * in via HK2 service registry, this method returns null if HK2 service registry is not set
     * and the type is not one of supported.
     * <p>
     * If null is passed to this method, it returns null instead of returning an arbitrary
     * {@link ArchiveType} or throwing an exception.
     *
     * @param moduleType String equivalent of the module type being looked up. null is allowed.
     * @return the corresponding {@link ArchiveType}, null if no such module type exists
     *         or HK2 Service registry is not set up
     */
    public static ArchiveType getModuleType(String moduleType) {
        if (moduleType == null) {
            return null;
        }
        switch (moduleType) {
            case WarArchiveType.ARCHIVE_TYPE:
                return WarArchiveType.WAR_ARCHIVE;
            case EjbArchiveType.ARCHIVE_TYPE:
                return EjbArchiveType.EJB_ARCHIVE;
            case EarArchiveType.ARCHIVE_TYPE:
                return EarArchiveType.EAR_ARCHIVE;
            case CarArchiveType.ARCHIVE_TYPE:
                return CarArchiveType.CAR_ARCHIVE;
            case RarArchiveType.ARCHIVE_TYPE:
                return RarArchiveType.RAR_ARCHIVE;
            case ScatteredWarArchiveType.ARCHIVE_TYPE:
                return ScatteredWarArchiveType.SCATTERED_WAR_ARCHIVE;
            default:
                ServiceLocator services = Globals.getStaticBaseServiceLocator();
                if (services != null) {
                    return services.getService(ArchiveType.class, moduleType);
                }
        }
        throw new IllegalArgumentException("Unsupported type: " + moduleType);
    }

    // returns true if GF DD should have higher precedence over
    // WLS DD when both present in the same archive
    public static boolean isGFDDOverWLSDD() {
        return Boolean.getBoolean(GFDD_OVER_WLSDD);
    }

    // returns true if we should ignore WLS DD in the archive
    public static boolean isIgnoreWLSDD() {
        return Boolean.getBoolean(IGNORE_WLSDD);
    }

    // process the list of the configuration files, and return the sorted
    // configuration file with precedence from high to low
    // this list does not take consideration of what runtime files are
    // present in the current archive
    private static List<ConfigurationDeploymentDescriptorFile> sortConfigurationDDFiles(List<ConfigurationDeploymentDescriptorFile> ddFiles, ArchiveType archiveType, ReadableArchive archive) {
        ConfigurationDeploymentDescriptorFile wlsConfDD = null;
        ConfigurationDeploymentDescriptorFile gfConfDD = null;
        ConfigurationDeploymentDescriptorFile sunConfDD = null;
        for (ConfigurationDeploymentDescriptorFile ddFile : ddFiles) {
            ddFile.setArchiveType(archiveType);
            String ddPath = ddFile.getDeploymentDescriptorPath();
            if (ddPath.indexOf(DescriptorConstants.WLS) != -1) {
                wlsConfDD = ddFile;
            } else if (ddPath.indexOf(DescriptorConstants.GF_PREFIX) != -1) {
                gfConfDD = ddFile;
            } else if (ddPath.indexOf(DescriptorConstants.S1AS_PREFIX) != -1) {
                sunConfDD = ddFile;
            }
        }
        List<ConfigurationDeploymentDescriptorFile> sortedConfDDFiles = new ArrayList<>();

        // if there is external runtime alternate deployment descriptor
        // specified, just use that
        File runtimeAltDDFile = archive.getArchiveMetaData(
            DeploymentProperties.RUNTIME_ALT_DD, File.class);
        if (runtimeAltDDFile != null && runtimeAltDDFile.exists() && runtimeAltDDFile.isFile()) {
            String runtimeAltDDPath = runtimeAltDDFile.getPath();
            validateRuntimeAltDDPath(runtimeAltDDPath);
            if (runtimeAltDDPath.indexOf(
                DescriptorConstants.GF_PREFIX) != -1 && gfConfDD != null) {
                sortedConfDDFiles.add(gfConfDD);
                return sortedConfDDFiles;
            }
        }

        // sort the deployment descriptor files by precedence order
        // when they are present in the same archive

        if (Boolean.getBoolean(GFDD_OVER_WLSDD)) {
            // if this property set, it means we need to make GF deployment
            // descriptors higher precedence
            if (gfConfDD != null) {
                sortedConfDDFiles.add(gfConfDD);
            }
            if (wlsConfDD != null) {
                sortedConfDDFiles.add(wlsConfDD);
            }
        } else if (Boolean.getBoolean(IGNORE_WLSDD)) {
            // if this property set, it means we need to ignore
            // WLS deployment descriptors
            if (gfConfDD != null) {
                sortedConfDDFiles.add(gfConfDD);
            }
        } else  {
            // the default will be WLS DD has higher precedence
            if (wlsConfDD != null) {
                sortedConfDDFiles.add(wlsConfDD);
            }
            if (gfConfDD != null) {
                sortedConfDDFiles.add(gfConfDD);
            }
        }

        if (sunConfDD != null) {
            sortedConfDDFiles.add(sunConfDD);
        }

        return sortedConfDDFiles;
    }

    public static void validateRuntimeAltDDPath(String runtimeAltDDPath) {
        if (runtimeAltDDPath.indexOf(DescriptorConstants.GF_PREFIX) == -1) {
            String msg = localStrings.getLocalString(
                "enterprise.deployment.util.unsupportedruntimealtdd", "Unsupported external runtime alternate deployment descriptor [{0}].", new Object[] {runtimeAltDDPath});
            throw new IllegalArgumentException(msg);
        }
    }

    // process the list of the configuration files, and return the sorted
    // configuration file with precedence from high to low
    // this list takes consideration of what runtime files are
    // present in the current archive
    public static List<ConfigurationDeploymentDescriptorFile> processConfigurationDDFiles(List<ConfigurationDeploymentDescriptorFile> ddFiles, ReadableArchive archive, ArchiveType archiveType) throws IOException {
        File runtimeAltDDFile = archive.getArchiveMetaData(
            DeploymentProperties.RUNTIME_ALT_DD, File.class);
        if (runtimeAltDDFile != null && runtimeAltDDFile.exists() && runtimeAltDDFile.isFile()) {
            // if there are external runtime alternate deployment descriptor
            // specified, the config DD files are already processed
            return sortConfigurationDDFiles(ddFiles, archiveType, archive);
        }
        List<ConfigurationDeploymentDescriptorFile> processedConfDDFiles = new ArrayList<>();
        for (ConfigurationDeploymentDescriptorFile ddFile : sortConfigurationDDFiles(ddFiles, archiveType, archive)) {
            if (archive.exists(ddFile.getDeploymentDescriptorPath())) {
                processedConfDDFiles.add(ddFile);
            }
        }
        return processedConfDDFiles;
    }

    // read alternative runtime descriptor if there is an alternative runtime
    // DD packaged inside the archive
    public static void readAlternativeRuntimeDescriptor(ReadableArchive appArchive, ReadableArchive embeddedArchive, Archivist archivist, BundleDescriptor descriptor, String altDDPath) throws IOException, SAXException {
        String altRuntimeDDPath = null;
        ConfigurationDeploymentDescriptorFile confDD = null;
        @SuppressWarnings("unchecked")
        List<ConfigurationDeploymentDescriptorFile> archivistConfDDFiles = archivist.getConfigurationDDFiles();
        for (ConfigurationDeploymentDescriptorFile ddFile : sortConfigurationDDFiles(archivistConfDDFiles, archivist.getModuleType(), embeddedArchive)) {
            String ddPath = ddFile.getDeploymentDescriptorPath();
            if (ddPath.indexOf(DescriptorConstants.WLS) != -1 &&
                appArchive.exists(DescriptorConstants.WLS + altDDPath)) {
                // TODO: need to revisit this for WLS alt-dd pattern
                confDD = ddFile;
                altRuntimeDDPath = DescriptorConstants.WLS + altDDPath;
            } else if (ddPath.indexOf(DescriptorConstants.GF_PREFIX) != -1 &&
                appArchive.exists(DescriptorConstants.GF_PREFIX + altDDPath)) {
                confDD = ddFile;
                altRuntimeDDPath = DescriptorConstants.GF_PREFIX + altDDPath;
            } else if (ddPath.indexOf(DescriptorConstants.S1AS_PREFIX) != -1
                && appArchive.exists(DescriptorConstants.S1AS_PREFIX + altDDPath)){
                confDD = ddFile;
                altRuntimeDDPath = DescriptorConstants.S1AS_PREFIX + altDDPath;
            }
        }

        if (confDD != null && altRuntimeDDPath != null) {
            // found an alternative runtime DD file
            InputStream is = appArchive.getEntry(altRuntimeDDPath);
            confDD.setXMLValidation(
                archivist.getRuntimeXMLValidation());
            confDD.setXMLValidationLevel(
                archivist.getRuntimeXMLValidationLevel());
            if (appArchive.getURI()!=null) {
                confDD.setErrorReportingString(
                    appArchive.getURI().getSchemeSpecificPart());
            }

            confDD.read(descriptor, is);
            is.close();
            archivist.postRuntimeDDsRead(descriptor, embeddedArchive);
        } else {
            archivist.readRuntimeDeploymentDescriptor(embeddedArchive,descriptor);
        }
    }

    /**
     * Read the runtime deployment descriptors (can contained in one or
     * many file) set the corresponding information in the passed descriptor.
     * By default, the runtime deployment descriptors are all contained in
     * the xml file characterized with the path returned by
     *
     * @param confDDFiles the sorted configuration files for this archive
     * @param archive the archive
     * @param descriptor the initialized deployment descriptor
     * @param main the main archivist
     * @param warnIfMultipleDDs whether to log warnings if both the GlassFish and the legacy Sun descriptors are present
     */
    public static void readRuntimeDeploymentDescriptor(List<ConfigurationDeploymentDescriptorFile> confDDFiles, ReadableArchive archive, RootDeploymentDescriptor descriptor, Archivist main, final boolean warnIfMultipleDDs) throws IOException, SAXException {
        if (confDDFiles == null || confDDFiles.isEmpty()) {
            return;
        }
        ConfigurationDeploymentDescriptorFile confDD = confDDFiles.get(0);
        InputStream is = null;
        try {
            File runtimeAltDDFile = archive.getArchiveMetaData(
                DeploymentProperties.RUNTIME_ALT_DD, File.class);
            if (runtimeAltDDFile != null && runtimeAltDDFile.exists() && runtimeAltDDFile.isFile()) {
                is = new FileInputStream(runtimeAltDDFile);
            } else {
                is = archive.getEntry(confDD.getDeploymentDescriptorPath());
            }
            if (is == null) {
                return;
            }
            for (int i = 1; i < confDDFiles.size(); i++) {
                if (warnIfMultipleDDs) {
                    deplLogger.log(Level.WARNING,
                        COUNTERPART_CONFIGDD_EXISTS,
                        new Object[] {
                            confDDFiles.get(i).getDeploymentDescriptorPath(),
                            archive.getURI().getSchemeSpecificPart(),
                            confDD.getDeploymentDescriptorPath()});
                }
            }
            confDD.setErrorReportingString(archive.getURI().getSchemeSpecificPart());
            if (confDD.isValidating()) {
                confDD.setXMLValidation(main.getRuntimeXMLValidation());
                confDD.setXMLValidationLevel(main.getRuntimeXMLValidationLevel());
            } else {
                confDD.setXMLValidation(false);
            }
            confDD.read(descriptor, is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                }
            }
        }
    }


    public static void setExtensionArchivistForSubArchivist(ServiceLocator habitat, ReadableArchive archive, ModuleDescriptor md, Application app, Archivist subArchivist) {
        try {
            Collection<Sniffer> sniffers = getSniffersForModule(habitat, archive, md, app);
            ArchivistFactory archivistFactory = habitat.getService(ArchivistFactory.class);
            subArchivist.setExtensionArchivists(archivistFactory.getExtensionsArchivists(sniffers, subArchivist.getModuleType()));
        } catch (Exception e) {
            deplLogger.log(Level.WARNING,
                EXCEPTION_CAUGHT,
                new Object[] { e.getMessage(), e });
        }
    }

    // get sniffer list for sub modules of an ear application
    private static Collection<Sniffer> getSniffersForModule(ServiceLocator habitat, ReadableArchive archive, ModuleDescriptor md, Application app) throws Exception {
        ArchiveHandler handler = habitat.getService(ArchiveHandler.class, md.getModuleType().toString());
        SnifferManager snifferManager = habitat.getService(SnifferManager.class);
        List<URI> classPathURIs = handler.getClassPathURIs(archive);
        classPathURIs.addAll(getLibraryJarURIs(app, archive));
        Types types = archive.getParentArchive().getExtraData(Types.class);
        DeployCommandParameters parameters = archive.getParentArchive().getArchiveMetaData(DeploymentProperties.COMMAND_PARAMS, DeployCommandParameters.class);
        Properties appProps = archive.getParentArchive().getArchiveMetaData(DeploymentProperties.APP_PROPS, Properties.class);
        ExtendedDeploymentContext context = new DeploymentContextImpl(null, archive, parameters, habitat.<ServerEnvironment>getService(ServerEnvironment.class));
        if (appProps != null) {
            context.getAppProps().putAll(appProps);
        }
        context.setArchiveHandler(handler);
        context.addTransientAppMetaData(Types.class.getName(), types);
        Collection<Sniffer> sniffers = snifferManager.getSniffers(context, classPathURIs, types);
        context.postDeployClean(true);
        String type = getTypeFromModuleType(md.getModuleType());
        Sniffer mainSniffer = null;
        for (Sniffer sniffer : sniffers) {
            if (sniffer.getModuleType().equals(type)) {
                mainSniffer = sniffer;
            }
        }

        // if the sub module does not show characteristics of certain module
        // type, we should still use the application.xml defined module type
        // to add the appropriate sniffer
        if (mainSniffer == null) {
            mainSniffer = snifferManager.getSniffer(type);
            sniffers.add(mainSniffer);
        }

        String [] incompatibleTypes = mainSniffer.getIncompatibleSnifferTypes();
        List<String> allIncompatTypes = addAdditionalIncompatTypes(mainSniffer, incompatibleTypes);

        List<Sniffer> sniffersToRemove = new ArrayList<>();
        for (Sniffer sniffer : sniffers) {
            for (String incompatType : allIncompatTypes) {
                if (sniffer.getModuleType().equals(incompatType)) {
                    deplLogger.log(Level.WARNING,
                        INCOMPATIBLE_TYPE,
                        new Object[] { type,
                            md.getArchiveUri(),
                            incompatType });

                    sniffersToRemove.add(sniffer);
                }
            }
        }

        sniffers.removeAll(sniffersToRemove);

        // store the module sniffer information so we don't need to
        // recalculate them later
        Hashtable sniffersTable = archive.getParentArchive().getExtraData(Hashtable.class);
        if (sniffersTable == null) {
            sniffersTable = new Hashtable<String, Collection<Sniffer>>();
            archive.getParentArchive().setExtraData(Hashtable.class, sniffersTable);
        }
        sniffersTable.put(md.getArchiveUri(), sniffers);

        return sniffers;
    }

    /**
     * @return Sniffer/Container type for moduleType or null
     */
    private static String getTypeFromModuleType(ArchiveType moduleType) {
        if (moduleType.equals(DOLUtils.warType())) {
            return "web";
        } else if (moduleType.equals(DOLUtils.ejbType())) {
            return "ejb";
        } else if (moduleType.equals(DOLUtils.carType())) {
            return "appclient";
        } else if (moduleType.equals(DOLUtils.rarType())) {
            return "connector";
        }
        return null;
    }

    // this is to add additional incompatible sniffers at ear level where
    // we have information to determine what is the main sniffer
    private static List<String> addAdditionalIncompatTypes(Sniffer mainSniffer, String[] incompatTypes) {
        List<String> allIncompatTypes = new ArrayList<>();
        for (String incompatType : incompatTypes) {
            allIncompatTypes.add(incompatType);
        }
        if (mainSniffer.getModuleType().equals("appclient")) {
            allIncompatTypes.add("ejb");
        } else if (mainSniffer.getModuleType().equals("ejb")) {
            allIncompatTypes.add("appclient");
        }
        return allIncompatTypes;
    }

    public static List<ConfigurationDeploymentDescriptorFile> getConfigurationDeploymentDescriptorFiles(ServiceLocator habitat, String containerType) {
        List<ConfigurationDeploymentDescriptorFile> confDDFiles = new ArrayList<>();
        for (ServiceHandle<?> serviceHandle : habitat.getAllServiceHandles(ConfigurationDeploymentDescriptorFileFor.class)) {
            ActiveDescriptor<?> descriptor = serviceHandle.getActiveDescriptor();
            String indexedType = descriptor.getMetadata().get(ConfigurationDeploymentDescriptorFileFor.DESCRIPTOR_FOR).get(0);
            if(indexedType.equals(containerType)) {
                ConfigurationDeploymentDescriptorFile confDD = (ConfigurationDeploymentDescriptorFile) serviceHandle.getService();
                confDDFiles.add(confDD);
            }
        }
        return confDDFiles;
    }

    /**
     * Receives notiification of the value for a particular tag
     *
     * @param element the xml element
     * @param value it's associated value
     * @param o descriptor
     * @return true if the value has been processed, false if it is on the caller.
     */
    public static boolean setElementValue(XMLElement element, String value, Object o) {
        LOGGER.log(DEBUG, "setElementValue(element={0}, value={1}, o={2})", element, value, o);
        if (SCHEMA_LOCATION_TAG.equals(element.getCompleteName())) {
            // we need to keep all the non j2ee/javaee schemaLocation tags
            StringTokenizer st = new StringTokenizer(value);
            StringBuilder sb = new StringBuilder();
            while (st.hasMoreElements()) {
                String namespace = (String) st.nextElement();
                String schema;
                if (st.hasMoreElements()) {
                    schema = (String) st.nextElement();
                } else {
                    schema = namespace;
                    namespace = TagNames.JAKARTAEE_NAMESPACE;
                }
                if (namespace.equals(TagNames.J2EE_NAMESPACE)) {
                    continue;
                }
                if (namespace.equals(TagNames.JAVAEE_NAMESPACE)) {
                    continue;
                }
                if (namespace.equals(TagNames.JAKARTAEE_NAMESPACE)) {
                    continue;
                }
                if (namespace.equals(W3C_XML_SCHEMA)) {
                    continue;
                }
                sb.append(namespace);
                sb.append(' ');
                sb.append(schema);
            }
            String clientSchemaLocation = sb.toString();
            if (!clientSchemaLocation.isEmpty()) {
                if (o instanceof RootDeploymentDescriptor) {
                    ((RootDeploymentDescriptor) o).setSchemaLocation(clientSchemaLocation);
                }
            }
            return true;
        } else if (element.getQName().equals(TagNames.METADATA_COMPLETE)) {
            if (o instanceof BundleDescriptor) {
                ((BundleDescriptor) o).setFullAttribute(value);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns a list of the proprietary schema namespaces
     */
    public static List<String> getProprietarySchemaNamespaces() {
        ArrayList<String> ns = new ArrayList<>();
        ns.add(DescriptorConstants.WLS_SCHEMA_NAMESPACE_BEA);
        ns.add(DescriptorConstants.WLS_SCHEMA_NAMESPACE_ORACLE);
        return ns;
    }

    /**
     * Returns a list of the proprietary dtd system IDs
     */
    public static List<String> getProprietaryDTDStart() {
        ArrayList<String> ns = new ArrayList<>();
        ns.add(DescriptorConstants.WLS_DTD_SYSTEM_ID_BEA);
        return ns;
    }

    public static Application getApplicationFromEnv(JndiNameEnvironment env) {
        if (env instanceof EjbDescriptor) {
            // EJB component
            EjbDescriptor ejbEnv = (EjbDescriptor) env;
            return ejbEnv.getApplication();
        } else if (env instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor) env;
            return ejbBundle.getApplication();
        } else if (env instanceof WebBundleDescriptor) {
            WebBundleDescriptor webEnv = (WebBundleDescriptor) env;
            return webEnv.getApplication();
        } else if (env instanceof ApplicationClientDescriptor) {
            ApplicationClientDescriptor appEnv = (ApplicationClientDescriptor) env;
            return appEnv.getApplication();
        } else if (env instanceof ManagedBeanDescriptor) {
            ManagedBeanDescriptor mb = (ManagedBeanDescriptor) env;
            return mb.getBundle().getApplication();
        } else if (env instanceof Application) {
            return ((Application) env);
        } else {
            throw new IllegalArgumentException("IllegalJndiNameEnvironment : env");
        }
    }

    public static String getApplicationName(JndiNameEnvironment env) {
        final Application app = getApplicationFromEnv(env);
        if (app != null) {
            return app.getAppName();
        }
        throw new IllegalArgumentException("IllegalJndiNameEnvironment : env");
    }

    public static String getModuleName(JndiNameEnvironment env) {
        if (env instanceof EjbDescriptor) {
            // EJB component
            EjbDescriptor ejbEnv = (EjbDescriptor) env;
            EjbBundleDescriptor ejbBundle = ejbEnv.getEjbBundleDescriptor();
            return ejbBundle.getModuleDescriptor().getModuleName();
        } else if (env instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor) env;
            return ejbBundle.getModuleDescriptor().getModuleName();
        } else if (env instanceof WebBundleDescriptor) {
            WebBundleDescriptor webEnv = (WebBundleDescriptor) env;
            return webEnv.getModuleName();
        } else if (env instanceof ApplicationClientDescriptor) {
            ApplicationClientDescriptor appEnv = (ApplicationClientDescriptor) env;
            return appEnv.getModuleName();
        } else if (env instanceof ManagedBeanDescriptor) {
            ManagedBeanDescriptor mb = (ManagedBeanDescriptor) env;
            return mb.getBundle().getModuleName();
        } else {
            throw new IllegalArgumentException("Unsupported: " + env);
        }
    }

    public static boolean getTreatComponentAsModule(JndiNameEnvironment env) {
        if (env instanceof WebBundleDescriptor) {
            return true;
        }
        if (env instanceof EjbDescriptor) {
            EjbDescriptor ejbDesc = (EjbDescriptor) env;
            EjbBundleDescriptor ejbBundle = ejbDesc.getEjbBundleDescriptor();
            if (ejbBundle.getModuleDescriptor().getDescriptor() instanceof WebBundleDescriptor) {
                return true;
            }
        }
        return false;
    }


    /**
     * Generate a unique id name for each JEE component.
     *
     * @param env can be null, then method returns null too.
     * @return componentId
     */
    public static String getComponentEnvId(JndiNameEnvironment env) {
        LOGGER.log(DEBUG, "getComponentEnvId(env.class={0})", env.getClass().getName());
        if (env instanceof EjbDescriptor) {
            // EJB component
            EjbDescriptor ejbEnv = (EjbDescriptor) env;

            // Make jndi name flat so it won't result in the creation of
            // a bunch of sub-contexts.
            String flattedJndiName = ejbEnv.getJndiName().toString().replace('/', '.');

            EjbBundleDescriptor ejbBundle = ejbEnv.getEjbBundleDescriptor();
            Descriptor d = ejbBundle.getModuleDescriptor().getDescriptor();
            // if this EJB is in a war file, use the same component ID
            // as the web bundle, because they share the same JNDI namespace
            if (d instanceof WebBundleDescriptor) {
                // copy of code below
                WebBundleDescriptor webEnv = (WebBundleDescriptor) d;
                return webEnv.getApplication().getName() + ID_SEPARATOR + webEnv.getContextRoot();
            }
            return ejbEnv.getApplication().getName() + ID_SEPARATOR + ejbBundle.getModuleDescriptor().getArchiveUri()
                + ID_SEPARATOR + ejbEnv.getName() + ID_SEPARATOR + flattedJndiName + ejbEnv.getUniqueId();
        } else if (env instanceof WebBundleDescriptor) {
            WebBundleDescriptor webEnv = (WebBundleDescriptor) env;
            return webEnv.getApplication().getName() + ID_SEPARATOR + webEnv.getContextRoot();
        } else if (env instanceof ApplicationClientDescriptor) {
            ApplicationClientDescriptor appEnv = (ApplicationClientDescriptor) env;
            return "client" + ID_SEPARATOR + appEnv.getName() + ID_SEPARATOR + appEnv.getMainClassName();
        } else if (env instanceof ManagedBeanDescriptor) {
            SimpleJndiName jndiName = ((ManagedBeanDescriptor) env).getGlobalJndiName();
            return jndiName == null ? null : jndiName.toString();
        } else if (env instanceof EjbBundleDescriptor) {
            EjbBundleDescriptor ejbBundle = (EjbBundleDescriptor) env;
            return "__ejbBundle__" + ID_SEPARATOR + ejbBundle.getApplication().getName() + ID_SEPARATOR
                + ejbBundle.getModuleName();
        } else {
            return null;
        }
    }
}
