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

package org.glassfish.appclient.server.core.jws;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.appclient.server.core.AppClientDeployerHelper;
import org.glassfish.appclient.server.core.AppClientServerApplication;
import org.glassfish.appclient.server.core.jws.servedcontent.ASJarSigner;
import org.glassfish.appclient.server.core.jws.servedcontent.AutoSignedContent;
import org.glassfish.appclient.server.core.jws.servedcontent.Content;
import org.glassfish.appclient.server.core.jws.servedcontent.DynamicContent;
import org.glassfish.appclient.server.core.jws.servedcontent.FixedContent;
import org.glassfish.appclient.server.core.jws.servedcontent.SimpleDynamicContentImpl;
import org.glassfish.appclient.server.core.jws.servedcontent.StaticContent;
import org.glassfish.appclient.server.core.jws.servedcontent.StreamedAutoSignedStaticContent;
import org.glassfish.appclient.server.core.jws.servedcontent.TokenHelper;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.types.Property;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Encapsulates information related to Java Web Start support for a single
 * app client.
 * <p>
 * The AppClientServerApplication creates one instance of this class for each
 * app client that is deployed - either standalone or as part of an EAR.
 *
 * @author tjquinn
 */
@Service
@PerLookup
public class JavaWebStartInfo implements ConfigListener {

    @Inject
    private JWSAdapterManager jwsAdapterManager;

    @Inject
    private ASJarSigner jarSigner;

    @Inject
    private DeveloperContentHandler dch;

    @Inject
    private ServerEnvironment serverEnv;

    private AppClientServerApplication acServerApp;

    private Set<Content> myContent;

    private DeploymentContext dc;

    private TokenHelper tHelper;

    @LogMessagesResourceBundle
    public static final String APPCLIENT_SERVER_LOGMESSAGE_RESOURCE = "org.glassfish.appclient.server.LogMessages";

    @LoggerInfo(subsystem="SERVER", description="Appclient Server-side Logger", publish=true)
    public static final String APPCLIENT_SERVER_MAIN_LOGGER = "jakarta.enterprise.system.container.appclient";
    private static final Logger LOG = Logger.getLogger(APPCLIENT_SERVER_MAIN_LOGGER,
        APPCLIENT_SERVER_LOGMESSAGE_RESOURCE);

    @LogMessageInfo(
            message = "Java Web Start services started for the app client {0} (contextRoot: {1})",
            level = "INFO")
    public static final String JWS_STARTED = "AS-ACDEPL-00103";

    @LogMessageInfo(
            message = "Java Web Start services stopped for the app client {0}",
            level = "INFO")
    public static final String JWS_STOPPED = "AS_ACDEPL-00104";

    @LogMessageInfo(
            message = "Java Web Start services not started for the app client {0}; its developer has marked it as ineligible",
            cause = "The developer's glassfish-application-client.xml file marks the app client as ineligible for Java Web Start support.",
            action = "If users should be able to launch this client using Java Web Start, change the <java-web-start-support> 'enabled' attribute.")
    public static final String JWS_INELIGIBLE = "AS_ACDEPL-00101";

    @LogMessageInfo(
            message = "Java Web Start services not started for the app client {0}; the administrator has disabled Java Web Start support for it",
            cause = "The administrator disabled Java Web Start launches for the app client, either using '--properties java-web-start-enabled=false' during deployment or changing the properties afterwards.",
            action = "If users should be able to launch this client using Java Web Start, either deploy the application again without --properties or adjust the configuration using the admin console or the asadmin 'set' command")
    public static final String JWS_DISABLED = "AS_ACDEPL_00102";

    private VendorInfo vendorInfo;

    private String signingAlias;

    final private Map<String,StaticContent> staticContent = new HashMap<>();
    final private Map<String,DynamicContent> dynamicContent = new HashMap<>();

    private static final String JNLP_MIME_TYPE = "application/x-java-jnlp-file";

    public static final String DOC_TEMPLATE_PREFIX = "/org/glassfish/appclient/server/core/jws/templates/";

    private static final String MAIN_DOCUMENT_TEMPLATE =
            DOC_TEMPLATE_PREFIX + "appclientMainDocumentTemplate.jnlp";
    private static final String CLIENT_DOCUMENT_TEMPLATE =
            DOC_TEMPLATE_PREFIX + "appclientClientDocumentTemplate.jnlp";
    public static final String DEVELOPER_EXTENSION_DOCUMENT_TEMPLATE =
            DOC_TEMPLATE_PREFIX + "developerProvidedDocumentTemplate.jnlp";

    private static final String MAIN_IMAGE_XML_PROPERTY_NAME =
            "appclient.main.information.images";
    public static final String APP_LIBRARY_EXTENSION_PROPERTY_NAME = "app.library.extension";
    private static final String APP_CLIENT_MAIN_CLASS_ARGUMENTS_PROPERTY_NAME =
            "appclient.main.class.arguments";
    private static final String CLIENT_FACADE_JAR_PATH_PROPERTY_NAME =
            "client.facade.jar.path";
    private static final String CLIENT_JAR_PATH_PROPERTY_NAME =
            "client.jar.path";
    private static final String GROUP_FACADE_PATH_PROPERTY_NAME =
            "group.facade.jar.path";


    /**
     * records if the app client is eligible for Java Web Start support, as
     * defined in the developer-provided sun-application-client.xml descriptor
     */
    private boolean isJWSEligible;

    /**
     * records if the containing app is set to enable Java Web
     * Start access (in the domain.xml config for the application and the
     * module) - could be updated from a separate
     * thread if the administrator changes the java-web-start-enabled setting
     */
    private volatile boolean isJWSEnabledAtApp = true;
    private volatile boolean isJWSEnabledAtModule = true;

    private final JavaWebStartState jwsState = new JavaWebStartState();

    private static final String JAVA_WEB_START_ENABLED_PROPERTY_NAME = "java-web-start-enabled";

    private AppClientDeployerHelper helper;

    private ApplicationClientDescriptor acDesc;

    private String developerJNLPDoc;

    private static LocalStringsImpl servedContentLocalStrings =
            new LocalStringsImpl(TokenHelper.class);

    private static class SignedSystemContentFromApp {
        private final String tokenName;
        private final String relativePath;

        private SignedSystemContentFromApp(String tokenName, String relativePath) {
            this.tokenName = tokenName;
            this.relativePath = relativePath;
        }

        String getRelativePath() {
            return relativePath;
        }

        String getTokenName() {
            return tokenName;
        }

        URI getRelativePathURI() {
            return URI.create(relativePath);
        }
    }

    /**
     * Completes initialization of the object.  Should be invoked immediate
     * after the object is created by the habitat.
     *
     * @param acServerApp the per-client AppClientServerApplication object for the client of interest
     */
    public void init(final AppClientServerApplication acServerApp) {
        this.acServerApp = acServerApp;
        helper = acServerApp.helper();
        acDesc = acServerApp.getDescriptor();

        dc = acServerApp.dc();
        isJWSEligible = acDesc.getJavaWebStartAccessDescriptor().isEligible();
        isJWSEnabledAtApp = isJWSEnabled(dc.getAppProps());
        isJWSEnabledAtModule = isJWSEnabled(dc.getModuleProps());
        tHelper = TokenHelper.newInstance(helper, vendorInfo());
        final String devJNLPDoc = acDesc.getJavaWebStartAccessDescriptor().getJnlpDocument();
        final File sourceDir = acDesc.getApplication().isVirtual() ?
            dc.getSourceDir() : new File(dc.getSource().getParentArchive().getURI());
        this.developerJNLPDoc = devJNLPDoc;
        signingAlias = JWSAdapterManager.signingAlias(dc);
        dch.init(dc.getClassLoader(),
                    tHelper,
                    sourceDir,
                    dc.getSource(),
                    staticContent,
                    dynamicContent,
                    helper);
    }

    /**
     * Starts Java Web Start services for this client, if the client is
     * eligible (as decided by the developer) and enabled (as decided by the
     * administrator).
     */
    public void start() {
    /*
         * The developer might have disabled Java Web Start support in the
         * sun-application-client.xml or in the domain's configuration,
         * so check those before starting JWS services.
         */
        if (isJWSRunnable()) {
            jwsState.transition(JavaWebStartState.Action.START, new Runnable() {
                @Override
                public void run() {
                    try {
                        startJWSServices();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }
    }

    /**
     * Stops Java Web Start services for this client.
     */
    public void stop() {
        jwsState.transition(JavaWebStartState.Action.STOP, new Runnable() {
            @Override
            public void run() {
                try {
                    stopJWSServices();
                } catch (EndpointRegistrationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Suspends Java Web Start services for this client.
     */
    public void suspend() {
        jwsState.transition(JavaWebStartState.Action.SUSPEND, new Runnable() {
            @Override
            public void run() {
                suspendJWSServices();
            }
        });

    }

    /**
     * Resumes Java Web Start services for this client.
     */
    public void resume() {
        if (isJWSRunnable()) {
            jwsState.transition(JavaWebStartState.Action.RESUME, new Runnable() {
                @Override
                public void run() {
                    resumeJWSServices();
                }
            });
        }
    }


    static Document parseEntry(final ReadableArchive appClientArchive, final String pathToContent,
        final InputStreamToDocument parser) throws IOException, SAXException {
        final int bang = pathToContent.indexOf('!');
        if (bang == -1) {
            return parser.parse(appClientArchive.getEntry(pathToContent));
        }
        if (appClientArchive.getParentArchive() == null) {
            throw new IllegalArgumentException(pathToContent);
        }
        try (ReadableArchive subArchive = appClientArchive.getParentArchive()
            .getSubArchive(pathToContent.substring(0, bang))) {
            if (subArchive == null) {
                throw new FileNotFoundException(pathToContent);
            }
            return parser.parse(subArchive.getEntry(pathToContent.substring(bang + 1)));
        }
    }

    private void startJWSServices() throws EndpointRegistrationException, IOException {
        if (myContent == null) {
            myContent = addClientContentToHTTPAdapters();
        }

        /*
         * Currently, we implement the ability to disable or enable app clients
         * within an EAR by marking the associated content as disabled or
         * enabled, which the Grizzly adapter looks at before responding to
         * a request for that bit of content.  So mark all the content as
         * started.
         */
        for (Content c : myContent) {
            c.start();
        }

        LOG.log(Level.INFO, JWS_STARTED,
            new Object[] {acServerApp.moduleExpression(),
            JWSAdapterManager.userFriendlyContextRoot(acServerApp)});
    }

    private void stopJWSServices() throws EndpointRegistrationException {
        /*
         * Mark all this client's content as stopped so the Grizzly adapter
         * will not serve it.
         */
        for (Content c : myContent) {
            c.stop();
        }

        jwsAdapterManager.removeContentForAppClient(
                acServerApp.deployedAppName(),
                (acDesc.isStandalone() ? null : acDesc.getModuleName()),
                acServerApp);
        LOG.log(Level.INFO, JWS_STOPPED,
                acServerApp.moduleExpression());
    }

    private void suspendJWSServices() {
        for (Content c : myContent) {
            c.suspend();
        }
    }

    private void resumeJWSServices() {
        for (Content c : myContent) {
            c.resume();
        }
    }


    /**
     * Returns if this client is enabled for Java Web Start access.
     * <p>
     * The administrator can set the java-web-start-enabled property at
     * either the application level or the module level or both.  For this
     * client to be enabled, any such specified property must be set to true.
     * The default is true.
     */
    private boolean isJWSEnabled(final Properties props) {
        boolean result = true;
        final String propsSetting = props.getProperty(JAVA_WEB_START_ENABLED_PROPERTY_NAME);
        if (propsSetting != null) {
            result &= Boolean.parseBoolean(propsSetting);
        }
        return result;
    }

    private boolean isJWSEnabled() {
        return isJWSEnabledAtApp && isJWSEnabledAtModule;
    }

    private boolean isJWSRunnable() {
        if (!isJWSEligible) {
            LOG.log(Level.INFO, JWS_INELIGIBLE, acServerApp.moduleExpression());
        }

        if (!isJWSEnabled()) {
            LOG.log(Level.INFO, JWS_DISABLED, acServerApp.moduleExpression());
        }
        return isJWSEligible && isJWSEnabled();
    }

    private Set<Content> addClientContentToHTTPAdapters() throws EndpointRegistrationException, IOException {

        /*
         * NOTE - Be sure to initialize the static content first.  That method
         * assigns some properties that can appear as placeholders in the
         * dynamic content.
         */
        initClientStaticContent();

        initClientDynamicContent();

        dch.addDeveloperContentFromPath(developerJNLPDoc);

        Set<Content> result = new HashSet<>(staticContent.values());
        result.addAll(dynamicContent.values());

        jwsAdapterManager.addContentForAppClient(
                acServerApp.deployedAppName(),
                (acDesc.isStandalone() ? null : acDesc.getModuleName()),
                acServerApp, tHelper.tokens(),
                staticContent, dynamicContent);
        return result;
    }

    private void initClientStaticContent()
            throws IOException, EndpointRegistrationException {

        /*
         * The client-level adapter's static content is the app client JAR and
         * the app client facade.
         */
        createAndAddSignedContentFromAppFile(
                staticContent,
                helper.appClientServerURI(dc),
                helper.appClientUserURI(dc),
                CLIENT_JAR_PATH_PROPERTY_NAME,
                acServerApp.getDescriptor().getName());

        createAndAddSignedStaticContentFromMainJAR(
                staticContent,
                helper.facadeServerURI(dc),
                helper.facadeUserURI(dc),
                CLIENT_FACADE_JAR_PATH_PROPERTY_NAME);

        if ( ! acDesc.isStandalone()) {
            createAndAddSignedStaticContentFromGeneratedFile(
                    staticContent,
                    helper.groupFacadeServerURI(dc),
                    helper.groupFacadeUserURI(dc),
                    GROUP_FACADE_PATH_PROPERTY_NAME,
                    acServerApp.getDescriptor().getName());
        }

        /*
         * Make sure that there are versions of all GF system JARs
         * that are signed by the same cert used to sign the facade JAR for
         * this app.  That's because the user might have chosen to sign using
         * a particular alias so the end-users will accept JARs signed by
         * the corresponding cert.  (Java Web Start will prompt them to do this
         * during the download of signed JARs.)  Also, Java Web Start (as of
         * 1.6.0_19) warns users about apps which run boh signed and unsigned
         * code and segregates the two into different class loaders which
         * would not work for us.
         *
         * Note that the following logic makes sure that such signed versions
         * exist.  If multiple apps use the same cert to sign JARs, then the
         * multiple instances of AutoSignedContent class for the same signed
         * JAR will point to and reuse the same signed JAR, rather than
         * re-sign it each time an app needed it is started.
         */
        addSignedSystemContent();

        /*
         * The developer might have used the sun-application-client.xml
         * java-web-start-support/vendor setting to communicate icon and/or
         * splash screen images URIs.
         */
        prepareImageInfo(staticContent);

        for (Map.Entry<String,Map<URI,StaticContent>> signedEntry : helper.signingAliasToJar().entrySet()) {
            for (Map.Entry<URI,StaticContent> contentEntry : signedEntry.getValue().entrySet()) {
                staticContent.put(contentEntry.getKey().toASCIIString(), contentEntry.getValue());
            }
        }
    }

    private void addSignedSystemContent(
            ) throws FileNotFoundException, IOException {
        final List<String> systemJARRelativeURIs = new ArrayList<>();
        final Map<String,StaticContent> addedStaticContent =
                jwsAdapterManager.addStaticSystemContent(
                    systemJARRelativeURIs,
                    signingAlias);
        final Map<String,DynamicContent> addedDynContent =
                jwsAdapterManager.addDynamicSystemContent(
                    systemJARRelativeURIs,
                    signingAlias);
        jwsAdapterManager.addContentIfAbsent(addedStaticContent, addedDynContent);

        tHelper.setProperty("gf-client.jar", jwsAdapterManager.systemPathInClientJNLP(
                jwsAdapterManager.gfClientJAR().toURI(), signingAlias));
        tHelper.setProperty("gf-client-module.jar", jwsAdapterManager.systemPathInClientJNLP(
                jwsAdapterManager.gfClientModuleJAR().toURI(), signingAlias));
    }

    private void createAndAddSignedContentFromAppFile(final Map<String,StaticContent> content,
            final URI uriToFile,
            final URI uriForLookup,
            final String tokenName,
            final String appName) throws FileNotFoundException {

        final File unsignedFile = new File(uriToFile);
        final File signedFile = signedFileForProvidedAppFile(unsignedFile);
        createAndAddSignedStaticContent(content, unsignedFile, signedFile,
                uriForLookup, tokenName, appName);
    }

    private void createAndAddSignedStaticContentFromGeneratedFile(final Map<String,StaticContent> content,
            final URI uriToFile,
            final URI uriForLookup,
            final String tokenName,
            final String appName) throws FileNotFoundException {

        final File unsignedFile = new File(uriToFile);
        final File signedFile = signedFileForGeneratedAppFile(unsignedFile);
        createAndAddSignedStaticContent(content, unsignedFile, signedFile,
                uriForLookup, tokenName, appName);
    }

    private void createAndAddSignedStaticContent(
            final Map<String,StaticContent> content,
            final File unsignedFile,
            final File signedFile,
            final URI uriForLookup,
            final String tokenName,
            final String appName
            ) throws FileNotFoundException {
        final StaticContent signedJarContent = createSignedStaticContent(
                unsignedFile, signedFile, uriForLookup, appName);
        recordStaticContent(content, signedJarContent, uriForLookup, tokenName);
    }

    private StaticContent createSignedStaticContent(
            final File unsignedFile,
            final File signedFile,
            final URI uriForLookup,
            final String appName) throws FileNotFoundException {
        mkdirs(signedFile.getParentFile());
        final StaticContent signedJarContent = new AutoSignedContent(
                unsignedFile,
                signedFile,
                signingAlias,
                jarSigner,
                uriForLookup.toASCIIString(),
                appName);
        return signedJarContent;
    }

    private void createAndAddSignedStaticContentFromMainJAR(
            final Map<String,StaticContent> content,
            final URI uriToFile,
            final URI uriForLookup,
            final String tokenName) throws FileNotFoundException {
        final File unsignedFile = new File(uriToFile);
        final StaticContent signedContent = new StreamedAutoSignedStaticContent(unsignedFile, signingAlias, jarSigner,
                uriForLookup.toASCIIString(), acServerApp.getDescriptor().getName());
        recordStaticContent(content, signedContent, uriForLookup, tokenName);
    }

    private void recordStaticContent(final Map<String,StaticContent> content,
            final StaticContent newContent,
            final URI uriForLookup,
            final String tokenName) {

        final String uriStringForLookup = uriForLookup.toASCIIString();
        recordStaticContent(content, newContent, uriStringForLookup);
        if (tokenName != null) {
            tHelper.setProperty(tokenName, uriForLookup.toASCIIString());
        }
    }

    private void recordStaticContent(final Map<String,StaticContent> content,
            final StaticContent newContent,
            final String uriStringForLookup) {
        content.put(uriStringForLookup, newContent);
        LOG.log(Level.FINE, "Recording static content: URI for lookup = {0}; content = {1}",
                new Object[]{uriStringForLookup, newContent.toString()});
    }

    public static URI relativeURIForProvidedOrGeneratedAppFile(
            final DeploymentContext dc, final URI absURI, AppClientDeployerHelper helper) {
        URI possiblyRelativeURI = rootForSignedFilesInApp(helper).relativize(absURI);
        if ( ! possiblyRelativeURI.isAbsolute()) {
            return possiblyRelativeURI;
        }

        /*
         * The file could be a generated JAR file for a submodule in a
         * directory-deployed app, in which case the URI is within the EAR's
         * generated subdirectory.
         */
        possiblyRelativeURI = rootForGeneratedSubmoduleJAR(dc, helper).relativize(absURI);
        if ( ! possiblyRelativeURI.isAbsolute()) {
            return possiblyRelativeURI;
        }

        return helper.URIWithinAppDir(dc, absURI);
    }

    private static URI rootForSignedFilesInApp(final AppClientDeployerHelper helper) {
        return helper.rootForSignedFilesInApp().toURI();
    }

    /**
     * Returns an absolute URI for the root directory that contains JARs
     * for submodules that are generated from a directory deployment submodule
     * directory.
     * @param dc the deployment context for the app client deployment underway
     * @return absolute URI to the generated submodule JAR root directory
     */
    private static URI rootForGeneratedSubmoduleJAR(final DeploymentContext dc,
            final AppClientDeployerHelper helper) {
        final File f = new File(dc.getScratchDir("xml").getParentFile(),
                NamingConventions.anchorSubpathForNestedClient(helper.appName(dc)));
        return f.toURI();
    }

    private File signedFileForGeneratedAppFile(final File unsignedFile) {
        /*
         * Signed files at the app level go in
         *
         * generated/xml/(appName)/signed/(path-within-app-of-unsigned-file)
         *
         * and when we're signing a generated file we just use its URI
         * relative to the app's scratch directory to compute the URI relative
         * to generated/xml/(appName)/signed where the signed file should reside.
         */
        final File rootForSignedFilesInApp = helper.rootForSignedFilesInApp();
        mkdirs(rootForSignedFilesInApp);
        final URI unsignedFileURIRelativeToXMLDir = dc.getScratchDir("xml").getParentFile().toURI().
                relativize(unsignedFile.toURI());
        final URI signedFileURI = rootForSignedFilesInApp.toURI().resolve(unsignedFileURIRelativeToXMLDir);
        return new File(signedFileURI);
    }

    public File signedFileForProvidedAppFile(final File unsignedFile) {
        return signedFileForProvidedAppFile(helper.appClientURIWithinApp(dc),
                unsignedFile, helper, dc);
    }

    public static File signedFileForProvidedAppFile(final URI relURI,
            final File unsignedFile,
            final AppClientDeployerHelper helper,
            final DeploymentContext dc) {
         // Place a signed file for a developer-provided file at
        // generated/xml/(appName)/signed/(path-within-app-of-unsigned-file)
        final File rootForSignedFilesInApp = helper.rootForSignedFilesInApp();
        mkdirs(rootForSignedFilesInApp);
        final URI signedFileURI = rootForSignedFilesInApp.toURI().resolve(relURI);
        return new File(signedFileURI);
    }

    private void initClientDynamicContent() throws IOException {

        helper.createAndAddLibraryJNLPs(helper, tHelper, dynamicContent);

        tHelper.setProperty(APP_CLIENT_MAIN_CLASS_ARGUMENTS_PROPERTY_NAME, "");

        final String mainDocument = dch.combineJNLP(
                    textFromURL(MAIN_DOCUMENT_TEMPLATE),
                    developerJNLPDoc);
        createAndAddDynamicContentFromTemplateText(
                dynamicContent, tHelper.mainJNLP(), mainDocument, true /* isMain */);

        /*
         * Add the main JNLP again but with an empty URI string so the user
         * can launch the app client by specifying only the context root.
         */
        createAndAddDynamicContentFromTemplateText(dynamicContent, "", mainDocument, true /* isMain */);
        createAndAddDynamicContent(
                dynamicContent, tHelper.clientJNLP(), CLIENT_DOCUMENT_TEMPLATE);

    }

    public static void createAndAddDynamicContent(
            final TokenHelper tHelper,
            final Map<String,DynamicContent> content,
            final String uriStringForContent,
            final String uriStringForTemplate) throws IOException {
        createAndAddDynamicContentFromTemplateText(
                tHelper, content, uriStringForContent,
                textFromURL(uriStringForTemplate), false /* isMain */);
    }

    private void createAndAddDynamicContent(
            final Map<String,DynamicContent> content,
            final String uriStringForContent,
            final String uriStringForTemplate) throws IOException {
        createAndAddDynamicContentFromTemplateText(
                content, uriStringForContent,
                textFromURL(uriStringForTemplate));
    }

    private void createAndAddDynamicContentFromTemplateText(
            final Map<String,DynamicContent> content,
            final String uriStringForContent,
            final String templateText) throws IOException {
        createAndAddDynamicContentFromTemplateText(content, uriStringForContent, templateText, false /* isMain */);
    }

    private void createAndAddDynamicContentFromTemplateText(
            final Map<String,DynamicContent> content,
            final String uriStringForContent,
            final String templateText,
            final boolean isMain) throws IOException {
        createAndAddDynamicContentFromTemplateText(tHelper,
                content, uriStringForContent, templateText, isMain);
    }

    private static void createAndAddDynamicContentFromTemplateText(
            final TokenHelper tHelper,
            final Map<String,DynamicContent> content,
            final String uriStringForContent,
            final String templateText,
            final boolean isMain) throws IOException {
        final String processedTemplate = Util.replaceTokens(
                templateText, tHelper.tokens());
        content.put(uriStringForContent, newDynamicContent(processedTemplate,
                JNLP_MIME_TYPE, isMain));
        LOG.log(Level.FINE, "Adding dyn content {0}{1}{2}",
                new Object[]{uriStringForContent,
                    System.getProperty("line.separator"), LOG.isLoggable(Level.FINER) ? processedTemplate : ""});
    }

    private static DynamicContent newDynamicContent(final String template,
            final String mimeType, final boolean isMain) {
        return new SimpleDynamicContentImpl(template, mimeType, isMain);
    }

    /**
     * Prepares XML (for the generated JNLP) and the static content
     * for the icon image, the splash screen image, neither, or
     * both, depending on the contents (if any) of the <vendor> text in the
     * developer-provided sun-application-client.xml descriptor.
     */
    private void prepareImageInfo(final Map<String,StaticContent> staticContent) throws IOException {

        /*
         * Deployment has already expanded the app client module into a
         * directory, so each image entry of the JAR which the developer
         * specified in the descriptor should already reside as a
         * file on the disk within the DeploymentContext.getSource() directory.
         */
        addImageContentIfSpecified(vendorInfo().getImageURI(),
                vendorInfo().JNLPImageURI(), staticContent);
        addImageContentIfSpecified(vendorInfo().getSplashImageURI(),
                vendorInfo().JNLPSplashImageURI(), staticContent);
    }

    private void addImageContentIfSpecified(
            final String imageURIStringWithinAppClient,
            final String imageURIStringForJNLP,
            final Map<String,StaticContent> staticContent) {

        if (imageURIStringWithinAppClient == null ||
                imageURIStringWithinAppClient.length() == 0) {
            return;
        }

        final URI absoluteImageURI = dc.getSource().getURI().resolve(imageURIStringWithinAppClient);
        final File imageFile = new File(absoluteImageURI);
        if ( ! imageFile.exists()) {
            return;
        }

        staticContent.put(imageURIStringForJNLP,
                new FixedContent(imageFile));
    }


    private VendorInfo vendorInfo() {
        if (vendorInfo == null) {
            vendorInfo = new VendorInfo(
                    acDesc.getJavaWebStartAccessDescriptor().getVendor(),
                    helper.pathToAppclientWithinApp(dc));
        }
        return vendorInfo;
    }

    public static class VendorInfo {
        private final String vendorStringFromDescriptor;
        private String vendor = "";
        private String imageURIString = "";
        private String splashImageURIString = "";
        private final String JNLPPathFullPrefix;

        public VendorInfo(String vendorStringFromDescriptor,
                final String JNLPPathPrefix) {
            this.JNLPPathFullPrefix = "__content/" + JNLPPathPrefix;
            this.vendorStringFromDescriptor = vendorStringFromDescriptor != null ?
                vendorStringFromDescriptor : "";
            String [] parts = this.vendorStringFromDescriptor.split("::");
            if (parts.length == 1) {
                vendor = parts[0];
            } else if (parts.length == 2) {
                imageURIString = parts[0];
                vendor = parts[1];
            } else if (parts.length == 3) {
                imageURIString = parts[0];
                splashImageURIString = parts[1];
                vendor = parts[2];
            }
            if (vendor.length() == 0) {
                vendor = servedContentLocalStrings.get("jws.defaultVendorName");
            }
        }

        public String getVendor() {
            return vendor;
        }

        public String getImageURI() {
            return imageURIString;
        }

        public String getSplashImageURI() {
            return splashImageURIString;
        }

        public String JNLPImageURI() {
            return (imageURIString.length() > 0) ?
                JNLPPathFullPrefix + imageURIString : "";
        }

        public String JNLPSplashImageURI() {
            return (splashImageURIString.length() > 0) ?
                JNLPPathFullPrefix + splashImageURIString : "";
        }
    }

    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {
        /* Record any events we tried to process but could not. */
        List<UnprocessedChangeEvent> unprocessedEvents = new ArrayList<>();

        for (PropertyChangeEvent event : events) {
            try {
                processChangeEventIfInteresting(event);
            } catch (Exception e) {
                UnprocessedChangeEvent uce =
                        new UnprocessedChangeEvent(event, e.getLocalizedMessage());
                unprocessedEvents.add(uce);
            }
        }

        return (unprocessedEvents.size() > 0) ? new UnprocessedChangeEvents(unprocessedEvents) : null;
    }

    private void processChangeEventIfInteresting(final PropertyChangeEvent event) throws EndpointRegistrationException {
        /*
         * If the source is of type Application or Module and the newValue is of type
         * Property then this could be a change we're interested in.
         */
        final boolean isSourceApp = event.getSource() instanceof
                com.sun.enterprise.config.serverbeans.Application;
        final boolean isSourceModule = event.getSource() instanceof
                com.sun.enterprise.config.serverbeans.Module;

        if (     (! isSourceApp && ! isSourceModule)
              || ! (event.getNewValue() instanceof Property)) {
            return;
        }

        /*
         * Make sure the property name is java-web-start-enabled.
         */
        Property newPropertySetting = (Property) event.getNewValue();
        if ( ! newPropertySetting.getName().equals(JAVA_WEB_START_ENABLED_PROPERTY_NAME)) {
            return;
        }

        String eventSourceName;
        String thisAppOrModuleName;
        if (isSourceApp) {
            eventSourceName = ((com.sun.enterprise.config.serverbeans.Application) event.getSource()).getName();
            thisAppOrModuleName = acServerApp.registrationName();
        } else {
            eventSourceName = ((com.sun.enterprise.config.serverbeans.Module) event.getSource()).getName();
            thisAppOrModuleName = acDesc.getModuleName();
        }

        if ( ! thisAppOrModuleName.equals(eventSourceName)) {
            return;
        }

        /*
         * At this point we know that the event applies to this app client,
         * so return a Boolean carrying the newly-assigned value.
         */
        final Boolean newEnabledValue = Boolean.valueOf(newPropertySetting.getValue());
        final Property oldPropertySetting = (Property) event.getOldValue();
        final String oldPropertyValue = (oldPropertySetting != null)
                ? oldPropertySetting.getValue()
                : null;
        final Boolean oldEnabledValue = (oldPropertyValue == null
                ? Boolean.TRUE
                : Boolean.valueOf(oldPropertyValue));

        /*
         * Record the new value of the relevant enabled setting.
         */
        if (isSourceApp) {
            isJWSEnabledAtApp = newEnabledValue;
                } else {
            isJWSEnabledAtModule = newEnabledValue;
        }

        /*
         * Now act on the change of state.
         */
        if ( ! newEnabledValue.equals(oldEnabledValue)) {
            if (newEnabledValue) {
                start();
            } else {
                stop();
            }
        }
    }

    public static String textFromURL(final String templateURLString) throws IOException {
        final InputStream is = AppClientServerApplication.class.getResourceAsStream(templateURLString);
        if (is == null) {
            throw new FileNotFoundException(templateURLString);
        }
        StringBuilder sb = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(is);
        char[] buffer = new char[1024];
        int charsRead;
        try {
            while ((charsRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, charsRead);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new IOException(templateURLString, e);
        } finally {
            try {
                reader.close();
            } catch (IOException ignore) {
                throw new IOException("Error closing template stream after error", ignore);
            }
        }

    }

    private static void mkdirs(final File dir) {
        if ( ! dir.exists()) {
            if ( ! dir.mkdirs()) {
                final String msg = LOG.getResourceBundle().getString("enterprise.deployment.appclient.errormkdirs");
                throw new RuntimeException(MessageFormat.format(msg, dir.getAbsolutePath()));
            }
        }
    }

    @FunctionalInterface
    interface InputStreamToDocument {
        Document parse(InputStream t) throws IOException, SAXException;
    }
}
