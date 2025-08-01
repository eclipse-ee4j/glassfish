/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.adapter;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.v3.admin.AdminConsoleConfigUpgrade;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.io.OutputBuffer;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.types.Property;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * An HK-2 Service that provides the functionality so that admin console access is handled properly.
 *
 * <p>The general contract of this adapter is as follows:
 * <ol>
 * <li>This adapter is <strong>always</strong> installed as a Grizzly adapter for a particular  URL designated
 * as admin URL in {@code domain.xml}. This translates to {@code context-root} of admin console application.
 * </li>
 * <li>When the control comes to the adapter for the first time, the admin console application is downloaded
 * and expanded. While the download and installation is happening, all the clients or browser refreshes get
 * a status message. No push from the server side is attempted (yet). After the application is "installed",
 * {@code ApplicationLoaderService} is contacted, so that the application is loaded by the containers. This
 * application is available as a {@code system-application} and is persisted as such in the {@code domain.xml}.
 * </li>
 * <li>Even after this application is available, we don't load it on server startup by default. It is always
 * loaded {@code on demand}. Hence, this adapter will always be available to find out if application is loaded
 * and load it in the container(s) if it is not. If the application is already loaded, it simply exits.
 * </li>
 * </ol>
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352; (km@dev.java.net)
 * @author Ken Paulsen (kenpaulsen@dev.java.net)
 * @author Siraj Ghaffar (sirajg@dev.java.net)
 *
 * @since GlassFish V3 (March 2008)
 */
@Service
public final class AdminConsoleAdapter extends HttpHandler implements Adapter, EventListener {

    private static final Logger logger = KernelLoggerInfo.getLogger();

    private static final String TEST_BACKEND_IS_READY = "/testifbackendisready.html";

    // Don't change the following without changing the html status page
    private static final String STATUS_TOKEN = "%%%STATUS%%%";

    private static final String RESOURCE_PACKAGE = "com/sun/enterprise/v3/admin/adapter";

    private static final Set<Method> allowedHttpMethods = Set.of(Method.GET, Method.POST, Method.HEAD, Method.DELETE, Method.PUT);

    @Inject
    private ServerEnvironmentImpl serverEnvironment;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private AdminService adminService;

    @Inject
    private Domain domain;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config serverConfig;

    @Inject
    private Events events;

    @Inject
    private ServiceLocator serviceLocator;

    private final CountDownLatch serverReady = new CountDownLatch(1);

    private AdminConsoleConfigUpgrade adminConsoleConfigUpgrade;
    private AdminEndpointDecider endpointDecider;
    private ResourceBundle resourceBundle;
    private String contextRoot;
    private Path warFile; // GF Admin Console War File Location
    private ConsoleLoadingOption loadingOption = ConsoleLoadingOption.DEFAULT;
    private volatile AdapterState stateMsg = AdapterState.UNINITIALIZED;
    private volatile boolean installing;
    private boolean isRegistered;
    private volatile boolean isRestStarted;
    private volatile boolean isRestBeingStarted;

    /**
     *  Returns Admin console context root.
     */
    @Override
    public String getContextRoot() {
        // Default is /admin
        return endpointDecider.getGuiContextRoot();
    }

    @Override
    public HttpHandler getHttpService() {
        return this;
    }

    /**
     *
     */
    @Override
    public void service(Request request, Response response) {
        resourceBundle = getResourceBundle(request.getLocale());

        Method method = request.getMethod();
        if (!checkHttpMethodAllowed(method)) {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            response.setHeader(Header.Allow, getAllowedHttpMethodsAsString());
            return;
        }

        if (!serverEnvironment.isDas()) {
            sendStatusNotAvailable(response, "statusNotDAS.html");
            return;
        }

        if (loadingOption == ConsoleLoadingOption.NEVER) {
            sendStatusNotAvailable(response, "statusDisabled.html");
            return;
        }

        //This is needed to support the case where user update from release prior to 3.1.
        if (adminConsoleConfigUpgrade == null) {
            adminConsoleConfigUpgrade = serviceLocator.getService(AdminConsoleConfigUpgrade.class);
        }

        try {
            if (!serverReady.await(100L, TimeUnit.SECONDS)) {
                logger.log(Level.SEVERE, KernelLoggerInfo.consoleRequestTimeout);
                return;
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.consoleCannotProcess);
            return;
        }

        logRequest(request);

        if (isResourceRequest(request)) {
            try {
                handleResourceRequest(request, response);
            } catch (IOException e) {
                logger.log(Level.SEVERE, KernelLoggerInfo.consoleResourceError, new Object[] {request.getRequestURI(), e});
                logger.log(Level.FINE, e, e::toString);
            }
            return;
        }

        response.setContentType("text/html; charset=UTF-8");

        // Simple get request use via javascript to give back the console status
        // as a simple string (starting with :::).
        // See usage in status.html

        String serverVersion = Version.getProductIdInfo();

        if (TEST_BACKEND_IS_READY.equals(request.getRequestURI())) {
            // Replace state token
            String status;
            try {
                // Try to get a localized version of this key
                status = resourceBundle.getString(getStateMsg().getI18NKey());
            } catch (MissingResourceException e) {
                // Use the non-localized String version of the status
                status = getStateMsg().toString();
            }

            String welcomeKey = AdapterState.WELCOME_TO.getI18NKey();
            try {
                // Try to get a localized version of this key
                serverVersion = resourceBundle.getString(welcomeKey) + " " + serverVersion + ".";
            } catch (MissingResourceException e) {
                // Use the non-localized String version of the status
                serverVersion = AdapterState.WELCOME_TO + " " + serverVersion + ".";
            }

            status += "\n" + serverVersion;

            try {
                OutputBuffer outputBuffer = getOutputBuffer(response);

                byte[] bytes = (":::" + status).getBytes(UTF_8);
                response.setContentLength(bytes.length);
                outputBuffer.write(bytes);
                outputBuffer.flush();
            } catch (IOException e) {
                logger.log(Level.SEVERE, KernelLoggerInfo.consoleResourceError, e);
            }

            return;
        }

        if (!isApplicationLoaded()) {
            // If the admin console is not loaded, and someone use the REST access,
            // browsers also request the favicon icon. Since we do not want to load
            // the admin gui just to return a non-existing icon, we just return without
            // loading the entire console...
            if ("/favicon.ico".equals(request.getRequestURI())) {
                return;
            }

            if (!isRestStarted) {
                forceRestModuleLoad();
            }

            synchronized(this) {
                if (isInstalling()) {
                    sendStatusPage(response);
                } else {
                    // Double check here that it is not yet loaded
                    // (not likely, but possible)
                    if (!isApplicationLoaded()) {
                        loadConsole();
                        sendStatusPage(response);
                    }
                }
            }
        }
    }

    void loadConsole() {
        try {
            // We have permission and now we should install
            // (or load) the application.
            setInstalling(true);
            // Thread must set installing false
            startThread();
        } catch (Exception e) {
            // Ensure we haven't crashed with the installing
            // flag set to true (not likely).
            setInstalling(false);
            throw new RuntimeException("Unable to install Admin Console!", e);
        }
    }

    /**
     * Checks if the request is for a resource with a known content type.
     *
     * @param request the {@link Request}
     * @return {@code true} if the request is for a resource with a known content type, {@code false} otherwise.
     */
    private boolean isResourceRequest(Request request) {
        return getContentType(request.getRequestURI()) != null;
    }

    /**
     * All that needs to happen for the REST module to be initialized is a request of some sort.
     * Here, we don't care about the response, so we make the request then close the stream and move on.
     */
    private void forceRestModuleLoad() {
        if (isRestBeingStarted) {
            return;
        }

        isRestBeingStarted = true;

        Thread thread = new Thread("Force REST Module Load Thread") {
            @Override
            public void run() {
                initRest();
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    private String getContentType(String resource) {
        if (resource == null || resource.isEmpty()) {
            return null;
        }

        // This may need to be expanded upon the future, in which case,
        // the current implementation may not be worth maintaining

        if (resource.endsWith(".gif")) {
            return "image/gif";
        }

        if (resource.endsWith(".jpg")) {
            return "image/jpeg";
        }

        logger.log(Level.FINE, () -> "Unhandled content type: " + resource);

        return null;
    }

    private void handleResourceRequest(Request request, Response response) throws IOException {
        String resourcePath = RESOURCE_PACKAGE + request.getRequestURI();
        ClassLoader loader = AdminConsoleAdapter.class.getClassLoader();

        try (InputStream resourceStream = loader.getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                logger.log(Level.WARNING, KernelLoggerInfo.consoleResourceNotFound, resourcePath);
                return;
            }

            byte[] bytes = resourceStream.readAllBytes();

            String contentType = getContentType(resourcePath);
            if (contentType != null) {
                response.setContentType(contentType);
            }
            response.setContentLength(bytes.length);

            OutputStream out = response.getOutputStream();
            out.write(bytes);
            out.flush();
        }
    }

    boolean isApplicationLoaded() {
        return stateMsg == AdapterState.APPLICATION_LOADED;
    }

    /**
     *  Checks if admin console installation is in progress.
     */
    boolean isInstalling() {
        return installing;
    }

    /**
     *  Sets {@code installing} flag.
     */
    void setInstalling(boolean installing) {
        this.installing = installing;
    }

    /**
     * Checks whether this adapter has been registered as a network endpoint.
     */
    @Override
    public boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Marks this adapter as having been registered or unregistered as a network endpoint.
     */
    @Override
    public void setRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    /**
     * Sets the current state.
     */
    void setStateMsg(AdapterState stateMsg) {
        this.stateMsg = stateMsg;
        logger.log(Level.FINE, stateMsg::toString);
    }

    /**
     * This method returns the current state, which will be one of the valid values
     * defined by {@link AdapterState}.
     */
    AdapterState getStateMsg() {
        return stateMsg;
    }

    ConsoleLoadingOption getLoadingOption() {
        return loadingOption;
    }

    @PostConstruct
    public void postConstruct() {
        events.register(this);

        // Set up the environment properly
        init();
    }

    /**
     *
     */
    @Override
    public void event(@RestrictTo(EventTypes.SERVER_READY_NAME) Event<?> event) {
        serverReady.countDown();
        if (logger != null) {
            logger.log(Level.FINE, "AdminConsoleAdapter is ready.");
        }
    }

    /**
     *  Set up the environment.
     */
    private void init() {
        // Get loading option
        Property loadingOptionProperty = adminService.getProperty(ServerTags.ADMIN_CONSOLE_STARTUP);
        if (loadingOptionProperty != null) {
            String loadingOptionValue = loadingOptionProperty.getValue();
            if (loadingOptionValue != null) {
                try {
                    loadingOption = ConsoleLoadingOption.valueOf(loadingOptionValue.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "AdminConsoleAdapter: Illegal console loading option \"{0}\"", loadingOptionValue);
                }
            }
        }

        Property locationProperty = adminService.getProperty(ServerTags.ADMIN_CONSOLE_DOWNLOAD_LOCATION);
        if (locationProperty == null || locationProperty.getValue() == null || locationProperty.getValue().isEmpty()) {
            warFile = Path.of(System.getProperty(INSTALL_ROOT.getSystemPropertyName()),
                "lib/install/applications/admingui.war");
            writeAdminServiceProperty(ServerTags.ADMIN_CONSOLE_DOWNLOAD_LOCATION,
                INSTALL_ROOT.toExpression() + "/lib/install/applications/admingui.war");
        } else {
            // For any non-absolute path, we start from the installation, ie glassfish8
            // eg, v3 prelude upgrade, where the location property was "glassfish/lib..."
            String locationValue = locationProperty.getValue();
            warFile = Path.of(locationValue);
            if (!warFile.isAbsolute()) {
                warFile = Path.of(System.getProperty(INSTALL_ROOT.getSystemPropertyName()))
                    .resolveSibling(locationValue);
            }
        }

        logger.log(Level.FINE, () -> "Admin Console download location: " + warFile.toAbsolutePath());

        initState();

        try {
            endpointDecider = new AdminEndpointDecider(serverConfig);
            contextRoot = endpointDecider.getGuiContextRoot();
        } catch (Exception e) {
            logger.log(Level.INFO, KernelLoggerInfo.consoleCannotInitialize, e);
        }
    }

    void initRest() {
        try {
            NetworkListener adminListener = domain.getServerNamed("server").getConfig().getNetworkConfig()
                    .getNetworkListener(ServerTags.ADMIN_LISTENER_ID);
            SecureAdmin secureAdmin = serviceLocator.getService(SecureAdmin.class);

            URL url = new URL(SecureAdmin.isEnabled(secureAdmin) ? "https" : "http",
                    adminListener.getAddress(), Integer.parseInt(adminListener.getPort()), "/management/domain");
            URLConnection connection = url.openConnection();
            try (InputStream ignored = connection.getInputStream()) {
                isRestStarted = true;
            }
        } catch (Exception e) {
           logger.log(Level.FINE, null, e);
        }
    }


    /**
     *
     */
    private void initState() {
        // It is a given that the application is NOT loaded to begin with
        if (appExistsInConfig()) {
            setStateMsg(AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED);
        } else {
            Path explodedWar = warFile.resolveSibling(ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME);
            if (Files.exists(explodedWar) || Files.exists(warFile)) {
                // The exploded dir, or the .war exists. Mark as downloaded.
                setStateMsg(AdapterState.DOWNLOADED);
            } else {
                setStateMsg(AdapterState.APPLICATION_NOT_INSTALLED);
            }
        }
    }

    /**
     *  Checks if console application exists in {@code domain.xml}
     */
    private boolean appExistsInConfig() {
        return getConfig() != null;
    }

    Application getConfig() {
        // No application-ref logic here - that's on purpose for now
        return domain.getSystemApplicationReferencedFrom(serverEnvironment.getInstanceName(),
                ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME);
    }

    private void logRequest(Request request) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "AdminConsoleAdapter''s STATE IS: {0}", getStateMsg());
            logger.log(Level.FINE, "Current Thread: {0}", Thread.currentThread().getName());
            for (final String name : request.getParameterNames()) {
                final String values = Arrays.toString(request.getParameterValues(name));
                logger.log(Level.FINE, "Parameter name: {0} values: {1}", new Object[] {name, values});
            }
        }
    }

    /**
     * Starts console installation in the separate thread.
     */
    private void startThread() {
        new InstallerThread(this, serviceLocator, domain,
                serverEnvironment, contextRoot, endpointDecider.getGuiHosts()).start();
    }

    private OutputBuffer getOutputBuffer(Response response) {
        response.setStatus(HttpStatus.ACCEPTED_202);
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        return response.getOutputBuffer();
    }

    private void sendStatusPage(Response response) {
        try {
            OutputBuffer outputBuffer = getOutputBuffer(response);

            String html = loadResource("status.html");
            // Replace locale specific Strings
            String localHtml = replaceTokens(html, resourceBundle);

            // Replace state token
            String status = getStateMsg().getI18NKey();
            try {
                // Try to get a localized version of this key
                status = resourceBundle.getString(status);
            } catch (MissingResourceException e) {
                // Use the non-localized String version of the status
                status = getStateMsg().toString();
            }

            byte[] bytes = localHtml.replace(STATUS_TOKEN, status).getBytes(UTF_8);
            response.setContentLength(bytes.length);
            outputBuffer.write(bytes);
            outputBuffer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendStatusNotAvailable(Response response, String statusPage) {
        try {
            OutputBuffer outputBuffer = getOutputBuffer(response);

            String html = loadResource(statusPage);
            String localHtml = replaceTokens(html, resourceBundle);

            byte[] bytes = localHtml.getBytes(UTF_8);
            response.setContentLength(bytes.length);
            outputBuffer.write(bytes);
            outputBuffer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the given file in this package and returns it as a String.
     * If there is any problem in reading an IOException is thrown.
     *
     * @param name representing just the complete name of file to be read, e.g. foo.html
     * @return String
     * @throws IOException
     */
    private static String loadResource(String name) throws IOException {
        try (InputStream is = AdminConsoleAdapter.class.getResourceAsStream(name)) {
            return new String(is.readAllBytes(), UTF_8);
        }
    }

    /**
     * This method returns the resource bundle for localized strings used by the AdminConsoleAdapter.
     *
     * @param locale the {@link Locale} to be used
     */
    private ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle("com.sun.enterprise.v3.admin.adapter.LocalStrings", locale);
    }

    /**
     * This method replaces all tokens in text with values from the given {@link ResourceBundle}.
     *
     * <p>A token starts and ends with 3 percent (%) characters.  The value between the percent characters
     * will be used as the key to the given {@link ResourceBundle}. If a key does not exist in the bundle,
     * no substitution will take place for that token.
     *
     * @param text the text containing tokens to be replaced
     * @param bundle the {@link  ResourceBundle} with keys for the value
     * @return the same text except with substituted tokens when available
     */
    private String replaceTokens(String text, ResourceBundle bundle) {
        StringBuilder sb = new StringBuilder();

        int start = 0;
        int end = 0;
        while (start != -1) {
            // Find start of token
            start = text.indexOf("%%%", end);
            if (start != -1) {
                // First copy the stuff before the start
                sb.append(text, end, start);

                // Move past the %%%
                start += 3;

                // Find end of token
                end = text.indexOf("%%%", start);
                if (end != -1) {
                    try {
                        // Copy the token value to the buffer
                        sb.append(bundle.getString(text.substring(start, end)));
                    } catch (MissingResourceException e) {
                        // Unable to find the resource, so we don't do anything
                        sb.append("%%%").append(text, start, end).append("%%%");
                    }

                    // Move past the %%%
                    end += 3;
                } else {
                    // Add back the %%% because we didn't find a matching end
                    sb.append("%%%");

                    // Reset end so we can copy the remainder of the text
                    end = start;
                }
            }
        }

        // Copy the remainder of the text
        sb.append(text.substring(end));

        // Return the new String
        return sb.toString();
    }

    private void writeAdminServiceProperty(final String propertyName, final String propertyValue) {
        try {
            ConfigSupport.apply(adminService -> {
                Property newProperty = adminService.createChild(Property.class);
                adminService.getProperty().add(newProperty);
                newProperty.setName(propertyName);
                newProperty.setValue(propertyValue);
                return newProperty;
            }, adminService);
        } catch (Exception e) {
            logger.log(Level.WARNING, KernelLoggerInfo.consoleCannotWriteProperty,
                    new Object[] {propertyName, propertyValue, e});
        }
    }

    @Override
    public int getListenPort() {
        return endpointDecider.getListenPort();
    }

    @Override
    public InetAddress getListenAddress() {
        return endpointDecider.getListenAddress();
    }

    @Override
    public List<String> getVirtualServers() {
        return endpointDecider.getGuiHosts();
    }

    private static boolean checkHttpMethodAllowed(Method method) {
        return allowedHttpMethods.contains(method);
    }

    private static String getAllowedHttpMethodsAsString() {
        StringBuilder sb = new StringBuilder();
        for (Method method : allowedHttpMethods) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(method.getMethodString());
        }
        return sb.toString();
    }
}
