/*
 * Copyright (c) 2021, 2024 Contributors to Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.core;

import static com.sun.enterprise.util.Utility.isAllNull;
import static com.sun.enterprise.util.Utility.isEmpty;
import static com.sun.logging.LogCleanerUtil.neutralizeForLog;
import static jakarta.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static java.text.MessageFormat.format;
import static java.util.Collections.synchronizedList;
import static java.util.Collections.unmodifiableMap;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.apache.catalina.ContainerEvent.AFTER_CONTEXT_DESTROYED;
import static org.apache.catalina.ContainerEvent.AFTER_CONTEXT_INITIALIZED;
import static org.apache.catalina.ContainerEvent.AFTER_CONTEXT_INITIALIZER_ON_STARTUP;
import static org.apache.catalina.ContainerEvent.AFTER_REQUEST_DESTROYED;
import static org.apache.catalina.ContainerEvent.AFTER_REQUEST_INITIALIZED;
import static org.apache.catalina.ContainerEvent.BEFORE_CONTEXT_DESTROYED;
import static org.apache.catalina.ContainerEvent.BEFORE_CONTEXT_INITIALIZED;
import static org.apache.catalina.ContainerEvent.BEFORE_CONTEXT_INITIALIZER_ON_STARTUP;
import static org.apache.catalina.ContainerEvent.BEFORE_REQUEST_DESTROYED;
import static org.apache.catalina.ContainerEvent.BEFORE_REQUEST_INITIALIZED;
import static org.apache.catalina.ContainerEvent.PRE_DESTROY;
import static org.apache.catalina.Globals.ALTERNATE_RESOURCES_ATTR;
import static org.apache.catalina.Globals.ALT_DD_ATTR;
import static org.apache.catalina.Globals.FACES_INITIALIZER;
import static org.apache.catalina.Globals.META_INF_RESOURCES;
import static org.apache.catalina.Globals.RESOURCES_ATTR;
import static org.apache.catalina.Globals.WEBSOCKET_INITIALIZER;
import static org.apache.catalina.LogFacade.BIND_THREAD_EXCEPTION;
import static org.apache.catalina.LogFacade.CONTAINER_ALREADY_STARTED_EXCEPTION;
import static org.apache.catalina.LogFacade.CONTAINER_NOT_STARTED_EXCEPTION;
import static org.apache.catalina.LogFacade.DEPENDENCY_CHECK_EXCEPTION;
import static org.apache.catalina.LogFacade.DUPLICATE_SERVLET_MAPPING_EXCEPTION;
import static org.apache.catalina.LogFacade.ERROR_PAGE_LOCATION_EXCEPTION;
import static org.apache.catalina.LogFacade.ERROR_PAGE_REQUIRED_EXCEPTION;
import static org.apache.catalina.LogFacade.FILTER_MAPPING_INVALID_URL_EXCEPTION;
import static org.apache.catalina.LogFacade.FILTER_MAPPING_NAME_EXCEPTION;
import static org.apache.catalina.LogFacade.FILTER_WITHOUT_ANY_CLASS;
import static org.apache.catalina.LogFacade.INIT_RESOURCES_EXCEPTION;
import static org.apache.catalina.LogFacade.INVALID_ERROR_PAGE_CODE_EXCEPTION;
import static org.apache.catalina.LogFacade.INVOKING_SERVLET_CONTAINER_INIT_EXCEPTION;
import static org.apache.catalina.LogFacade.JSP_FILE_FINE;
import static org.apache.catalina.LogFacade.LISTENER_STOP_EXCEPTION;
import static org.apache.catalina.LogFacade.LOGIN_CONFIG_ERROR_PAGE_EXCEPTION;
import static org.apache.catalina.LogFacade.LOGIN_CONFIG_LOGIN_PAGE_EXCEPTION;
import static org.apache.catalina.LogFacade.LOGIN_CONFIG_REQUIRED_EXCEPTION;
import static org.apache.catalina.LogFacade.MISS_PATH_OR_URL_PATTERN_EXCEPTION;
import static org.apache.catalina.LogFacade.NO_WRAPPER_EXCEPTION;
import static org.apache.catalina.LogFacade.NULL_EMPTY_FILTER_NAME_EXCEPTION;
import static org.apache.catalina.LogFacade.NULL_EMPTY_SERVLET_NAME_EXCEPTION;
import static org.apache.catalina.LogFacade.NULL_FILTER_INSTANCE_EXCEPTION;
import static org.apache.catalina.LogFacade.NULL_SERVLET_INSTANCE_EXCEPTION;
import static org.apache.catalina.LogFacade.RELOADING_STARTED;
import static org.apache.catalina.LogFacade.REQUEST_DESTROY_EXCEPTION;
import static org.apache.catalina.LogFacade.REQUEST_INIT_EXCEPTION;
import static org.apache.catalina.LogFacade.RESETTING_CONTEXT_EXCEPTION;
import static org.apache.catalina.LogFacade.RESOURCES_STARTED;
import static org.apache.catalina.LogFacade.SECURITY_CONSTRAINT_PATTERN_EXCEPTION;
import static org.apache.catalina.LogFacade.SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION;
import static org.apache.catalina.LogFacade.SERVLET_LOAD_EXCEPTION;
import static org.apache.catalina.LogFacade.SERVLET_MAPPING_INVALID_URL_EXCEPTION;
import static org.apache.catalina.LogFacade.SERVLET_MAPPING_UNKNOWN_NAME_EXCEPTION;
import static org.apache.catalina.LogFacade.STARTING_CONTEXT_EXCEPTION;
import static org.apache.catalina.LogFacade.STARTING_RESOURCES_EXCEPTION;
import static org.apache.catalina.LogFacade.STARTING_RESOURCE_EXCEPTION_MESSAGE;
import static org.apache.catalina.LogFacade.STARTUP_CONTEXT_FAILED_EXCEPTION;
import static org.apache.catalina.LogFacade.STOPPING_CONTEXT_EXCEPTION;
import static org.apache.catalina.LogFacade.STOPPING_RESOURCES_EXCEPTION;
import static org.apache.catalina.LogFacade.WRAPPER_ERROR_EXCEPTION;
import static org.apache.catalina.core.Constants.DEFAULT_SERVLET_NAME;
import static org.apache.catalina.core.Constants.JSP_SERVLET_NAME;
import static org.apache.catalina.startup.Constants.WebDtdPublicId_22;
import static org.apache.catalina.util.RequestUtil.urlDecode;
import static org.apache.naming.resources.ProxyDirContext.CONTEXT;
import static org.apache.naming.resources.ProxyDirContext.HOST;
import static org.glassfish.web.loader.ServletContainerInitializerUtil.getInitializerList;
import static org.glassfish.web.loader.ServletContainerInitializerUtil.getInterestList;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionIdListener;
import jakarta.servlet.http.HttpSessionListener;
import jakarta.servlet.http.HttpUpgradeHandler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.naming.Binding;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import org.apache.catalina.Auditor;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.LogFacade;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Server;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.MappingImpl;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.FilterMaps;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.MessageDestination;
import org.apache.catalina.deploy.MessageDestinationRef;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceParams;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.deploy.ServletMap;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.util.CharsetMapper;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.catalina.util.ExtensionValidator;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.URLEncoder;
import org.apache.naming.ContextBindings;
import org.apache.naming.resources.BaseDirContext;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.ProxyDirContext;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.UrlResource;
import org.apache.naming.resources.WARDirContext;
import org.apache.naming.resources.WebDirContext;
import org.glassfish.grizzly.http.server.util.AlternateDocBase;
import org.glassfish.grizzly.http.server.util.Mapper;
import org.glassfish.grizzly.http.server.util.MappingData;
import org.glassfish.grizzly.http.util.CharChunk;
import org.glassfish.grizzly.http.util.MessageBytes;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.web.loader.ServletContainerInitializerUtil;
import org.glassfish.web.loader.WebappClassLoader;
import org.glassfish.web.valve.GlassFishValve;

/**
 * Standard implementation of the <b>Context</b> interface. Each child container must be a Wrapper implementation to
 * process the requests directed to a particular servlet.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.48 $ $Date: 2007/07/25 00:52:04 $
 */

public class StandardContext extends ContainerBase implements Context, ServletContext {
    private static final String DEFAULT_RESPONSE_CHARACTER_ENCODING = StandardCharsets.ISO_8859_1.name();
    private static final ClassLoader standardContextClassLoader = StandardContext.class.getClassLoader();
    private static final Set<SessionTrackingMode> DEFAULT_SESSION_TRACKING_MODES = EnumSet.of(SessionTrackingMode.COOKIE);

    /**
     * Array containing the safe characters set.
     */
    protected static final URLEncoder urlEncoder;

    private static final RuntimePermission GET_CLASSLOADER_PERMISSION = new RuntimePermission("getClassLoader");

    /**
     * GMT timezone - all HTTP dates are on GMT
     */
    static {
        urlEncoder = new URLEncoder();
        urlEncoder.addSafeCharacter('~');
        urlEncoder.addSafeCharacter('-');
        urlEncoder.addSafeCharacter('_');
        urlEncoder.addSafeCharacter('.');
        urlEncoder.addSafeCharacter('*');
        urlEncoder.addSafeCharacter('/');
    }

    /**
     * The alternate deployment descriptor name.
     */
    private String altDDName;

    /**
     * The antiJARLocking flag for this Context.
     */
    private boolean antiJARLocking;

    /**
     * Associated host name.
     */
    private String hostName;

    /**
     * The list of instantiated application event listeners
     */
    private final List<EventListener> eventListeners = new ArrayList<>();

    /**
     * The list of ServletContextListeners
     */
    protected List<ServletContextListener> contextListeners = new ArrayList<>();

    /**
     * The list of HttpSessionListeners
     */
    private final List<HttpSessionListener> sessionListeners = new ArrayList<>();

    /**
     * The set of application parameters defined for this application.
     */
    private final List<ApplicationParameter> applicationParameters = new ArrayList<>();

    /**
     * The application available flag for this Context.
     */
    private boolean available;

    /**
     * The broadcaster that sends EE notifications.
     */
    private NotificationBroadcasterSupport broadcaster;

    /**
     * The Locale to character set mapper for this application.
     */
    private CharsetMapper charsetMapper;

    /**
     * The Java class name of the CharsetMapper class to be created.
     */
    private String charsetMapperClass = CharsetMapper.class.getName();

    /**
     * The request character encoding.
     */
    private String requestCharacterEncoding;

    /**
     * The response character encoding.
     */
    private String responseCharacterEncoding;

    /**
     * The path to a file to save this Context information.
     */
    private String configFile;

    /**
     * The "correctly configured" flag for this Context.
     */
    private boolean configured;

    /**
     * The security constraints for this web application.
     */
    private final List<SecurityConstraint> constraints = new ArrayList<>();

    /**
     * The ServletContext implementation associated with this Context.
     */
    protected ApplicationContext context;

    /**
     * Is the context initialized.
     */
    private boolean isContextInitializedCalled;

    /**
     * Compiler classpath to use.
     */
    private String compilerClasspath;

    /**
     * Should we attempt to use cookies for session id communication?
     */
    private boolean cookies = true;

    /**
     * true if the rewriting of URLs with the jsessionids of HTTP sessions belonging to this context is enabled, false
     * otherwise
     */
    private boolean enableURLRewriting = true;

    /**
     * Should we allow the <code>ServletContext.getContext()</code> method to access the context of other web applications
     * in this server?
     */
    private boolean crossContext;

    /**
     * The "follow standard delegation model" flag that will be used to configure our ClassLoader.
     */
    private boolean delegate;

    /**
     * The display name of this web application.
     */
    private String displayName;

    /**
     * Override the default web xml location. ContextConfig is not configurable so the setter is not used.
     */
    private String defaultWebXml;

    /**
     * The distributable flag for this web application.
     */
    private boolean distributable;

    /**
     * Thread local data used during request dispatch.
     */
    private final ThreadLocal<DispatchData> dispatchData = new ThreadLocal<>();

    /**
     * The document root for this web application.
     */
    private String docBase;

    /**
     * The exception pages for this web application, keyed by fully qualified class name of the Java exception.
     */
    private final Map<String, ErrorPage> exceptionPages = new HashMap<>();

    /**
     * The default error page (error page that was declared without any exception-type and error-code).
     */
    private ErrorPage defaultErrorPage;

    /**
     * The set of filter configurations (and associated filter instances) we have initialized, keyed by filter name.
     */
    private final Map<String, FilterConfig> filterConfigs = new HashMap<>();

    /**
     * The set of filter definitions for this application, keyed by filter name.
     */
    private final Map<String, FilterDef> filterDefs = new HashMap<>();

    /**
     * The list of filter mappings for this application, in the order they were defined in the deployment descriptor.
     */
    private final List<FilterMap> filterMaps = new ArrayList<>();

    /**
     * The list of classnames of InstanceListeners that will be added to each newly created Wrapper by
     * <code>createWrapper()</code>.
     */
    private final ArrayList<String> instanceListeners = new ArrayList<>();

    /**
     * The set of already instantiated InstanceListeners that will be added to each newly created Wrapper by
     * <code>createWrapper()</code>.
     */
    private final List<InstanceListener> instanceListenerInstances = new ArrayList<>();

    /**
     * The login configuration descriptor for this web application.
     */
    private LoginConfig loginConfig;

    /**
     * The mapper associated with this context.
     */
    private final Mapper mapper = new Mapper();

    /**
     * The naming context listener for this web application.
     */
    private NamingContextListener namingContextListener;

    /**
     * The naming resources for this web application.
     */
    private NamingResources namingResources = new NamingResources();

    /**
     * The message destinations for this web application.
     */
    private final Map<String, MessageDestination> messageDestinations = new HashMap<>();

    /**
     * The MIME mappings for this web application, keyed by extension.
     */
    private final Map<String, String> mimeMappings = new HashMap<>();

    /**
     * The context initialization parameters for this web application, keyed by name.
     */
    private final HashMap<String, String> parameters = new HashMap<>();

    /**
     * The request processing pause flag (while reloading occurs)
     */
    private boolean paused;

    /**
     * The public identifier of the DTD for the web application deployment descriptor version we are currently parsing. This
     * is used to support relaxed validation rules when processing version 2.2 web.xml files.
     */
    private String publicId;

    /**
     * The reloadable flag for this web application.
     */
    private boolean reloadable;

    /**
     * Unpack WAR property.
     */
    private boolean unpackWAR = true;

    /**
     * The DefaultContext override flag for this web application.
     */
    private boolean override;

    /**
     * The original document root for this web application.
     */
    private String originalDocBase;

    /**
     * The privileged flag for this web application.
     */
    private boolean privileged;

    /**
     * Should the next call to <code>addWelcomeFile()</code> cause replacement of any existing welcome files? This will be
     * set before processing the web application's deployment descriptor, so that application specified choices
     * <strong>replace</strong>, rather than append to, those defined in the global descriptor.
     */
    private boolean replaceWelcomeFiles;

    /**
     * With proxy caching disabled, setting this flag to true adds Pragma and Cache-Control headers with "No-cache" as
     * value. Setting this flag to false does not add any Pragma header, but sets the Cache-Control header to "private".
     */
    private boolean securePagesWithPragma = true;

    /**
     * The security role mappings for this application, keyed by role name (as used within the application).
     */
    private final Map<String, String> roleMappings = new HashMap<>();

    /**
     * The security roles for this application
     */
    private final List<String> securityRoles = new ArrayList<>();

    /**
     * The servlet mappings for this web application, keyed by matching pattern.
     */
    private final Map<String, String> servletMappings = new HashMap<>();

    /**
     * The session timeout (in minutes) for this web application.
     */
    private int sessionTimeout = 30;

    /**
     * Has the session timeout (in minutes) for this web application been over-ridden by web-xml HERCULES:add
     */
    private boolean sessionTimeoutOveridden;

    /**
     * The notification sequence number.
     */
    private long sequenceNumber;

    /**
     * The status code error pages for this web application, keyed by HTTP status code (as an Integer).
     */
    private final Map<Integer, ErrorPage> statusPages = new HashMap<>();

    /**
     * Amount of ms that the container will wait for servlets to unload.
     */
    private long unloadDelay = 2000;

    /**
     * The watched resources for this application.
     */
    private final List<String> watchedResources = synchronizedList(new ArrayList<String>());

    /**
     * The welcome files for this application.
     */
    private String[] welcomeFiles = new String[0];

    /**
     * The list of classnames of LifecycleListeners that will be added to each newly created Wrapper by
     * <code>createWrapper()</code>.
     */
    private final List<String> wrapperLifecycles = new ArrayList<>();

    /**
     * The list of classnames of ContainerListeners that will be added to each newly created Wrapper by
     * <code>createWrapper()</code>.
     */
    private final List<String> wrapperListeners = new ArrayList<>();

    /**
     * The pathname to the work directory for this context (relative to the server's home if not absolute).
     */
    private String workDir;

    /**
     * JNDI use flag.
     */
    private boolean useNaming = true;

    /**
     * Filesystem based flag.
     */
    private boolean filesystemBased;

    /**
     * Name of the associated naming context.
     */
    private String namingContextName;

    /**
     * Frequency of the session expiration, and related manager operations. Manager operations will be done once for the
     * specified amount of backgrondProcess calls (ie, the lower the amount, the most often the checks will occur).
     */
    private int managerChecksFrequency = 6;

    /**
     * Iteration count for background processing.
     */
    private int count;

    /**
     * Caching allowed flag.
     */
    private boolean cachingAllowed = true;

    /**
     * Case sensitivity.
     */
    protected boolean caseSensitive = true;

    /**
     * Allow linking.
     */
    protected boolean allowLinking;

    /**
     * Cache max size in KB.
     */
    protected int cacheMaxSize = 10_240; // 10 MB

    /**
     * Cache TTL in ms.
     */
    protected int cacheTTL = 5000;

    /**
     * Non proxied resources.
     */
    private DirContext webappResources;

    /**
     * Time (in milliseconds) it took to start this context
     */
    private long startupTime;

    /**
     * Time (in milliseconds since January 1, 1970, 00:00:00) when this context was started
     */
    private long startTimeMillis;

    private long tldScanTime;

    /** Should the filter and security mapping be done in a case sensitive manner */
    protected boolean caseSensitiveMapping = true;

    /**
     * The flag that specifies whether to reuse the session id (if any) from the request for newly created sessions
     */
    private boolean reuseSessionID;

    /**
     * The flag that specifies whether this context allows sendRedirect() to redirect to a relative URL.
     */
    private boolean allowRelativeRedirect;

    /**
     * Name of the engine. If null, the domain is used.
     */
    private String engineName;

    private String eeApplication = "null";
    private String eeServer = "none";

    /**
     * List of configured Auditors for this context.
     */
    private Auditor[] auditors;

    /**
     * used to create unique id for each app instance.
     */
    private static AtomicInteger instanceIDCounter = new AtomicInteger(1);

    /**
     * Attribute value used to turn on/off XML validation
     */
    private boolean webXmlValidation;

    private String jvmRoute;

    /**
     * Attribute value used to turn on/off XML namespace validation
     */
    private boolean webXmlNamespaceAware;

    /**
     * Attribute value used to turn on/off XML validation
     */
    private boolean tldValidation;

    /**
     * Attribute value used to turn on/off TLD XML namespace validation
     */
    private boolean tldNamespaceAware;

    /**
     * Is the context contains the JSF servlet.
     */
    protected boolean isJsfApplication;

    private boolean isReload;

    /**
     * Alternate doc base resources
     */
    private ArrayList<AlternateDocBase> alternateDocBases;

    private boolean useMyFaces;

    private Set<SessionTrackingMode> sessionTrackingModes;

    /**
     * Encoded path.
     */
    private String encodedPath;

    /**
     * Session cookie config
     */
    private SessionCookieConfig sessionCookieConfig;

    /**
     * The name of the session tracking cookies created by this context Cache the name here as the getSessionCookieConfig()
     * is synchronized.
     */
    private String sessionCookieName = Globals.SESSION_COOKIE_NAME;

    private boolean sessionCookieNameInitialized;

    protected Map<String, ServletRegistrationImpl> servletRegistrationMap = new ConcurrentHashMap<>();

    protected Map<String, FilterRegistrationImpl> filterRegistrationMap = new ConcurrentHashMap<>();

    /**
     * The list of ordered libs, which is used as the value of the ServletContext attribute with name
     * jakarta.servlet.context.orderedLibs
     */
    private List<String> orderedLibs;

    /** The <code>jsp-config</code> element related info aggregated from web.xml and web-fragment.xml */
    private JspConfigDescriptor jspConfigDesc;

    /**
     * ServletContextListeners may be registered (via ServletContext#addListener) only within
     * the scope of ServletContainerInitializer#onStartup
     */
    private boolean isProgrammaticServletContextListenerRegistrationAllowed;

    /** Iterable over all ServletContainerInitializers that were discovered */
    private ServiceLoader<ServletContainerInitializer> servletContainerInitializers;

    /** The major Servlet spec version of the web.xml */
    private int effectiveMajorVersion;

    /** The minor Servlet spec version of the web.xml */
    private int effectiveMinorVersion;

    /** Created via embedded API */
    private boolean isEmbedded;

    protected boolean directoryDeployed;

    protected boolean showArchivedRealPathEnabled = true;

    /**
     * Should we generate directory listings?
     */
    protected boolean directoryListing;

    /** Fine tune log levels for ServletContainerInitializerUtil to avoid spurious or too verbose logging */
    protected ServletContainerInitializerUtil.LogContext logContext = new ServletContainerInitializerUtil.LogContext() {

        @Override
        public Level getNonCriticalClassloadingErrorLogLevel() {
            return isStandaloneModule() ? Level.WARNING : Level.FINE;
        }
    };


    /**
     * Create a new StandardContext component with the default basic Valve.
     */
    public StandardContext() {
        pipeline.setBasic(new StandardContextValve());
        namingResources.setContainer(this);
    }

    @Override
    public String getEncodedPath() {
        return encodedPath;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        encodedPath = urlEncoder.encode(name);
    }

    /**
     * @return true if caching allowed
     */
    public boolean isCachingAllowed() {
        return cachingAllowed;
    }

    /**
     * Set caching allowed flag.
     */
    public void setCachingAllowed(boolean cachingAllowed) {
        this.cachingAllowed = cachingAllowed;
    }

    /**
     * Set case sensitivity.
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * @return true if is case sensitive
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * Set case sensitivity for filter and security constraint mappings.
     */
    public void setCaseSensitiveMapping(boolean caseSensitiveMap) {
        caseSensitiveMapping = caseSensitiveMap;
    }

    /**
     * @return true if filters and security constraints are mapped in a case sensitive manner
     */
    public boolean isCaseSensitiveMapping() {
        return caseSensitiveMapping;
    }

    /**
     * Set allow linking.
     */
    public void setAllowLinking(boolean allowLinking) {
        this.allowLinking = allowLinking;
    }

    /**
     * @return true if linking allowed.
     */
    public boolean isAllowLinking() {
        return allowLinking;
    }

    /**
     * Set cache TTL.
     */
    public void setCacheTTL(int cacheTTL) {
        this.cacheTTL = cacheTTL;
    }

    /**
     * @return cache TTL.
     */
    public int getCacheTTL() {
        return cacheTTL;
    }

    /**
     * @return the maximum size of the cache in KB.
     */
    public int getCacheMaxSize() {
        return cacheMaxSize;
    }

    /**
     * Set the maximum size of the cache in KB.
     */
    public void setCacheMaxSize(int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }

    /**
     * @return the "follow standard delegation model" flag used to configure our ClassLoader.
     */
    public boolean getDelegate() {
        return delegate;
    }

    /**
     * Set the "follow standard delegation model" flag used to configure our ClassLoader.
     *
     * @param delegate The new flag
     */
    public void setDelegate(boolean delegate) {
        boolean oldDelegate = this.delegate;
        this.delegate = delegate;
        support.firePropertyChange("delegate", Boolean.valueOf(oldDelegate), Boolean.valueOf(this.delegate));
    }

    /**
     * @return true if the internal naming support is used.
     */
    public boolean isUseNaming() {
        synchronized (this) {
            return useNaming;
        }
    }

    /**
     * Enables or disables naming.
     */
    public void setUseNaming(boolean useNaming) {
        this.useNaming = useNaming;
    }

    /**
     * @return true if the resources associated with this context are filesystem based, false otherwise
     */
    public boolean isFilesystemBased() {
        return filesystemBased;
    }

    @Override
    public List<EventListener> getApplicationEventListeners() {
        return eventListeners;
    }

    public List<HttpSessionListener> getSessionListeners() {
        return sessionListeners;
    }

    @Override
    public boolean getAvailable() {
        return available;
    }

    @Override
    public void setAvailable(boolean available) {
        boolean oldAvailable = this.available;
        this.available = available;
        support.firePropertyChange("available", Boolean.valueOf(oldAvailable), Boolean.valueOf(this.available));
    }

    /**
     * @return the antiJARLocking flag for this Context.
     */
    public boolean getAntiJARLocking() {
        return antiJARLocking;
    }

    /**
     * Set the antiJARLocking feature for this Context.
     *
     * @param antiJARLocking The new flag value
     */
    public void setAntiJARLocking(boolean antiJARLocking) {
        boolean oldAntiJARLocking = this.antiJARLocking;
        this.antiJARLocking = antiJARLocking;
        support.firePropertyChange("antiJARLocking", oldAntiJARLocking, this.antiJARLocking);
    }

    @Override
    public CharsetMapper getCharsetMapper() {
        // Create a mapper the first time it is requested
        if (charsetMapper == null) {
            try {
                this.charsetMapper = (CharsetMapper)
                    Class.forName(charsetMapperClass)
                         .getDeclaredConstructor()
                         .newInstance();
            } catch (Throwable t) {
                charsetMapper = new CharsetMapper();
            }
        }

        return charsetMapper;
    }

    @Override
    public void setCharsetMapper(CharsetMapper mapper) {
        CharsetMapper oldCharsetMapper = this.charsetMapper;
        this.charsetMapper = mapper;
        if (mapper != null) {
            this.charsetMapperClass = mapper.getClass().getName();
        }

        support.firePropertyChange("charsetMapper", oldCharsetMapper, this.charsetMapper);
    }

    @Override
    public String getRequestCharacterEncoding() {
        return requestCharacterEncoding;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {
        this.requestCharacterEncoding = encoding;
    }

    @Override
    public String getResponseCharacterEncoding() {
        return responseCharacterEncoding;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {
        this.responseCharacterEncoding = encoding;
    }

    @Override
    public String getConfigFile() {
        return configFile;
    }

    @Override
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    @Override
    public boolean getConfigured() {
        return configured;
    }

    @Override
    public void setConfigured(boolean configured) {
        boolean oldConfigured = this.configured;
        this.configured = configured;
        support.firePropertyChange("configured", Boolean.valueOf(oldConfigured), Boolean.valueOf(this.configured));
    }

    @Override
    public boolean getCookies() {
        return cookies;
    }

    @Override
    public void setCookies(boolean cookies) {
        boolean oldCookies = this.cookies;
        this.cookies = cookies;
        support.firePropertyChange("cookies", Boolean.valueOf(oldCookies), Boolean.valueOf(this.cookies));
    }

    @Override
    public boolean isEnableURLRewriting() {
        return enableURLRewriting;
    }

    @Override
    public void setEnableURLRewriting(boolean enableURLRewriting) {
        boolean oldEnableURLRewriting = this.enableURLRewriting;
        this.enableURLRewriting = enableURLRewriting;
        support.firePropertyChange("enableURLRewriting", Boolean.valueOf(oldEnableURLRewriting), Boolean.valueOf(this.enableURLRewriting));
    }

    @Override
    public boolean getCrossContext() {
        return crossContext;
    }

    @Override
    public void setCrossContext(boolean crossContext) {
        boolean oldCrossContext = this.crossContext;
        this.crossContext = crossContext;
        support.firePropertyChange("crossContext", Boolean.valueOf(oldCrossContext), Boolean.valueOf(this.crossContext));
    }


    /**
     * @return the location of the default web xml that will be used. If not absolute, it'll be made
     *         relative to the engine's base dir (which defaults to catalina.base system property).
     */
    public String getDefaultWebXml() {
        return defaultWebXml;
    }


    /**
     * Set the location of the default web xml that will be used. If not absolute, it'll be made
     * relative to the engine's base dir (which defaults to catalina.base system property).
     *
     * XXX If a file is not found - we can attempt a getResource()
     *
     * @param defaultWebXml
     */
    public void setDefaultWebXml(String defaultWebXml) {
        this.defaultWebXml = defaultWebXml;
    }

    /**
     * @return Time (in milliseconds) it took to start this context.
     */
    public long getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(long startupTime) {
        this.startupTime = startupTime;
    }

    public long getTldScanTime() {
        return tldScanTime;
    }

    public void setTldScanTime(long tldScanTime) {
        this.tldScanTime = tldScanTime;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        String oldDisplayName = this.displayName;
        this.displayName = displayName;
        support.firePropertyChange("displayName", oldDisplayName, this.displayName);
    }

    @Override
    public String getAltDDName() {
        return altDDName;
    }

    @Override
    public void setAltDDName(String altDDName) {
        this.altDDName = altDDName;
        if (context != null) {
            context.setAttribute(ALT_DD_ATTR, altDDName);
            context.setAttributeReadOnly(ALT_DD_ATTR);
        }
    }

    /**
     * @return the compiler classpath.
     */
    public String getCompilerClasspath() {
        return compilerClasspath;
    }

    /**
     * Set the compiler classpath.
     */
    public void setCompilerClasspath(String compilerClasspath) {
        this.compilerClasspath = compilerClasspath;
    }


    @Override
    public boolean getDistributable() {
        return distributable;
    }

    @Override
    public void setDistributable(boolean distributable) {
        boolean oldDistributable = this.distributable;
        this.distributable = distributable;
        support.firePropertyChange("distributable", Boolean.valueOf(oldDistributable), Boolean.valueOf(this.distributable));
        if (getManager() != null) {
            log.log(FINE, () -> "Propagating distributable=" + distributable + " to manager");
            getManager().setDistributable(distributable);
        }
    }

    @Override
    public String getDocBase() {
        return docBase;
    }

    @Override
    public void setDocBase(String docBase) {
        synchronized (this) {
            this.docBase = docBase;
        }
    }

    /**
     * Configures this context's alternate doc base mappings.
     *
     * @param urlPattern
     * @param docBase
     */
    public void addAlternateDocBase(String urlPattern, String docBase) {
        if (urlPattern == null || docBase == null) {
            throw new IllegalArgumentException(rb.getString(MISS_PATH_OR_URL_PATTERN_EXCEPTION));
        }

        AlternateDocBase alternateDocBase = new AlternateDocBase();
        alternateDocBase.setUrlPattern(urlPattern);
        alternateDocBase.setDocBase(docBase);
        alternateDocBase.setBasePath(getBasePath(docBase));

        if (alternateDocBases == null) {
            alternateDocBases = new ArrayList<>();
        }

        alternateDocBases.add(alternateDocBase);
    }

    /**
     * @return This context's configured alternate doc bases
     */
    public List<AlternateDocBase> getAlternateDocBases() {
        return alternateDocBases;
    }

    /**
     * @return the frequency of manager checks.
     */
    public int getManagerChecksFrequency() {
        return managerChecksFrequency;
    }

    /**
     * Set the manager checks frequency.
     *
     * @param managerChecksFrequency the new manager checks frequency
     */
    public void setManagerChecksFrequency(int managerChecksFrequency) {
        if (managerChecksFrequency <= 0) {
            return;
        }

        int oldManagerChecksFrequency = this.managerChecksFrequency;
        this.managerChecksFrequency = managerChecksFrequency;
        support.firePropertyChange("managerChecksFrequency", oldManagerChecksFrequency, this.managerChecksFrequency);
    }

    @Override
    public String getInfo() {
        return "org.apache.catalina.core.StandardContext/1.0";
    }

    public String getJvmRoute() {
        return jvmRoute;
    }

    public void setJvmRoute(String jvmRoute) {
        this.jvmRoute = jvmRoute;
    }

    public String getEngineName() {
        if (engineName != null) {
            return engineName;
        }
        return domain;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getEEApplication() {
        return eeApplication;
    }

    public void setEEApplication(String eeApplication) {
        this.eeApplication = eeApplication;
    }

    public String getEEServer() {
        return eeServer;
    }

    public void setEEServer(String eeServer) {
        this.eeServer = eeServer;
    }

    @Override
    public LoginConfig getLoginConfig() {
        return loginConfig;
    }

    @Override
    public void setLoginConfig(LoginConfig config) {
        // Validate the incoming property value
        if (config == null) {
            throw new IllegalArgumentException(rb.getString(LOGIN_CONFIG_REQUIRED_EXCEPTION));
        }

        String loginPage = config.getLoginPage();
        if ((loginPage != null) && !loginPage.startsWith("/")) {
            if (isServlet22()) {
                log.log(FINE, LogFacade.FORM_LOGIN_PAGE_FINE, loginPage);
                config.setLoginPage("/" + loginPage);
            } else {
                throw new IllegalArgumentException(format(rb.getString(LOGIN_CONFIG_LOGIN_PAGE_EXCEPTION), loginPage));
            }
        }

        String errorPage = config.getErrorPage();
        if ((errorPage != null) && !errorPage.startsWith("/")) {
            if (isServlet22()) {
                log.log(FINE, LogFacade.FORM_ERROR_PAGE_FINE, errorPage);
                config.setErrorPage("/" + errorPage);
            } else {
                throw new IllegalArgumentException(format(rb.getString(LOGIN_CONFIG_ERROR_PAGE_EXCEPTION), errorPage));
            }
        }

        // Process the property setting change
        LoginConfig oldLoginConfig = this.loginConfig;
        this.loginConfig = config;
        support.firePropertyChange("loginConfig", oldLoginConfig, this.loginConfig);
    }

    @Override
    public Mapper getMapper() {
        return mapper;
    }

    /**
     * Sets a new pipeline
     */
    public void restrictedSetPipeline(Pipeline pipeline) {
        synchronized (this) {
            pipeline.setBasic(new StandardContextValve());
            this.pipeline = pipeline;
            hasCustomPipeline = true;
        }
    }

    @Override
    public NamingResources getNamingResources() {
        return namingResources;
    }

    @Override
    public void setNamingResources(NamingResources namingResources) {
        // Process the property setting change
        NamingResources oldNamingResources = this.namingResources;
        this.namingResources = namingResources;
        support.firePropertyChange("namingResources", oldNamingResources, this.namingResources);
    }

    @Override
    public String getPath() {
        return getName();
    }

    /**
     * Set the context path for this Context.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>: The context path is used as the "name" of a Context, because it must be unique.
     *
     * @param path The new context path
     */
    @Override
    public void setPath(String path) {
        // XXX Use host in name
        setName(urlDecode(path, "UTF-8"));
    }

    @Override
    public String getPublicId() {
        return publicId;
    }

    @Override
    public void setPublicId(String publicId) {
        log.log(FINEST, () -> "Setting deployment descriptor public ID to '" + publicId + "'");

        String oldPublicId = this.publicId;
        this.publicId = publicId;
        support.firePropertyChange("publicId", oldPublicId, publicId);
    }

    @Override
    public boolean getReloadable() {
        return reloadable;
    }

    @Override
    public boolean getOverride() {
        return override;
    }


    /**
     * Gets the original document root for this Context, which can be an absolute pathname,
     * a relative pathname, or a URL. Set only if deployment has changed the docRoot!
     *
     * @return the original document root for this Context or null
     */
    public String getOriginalDocBase() {
        return originalDocBase;
    }


    /**
     * Set the original document root for this Context, which can be an absolute pathname,
     * a relative pathname, or a URL.
     *
     * @param docBase The original document root
     */
    public void setOriginalDocBase(String docBase) {
        this.originalDocBase = docBase;
    }

    @Override
    public boolean getPrivileged() {
        return privileged;
    }

    @Override
    public void setPrivileged(boolean privileged) {
        boolean oldPrivileged = this.privileged;
        this.privileged = privileged;
        support.firePropertyChange("privileged", oldPrivileged, this.privileged);
    }

    @Override
    public void setReloadable(boolean reloadable) {
        boolean oldReloadable = this.reloadable;
        this.reloadable = reloadable;
        support.firePropertyChange("reloadable", oldReloadable, this.reloadable);
    }

    @Override
    public void setOverride(boolean override) {
        boolean oldOverride = this.override;
        this.override = override;
        support.firePropertyChange("override", oldOverride, this.override);
    }

    /**
     * Scan the parent when searching for TLD listeners.
     */
    @Override
    public boolean isJsfApplication() {
        return isJsfApplication;
    }

    @Override
    public boolean hasAdHocPaths() {
        return false;
    }

    @Override
    public String getAdHocServletName(String path) {
        return null;
    }

    /**
     * @return the "replace welcome files" property.
     */
    public boolean isReplaceWelcomeFiles() {
        return replaceWelcomeFiles;
    }

    /**
     * Set the "replace welcome files" property.
     *
     * @param replaceWelcomeFiles The new property value
     */
    public void setReplaceWelcomeFiles(boolean replaceWelcomeFiles) {
        boolean oldReplaceWelcomeFiles = this.replaceWelcomeFiles;
        this.replaceWelcomeFiles = replaceWelcomeFiles;
        support.firePropertyChange("replaceWelcomeFiles", oldReplaceWelcomeFiles, this.replaceWelcomeFiles);
    }

    @Override
    public boolean isSecurePagesWithPragma() {
        return securePagesWithPragma;
    }

    @Override
    public void setSecurePagesWithPragma(boolean securePagesWithPragma) {
        boolean oldSecurePagesWithPragma = this.securePagesWithPragma;
        this.securePagesWithPragma = securePagesWithPragma;
        support.firePropertyChange("securePagesWithPragma", oldSecurePagesWithPragma,  this.securePagesWithPragma);
    }

    public void setUseMyFaces(boolean useMyFaces) {
        this.useMyFaces = useMyFaces;
    }

    public boolean isUseMyFaces() {
        return useMyFaces;
    }

    @Override
    public ServletContext getServletContext() {
        if (context == null) {
            context = new ApplicationContext(this);
            if (altDDName != null && context.getAttribute(ALT_DD_ATTR) == null) {
                context.setAttribute(ALT_DD_ATTR, altDDName);
                context.setAttributeReadOnly(ALT_DD_ATTR);
            }
        }

        return context.getFacade();
    }

    @Override
    public int getSessionTimeout() {
        return sessionTimeout;
    }


    /**
     * @return true if the session timeout (in minutes) for this web application over-ridden from
     *         the default HERCULES:add
     */
    public boolean isSessionTimeoutOveridden() {
        return sessionTimeoutOveridden;
    }

    @Override
    public void setSessionTimeout(int timeout) {
        if (isContextInitializedCalled) {
            throw new IllegalStateException(format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION),
                new Object[] {"setSessionTimeout", getName()}));
        }

        int oldSessionTimeout = this.sessionTimeout;

        // SRV.13.4 ("Deployment Descriptor"): If the timeout is 0 or less, the container ensures
        // the default behaviour of sessions is never to time out.
        this.sessionTimeout = timeout == 0 ? -1 : timeout;
        support.firePropertyChange("sessionTimeout", oldSessionTimeout, this.sessionTimeout);
        sessionTimeoutOveridden = true;
    }

    /**
     * @return the value of the unloadDelay flag.
     */
    public long getUnloadDelay() {
        return unloadDelay;
    }

    /**
     * Set the value of the unloadDelay flag, which represents the amount of ms that the container will wait when unloading
     * servlets. Setting this to a small value may cause more requests to fail to complete when stopping a web application.
     *
     * @param unloadDelay The new value
     */
    public void setUnloadDelay(long unloadDelay) {
        long oldUnloadDelay = this.unloadDelay;
        this.unloadDelay = unloadDelay;
        support.firePropertyChange("unloadDelay", oldUnloadDelay, this.unloadDelay);
    }

    /**
     * @return true if Unpack WAR flag accessor enabled.
     */
    public boolean getUnpackWAR() {
        return unpackWAR;
    }

    /**
     * Unpack WAR flag mutator.
     */
    public void setUnpackWAR(boolean unpackWAR) {
        this.unpackWAR = unpackWAR;
    }

    @Override
    public synchronized void setResources(DirContext resources) {
        if (started) {
            throw new IllegalStateException(rb.getString(RESOURCES_STARTED));
        }

        DirContext oldResources = this.webappResources;
        if (oldResources == resources) {
            return;
        }

        if (resources instanceof BaseDirContext) {
            BaseDirContext baseDirContext = (BaseDirContext) resources;
            baseDirContext.setCached(isCachingAllowed());
            baseDirContext.setCacheTTL(getCacheTTL());
            baseDirContext.setCacheMaxSize(getCacheMaxSize());
        }

        if (resources instanceof FileDirContext) {
            filesystemBased = true;
            FileDirContext fileDirContext = (FileDirContext) resources;
            fileDirContext.setCaseSensitive(isCaseSensitive());
            fileDirContext.setAllowLinking(isAllowLinking());
        }
        this.webappResources = resources;

        // The proxied resources will be refreshed on start
        this.resources = null;

        support.firePropertyChange("resources", oldResources, this.webappResources);
    }

    private synchronized void setAlternateResources(AlternateDocBase alternateDocBase, DirContext resources) {
        if (started) {
            throw new IllegalStateException(rb.getString(RESOURCES_STARTED));
        }

        final DirContext oldResources = ContextsAdapterUtility.unwrap(alternateDocBase.getWebappResources());

        if (oldResources == resources) {
            return;
        }

        if (resources instanceof BaseDirContext) {
            ((BaseDirContext) resources).setCached(isCachingAllowed());
            ((BaseDirContext) resources).setCacheTTL(getCacheTTL());
            ((BaseDirContext) resources).setCacheMaxSize(getCacheMaxSize());
        }

        if (resources instanceof FileDirContext) {
            filesystemBased = true;
            ((FileDirContext) resources).setCaseSensitive(isCaseSensitive());
            ((FileDirContext) resources).setAllowLinking(isAllowLinking());
        }
        alternateDocBase.setWebappResources(ContextsAdapterUtility.wrap(resources));
        // The proxied resources will be refreshed on start
        alternateDocBase.setResources(null);
    }

    @Override
    public boolean getReuseSessionID() {
        return reuseSessionID;
    }

    @Override
    public void setReuseSessionID(boolean reuse) {
        reuseSessionID = reuse;
    }

    @Override
    public boolean getAllowRelativeRedirect() {
        return allowRelativeRedirect;
    }

    @Override
    public void setAllowRelativeRedirect(boolean allowRelativeURLs) {
        allowRelativeRedirect = allowRelativeURLs;
    }

    @Override
    public Auditor[] getAuditors() {
        return auditors;
    }

    @Override
    public void setAuditors(Auditor[] auditor) {
        this.auditors = auditor;
    }

    public void setReload(boolean isReload) {
        this.isReload = isReload;
    }

    public boolean isReload() {
        return isReload;
    }

    public void setEmbedded(boolean isEmbedded) {
        this.isEmbedded = isEmbedded;
    }

    public boolean isEmbedded() {
        return isEmbedded;
    }

    /**
     * Enables or disables directory listings on this <tt>Context</tt>.
     */
    public void setDirectoryListing(boolean directoryListing) {
        this.directoryListing = directoryListing;
        Wrapper wrapper = (Wrapper) findChild(DEFAULT_SERVLET_NAME);
        if (wrapper != null) {
            Servlet servlet = ((StandardWrapper) wrapper).getServlet();
            if (servlet instanceof DefaultServlet) {
                ((DefaultServlet) servlet).setListings(directoryListing);
            }
        }
    }

    /**
     * @return true if directory listings are enabled on this <tt>Context</tt>.
     */
    public boolean isDirectoryListing() {
        return directoryListing;
    }


    /**
     * @return the Locale to character set mapper class for this Context.
     */
    public String getCharsetMapperClass() {
        return charsetMapperClass;
    }

    /**
     * Set the Locale to character set mapper class for this Context.
     *
     * @param mapper The new mapper class
     */
    public void setCharsetMapperClass(String mapper) {
        String oldCharsetMapperClass = this.charsetMapperClass;
        this.charsetMapperClass = mapper;
        support.firePropertyChange("charsetMapperClass", oldCharsetMapperClass, this.charsetMapperClass);
    }

    /**
     * Get the absolute path to the work dir.
     *
     * @return the absolute path to the work dir
     */
    public String getWorkPath() {
        if (getWorkDir() == null) {
            return null;
        }

        File workDir = new File(getWorkDir());
        if (!workDir.isAbsolute()) {
            File catalinaHome = engineBase();
            try {
                workDir = new File(catalinaHome.getCanonicalPath(), getWorkDir());
            } catch (IOException e) {
            }
        }

        return workDir.getAbsolutePath();
    }

    /**
     * @return the work directory for this Context.
     */
    public String getWorkDir() {
        return workDir;
    }

    /**
     * Set the work directory for this Context.
     *
     * @param workDir The new work directory
     */
    public void setWorkDir(String workDir) {
        synchronized (this) {
            this.workDir = workDir;
            if (started) {
                postWorkDirectory();
            }
        }
    }

    @Override
    public void addApplicationListener(String listener) {
        addListener(listener, false);
    }

    @Override
    public void addApplicationParameter(ApplicationParameter parameter) {
        String newName = parameter.getName();

        for (ApplicationParameter applicationParameter : applicationParameters) {
            if (newName.equals(applicationParameter.getName())) {
                if (applicationParameter.getOverride()) {
                    applicationParameter.setValue(parameter.getValue());
                }

                return;
            }
        }

        applicationParameters.add(parameter);

        if (notifyContainerListeners) {
            fireContainerEvent("addApplicationParameter", parameter);
        }
    }

    @Override
    public void addChild(Container child) {
        addChild(child, false, true);
    }


    /**
     * Adds the given child (Servlet) to this context.
     *
     * @param child the child (Servlet) to add
     * @param isProgrammatic true if the given child (Servlet) is being added via one of the
     *            programmatic interfaces, and false if it is declared in the deployment descriptor
     * @param createRegistration true if a ServletRegistration needs to be created for the given
     *            child, and false if a (preliminary) ServletRegistration had already been created
     *            (which would be the case if the Servlet had been declared in the deployment
     *            descriptor without any servlet-class, and the servlet-class was later provided via
     *            ServletContext#addServlet)
     * @throws IllegalArgumentException if the given child Container is not an instance of Wrapper
     */
    protected void addChild(Container child, boolean isProgrammatic, boolean createRegistration) {
        if (!(child instanceof Wrapper)) {
            throw new IllegalArgumentException(rb.getString(NO_WRAPPER_EXCEPTION));
        }

        Wrapper wrapper = (Wrapper) child;
        String wrapperName = child.getName();

        if (createRegistration) {
            ServletRegistrationImpl servletRegistration = null;
            if (isProgrammatic || (null == wrapper.getServletClassName() && null == wrapper.getJspFile())) {
                servletRegistration = createDynamicServletRegistrationImpl((StandardWrapper) wrapper);
            } else {
                servletRegistration = createServletRegistrationImpl((StandardWrapper) wrapper);
            }

            servletRegistrationMap.put(wrapperName, servletRegistration);

            if (isAllNull(wrapper.getServletClassName(), wrapper.getJspFile())) {
                // Preliminary registration for Servlet that was declared without any servlet-class.
                // Once the registration is completed via ServletContext#addServlet, addChild will
                // be called again, and 'wrapper' will have been configured with a proper class name
                // at that time
                return;
            }
        }

        if ("jakarta.faces.webapp.FacesServlet".equals(wrapper.getServletClassName())) {
            isJsfApplication = true;
        }

        // Global JspServlet
        Wrapper oldJspServlet = null;

        // Allow webapp to override JspServlet inherited from global web.xml.
        boolean isJspServlet = "jsp".equals(wrapperName);
        if (isJspServlet) {
            oldJspServlet = (Wrapper) findChild("jsp");
            if (oldJspServlet != null) {
                removeChild(oldJspServlet);
            }
        }

        String jspFile = wrapper.getJspFile();
        if ((jspFile != null) && !jspFile.startsWith("/")) {
            if (isServlet22()) {
                log.log(FINE, JSP_FILE_FINE, jspFile);
                wrapper.setJspFile("/" + jspFile);
            } else {
                throw new IllegalArgumentException(format(rb.getString(WRAPPER_ERROR_EXCEPTION), jspFile));
            }
        }

        super.addChild(child);

        if (getAvailable()) {
            // If this StandardContext has already been started, we need to register the newly added
            // child with JMX. Any children that were added before this StandardContext was started
            // have already been registered with JMX (as part of StandardContext.start()).
            if (wrapper instanceof StandardWrapper) {
                ((StandardWrapper) wrapper).registerJMX(this);
            }
        }

        if (isJspServlet && oldJspServlet != null) {
            // The webapp-specific JspServlet inherits all the mappings specified in the global
            // web.xml, and may add additional ones.
            String[] jspMappings = oldJspServlet.findMappings();
            if (jspMappings != null) {
                for (String jspMapping : jspMappings) {
                    addServletMapping(jspMapping, wrapperName);
                }
            }
        }
    }

    protected ServletRegistrationImpl createServletRegistrationImpl(StandardWrapper wrapper) {
        return new ServletRegistrationImpl(wrapper, this);
    }

    protected ServletRegistrationImpl createDynamicServletRegistrationImpl(StandardWrapper wrapper) {
        return new DynamicServletRegistrationImpl(wrapper, this);
    }

    /**
     * Add a security constraint to the set for this web application.
     */
    @Override
    public void addConstraint(SecurityConstraint constraint) {
        // Validate the proposed constraint
        SecurityCollection collections[] = constraint.findCollections();

        for (SecurityCollection collection : collections) {
            String patterns[] = collection.findPatterns();
            for (int j = 0; j < patterns.length; j++) {
                patterns[j] = adjustURLPattern(patterns[j]);
                if (!validateURLPattern(patterns[j])) {
                    throw new IllegalArgumentException(format(rb.getString(SECURITY_CONSTRAINT_PATTERN_EXCEPTION), patterns[j]));
                }
            }
        }

        // Add this constraint to the set for our web application
        constraints.add(constraint);
    }

    @Override
    public void addEjb(ContextEjb ejb) {
        namingResources.addEjb(ejb);
        if (notifyContainerListeners) {
            fireContainerEvent("addEjb", ejb.getName());
        }
    }

    @Override
    public void addEnvironment(ContextEnvironment environment) {
        ContextEnvironment existingEnvironment = findEnvironment(environment.getName());
        if (existingEnvironment != null && !existingEnvironment.getOverride()) {
            return;
        }
        namingResources.addEnvironment(environment);
        if (notifyContainerListeners) {
            fireContainerEvent("addEnvironment", environment.getName());
        }
    }

    /**
     * Add resource parameters for this web application.
     *
     * @param resourceParameters New resource parameters
     */
    public void addResourceParams(ResourceParams resourceParameters) {
        namingResources.addResourceParams(resourceParameters);
        if (notifyContainerListeners) {
            fireContainerEvent("addResourceParams", resourceParameters.getName());
        }
    }

    @Override
    public void addErrorPage(ErrorPage errorPage) {
        // Validate the input parameters
        if (errorPage == null) {
            throw new IllegalArgumentException(rb.getString(ERROR_PAGE_REQUIRED_EXCEPTION));
        }

        String location = errorPage.getLocation();
        if ((location != null) && !location.startsWith("/")) {
            if (isServlet22()) {
                log.log(FINE, LogFacade.ERROR_PAGE_LOCATION_EXCEPTION);
                errorPage.setLocation("/" + location);
            } else {
                throw new IllegalArgumentException(format(rb.getString(ERROR_PAGE_LOCATION_EXCEPTION), location));
            }
        }

        // Add the specified error page to our internal collections
        String exceptionType = errorPage.getExceptionType();
        if (exceptionType != null) {
            synchronized (exceptionPages) {
                exceptionPages.put(exceptionType, errorPage);
            }
        } else if (errorPage.getErrorCode() > 0) {
            synchronized (statusPages) {
                int errorCode = errorPage.getErrorCode();
                if (errorCode >= 400 && errorCode < 600) {
                    statusPages.put(errorCode, errorPage);
                } else {
                    log.log(SEVERE, INVALID_ERROR_PAGE_CODE_EXCEPTION, errorCode);
                }
            }
        } else {
            defaultErrorPage = errorPage;
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addErrorPage", errorPage);
        }
    }

    @Override
    public void addFilterDef(FilterDef filterDef) {
        addFilterDef(filterDef, false, true);
    }

    public void addFilterDef(FilterDef filterDef, boolean isProgrammatic, boolean createRegistration) {
        log.log(Level.CONFIG, "addFilterDef(filterDef={0}, isProgrammatic={1}, createRegistration={2})",
            new Object[] {filterDef, isProgrammatic, createRegistration});
        if (createRegistration) {
            final FilterRegistrationImpl filterRegistration;
            if (isProgrammatic || filterDef.getFilterClassName() == null) {
                filterRegistration = new DynamicFilterRegistrationImpl(filterDef, this);
            } else {
                filterRegistration = new FilterRegistrationImpl(filterDef, this);
            }

            filterRegistrationMap.put(filterDef.getFilterName(), filterRegistration);
            if (filterDef.getFilterClassName() == null) {
                // Preliminary registration for Filter that was declared without any filter-class.
                // Once the registration is completed via ServletContext#addFilter, addFilterDef
                // will be called again, and 'filterDef' will have been configured with a proper
                // class name at that time
                return;
            }
        }

        synchronized (filterDefs) {
            filterDefs.put(filterDef.getFilterName(), filterDef);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addFilterDef", filterDef);
        }
    }


    /**
     * Add multiple filter mappings to this Context.
     *
     * @param filterMaps The filter mappings to be added
     * @throws IllegalArgumentException if the specified filter name does not match an existing
     *             filter definition, or the filter mapping is malformed
     */
    public void addFilterMaps(FilterMaps filterMaps) {
        String[] servletNames = filterMaps.getServletNames();
        String[] urlPatterns = filterMaps.getURLPatterns();

        for (String servletName : servletNames) {
            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName(filterMaps.getFilterName());
            filterMap.setServletName(servletName);
            filterMap.setDispatcherTypes(filterMaps.getDispatcherTypes());
            addFilterMap(filterMap);
        }

        for (String urlPattern : urlPatterns) {
            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName(filterMaps.getFilterName());
            filterMap.setURLPattern(urlPattern);
            filterMap.setDispatcherTypes(filterMaps.getDispatcherTypes());
            addFilterMap(filterMap);
        }
    }


    /**
     * @throws IllegalArgumentException if the specified filter name does not match an existing
     *             filter definition, or the filter mapping is malformed
     */
    @Override
    public void addFilterMap(FilterMap filterMap) {
        addFilterMap(filterMap, true);
    }

    /**
     * Add a filter mapping to this Context.
     *
     * @param filterMap The filter mapping to be added
     *
     * @param isMatchAfter true if the given filter mapping should be matched against requests after any declared filter
     * mappings of this servlet context, and false if it is supposed to be matched before any declared filter mappings of
     * this servlet context
     *
     * @throws IllegalArgumentException if the specified filter name does not match an existing filter definition, or the
     * filter mapping is malformed
     *
     */
    public void addFilterMap(FilterMap filterMap, boolean isMatchAfter) {
        // Validate the proposed filter mapping
        String filterName = filterMap.getFilterName();
        String servletName = filterMap.getServletName();
        String urlPattern = filterMap.getURLPattern();

        if (filterRegistrationMap.get(filterName) == null) {
            throw new IllegalArgumentException(format(rb.getString(FILTER_MAPPING_NAME_EXCEPTION), filterName));
        }
        if (servletName == null && urlPattern == null) {
            throw new IllegalArgumentException(rb.getString(LogFacade.FILTER_MAPPING_EITHER_EXCEPTION));
        }
        if (servletName != null && urlPattern != null) {
            throw new IllegalArgumentException(rb.getString(LogFacade.FILTER_MAPPING_EITHER_EXCEPTION));
        }

        // Because filter-pattern is new in 2.3, no need to adjust
        // for 2.2 backwards compatibility
        if (urlPattern != null && !validateURLPattern(urlPattern)) {
            throw new IllegalArgumentException(format(rb.getString(FILTER_MAPPING_INVALID_URL_EXCEPTION), urlPattern));
        }

        // Add this filter mapping to our registered set
        if (isMatchAfter) {
            filterMaps.add(filterMap);
        } else {
            filterMaps.add(0, filterMap);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addFilterMap", filterMap);
        }
    }

    /**
     * @return the current servlet name mappings of the Filter with the given name.
     */
    public Collection<String> getServletNameFilterMappings(String filterName) {
        Set<String> mappings = new HashSet<>();

        synchronized (filterMaps) {
            for (FilterMap filterMap : filterMaps) {
                if (filterName.equals(filterMap.getFilterName()) && filterMap.getServletName() != null) {
                    mappings.add(filterMap.getServletName());
                }
            }
        }

        return mappings;
    }

    /**
     * @return the current URL pattern mappings of the Filter with the given name.
     */
    public Collection<String> getUrlPatternFilterMappings(String filterName) {
        Set<String> mappings = new HashSet<>();

        synchronized (filterMaps) {
            for (FilterMap filterMap : filterMaps) {
                if (filterName.equals(filterMap.getFilterName()) && filterMap.getURLPattern() != null) {
                    mappings.add(filterMap.getURLPattern());
                }
            }
        }

        return mappings;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        log.log(Level.CONFIG, "addFilter(filterName={0}, className={1})", new Object[] {filterName, className});
        if (isContextInitializedCalled) {
            throw new IllegalStateException(
                format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION), new Object[] {"addFilter", getName()}));
        }

        if (isEmpty(filterName)) {
            throw new IllegalArgumentException(rb.getString(NULL_EMPTY_FILTER_NAME_EXCEPTION));
        }

        synchronized (filterDefs) {
            // Make sure filter name is unique for this context
            FilterDef oldDef = findFilterDef(filterName);
            if (oldDef != null) {
                log.log(Level.WARNING, "There's already existing filter {0} for name {1}, I am ignoring request"
                    + " to add {2} and returning null.", new Object[] {oldDef, filterName, className});
                return null;
            }

            DynamicFilterRegistrationImpl dynamicFilterRegistration = (DynamicFilterRegistrationImpl) filterRegistrationMap.get(filterName);
            final FilterDef filterDef;
            if (dynamicFilterRegistration == null) {
                filterDef = new FilterDef();
            } else {
                // Complete preliminary filter registration
                filterDef = dynamicFilterRegistration.getFilterDefinition();
            }

            filterDef.setFilterName(filterName);
            filterDef.setFilterClassName(className);

            addFilterDef(filterDef, true, (dynamicFilterRegistration == null));
            if (dynamicFilterRegistration == null) {
                dynamicFilterRegistration = (DynamicFilterRegistrationImpl) filterRegistrationMap.get(filterName);
            }

            return dynamicFilterRegistration;
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        if (isContextInitializedCalled) {
            throw new IllegalStateException(
                format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION), new Object[] {"addFilter", getName()}));
        }

        if (filterName == null || filterName.isEmpty()) {
            throw new IllegalArgumentException(rb.getString(NULL_EMPTY_FILTER_NAME_EXCEPTION));
        }

        if (filter == null) {
            throw new IllegalArgumentException(rb.getString(NULL_FILTER_INSTANCE_EXCEPTION));
        }

        // Make sure the given Filter instance is unique across all deployed contexts
        Container host = getParent();
        if (host != null) {
            for (Container child : host.findChildren()) {
                if (child == this) {
                    // Our own context will be checked further down
                    continue;
                }
                if (((StandardContext) child).hasFilter(filter)) {
                    return null;
                }
            }
        }

        // Make sure the given Filter name and instance are unique within this context
        synchronized (filterDefs) {
            for (Entry<String, FilterDef> e : filterDefs.entrySet()) {
                if (filterName.equals(e.getKey()) || filter == e.getValue().getFilter()) {
                    return null;
                }
            }

            DynamicFilterRegistrationImpl dynamicFilterRegistration = (DynamicFilterRegistrationImpl) filterRegistrationMap.get(filterName);
            final FilterDef filterDef;
            if (dynamicFilterRegistration == null) {
                filterDef = new FilterDef();
            } else {
                // Complete preliminary filter registration
                filterDef = dynamicFilterRegistration.getFilterDefinition();
            }

            filterDef.setFilterName(filterName);
            filterDef.setFilter(filter);

            addFilterDef(filterDef, true, dynamicFilterRegistration == null);
            if (dynamicFilterRegistration == null) {
                dynamicFilterRegistration = (DynamicFilterRegistrationImpl) filterRegistrationMap.get(filterName);
            }

            return dynamicFilterRegistration;
        }
    }

    /**
     * @return true if this context contains the given Filter instance
     */
    public boolean hasFilter(Filter filter) {
        for (Entry<String, FilterDef> filterDefEntry : filterDefs.entrySet()) {
            if (filter == filterDefEntry.getValue().getFilter()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        if (isContextInitializedCalled) {
            throw new IllegalStateException(
                format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION), new Object[] {"addFilter", getName()}));
        }

        if (isEmpty(filterName)) {
            throw new IllegalArgumentException(rb.getString(NULL_EMPTY_FILTER_NAME_EXCEPTION));
        }

        synchronized (filterDefs) {
            if (findFilterDef(filterName) != null) {
                return null;
            }

            DynamicFilterRegistrationImpl dynamicFilterRegistration = (DynamicFilterRegistrationImpl) filterRegistrationMap.get(filterName);
            final FilterDef filterDef;
            if (dynamicFilterRegistration == null) {
                filterDef = new FilterDef();
            } else {
                // Complete preliminary filter registration
                filterDef = dynamicFilterRegistration.getFilterDefinition();
            }

            filterDef.setFilterName(filterName);
            filterDef.setFilterClass(filterClass);

            addFilterDef(filterDef, true, dynamicFilterRegistration == null);

            if (dynamicFilterRegistration == null) {
                dynamicFilterRegistration = (DynamicFilterRegistrationImpl) filterRegistrationMap.get(filterName);
            }

            return dynamicFilterRegistration;
        }
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        try {
            return createFilterInstance(clazz);
        } catch (Throwable t) {
            throw new ServletException("Unable to create Filter from " + "class " + clazz.getName(), t);
        }
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return filterRegistrationMap.get(filterName);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return unmodifiableMap(filterRegistrationMap);
    }

    @Override
    public synchronized SessionCookieConfig getSessionCookieConfig() {
        if (sessionCookieConfig == null) {
            sessionCookieConfig = new SessionCookieConfigImpl(this);
        }
        return sessionCookieConfig;
    }

    /**
     * Sets the name that will be assigned to any session tracking cookies created on behalf of this context
     */
    void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
        sessionCookieNameInitialized = true;
    }

    @Override
    public String getSessionCookieName() {
        return sessionCookieName;
    }

    @Override
    public String getSessionParameterName() {
        if (sessionCookieNameInitialized) {
            if (sessionCookieName != null && !sessionCookieName.isEmpty()) {
                return sessionCookieName;
            }
        }
        return Globals.SESSION_PARAMETER_NAME;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {

        if (sessionTrackingModes.contains(SessionTrackingMode.SSL)) {
            String msg = format(rb.getString(LogFacade.UNSUPPORTED_TRACKING_MODE_EXCEPTION),
                    new Object[] { SessionTrackingMode.SSL, getName() });
            throw new IllegalArgumentException(msg);
        }

        if (isContextInitializedCalled) {
            String msg = format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION),
                    new Object[] { "setSessionTrackingModes", getName() });
            throw new IllegalStateException(msg);
        }

        this.sessionTrackingModes = Collections.unmodifiableSet(sessionTrackingModes);

        if (sessionTrackingModes.contains(SessionTrackingMode.COOKIE)) {
            setCookies(true);
        } else {
            setCookies(false);
        }

        if (sessionTrackingModes.contains(SessionTrackingMode.URL)) {
            setEnableURLRewriting(true);
        } else {
            setEnableURLRewriting(false);
        }
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return EnumSet.copyOf(DEFAULT_SESSION_TRACKING_MODES);
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return sessionTrackingModes == null ? getDefaultSessionTrackingModes() : new HashSet<>(sessionTrackingModes);
    }

    @Override
    public void addListener(String className) {
        addListener(className, true);
    }


    /**
     * Adds the listener with the given class name to this ServletContext.
     *
     * @param className the fully qualified class name of the listener
     * @param isProgrammatic true if the listener is being added programmatically, and false if it
     *            has been declared in the deployment descriptor
     */
    private void addListener(String className, boolean isProgrammatic) {
        final EventListener listener;
        try {
            listener = loadListener(getClassLoader(), className);
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
        addListener(listener, isProgrammatic);
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        addListener(t, true);
    }


    /**
     * Adds the given listener instance to this ServletContext.
     *
     * @param listener the listener to be added
     * @param isProgrammatic true if the listener is being added programmatically, and false if it
     *            has been declared in the deployment descriptor
     */
    private <T extends EventListener> void addListener(T listener, boolean isProgrammatic) {
        if (isContextInitializedCalled) {
            throw new IllegalStateException(
                format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION), new Object[] {"addListener", getName()}));
        }

        if ((listener instanceof ServletContextListener) && isProgrammatic
            && !isProgrammaticServletContextListenerRegistrationAllowed) {
            throw new IllegalArgumentException("Not allowed to register ServletContextListener programmatically");
        }

        boolean added = false;

        if (listener instanceof ServletContextAttributeListener ||
            listener instanceof ServletRequestAttributeListener ||
            listener instanceof ServletRequestListener ||
            listener instanceof HttpSessionAttributeListener ||
            listener instanceof HttpSessionIdListener) {
            eventListeners.add(listener);
            added = true;
        }

        if (listener instanceof HttpSessionListener) {
            sessionListeners.add((HttpSessionListener) listener);
            if (!added) {
                added = true;
            }
        }

        if (listener instanceof ServletContextListener) {
            ServletContextListener proxy = (ServletContextListener) listener;
            if (isProgrammatic) {
                proxy = new RestrictedServletContextListener((ServletContextListener) listener);
            }

            // Always add the Faces listener as the first element, see GlassFish Issue 2563 for details
            boolean isFirst = "com.sun.faces.config.ConfigureListener".equals(listener.getClass().getName());
            if (isFirst) {
                contextListeners.add(0, proxy);
            } else {
                contextListeners.add(proxy);
            }

            if (!added) {
                added = true;
            }
        }

        if (!added) {
            throw new IllegalArgumentException("Invalid listener type " + listener.getClass().getName());
        }
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        final EventListener listener;
        try {
            listener = createListenerInstance(listenerClass);
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
        addListener(listener);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        if (!ServletContextListener.class.isAssignableFrom(clazz) && !ServletContextAttributeListener.class.isAssignableFrom(clazz)
                && !ServletRequestListener.class.isAssignableFrom(clazz) && !ServletRequestAttributeListener.class.isAssignableFrom(clazz)
                && !HttpSessionAttributeListener.class.isAssignableFrom(clazz) && !HttpSessionIdListener.class.isAssignableFrom(clazz)
                && !HttpSessionListener.class.isAssignableFrom(clazz)) {
            String msg = format(rb.getString(LogFacade.UNABLE_ADD_LISTENER_EXCEPTION), new Object[] {clazz.getName()});
            throw new IllegalArgumentException(msg);
        }

        try {
            return createListenerInstance(clazz);
        } catch (Throwable t) {
            throw new ServletException(t);
        }
    }

    public void setJspConfigDescriptor(JspConfigDescriptor jspConfigDesc) {
        this.jspConfigDesc = jspConfigDesc;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return jspConfigDesc;
    }

    @Override
    public ClassLoader getClassLoader() {
        Loader containerLoader = getLoader();
        ClassLoader webappLoader = containerLoader == null ? null : containerLoader.getClassLoader();
        if (webappLoader == null) {
            return null;
        }

        return webappLoader;
    }

    @Override
    public void declareRoles(String... roleNames) {
        if (isContextInitializedCalled) {
            String msg = format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION),
                new Object[] {"declareRoles", getName()});
            throw new IllegalStateException(msg);
        }

        for (String roleName : roleNames) {
            addSecurityRole(roleName);
        }
    }

    public void setEffectiveMajorVersion(int effectiveMajorVersion) {
        this.effectiveMajorVersion = effectiveMajorVersion;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return effectiveMajorVersion;
    }

    public void setEffectiveMinorVersion(int effectiveMinorVersion) {
        this.effectiveMinorVersion = effectiveMinorVersion;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return effectiveMinorVersion;
    }

    @Override
    public String getVirtualServerName() {
        String virtualServerName = null;
        Container parent = getParent();
        if (parent != null) {
            virtualServerName = parent.getName();
        }
        return virtualServerName;
    }

    @Override
    public void addInstanceListener(String listener) {
        instanceListeners.add(listener);
        if (notifyContainerListeners) {
            fireContainerEvent("addInstanceListener", listener);
        }
    }

    public void addInstanceListener(InstanceListener listener) {
        instanceListenerInstances.add(listener);
        if (notifyContainerListeners) {
            fireContainerEvent("addInstanceListener", listener);
        }
    }

    @Override
    public void addJspMapping(String pattern) {
        String servletName = findServletMapping("*.jsp");
        if (servletName == null) {
            servletName = "jsp";
        }

        if (findChild(servletName) != null) {
            addServletMapping(pattern, servletName, true);
        } else {
            if (log.isLoggable(FINE)) {
                log.log(FINE, "Skipping " + pattern + " , no servlet " + servletName);
            }
        }
    }

    @Override
    public void addLocaleEncodingMappingParameter(String locale, String encoding) {
        getCharsetMapper().addCharsetMappingFromDeploymentDescriptor(locale, encoding);
    }

    @Override
    public void addLocalEjb(ContextLocalEjb ejb) {
        namingResources.addLocalEjb(ejb);
        if (notifyContainerListeners) {
            fireContainerEvent("addLocalEjb", ejb.getName());
        }
    }

    /**
     * Add a message destination for this web application.
     *
     * @param md New message destination
     */
    public void addMessageDestination(MessageDestination md) {
        synchronized (messageDestinations) {
            messageDestinations.put(md.getName(), md);
        }
        if (notifyContainerListeners) {
            fireContainerEvent("addMessageDestination", md.getName());
        }
    }

    /**
     * Add a message destination reference for this web application.
     *
     * @param mdr New message destination reference
     */
    public void addMessageDestinationRef(MessageDestinationRef mdr) {
        namingResources.addMessageDestinationRef(mdr);
        if (notifyContainerListeners) {
            fireContainerEvent("addMessageDestinationRef", mdr.getName());
        }
    }

    @Override
    public void addMimeMapping(String extension, String mimeType) {
        mimeMappings.put(extension.toLowerCase(Locale.ENGLISH), mimeType);
        if (notifyContainerListeners) {
            fireContainerEvent("addMimeMapping", extension);
        }
    }


    @Override
    public void addParameter(String name, String value) {
        // Validate the proposed context initialization parameter
        if ((name == null) || (value == null)) {
            String msg = format(rb.getString(LogFacade.PARAMETER_REQUIRED_EXCEPTION), name);
            throw new IllegalArgumentException(msg);
        }
        if (parameters.get(name) != null) {
            String msg = format(rb.getString(LogFacade.DUPLICATE_PARAMETER_EXCEPTION), name);
            throw new IllegalArgumentException(msg);
        }

        // Add this parameter to our defined set
        synchronized (parameters) {
            parameters.put(name, value);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("addParameter", name);
        }
    }

    @Override
    public void addResource(ContextResource resource) {
        namingResources.addResource(resource);
        if (notifyContainerListeners) {
            fireContainerEvent("addResource", resource.getName());
        }
    }

    @Override
    public void addResourceEnvRef(String name, String type) {
        namingResources.addResourceEnvRef(name, type);
        if (notifyContainerListeners) {
            fireContainerEvent("addResourceEnvRef", name);
        }
    }

    @Override
    public void addResourceLink(ContextResourceLink resourceLink) {
        namingResources.addResourceLink(resourceLink);
        if (notifyContainerListeners) {
            fireContainerEvent("addResourceLink", resourceLink.getName());
        }
    }

    @Override
    public void addRoleMapping(String role, String link) {
        synchronized (roleMappings) {
            roleMappings.put(role, link);
        }
        if (notifyContainerListeners) {
            fireContainerEvent("addRoleMapping", role);
        }
    }

    @Override
    public void addSecurityRole(String role) {
        securityRoles.add(role);
        if (notifyContainerListeners) {
            fireContainerEvent("addSecurityRole", role);
        }
    }


    /**
     * Adds the given servlet mappings to this Context.
     * <p>
     * If any of the specified URL patterns are already mapped to a different Servlet, no updates
     * will be performed.
     *
     * @param servletMap the Servlet mappings containing the Servlet name and URL patterns
     * @return (possibly empty) Set of URL patterns that are already mapped to a different Servlet
     * @throws IllegalArgumentException if the specified servlet name is not known to this Context
     */
    public Set<String> addServletMapping(ServletMap servletMap) {
        return addServletMapping(servletMap.getServletName(), servletMap.getURLPatterns());
    }


    /**
     * Adds the given servlet mappings to this Context.
     * <p>
     * If any of the specified URL patterns are already mapped to a different Servlet, no updates
     * will be performed.
     *
     * @param name the Servlet name
     * @param urlPatterns the URL patterns
     * @return (possibly empty) Set of URL patterns that are already mapped to a different Servlet
     * @throws IllegalArgumentException if the specified servlet name is not known to this Context
     */
    public Set<String> addServletMapping(String name, String[] urlPatterns) {
        Set<String> conflicts = null;

        synchronized (servletMappings) {
            for (String pattern : urlPatterns) {
                pattern = adjustURLPattern(urlDecode(pattern));
                if (!validateURLPattern(pattern)) {
                    throw new IllegalArgumentException(format(rb.getString(SERVLET_MAPPING_INVALID_URL_EXCEPTION), pattern));
                }

                // Ignore any conflicts with the container provided
                // Default- and JspServlet
                String existing = servletMappings.get(pattern);
                if (existing != null && !existing.equals(DEFAULT_SERVLET_NAME) && !existing.equals(JSP_SERVLET_NAME)
                        && !name.equals(DEFAULT_SERVLET_NAME) && !name.equals(JSP_SERVLET_NAME)) {
                    if (conflicts == null) {
                        conflicts = new HashSet<>();
                    }
                    conflicts.add(pattern);
                }
            }

            if (conflicts == null) {
                for (String urlPattern : urlPatterns) {
                    addServletMapping(urlPattern, name, false);
                }
                return Collections.emptySet();
            }

            return conflicts;
        }
    }

    @Override
    public void addServletMapping(String pattern, String name) {
        addServletMapping(pattern, name, false);
    }


    /**
     * Adds the given servlet mapping to this Context, overriding any existing mapping for the
     * specified pattern.
     *
     * @param pattern URL pattern to be mapped
     * @param name Name of the corresponding servlet to execute
     * @param jspWildCard true if name identifies the JspServlet and pattern contains a wildcard;
     *            false otherwise
     * @throws IllegalArgumentException if the specified servlet name is not known to this Context
     */
    public void addServletMapping(String pattern, String name, boolean jspWildCard) {
        // Validate the proposed mapping
        ServletRegistrationImpl servletRegistration = servletRegistrationMap.get(name);
        if (servletRegistration == null) {
            throw new IllegalArgumentException(format(rb.getString(SERVLET_MAPPING_UNKNOWN_NAME_EXCEPTION), name));
        }

        pattern = adjustURLPattern(RequestUtil.urlDecode(pattern));
        if (!validateURLPattern(pattern)) {
            throw new IllegalArgumentException(
                format(rb.getString(LogFacade.SERVLET_MAPPING_INVALID_URL_EXCEPTION), pattern));
        }

        // Add this mapping to our registered set. Make sure that it is possible to override
        // the mappings of the container provided Default- and JspServlet, and that these servlets
        // are prevented from overriding any user-defined mappings (depending on the order in which
        // the contents of the default-web.xml are merged with those of the app's deployment
        // descriptor). This is to prevent the DefaultServlet from hijacking '/', and the JspServlet
        // from hijacking *.jsp(x).
        synchronized (servletMappings) {
            String existing = servletMappings.get(pattern);
            if (existing != null) {
                if (!existing.equals(DEFAULT_SERVLET_NAME) && !existing.equals(JSP_SERVLET_NAME)
                    && !name.equals(DEFAULT_SERVLET_NAME) && !name.equals(JSP_SERVLET_NAME)) {
                    throw new IllegalArgumentException(format(rb.getString(DUPLICATE_SERVLET_MAPPING_EXCEPTION),
                        new Object[] {name, pattern, existing}));
                }

                if (existing.equals(DEFAULT_SERVLET_NAME) || existing.equals(JSP_SERVLET_NAME)) {
                    // Override the mapping of the container provided Default- or JspServlet
                    removePatternFromServlet((Wrapper) findChild(existing), pattern);
                    mapper.removeWrapper(pattern);
                    servletMappings.put(pattern, name);
                }
            } else {
                servletMappings.put(pattern, name);
            }
        }

        Wrapper wrapper = servletRegistration.getWrapper();
        wrapper.addMapping(pattern);

        // Update context mapper
        mapper.addWrapper(pattern, wrapper, jspWildCard, name, true);

        if (notifyContainerListeners) {
            fireContainerEvent("addServletMapping", pattern);
        }
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        if (isContextInitializedCalled) {
            throw new IllegalStateException(
                format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION), new Object[] {"addServlet", getName()}));
        }

        if (isEmpty(servletName)) {
            throw new IllegalArgumentException(rb.getString(NULL_EMPTY_SERVLET_NAME_EXCEPTION));
        }

        synchronized (children) {
            if (findChild(servletName) != null) {
                return null;
            }

            DynamicServletRegistrationImpl dynamicServletRegistration = (DynamicServletRegistrationImpl) servletRegistrationMap.get(servletName);
            Wrapper wrapper = null;
            if (dynamicServletRegistration == null) {
                wrapper = createWrapper();
                wrapper.setServletClassName(className);
            } else {
                // Complete preliminary servlet registration
                wrapper = dynamicServletRegistration.getWrapper();
                dynamicServletRegistration.setServletClassName(className);
            }

            wrapper.setName(servletName);
            addChild(wrapper, true, (null == dynamicServletRegistration));
            if (null == dynamicServletRegistration) {
                dynamicServletRegistration = (DynamicServletRegistrationImpl) servletRegistrationMap.get(servletName);
            }

            return dynamicServletRegistration;
        }
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, servlet, null, null);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        if (isContextInitializedCalled) {
            throw new IllegalStateException(
                format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION), new Object[] {"addServlet", getName()}));
        }

        if (isEmpty(servletName)) {
            throw new IllegalArgumentException(rb.getString(NULL_EMPTY_SERVLET_NAME_EXCEPTION));
        }

        // Make sure servlet name is unique for this context
        synchronized (children) {
            if (findChild(servletName) != null) {
                return null;
            }

            DynamicServletRegistrationImpl dynamicServletRegistration = (DynamicServletRegistrationImpl) servletRegistrationMap.get(servletName);
            Wrapper wrapper = null;
            if (dynamicServletRegistration == null) {
                wrapper = createWrapper();
                wrapper.setServletClass(servletClass);
            } else {
                // Complete preliminary servlet registration
                wrapper = dynamicServletRegistration.getWrapper();
                dynamicServletRegistration.setServletClass(servletClass);
            }

            wrapper.setName(servletName);
            addChild(wrapper, true, (null == dynamicServletRegistration));

            if (dynamicServletRegistration == null) {
                dynamicServletRegistration = (DynamicServletRegistrationImpl) servletRegistrationMap.get(servletName);
            }

            return dynamicServletRegistration;
        }
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet instance, Map<String, String> initParams) {
        return addServlet(servletName, instance, initParams, null);
    }


    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet, Map<String, String> initParams, String... urlPatterns) {
        if (isContextInitializedCalled) {
            throw new IllegalStateException(
                format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION), new Object[] {"addServlet", getName()}));
        }

        if (isEmpty(servletName)) {
            throw new IllegalArgumentException(rb.getString(NULL_EMPTY_SERVLET_NAME_EXCEPTION));
        }

        if (servlet == null) {
            throw new NullPointerException(rb.getString(NULL_SERVLET_INSTANCE_EXCEPTION));
        }

        // Make sure the given Servlet instance is unique across all deployed contexts
        Container host = getParent();
        if (host != null) {
            for (Container child : host.findChildren()) {
                if (child == this) {
                    // Our own context will be checked further down
                    continue;
                }
                if (((StandardContext) child).hasServlet(servlet)) {
                    return null;
                }
            }
        }

        // Make sure the given Servlet name and instance are unique within this context
        synchronized (children) {
            for (Entry<String, Container> e : children.entrySet()) {
                if (servletName.equals(e.getKey()) || servlet == ((StandardWrapper) e.getValue()).getServlet()) {
                    return null;
                }
            }

            DynamicServletRegistrationImpl dynamicServletRegistration = (DynamicServletRegistrationImpl) servletRegistrationMap.get(servletName);
            StandardWrapper wrapper = null;
            if (dynamicServletRegistration == null) {
                wrapper = (StandardWrapper) createWrapper();
            } else {
                // Complete preliminary servlet registration
                wrapper = dynamicServletRegistration.getWrapper();
            }

            wrapper.setName(servletName);
            wrapper.setServlet(servlet);
            if (initParams != null) {
                for (Entry<String, String> e : initParams.entrySet()) {
                    wrapper.addInitParameter(e.getKey(), e.getValue());
                }
            }

            addChild(wrapper, true, (null == dynamicServletRegistration));
            if (dynamicServletRegistration == null) {
                dynamicServletRegistration = (DynamicServletRegistrationImpl) servletRegistrationMap.get(servletName);
            }

            if (urlPatterns != null) {
                for (String urlPattern : urlPatterns) {
                    addServletMapping(urlPattern, servletName, false);
                }
            }

            return dynamicServletRegistration;
        }
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        if (isContextInitializedCalled) {
            throw new IllegalStateException(
                format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION), new Object[] {"addJspFile", getName()}));
        }

        if (isEmpty(servletName)) {
            throw new IllegalArgumentException(rb.getString(NULL_EMPTY_SERVLET_NAME_EXCEPTION));
        }

        synchronized (children) {
            if (findChild(servletName) != null) {
                return null;
            }

            DynamicServletRegistrationImpl dynamicServletRegistration = (DynamicServletRegistrationImpl) servletRegistrationMap.get(servletName);
            Wrapper wrapper = null;
            if (dynamicServletRegistration == null) {
                wrapper = createWrapper();
            } else {
                // Override an existing registration
                wrapper = dynamicServletRegistration.getWrapper();
            }

            wrapper.setJspFile(jspFile);
            wrapper.setName(servletName);
            addChild(wrapper, true, (null == dynamicServletRegistration));

            if (dynamicServletRegistration == null) {
                dynamicServletRegistration = (DynamicServletRegistrationImpl) servletRegistrationMap.get(servletName);
            }

            return dynamicServletRegistration;
        }
    }

    /**
     * This method is overridden in web-glue to also remove the given mapping from the deployment backend's
     * WebBundleDescriptor.
     */
    protected void removePatternFromServlet(Wrapper wrapper, String pattern) {
        wrapper.removeMapping(pattern);
    }

    /**
     * @return true if this context contains the given Servlet instance
     */
    public boolean hasServlet(Servlet servlet) {
        for (Entry<String, Container> e : children.entrySet()) {
            if (servlet == ((StandardWrapper) e.getValue()).getServlet()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        try {
            return createServletInstance(clazz);
        } catch (Throwable t) {
            throw new ServletException("Unable to create Servlet from " + "class " + clazz.getName(), t);
        }
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return servletRegistrationMap.get(servletName);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return unmodifiableMap(servletRegistrationMap);
    }

    @Override
    public void addWatchedResource(String name) {
        watchedResources.add(name);
        fireContainerEvent("addWatchedResource", name);
    }

    @Override
    public void addWelcomeFile(String name) {
        // Welcome files from the application deployment descriptor
        // completely replace those from the default conf/web.xml file
        if (replaceWelcomeFiles) {
            welcomeFiles = new String[0];
            setReplaceWelcomeFiles(false);
        }

        String results[] = new String[welcomeFiles.length + 1];
        for (int i = 0; i < welcomeFiles.length; i++) {
            results[i] = welcomeFiles[i];
        }

        results[welcomeFiles.length] = name;
        welcomeFiles = results;

        if (notifyContainerListeners) {
            fireContainerEvent("addWelcomeFile", name);
        }
    }

    @Override
    public void addWrapperLifecycle(String listener) {
        wrapperLifecycles.add(listener);
        if (notifyContainerListeners) {
            fireContainerEvent("addWrapperLifecycle", listener);
        }
    }

    @Override
    public void addWrapperListener(String listener) {
        wrapperListeners.add(listener);
        if (notifyContainerListeners) {
            fireContainerEvent("addWrapperListener", listener);
        }
    }


    @Override
    public Wrapper createWrapper() {
        Wrapper wrapper = new StandardWrapper();

        synchronized (instanceListeners) {
            for (String instanceListener : instanceListeners) {
                try {
                    wrapper.addInstanceListener((InstanceListener) Class.forName(instanceListener).getDeclaredConstructor().newInstance());
                } catch (Throwable t) {
                    log.log(SEVERE, format(rb.getString(LogFacade.CREATING_INSTANCE_LISTENER_EXCEPTION), instanceListener), t);
                    return null;
                }
            }
        }

        synchronized (instanceListenerInstances) {
            for (InstanceListener instanceListenerInstance : instanceListenerInstances) {
                wrapper.addInstanceListener(instanceListenerInstance);
            }
        }

        Iterator<String> i = wrapperLifecycles.iterator();
        while (i.hasNext()) {
            String wrapperLifecycle = i.next();
            try {
                if (wrapper instanceof Lifecycle) {
                    ((Lifecycle) wrapper).addLifecycleListener((LifecycleListener) Class.forName(wrapperLifecycle).getDeclaredConstructor().newInstance());
                }
            } catch (Throwable t) {
                log.log(SEVERE, format(rb.getString(LogFacade.CREATING_LIFECYCLE_LISTENER_EXCEPTION), wrapperLifecycle), t);
                return null;
            }
        }

        i = wrapperListeners.iterator();
        while (i.hasNext()) {
            String wrapperListener = i.next();
            try {
                wrapper.addContainerListener((ContainerListener) Class.forName(wrapperListener).getDeclaredConstructor().newInstance());
            } catch (Throwable t) {
                log.log(SEVERE, format(rb.getString(LogFacade.CREATING_CONTAINER_LISTENER_EXCEPTION), wrapperListener), t);
                return null;
            }
        }

        return wrapper;
    }

    @Override
    public List<ApplicationParameter> findApplicationParameters() {
        return applicationParameters;
    }

    @Override
    public List<SecurityConstraint> getConstraints() {
        return constraints;
    }

    @Override
    public boolean hasConstraints() {
        return !constraints.isEmpty();
    }

    @Override
    public ContextEjb findEjb(String name) {
        return namingResources.findEjb(name);
    }

    @Override
    public ContextEjb[] findEjbs() {
        return namingResources.findEjbs();
    }

    @Override
    public ContextEnvironment findEnvironment(String name) {
        return namingResources.findEnvironment(name);
    }

    @Override
    public ContextEnvironment[] findEnvironments() {
        return namingResources.findEnvironments();
    }

    @Override
    public ErrorPage findErrorPage(int errorCode) {
        if ((errorCode >= 400) && (errorCode < 600)) {
            return statusPages.get(errorCode);
        }
        return null;
    }

    @Override
    public ErrorPage findErrorPage(String exceptionType) {
        synchronized (exceptionPages) {
            return exceptionPages.get(exceptionType);
        }
    }

    @Override
    public ErrorPage getDefaultErrorPage() {
        return defaultErrorPage;
    }

    @Override
    public FilterDef findFilterDef(String filterName) {
        synchronized (filterDefs) {
            return filterDefs.get(filterName);
        }
    }

    @Override
    public FilterDef[] findFilterDefs() {
        synchronized (filterDefs) {
            FilterDef results[] = new FilterDef[filterDefs.size()];
            return filterDefs.values().toArray(results);
        }
    }

    @Override
    public List<FilterMap> findFilterMaps() {
        return filterMaps;
    }

    @Override
    public List<String> findInstanceListeners() {
        return instanceListeners;
    }

    @Override
    public ContextLocalEjb findLocalEjb(String name) {
        return namingResources.findLocalEjb(name);
    }

    @Override
    public ContextLocalEjb[] findLocalEjbs() {
        return namingResources.findLocalEjbs();
    }

    /**
     * FIXME: Fooling introspection ...
     */
    public Context findMappingObject() {
        return (Context) getMappingObject();
    }


    /**
     * @param name Name of the desired message destination
     * @return the message destination with the specified name, if any; otherwise, return <code>null</code>.
     */
    public MessageDestination findMessageDestination(String name) {
        synchronized (messageDestinations) {
            return messageDestinations.get(name);
        }
    }


    /**
     * @return the set of defined message destinations for this web application. If none have been
     *         defined, a zero-length array is returned.
     */
    public MessageDestination[] findMessageDestinations() {
        synchronized (messageDestinations) {
            return messageDestinations.values().toArray(new MessageDestination[messageDestinations.size()]);
        }
    }


    /**
     * @param name Name of the desired message destination ref
     * @return the message destination ref with the specified name, if any; otherwise, return <code>null</code>.
     */
    public MessageDestinationRef findMessageDestinationRef(String name) {
        return namingResources.findMessageDestinationRef(name);
    }


    /**
     * @return the set of defined message destination refs for this web application. If none have
     *         been defined, a zero-length array is returned.
     */
    public MessageDestinationRef[] findMessageDestinationRefs() {
        return namingResources.findMessageDestinationRefs();
    }

    @Override
    public String findMimeMapping(String extension) {
        return mimeMappings.get(extension.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public String[] findMimeMappings() {
        return mimeMappings.keySet().toArray(new String[mimeMappings.size()]);
    }

    @Override
    public String findParameter(String name) {
        synchronized (parameters) {
            return parameters.get(name);
        }
    }

    @Override
    public String[] findParameters() {
        synchronized (parameters) {
            return parameters.keySet().toArray(new String[parameters.size()]);
        }
    }

    @Override
    public ContextResource findResource(String name) {
        return namingResources.findResource(name);
    }

    @Override
    public String findResourceEnvRef(String name) {
        return namingResources.findResourceEnvRef(name);
    }

    @Override
    public String[] findResourceEnvRefs() {
        return namingResources.findResourceEnvRefs();
    }

    @Override
    public ContextResourceLink findResourceLink(String name) {
        return namingResources.findResourceLink(name);
    }

    @Override
    public ContextResourceLink[] findResourceLinks() {
        return namingResources.findResourceLinks();
    }

    @Override
    public ContextResource[] findResources() {
        return namingResources.findResources();
    }

    @Override
    public String findRoleMapping(String role) {
        final String realRole;
        synchronized (roleMappings) {
            realRole = roleMappings.get(role);
        }
        if (realRole != null) {
            return realRole;
        }
        return role;
    }

    @Override
    public boolean hasSecurityRole(String role) {
        return securityRoles.contains(role);
    }

    @Override
    public void removeSecurityRoles() {
        // Inform interested listeners
        if (notifyContainerListeners) {
            for (String securityRole : securityRoles) {
                fireContainerEvent("removeSecurityRole", securityRole);
            }
        }

        securityRoles.clear();
    }

    @Override
    public String findServletMapping(String pattern) {
        synchronized (servletMappings) {
            return servletMappings.get(pattern);
        }
    }

    @Override
    public String[] findServletMappings() {
        synchronized (servletMappings) {
            String results[] = new String[servletMappings.size()];
            return servletMappings.keySet().toArray(results);
        }
    }

    @Override
    public ErrorPage findStatusPage(int status) {
        return statusPages.get(status);
    }

    @Override
    public int[] findStatusPages() {
        synchronized (statusPages) {
            int[] results = new int[statusPages.size()];
            int i = 0;
            for (Integer element : statusPages.keySet()) {
                results[i++] = element;
            }
            return results;
        }
    }

    @Override
    public boolean findWelcomeFile(String name) {
        synchronized (welcomeFiles) {
            for (String welcomeFile : welcomeFiles) {
                if (name.equals(welcomeFile)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> getWatchedResources() {
        return watchedResources;
    }

    @Override
    public String[] findWelcomeFiles() {
        return welcomeFiles;
    }

    @Override
    public List<String> findWrapperLifecycles() {
        return wrapperLifecycles;
    }

    @Override
    public List<String> findWrapperListeners() {
        return wrapperListeners;
    }

    @Override
    public Authenticator getAuthenticator() {
        Pipeline pipeline = getPipeline();
        if (pipeline != null) {
            for (GlassFishValve valve : pipeline.getValves()) {
                if (valve instanceof Authenticator) {
                    return (Authenticator) valve;
                }
            }
        }

        return null;
    }


    /**
     * Reload this web application, if reloading is supported.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>: This method is designed to deal with reloads required by changes
     * to classes in the underlying repositories of our class loader. It does not handle changes to
     * the web application deployment descriptor.
     * If that has occurred, you should stop this Context and create (and start) a new Context
     * instance instead.
     *
     * @throws IllegalStateException if the <code>started</code> property is set to <code>false</code>.
     */
    @Override
    public synchronized void reload() {
        // Validate our current component state
        if (!started) {
            throw new IllegalStateException(format(rb.getString(CONTAINER_NOT_STARTED_EXCEPTION), logName()));
        }

        log.log(INFO, RELOADING_STARTED);

        // Stop accepting requests temporarily
        setPaused(true);

        try {
            stop();
        } catch (LifecycleException e) {
            log.log(SEVERE, format(rb.getString(STOPPING_CONTEXT_EXCEPTION), this), e);
        }

        try {
            start();
        } catch (LifecycleException e) {
            log.log(SEVERE, format(rb.getString(STARTING_CONTEXT_EXCEPTION), this), e);
        }

        setPaused(false);
    }

    @Override
    public void removeApplicationParameter(String name) {
        ApplicationParameter match = null;
        for (ApplicationParameter applicationParameter : applicationParameters) {
            // Make sure this parameter is currently present
            if (name.equals(applicationParameter.getName())) {
                match = applicationParameter;
                break;
            }
        }

        if (match != null) {
            applicationParameters.remove(match);
            // Inform interested listeners
            if (notifyContainerListeners) {
                fireContainerEvent("removeApplicationParameter", name);
            }
        }
    }

    /**
     * @throws IllegalArgumentException if the given child container is not an implementation of Wrapper
     */
    @Override
    public void removeChild(Container child) {
        if (!(child instanceof Wrapper)) {
            throw new IllegalArgumentException(rb.getString(NO_WRAPPER_EXCEPTION));
        }

        super.removeChild(child);
    }

    @Override
    public void removeConstraints() {
        // Inform interested listeners
        if (notifyContainerListeners) {
            for (SecurityConstraint constraint : constraints) {
                fireContainerEvent("removeConstraint", constraint);
            }
        }

        constraints.clear();
    }

    @Override
    public void removeEjb(String name) {
        namingResources.removeEjb(name);
        if (notifyContainerListeners) {
            fireContainerEvent("removeEjb", name);
        }
    }

    @Override
    public void removeEnvironment(String name) {
        if (namingResources == null) {
            return;
        }

        ContextEnvironment env = namingResources.findEnvironment(name);
        if (env == null) {
            throw new IllegalArgumentException("Invalid environment name '" + name + "'");
        }

        namingResources.removeEnvironment(name);

        if (notifyContainerListeners) {
            fireContainerEvent("removeEnvironment", name);
        }
    }

    @Override
    public void removeErrorPages() {
        synchronized (exceptionPages) {
            if (notifyContainerListeners) {
                for (ErrorPage errorPage : exceptionPages.values()) {
                    fireContainerEvent("removeErrorPage", errorPage);
                }
            }
            exceptionPages.clear();
        }

        synchronized (statusPages) {
            if (notifyContainerListeners) {
                for (ErrorPage statusPage : statusPages.values()) {
                    fireContainerEvent("removeErrorPage", statusPage);
                }
            }
            statusPages.clear();
        }
    }

    @Override
    public void removeFilterDef(FilterDef filterDef) {
        synchronized (filterDefs) {
            filterDefs.remove(filterDef.getFilterName());
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeFilterDef", filterDef);
        }
    }

    @Override
    public void removeFilterMaps() {
        // Inform interested listeners
        if (notifyContainerListeners) {
            for (FilterMap filterMap : filterMaps) {
                fireContainerEvent("removeFilterMap", filterMap);
            }
        }

        filterMaps.clear();
    }

    @Override
    public void removeInstanceListener(String listener) {
        instanceListeners.remove(listener);

        // Inform interested listeners
        if (notifyContainerListeners) {
            fireContainerEvent("removeInstanceListener", listener);
        }
    }

    @Override
    public void removeLocalEjb(String name) {
        namingResources.removeLocalEjb(name);
        if (notifyContainerListeners) {
            fireContainerEvent("removeLocalEjb", name);
        }
    }

    /**
     * Remove any message destination with the specified name.
     *
     * @param name Name of the message destination to remove
     */
    public void removeMessageDestination(String name) {
        synchronized (messageDestinations) {
            messageDestinations.remove(name);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeMessageDestination", name);
        }
    }

    /**
     * Remove any message destination ref with the specified name.
     *
     * @param name Name of the message destination ref to remove
     */
    public void removeMessageDestinationRef(String name) {
        namingResources.removeMessageDestinationRef(name);

        if (notifyContainerListeners) {
            fireContainerEvent("removeMessageDestinationRef", name);
        }
    }

    @Override
    public void removeMimeMapping(String extension) {
        mimeMappings.remove(extension.toLowerCase(Locale.ENGLISH));

        if (notifyContainerListeners) {
            fireContainerEvent("removeMimeMapping", extension);
        }
    }

    @Override
    public void removeParameter(String name) {
        synchronized (parameters) {
            parameters.remove(name);
        }
        if (notifyContainerListeners) {
            fireContainerEvent("removeParameter", name);
        }
    }

    @Override
    public void removeResource(String resourceName) {
        String decoded = URLDecoder.decode(resourceName);
        if (namingResources == null) {
            return;
        }

        ContextResource resource = namingResources.findResource(decoded);
        if (resource == null) {
            throw new IllegalArgumentException("Invalid resource name '" + decoded + "'");
        }

        namingResources.removeResource(decoded);

        if (notifyContainerListeners) {
            fireContainerEvent("removeResource", decoded);
        }
    }

    @Override
    public void removeResourceEnvRef(String name) {
        namingResources.removeResourceEnvRef(name);

        if (notifyContainerListeners) {
            fireContainerEvent("removeResourceEnvRef", name);
        }
    }

    @Override
    public void removeResourceLink(String link) {
        String decoded = URLDecoder.decode(link);
        if (namingResources == null) {
            return;
        }

        ContextResourceLink resource = namingResources.findResourceLink(decoded);
        if (resource == null) {
            throw new IllegalArgumentException("Invalid resource name '" + decoded + "'");
        }

        namingResources.removeResourceLink(decoded);

        if (notifyContainerListeners) {
            fireContainerEvent("removeResourceLink", decoded);
        }
    }

    @Override
    public void removeRoleMapping(String role) {
        synchronized (roleMappings) {
            roleMappings.remove(role);
        }

        if (notifyContainerListeners) {
            fireContainerEvent("removeRoleMapping", role);
        }
    }

    @Override
    public void removeServletMapping(String pattern) {
        final String name;
        synchronized (servletMappings) {
            name = servletMappings.remove(pattern);
        }

        Wrapper wrapper = (Wrapper) findChild(name);
        if (wrapper != null) {
            wrapper.removeMapping(pattern);
        }

        mapper.removeWrapper(pattern);

        if (notifyContainerListeners) {
            fireContainerEvent("removeServletMapping", pattern);
        }
    }

    @Override
    public boolean hasWatchedResources() {
        return !watchedResources.isEmpty();
    }

    @Override
    public void removeWatchedResources() {
        synchronized (watchedResources) {
            // Inform interested listeners
            if (notifyContainerListeners) {
                for (String element : watchedResources) {
                    fireContainerEvent("removeWatchedResource", element);
                }
            }
            watchedResources.clear();
        }
    }

    @Override
    public void removeWelcomeFiles() {
        if (notifyContainerListeners) {
            for (String welcomeFile : welcomeFiles) {
                fireContainerEvent("removeWelcomeFile", welcomeFile);
            }
        }

        welcomeFiles = new String[0];
    }

    @Override
    public void removeWrapperLifecycles() {
        // Inform interested listeners
        if (notifyContainerListeners) {
            for (String wrapperLifecycle : wrapperLifecycles) {
                fireContainerEvent("removeWrapperLifecycle", wrapperLifecycle);
            }
        }

        wrapperLifecycles.clear();
    }

    @Override
    public void removeWrapperListeners() {
        // Inform interested listeners
        if (notifyContainerListeners) {
            for (String wrapperListener : wrapperListeners) {
                fireContainerEvent("removeWrapperListener", wrapperListener);
            }
        }

        wrapperListeners.clear();
    }

    @Override
    public void fireRequestInitializedEvent(ServletRequest request) {
        List<EventListener> listeners = getApplicationEventListeners();
        if (!listeners.isEmpty()) {
            ServletRequestEvent event = new ServletRequestEvent(getServletContext(), request);

            // Create pre-service event
            for (EventListener eventListener : listeners) {
                if (!(eventListener instanceof ServletRequestListener)) {
                    continue;
                }

                ServletRequestListener listener = (ServletRequestListener) eventListener;

                fireContainerEvent(BEFORE_REQUEST_INITIALIZED, listener);
                try {
                    listener.requestInitialized(event);
                } catch (Throwable t) {
                    log.log(WARNING, format(rb.getString(REQUEST_INIT_EXCEPTION), listener.getClass().getName()), t);
                    request.setAttribute(ERROR_EXCEPTION, t);
                } finally {
                    fireContainerEvent(AFTER_REQUEST_INITIALIZED, listener);
                }
            }
        }
    }

    @Override
    public void fireRequestDestroyedEvent(ServletRequest request) {
        List<EventListener> listeners = getApplicationEventListeners();
        if (!listeners.isEmpty()) {
            // create post-service event
            ServletRequestEvent event = new ServletRequestEvent(getServletContext(), request);
            int len = listeners.size();
            for (int i = 0; i < len; i++) {
                EventListener eventListener = listeners.get((len - 1) - i);
                if (!(eventListener instanceof ServletRequestListener)) {
                    continue;
                }
                ServletRequestListener listener = (ServletRequestListener) eventListener;

                fireContainerEvent(BEFORE_REQUEST_DESTROYED, listener);
                try {
                    listener.requestDestroyed(event);
                } catch (Throwable t) {
                    log.log(WARNING, format(rb.getString(REQUEST_DESTROY_EXCEPTION), listener.getClass().getName()), t);
                    request.setAttribute(ERROR_EXCEPTION, t);
                } finally {
                    fireContainerEvent(AFTER_REQUEST_DESTROYED, listener);
                }
            }
        }
    }


    /**
     * Configure and initialize the set of filters for this Context. Return <code>true</code> if all
     * filter initialization completed successfully, or <code>false</code> otherwise.
     */
    public boolean filterStart() {
        log.log(FINE, "Starting filters");

        // Instantiate and record a FilterConfig for each defined filter
        boolean ok = true;

        synchronized (filterConfigs) {
            filterConfigs.clear();
            for (String name : filterDefs.keySet()) {
                String safeName = neutralizeForLog(name);
                log.log(FINE, () -> " Starting filter '" + safeName + "'");

                try {
                    filterConfigs.put(name, new ApplicationFilterConfig(this, filterDefs.get(name)));
                } catch (Throwable t) {
                    getServletContext().log(format(rb.getString(LogFacade.STARTING_FILTER_EXCEPTION), safeName), t);
                    ok = false;
                }
            }
        }

        return ok;
    }


    /**
     * Finalize and release the set of filters for this Context. Return <code>true</code> if all
     * filter finalization completed successfully, or <code>false</code> otherwise.
     */
    public boolean filterStop() {
        log.log(FINE, "Stopping filters");

        // Release all Filter and FilterConfig instances
        synchronized (filterConfigs) {
            for (String filterName : filterConfigs.keySet()) {
                String safeFilterName = neutralizeForLog(filterName);
                log.log(FINE, () -> " Stopping filter '" + safeFilterName + "'");
                ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) filterConfigs.get(filterName);
                filterConfig.release();
            }
            filterConfigs.clear();
        }

        return true;
    }


    /**
     * Find and return the initialized <code>FilterConfig</code> for the specified filter name, if
     * any; otherwise return <code>null</code>.
     *
     * @param name Name of the desired filter
     */
    public FilterConfig findFilterConfig(String name) {
        return filterConfigs.get(name);
    }

    /**
     * Notifies all ServletContextListeners at their contextInitialized method.
     */
    protected void contextListenerStart() {
        ServletContextEvent event = new ServletContextEvent(getServletContext());
        for (ServletContextListener listener : contextListeners) {
            if (listener instanceof RestrictedServletContextListener) {
                listener = ((RestrictedServletContextListener) listener).getNestedListener();
                context.setRestricted(true);
            }

            try {
                fireContainerEvent(BEFORE_CONTEXT_INITIALIZED, listener);
                listener.contextInitialized(event);
            } finally {
                context.setRestricted(false);
                fireContainerEvent(AFTER_CONTEXT_INITIALIZED, listener);
            }
        }

        // Make sure there are no preliminary servlet or filter registrations left after all
        // listeners have been notified
        Collection<ServletRegistrationImpl> servletRegistrations = servletRegistrationMap.values();
        for (ServletRegistrationImpl servletRegistration : servletRegistrations) {
            if (servletRegistration.getClassName() == null && servletRegistration.getJspFile() == null) {
                throw new IllegalStateException(
                    format(rb.getString(LogFacade.SERVLET_WITHOUT_ANY_CLASS_OR_JSP), servletRegistration.getName()));
            }
        }

        Collection<FilterRegistrationImpl> filterRegistrations = filterRegistrationMap.values();
        for (FilterRegistrationImpl filterRegistration : filterRegistrations) {
            if (filterRegistration.getClassName() == null) {
                throw new IllegalStateException(
                    format(rb.getString(FILTER_WITHOUT_ANY_CLASS), filterRegistration.getName()));
            }
        }

        isContextInitializedCalled = true;
    }

    /**
     * Loads and instantiates the listener with the specified classname.
     *
     * @param loader the classloader to use
     * @param listenerClassName the fully qualified classname to instantiate
     *
     * @return the instantiated listener
     *
     * @throws Exception if the specified classname fails to be loaded or instantiated
     */
    @SuppressWarnings("unchecked")
    protected EventListener loadListener(ClassLoader loader, String listenerClassName) throws Exception {
        log.log(FINE, () -> "Configuring event listener class '" + neutralizeForLog(listenerClassName) + "'");

        return createListener((Class<EventListener>) loader.loadClass(listenerClassName));
    }

    /**
     * Notifies all ServletContextListeners at their contextDestroyed method.
     *
     * @return <code>true</code> if the event was processed successfully, <code>false</code> otherwise.
     */
    private boolean contextListenerStop() {
        boolean ok = true;

        if (contextListeners.isEmpty()) {
            return ok;
        }

        ServletContextEvent event = new ServletContextEvent(getServletContext());
        int len = contextListeners.size();
        for (int i = 0; i < len; i++) {
            // Invoke in reverse order of declaration
            ServletContextListener listener = contextListeners.get((len - 1) - i);
            if (listener instanceof RestrictedServletContextListener) {
                listener = ((RestrictedServletContextListener) listener).getNestedListener();
                context.setRestricted(true);
            }

            try {
                fireContainerEvent(BEFORE_CONTEXT_DESTROYED, listener);
                listener.contextDestroyed(event);
                fireContainerEvent(AFTER_CONTEXT_DESTROYED, listener);
            } catch (Throwable t) {
                context.setRestricted(false);
                fireContainerEvent(AFTER_CONTEXT_DESTROYED, listener);
                getServletContext().log(format(rb.getString(LISTENER_STOP_EXCEPTION), listener.getClass().getName()), t);
                ok = false;
            }
        }

        contextListeners.clear();

        return ok;
    }

    private void sessionListenerStop() {
        for (HttpSessionListener listener : sessionListeners) {
            // ServletContextListeners already had their PreDestroy called
            if (!(listener instanceof ServletContextListener)) {
                fireContainerEvent(PRE_DESTROY, listener);
            }
        }

        sessionListeners.clear();
    }

    private boolean eventListenerStop() {
        if (eventListeners.isEmpty()) {
            return true;
        }

        for (EventListener listener : eventListeners) {
            // ServletContextListeners and HttpSessionListeners
            // already had their PreDestroy called
            if (listener instanceof ServletContextListener || listener instanceof HttpSessionListener) {
                continue;
            }

            fireContainerEvent(PRE_DESTROY, listener);
        }

        eventListeners.clear();

        return true;
    }

    /**
     * Merge the context initialization parameters specified in the application deployment descriptor with the application
     * parameters described in the server configuration, respecting the <code>override</code> property of the application
     * parameters appropriately.
     */
    private void mergeParameters() {
        Map<String, String> mergedParams = new HashMap<>();

        for (String name : findParameters()) {
            mergedParams.put(name, findParameter(name));
        }

        for (ApplicationParameter param : findApplicationParameters()) {
            if (param.getOverride()) {
                if (mergedParams.get(param.getName()) == null) {
                    mergedParams.put(param.getName(), param.getValue());
                }
            } else {
                mergedParams.put(param.getName(), param.getValue());
            }
        }

        ServletContext servletContext = getServletContext();
        for (Entry<String, String> entry : mergedParams.entrySet()) {
            servletContext.setInitParameter(entry.getKey(), entry.getValue());
        }
    }


    /**
     * Allocate resources, including proxy.
     *
     * @return <code>true</code> if initialization was successfull, or
     *         <code>false</code> otherwise.
     */
    public boolean resourcesStart() {
        boolean ok = true;

        Hashtable<String, String> env = new Hashtable<>();
        if (getParent() != null) {
            env.put(HOST, getParent().getName());
        }
        env.put(CONTEXT, getName());

        try {
            ProxyDirContext proxyDirContext = new ProxyDirContext(env, webappResources);
            if (webappResources instanceof BaseDirContext) {
                ((BaseDirContext) webappResources).setDocBase(getBasePath(getDocBase()));
                ((BaseDirContext) webappResources).allocate();
            }
            this.resources = proxyDirContext;
        } catch (Throwable t) {
            if (log.isLoggable(FINE)) {
                log.log(SEVERE, format(rb.getString(STARTING_RESOURCES_EXCEPTION), neutralizeForLog(getName())), t);
            } else {
                log.log(SEVERE, STARTING_RESOURCE_EXCEPTION_MESSAGE,
                        new Object[] { neutralizeForLog(getName()), t.getMessage() });
            }
            ok = false;
        }

        return ok;
    }

    /**
     * Starts this context's alternate doc base resources.
     */
    public void alternateResourcesStart() throws LifecycleException {
        if (isEmpty(alternateDocBases)) {
            return;
        }

        Hashtable<String, String> env = new Hashtable<>();
        if (getParent() != null) {
            env.put(HOST, getParent().getName());
        }
        env.put(CONTEXT, getName());

        for (AlternateDocBase alternateDocBase : alternateDocBases) {
            String basePath = alternateDocBase.getBasePath();
            DirContext alternateWebappResources = ContextsAdapterUtility.unwrap(alternateDocBase.getWebappResources());
            try {
                ProxyDirContext proxyDirContext = new ProxyDirContext(env, alternateWebappResources);
                if (alternateWebappResources instanceof BaseDirContext) {
                    ((BaseDirContext) alternateWebappResources).setDocBase(basePath);
                    ((BaseDirContext) alternateWebappResources).allocate();
                }
                alternateDocBase.setResources(ContextsAdapterUtility.wrap(proxyDirContext));
            } catch (Throwable t) {
                throw new LifecycleException(
                    format(rb.getString(STARTING_RESOURCE_EXCEPTION_MESSAGE), new Object[] {getName(), t.getMessage()}),
                    t);
            }
        }
    }


    /**
     * Deallocate resources and destroy proxy.
     *
     * @return true if succeeded, false if logged an exception
     */
    public boolean resourcesStop() {
        boolean ok = true;

        try {
            if (resources != null) {
                if (resources instanceof Lifecycle) {
                    ((Lifecycle) resources).stop();
                }
                if (webappResources instanceof BaseDirContext) {
                    ((BaseDirContext) webappResources).release();
                }
            }
        } catch (Throwable t) {
            log.log(SEVERE, STOPPING_RESOURCES_EXCEPTION, t);
            ok = false;
        }

        this.resources = null;

        return ok;

    }


    /**
     * Stops this context's alternate doc base resources.
     *
     * @return true if succeeded, false if logged an exception
     */
    public boolean alternateResourcesStop() {
        boolean ok = true;

        if (isEmpty(alternateDocBases)) {
            return ok;
        }

        for (AlternateDocBase alternateDocBase : alternateDocBases) {
            final DirContext alternateResources = ContextsAdapterUtility.unwrap(alternateDocBase.getResources());
            if (alternateResources instanceof Lifecycle) {
                try {
                    ((Lifecycle) alternateResources).stop();
                } catch (Throwable t) {
                    log.log(SEVERE, STOPPING_RESOURCES_EXCEPTION, t);
                    ok = false;
                }
            }

            final DirContext alternateWebappResources = ContextsAdapterUtility.unwrap(alternateDocBase.getWebappResources());
            if (alternateWebappResources instanceof BaseDirContext) {
                try {
                    ((BaseDirContext) alternateWebappResources).release();
                } catch (Throwable t) {
                    log.log(SEVERE, STOPPING_RESOURCES_EXCEPTION, t);
                    ok = false;
                }
            }
        }

        this.alternateDocBases = null;

        return ok;
    }


    /**
     * Load and initialize all servlets marked "load on startup" in the web application deployment
     * descriptor.
     *
     * @param children Array of wrappers for all currently defined servlets (including those not
     *            declared load on startup)
     */
    public void loadOnStartup(Container[] children) throws LifecycleException {
        // Collect "load on startup" servlets that need to be initialized
        Map<Integer, List<Wrapper>> loadOnStartupServlets = new TreeMap<>();
        List<Wrapper> nonLoadOnStartupServlets = new ArrayList<>();
        for (Container aChildren : children) {
            Wrapper wrapper = (Wrapper) aChildren;
            int loadOnStartup = wrapper.getLoadOnStartup();
            if (loadOnStartup < 0) {
                nonLoadOnStartupServlets.add(wrapper);
            } else {
                loadOnStartupServlets.computeIfAbsent(loadOnStartup, e -> new ArrayList<>()).add(wrapper);
            }
        }

        // Combine the load on startup and non load on startup in one list, with the
        // latter loading after the ones with an explicit priority (load level).
        List<Wrapper> allServlets = new ArrayList<>();
        for (List<Wrapper> samePriorityServlets : loadOnStartupServlets.values()) {
            for (Wrapper wrapper : samePriorityServlets) {
                allServlets.add(wrapper);
            }
        }

        // Load the collected "load on startup" servlets
        for (Wrapper wrapper : allServlets) {
            loadServlet(wrapper);
        }

        if (Boolean.getBoolean("glassfish.servlet.loadAllOnStartup")) {
            // Also load the other servlets, which is one way to pass the CDI TCK, specifically
            // ContainerEventTest#testProcessInjectionTargetEventFiredForServlet and adhere to the rule
            // that injection points for Servlets have to be processed during start.
            for (Wrapper wrapper : nonLoadOnStartupServlets) {
                loadServlet(wrapper);
            }
        }
    }


    private void loadServlet(Wrapper wrapper) throws LifecycleException {
        try {
            wrapper.load();
        } catch (ServletException e) {
            getServletContext().log(
                format(rb.getString(SERVLET_LOAD_EXCEPTION), neutralizeForLog(getName())),
                StandardWrapper.getRootCause(e));
            // NOTE: load errors (including a servlet that throws
            // UnavailableException from the init() method) are NOT
            // fatal to application startup
            throw new LifecycleException(StandardWrapper.getRootCause(e));
        }
    }


    /**
     * Starts the session manager of this Context.
     */
    protected void managerStart() throws LifecycleException {
        if (manager instanceof Lifecycle) {
            ((Lifecycle) getManager()).start();
        }
    }

    /**
     * Stops the session manager of this Context.
     */
    protected void managerStop() throws LifecycleException {
        if (manager instanceof Lifecycle) {
            ((Lifecycle) manager).stop();
        }
    }

    /**
     * Start this Context component.
     *
     * @exception LifecycleException if a startup error occurs
     */
    @Override
    public synchronized void start() throws LifecycleException {
        if (started) {
            if (log.isLoggable(INFO)) {
                log.log(INFO, CONTAINER_ALREADY_STARTED_EXCEPTION, neutralizeForLog(logName()));
            }
            return;
        }

        long startupTimeStart = System.currentTimeMillis();

        if (!initialized) {
            try {
                init();
            } catch (Exception ex) {
                throw new LifecycleException("Error initializaing ", ex);
            }
        }

        log.log(FINE, () -> "Starting " + ("".equals(getName()) ? "ROOT" : neutralizeForLog(getName())));

        // Set JMX object name for proper pipeline registration
        preRegisterJMX();

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_START_EVENT, null);

        setAvailable(false);
        setConfigured(false);

        // Add missing components as necessary
        if (webappResources == null) { // (1) Required by Loader
            log.log(FINE, "Configuring default Resources");

            try {
                if (docBase != null && docBase.endsWith(".war") && !(new File(docBase).isDirectory())) {
                    setResources(new WARDirContext());
                } else {
                    setResources(new WebDirContext());
                }
            } catch (IllegalArgumentException e) {
                throw new LifecycleException(rb.getString(INIT_RESOURCES_EXCEPTION), e);
            }
        }

        resourcesStart();

        // Add alternate resources
        if (alternateDocBases != null && !alternateDocBases.isEmpty()) {
            for (AlternateDocBase alternateDocBase : alternateDocBases) {
                String docBase = alternateDocBase.getDocBase();
                log.log(FINE, "Configuring alternate resources");

                try {
                    if (docBase != null && docBase.endsWith(".war") && (!(new File(docBase).isDirectory()))) {
                        setAlternateResources(alternateDocBase, new WARDirContext());
                    } else {
                        setAlternateResources(alternateDocBase, new FileDirContext());
                    }
                } catch (IllegalArgumentException e) {
                    throw new LifecycleException(rb.getString(INIT_RESOURCES_EXCEPTION), e);
                }
            }

            alternateResourcesStart();
        }

        if (getLoader() == null) {
            createLoader();
        }

        // Initialize character set mapper
        getCharsetMapper();

        // Post work directory
        postWorkDirectory();

        // Validate required extensions
        try {
            ExtensionValidator.validateApplication(getResources(), this);
        } catch (IOException ioe) {
            String msg = format(rb.getString(DEPENDENCY_CHECK_EXCEPTION), this);
            throw new LifecycleException(msg, ioe);
        }

        // Reading the "catalina.useNaming" environment variable
        String useNamingProperty = System.getProperty("catalina.useNaming");
        if ((useNamingProperty != null) && ("false".equals(useNamingProperty))) {
            useNaming = false;
        }

        if (isUseNaming()) {
            if (namingContextListener == null) {
                namingContextListener = new NamingContextListener();
                namingContextListener.setDebug(getDebug());
                namingContextListener.setName(getNamingContextName());
                addLifecycleListener(namingContextListener);
            }
        }

        ClassLoader oldCCL = null;

        try {
            started = true;

            // Start our subordinate components, if any
            if ((loader != null) && (loader instanceof Lifecycle)) {
                ((Lifecycle) loader).start();
            }
            if ((logger != null) && (logger instanceof Lifecycle)) {
                ((Lifecycle) logger).start();
            }

            // Binding thread
            oldCCL = bindThread();

            if ((realm != null) && (realm instanceof Lifecycle)) {
                ((Lifecycle) realm).start();
            }
            if ((resources != null) && (resources instanceof Lifecycle)) {
                ((Lifecycle) resources).start();
            }

            // Start our child containers, if any
            for (Container child : findChildren()) {
                if (child instanceof Lifecycle) {
                    ((Lifecycle) child).start();
                }
            }

            // Start the Valves in our pipeline (including the basic),
            // if any
            if (pipeline instanceof Lifecycle) {
                ((Lifecycle) pipeline).start();
            }

            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(START_EVENT, null);
        } catch (Throwable t) {
            throw new LifecycleException(t);
        } finally {
            // Unbinding thread
            unbindThread(oldCCL);
        }

        if (!getConfigured()) {
            throw new LifecycleException(format(rb.getString(STARTUP_CONTEXT_FAILED_EXCEPTION), getName()));
        }

        // Store some required info as ServletContext attributes
        postResources();
        if (!isEmpty(orderedLibs)) {
            getServletContext().setAttribute(ORDERED_LIBS, orderedLibs);
            context.setAttributeReadOnly(ORDERED_LIBS);
        }

        // Initialize associated mapper
        mapper.setContext(getPath(), welcomeFiles, ContextsAdapterUtility.wrap(resources));

        // Binding thread
        oldCCL = bindThread();

        try {
            // Set up the context init params
            mergeParameters();

            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(AFTER_START_EVENT, null);

            // Support for pluggability : this has to be done before
            // listener events are fired
            callServletContainerInitializers();

            // Configure and call application event listeners
            contextListenerStart();

            // Start manager
            if ((manager != null) && (manager instanceof Lifecycle)) {
                ((Lifecycle) getManager()).start();
            }

            // Start ContainerBackgroundProcessor thread
            super.threadStart();

            // Configure and call application filters
            filterStart();

            // Load and initialize all "load on startup" servlets
            loadOnStartup(findChildren());
        } catch (Throwable t) {
            log.log(SEVERE, STARTUP_CONTEXT_FAILED_EXCEPTION, getName());
            LifecycleException exception = new LifecycleException(t);
            try {
                stop();
            } catch (Throwable tt) {
                exception.addSuppressed(tt);
                log.log(SEVERE, LogFacade.CLEANUP_FAILED_EXCEPTION, tt);
            }
            throw exception;
        } finally {
            // Unbinding thread
            unbindThread(oldCCL);
        }

        // Set available status depending upon startup success
        log.log(FINEST, "Startup successfully completed");

        setAvailable(true);

        // JMX registration
        registerJMX();

        startTimeMillis = System.currentTimeMillis();
        startupTime = startTimeMillis - startupTimeStart;

        // Send j2ee.state.running notification
        if (getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.running", this, sequenceNumber++);
            sendNotification(notification);
        }
    }

    protected Types getTypes() {
        return null;
    }

    protected boolean isStandaloneModule() {
        return true;
    }

    public void setServletContainerInitializerInterestList(ServiceLoader<ServletContainerInitializer> initializers) {
        servletContainerInitializers = initializers;
    }

    protected void callServletContainerInitializers() throws LifecycleException {
        List<ServletContainerInitializer> initializers = loadServletContainerInitializers();

        // Get the list of ServletContainerInitializers and the classes they are interested in
        var interestList = getInterestList(initializers);
        var initializerList = getInitializerList(initializers, interestList, getTypes(), getClassLoader(), logContext);

        if (initializerList == null) {
            return;
        }

        // Allow programmatic registration of ServletContextListeners, but
        // only within the scope of ServletContainerInitializer#onStartup
        isProgrammaticServletContextListenerRegistrationAllowed = true;
        try {
            // We have the list of initializers and the classes that satisfy
            // the condition. Time to call the initializers
            ServletContext ctxt = this.getServletContext();

            var initializerListOrdered = orderInitializers(initializerList);

            for (var entry : initializerListOrdered) {
                Class<? extends ServletContainerInitializer> initializer = entry.getKey();
                if (isUseMyFaces() && FACES_INITIALIZER.equals(initializer.getName())) {
                    continue;
                }

                try {
                    log.log(FINE, "Calling ServletContainerInitializer [{0}] onStartup with classes {1} ",
                        new Object[] {initializer, entry.getValue()});

                    ServletContainerInitializer iniInstance = initializer.getDeclaredConstructor().newInstance();
                    fireContainerEvent(BEFORE_CONTEXT_INITIALIZER_ON_STARTUP, iniInstance);
                    iniInstance.onStartup(initializerList.get(initializer), ctxt);
                    fireContainerEvent(AFTER_CONTEXT_INITIALIZER_ON_STARTUP, iniInstance);
                } catch (Throwable t) {
                    String msg = format(rb.getString(INVOKING_SERVLET_CONTAINER_INIT_EXCEPTION),
                        initializer.getCanonicalName());
                    throw new LifecycleException(msg, t);
                }
            }
        } finally {
            isProgrammaticServletContextListenerRegistrationAllowed = false;
        }
    }

    private LinkedList<Entry<Class<? extends ServletContainerInitializer>, Set<Class<?>>>> orderInitializers(Map<Class<? extends ServletContainerInitializer>, Set<Class<?>>> initializerList) {
        Entry<Class<? extends ServletContainerInitializer>, Set<Class<?>>> facesInitializerEntry = null;
        var initializerListOrdered = new LinkedList<>(initializerList.entrySet());
        for (var iterator = initializerListOrdered.listIterator(); iterator.hasNext();) {
            var initializerEntry = iterator.next();
            Class<? extends ServletContainerInitializer> initializer = initializerEntry.getKey();

            // Make sure the WEBSOCKET_INITIALIZER gets a chance to run before the FACES_INITIALIZER.
            // This is because of a complicated interaction, where the WEBSOCKET_INITIALIZER
            // is also executed by the FACES_INITIALIZER.
            // If both have classes to process, the WEBSOCKET_INITIALIZER called by the FACES_INITIALIZER
            // will create a container that will be later overwritten by the direct call to the
            // WEBSOCKET_INITIALIZER.
            // Ultimately this should be fixed in the WebSocket and Faces specs.
            if (FACES_INITIALIZER.equals(initializer.getName())) {
                facesInitializerEntry = initializerEntry;
                iterator.remove();
            } else if (WEBSOCKET_INITIALIZER.equals(initializer.getName())) {
                if (facesInitializerEntry != null) {
                    iterator.add(facesInitializerEntry);
                    facesInitializerEntry = null;
                }
                break;
            }
        }

        // Gather for the potential problem that a Faces initializer would be present
        // but no WebSocket one.
        if (facesInitializerEntry != null) {
            initializerListOrdered.add(facesInitializerEntry);
        }

        return initializerListOrdered;
    }

    private List<ServletContainerInitializer> loadServletContainerInitializers() {
        List<ServletContainerInitializer> initializers = new ArrayList<>();
        Iterator<ServletContainerInitializer> iterator = servletContainerInitializers.iterator();
        // Note: don't let editors to change it to foreach, both hasNext and next can throw an error!
        while (true) {
            try {
                if (!iterator.hasNext()) {
                    break;
                }
            } catch (ServiceConfigurationError e) {
                log.log(WARNING,
                    "Could not call hasNext! The initializer is probably not visible for the classloader. Moving on ...",
                    e);
                try {
                    iterator.next();
                } catch (ServiceConfigurationError ignore) {
                    // just move on
                }
                continue;
            }
            try {
                initializers.add(iterator.next());
            } catch (ServiceConfigurationError e) {
                log.log(WARNING, "Could not call next! The initializer could not be created. Skipped ...", e);
            }
        }
        return initializers;
    }

    /**
     * Creates a classloader for this context.
     */
    public void createLoader() {
        final ClassLoader defaultLoader;
        if (getPrivileged()) {
            log.log(FINE, "Configuring privileged default Loader");
            defaultLoader = this.getClass().getClassLoader();
        } else {
            log.log(FINE, "Configuring non-privileged default Loader");
            defaultLoader = getParentClassLoader();
        }
        WebappLoader webappLoader = new WebappLoader(defaultLoader);
        webappLoader.setDelegate(getDelegate());
        webappLoader.setUseMyFaces(useMyFaces);
        setLoader(webappLoader);
    }

    /**
     * Stop this Context component.
     *
     * @exception LifecycleException if a shutdown error occurs
     */
    @Override
    public synchronized void stop() throws LifecycleException {
        stop(false);
    }

    /**
     * Stop this Context component.
     *
     * @param isShutdown true if this Context is being stopped as part of a domain shutdown (as opposed to an undeployment),
     * and false otherwise
     * @exception LifecycleException if a shutdown error occurs
     */
    public synchronized void stop(boolean isShutdown) throws LifecycleException {
        // Validate and update our current component state
        if (!started) {
            if (log.isLoggable(INFO)) {
                log.log(INFO, CONTAINER_NOT_STARTED_EXCEPTION, logName());
            }
            return;
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(BEFORE_STOP_EVENT, null);

        // Send j2ee.state.stopping notification
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.stopping", this, sequenceNumber++);
            sendNotification(notification);
        }

        // Mark this application as unavailable while we shut down
        setAvailable(false);

        // Binding thread
        ClassLoader oldCCL = bindThread();

        try {
            // Stop our child containers, if any
            for (Container child : findChildren()) {
                if (child instanceof Lifecycle) {
                    ((Lifecycle) child).stop();
                }
            }

            // Stop our filters
            filterStop();

            // Stop ContainerBackgroundProcessor thread
            super.threadStop();

            if (manager instanceof Lifecycle) {
                if (manager instanceof StandardManager) {
                    StandardManager standardManager = (StandardManager) manager;
                    if (standardManager.isStarted()) {
                        standardManager.stop(isShutdown);
                    }
                } else {
                    ((Lifecycle) manager).stop();
                }
            }

            // Stop all ServletContextListeners. It is important that they are passed
            // a ServletContext to their contextDestroyed() method that still has all its attributes
            // set. In other words, it is important that we invoke these listeners before calling
            // context.clearAttributes()
            contextListenerStop();

            sessionListenerStop();

            // Clear all application-originated servlet context attributes
            if (context != null) {
                context.clearAttributes();
            }

            // Stop all event listeners, including those of type ServletContextAttributeListener.
            // For the latter, it is important that we invoke them after calling
            // context.clearAttributes, so that they receive the corresponding attribute removal
            // events
            eventListenerStop();

            // Notify our interested LifecycleListeners
            lifecycle.fireLifecycleEvent(STOP_EVENT, null);
            started = false;

            // Stop the Valves in our pipeline (including the basic), if any
            if (pipeline instanceof Lifecycle) {
                ((Lifecycle) pipeline).stop();
            }

            // Finalize our character set mapper
            setCharsetMapper(null);

            // Stop resources
            resourcesStop();
            alternateResourcesStop();

            if (realm instanceof Lifecycle) {
                ((Lifecycle) realm).stop();
            }
            if (logger instanceof Lifecycle) {
                ((Lifecycle) logger).stop();
            }
        } catch (Throwable t) {
            // started was "true" when it first enters the try block.
            // Note that it is set to false after STOP_EVENT is fired.
            // One need to fire STOP_EVENT to clean up naming information
            // if START_EVENT is processed successfully.
            if (started) {
                lifecycle.fireLifecycleEvent(STOP_EVENT, null);
            }

            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }

            if (t instanceof LifecycleException) {
                throw (LifecycleException) t;
            }

            throw new LifecycleException(t);
        } finally {

            // Unbinding thread
            unbindThread(oldCCL);

            // Delay the stopping of the webapp classloader until this point, because unbindThread()
            // calls the security-checked Thread.setContextClassLoader(), which may ask the current
            // thread context classloader (i.e., the webapp classloader) to load Principal classes
            // specified in the security policy file
            if ((loader != null) && (loader instanceof Lifecycle)) {
                ((Lifecycle) loader).stop();
            }
        }

        // Send j2ee.state.stopped notification
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.stopped", this, sequenceNumber++);
            sendNotification(notification);
        }

        // Reset application context
        context = null;

        // This object will no longer be visible or used.
        try {
            resetContext();
        } catch (Exception ex) {
            log.log(SEVERE, format(rb.getString(RESETTING_CONTEXT_EXCEPTION), this), ex);
        }

        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(AFTER_STOP_EVENT, null);

        log.log(FINE, "Stopping complete");

        if (oname != null) {
            // Send j2ee.object.deleted notification
            sendNotification(new Notification("j2ee.object.deleted", this, sequenceNumber++));
        }

    }


    /**
     * Destroys this context by cleaning it up completely.
     * The problem is that undoing all the config in start() and restoring a 'fresh' state is
     * impossible. After stop()/destroy()/init()/start() we should have the same state as if a fresh
     * start was done - i.e read modified web.xml, etc. This can only be done by completely removing
     * the context object and remapping a new one, or by cleaning up everything.
     *
     * XXX Should this be done in stop() ?
     */
    @Override
    public void destroy() throws Exception {
        super.destroy();

        // super.destroy() will stop session manager and cause it to unload
        // all its active sessions into a file. Delete this file, because this
        // context is being destroyed and must not leave any traces.
        if (getManager() instanceof ManagerBase) {
            ((ManagerBase) getManager()).release();
        }

        instanceListeners.clear();
        instanceListenerInstances.clear();
    }

    private void resetContext() throws Exception, MBeanRegistrationException {
        // Restore the original state ( pre reading web.xml in start )
        // If you extend this - override this method and make sure to clean up
        children = new HashMap<>();
        startupTime = 0;
        startTimeMillis = 0;
        tldScanTime = 0;

        // Bugzilla 32867
        distributable = false;

        eventListeners.clear();
        contextListeners.clear();
        sessionListeners.clear();

        requestCharacterEncoding = null;
        responseCharacterEncoding = DEFAULT_RESPONSE_CHARACTER_ENCODING;

        if (log.isLoggable(FINE)) {
            log.log(FINE, "resetContext " + oname);
        }
    }

    /**
     * Return a String representation of this component.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getParent() != null) {
            sb.append(getParent().toString());
            sb.append(".");
        }
        sb.append("StandardContext[");
        sb.append(getName());
        sb.append("]");
        return (sb.toString());

    }

    @Override
    public void backgroundProcess() {
        if (!started) {
            return;
        }

        count = (count + 1) % managerChecksFrequency;

        if ((getManager() != null) && (count == 0)) {
            if (getManager() instanceof StandardManager) {
                ((StandardManager) getManager()).processExpires();
            } else if (getManager() instanceof PersistentManagerBase) {
                PersistentManagerBase pManager = (PersistentManagerBase) getManager();
                pManager.backgroundProcess();
            }
        }

        if (isReload()) {
            if (getLoader() != null) {
                if (reloadable && getLoader().modified()) {
                    try {
                        Thread.currentThread().setContextClassLoader(standardContextClassLoader);
                        reload();
                    } finally {
                        if (getLoader() != null) {
                            Thread.currentThread().setContextClassLoader(getClassLoader());
                        }
                    }
                }

                if (getLoader() instanceof WebappLoader) {
                    ((WebappLoader) getLoader()).reload();
                }
            }
        }
    }


    /**
     * Adjust the URL pattern to begin with a leading slash, if appropriate (i.e. we are running a
     * servlet 2.2 application). Otherwise, return the specified URL pattern unchanged.
     *
     * @param urlPattern The URL pattern to be adjusted (if needed) and returned
     */
    protected String adjustURLPattern(String urlPattern) {
        if (urlPattern == null) {
            return urlPattern;
        }
        if (urlPattern.startsWith("/") || urlPattern.startsWith("*.")) {
            return urlPattern;
        }
        if (!isServlet22()) {
            return urlPattern;
        }

        log.log(FINE, LogFacade.URL_PATTERN_WARNING, urlPattern);

        return "/" + urlPattern;
    }

    /**
     * Are we processing a version 2.2 deployment descriptor?
     */
    protected boolean isServlet22() {
        return publicId != null && publicId.equals(WebDtdPublicId_22);
    }

    /**
     * Return a File object representing the base directory for the entire servlet container (i.e. the Engine container if
     * present).
     */
    protected File engineBase() {
        String base = System.getProperty("catalina.base");
        if (base == null) {
            StandardEngine eng = (StandardEngine) this.getParent().getParent();
            base = eng.getBaseDir();
        }

        return new File(base);
    }


    /**
     * Bind current thread, both for CL purposes and for JNDI ENC support during : startup, shutdown and realoading of the
     * context.
     *
     * @return the previous context class loader
     */
    private ClassLoader bindThread() {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        if (isUseNaming()) {
            try {
                ContextBindings.bindThread(this, this);
            } catch (Throwable e) {
                log.log(WARNING, BIND_THREAD_EXCEPTION, e);
            }
        }

        return oldContextClassLoader;

    }

    /**
     * Unbind thread.
     */
    private void unbindThread(ClassLoader oldContextClassLoader) {
        Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        if (isUseNaming()) {
            ContextBindings.unbindThread(this, this);
        }
    }

    /**
     * Get base path.
     */
    private String getBasePath(String docBase) {
        String basePath = null;
        Container container = this;
        while (container != null) {
            if (container instanceof Host) {
                break;
            }
            container = container.getParent();
        }

        File file = new File(docBase);
        if (!file.isAbsolute()) {
            if (container == null) {
                basePath = (new File(engineBase(), docBase)).getPath();
            } else {
                // Use the "appBase" property of this container
                String appBase = ((Host) container).getAppBase();
                file = new File(appBase);
                if (!file.isAbsolute()) {
                    file = new File(engineBase(), appBase);
                }
                basePath = (new File(file, docBase)).getPath();
            }
        } else {
            basePath = file.getPath();
        }

        return basePath;
    }

    /**
     * @return the config file name for a given context path.
     */
    protected String getDefaultConfigFile() {
        String basename = null;
        String path = getPath();
        if ("".equals(path)) {
            basename = "ROOT";
        } else {
            basename = path.substring(1).replace('/', '_');
        }

        return basename + ".xml";
    }

    /**
     * @return naming context full name.
     */
    public String getNamingContextName() {
        if (namingContextName != null) {
            return namingContextName;
        }

        Container parent = getParent();
        if (parent == null) {
            namingContextName = getName();
        } else {
            Stack<String> stk = new Stack<>();
            StringBuilder buff = new StringBuilder();
            while (parent != null) {
                stk.push(parent.getName());
                parent = parent.getParent();
            }
            while (!stk.empty()) {
                buff.append("/").append(stk.pop());
            }
            buff.append(getName());
            namingContextName = buff.toString();
        }

        // append an id to make the name unique to the instance.
        namingContextName += instanceIDCounter.getAndIncrement();

        return namingContextName;
    }

    /**
     * @return the request processing paused flag for this Context
     */
    public boolean getPaused() {
        return paused;
    }

    /**
     * Stores the resources of this application as ServletContext attributes.
     */
    private void postResources() {
        getServletContext().setAttribute(RESOURCES_ATTR, getResources());
        context.setAttributeReadOnly(RESOURCES_ATTR);

        getServletContext().setAttribute(ALTERNATE_RESOURCES_ATTR, getAlternateDocBases());
        context.setAttributeReadOnly(ALTERNATE_RESOURCES_ATTR);
    }

    public String getHostname() {
        Container parentHost = getParent();
        if (parentHost != null) {
            hostName = parentHost.getName();
        }
        if ((hostName == null) || (hostName.length() < 1)) {
            hostName = "_";
        }

        return hostName;
    }

    /**
     * Set the appropriate context attribute for our work directory.
     */
    private void postWorkDirectory() {
        // Acquire (or calculate) the work directory path
        String workDir = getWorkDir();
        if (workDir == null || workDir.length() == 0) {

            // Retrieve our parent (normally a host) name
            String hostName = null;
            String engineName = null;
            String hostWorkDir = null;
            Container parentHost = getParent();
            if (parentHost != null) {
                hostName = parentHost.getName();
                if (parentHost instanceof StandardHost) {
                    hostWorkDir = ((StandardHost) parentHost).getWorkDir();
                }
                Container parentEngine = parentHost.getParent();
                if (parentEngine != null) {
                    engineName = parentEngine.getName();
                }
            }
            if ((hostName == null) || (hostName.length() < 1)) {
                hostName = "_";
            }
            if ((engineName == null) || (engineName.length() < 1)) {
                engineName = "_";
            }

            String temp = getPath();
            if (temp.startsWith("/")) {
                temp = temp.substring(1);
            }
            temp = temp.replace('/', '_');
            temp = temp.replace('\\', '_');
            if (temp.length() < 1) {
                temp = "_";
            }
            if (hostWorkDir != null) {
                workDir = hostWorkDir + File.separator + temp;
            } else {
                workDir = "work" + File.separator + engineName + File.separator + hostName + File.separator + temp;
            }
            setWorkDir(workDir);
        }

        // Create this directory if necessary
        File dir = new File(workDir);
        if (!dir.isAbsolute()) {
            File catalinaHome = engineBase();
            String catalinaHomePath = null;
            try {
                catalinaHomePath = catalinaHome.getCanonicalPath();
                dir = new File(catalinaHomePath, workDir);
            } catch (IOException e) {
            }
        }
        if (!dir.mkdirs() && !dir.isDirectory()) {
            log.log(SEVERE, LogFacade.CREATE_WORK_DIR_EXCEPTION, dir.getAbsolutePath());
        }

        // Set the appropriate servlet context attribute
        getServletContext().setAttribute(ServletContext.TEMPDIR, dir);
        context.setAttributeReadOnly(ServletContext.TEMPDIR);

    }

    /**
     * Set the request processing paused flag for this Context.
     *
     * @param paused The new request processing paused flag
     */
    private void setPaused(boolean paused) {
        this.paused = paused;
    }


    /**
     * Validate the syntax of a proposed <code>&lt;url-pattern&gt;</code> for conformance with
     * specification requirements.
     *
     * @param urlPattern URL pattern to be validated
     */
    protected boolean validateURLPattern(String urlPattern) {
        if (urlPattern == null) {
            return false;
        }

        if (urlPattern.isEmpty()) {
            return true;
        }

        if (urlPattern.indexOf('\n') >= 0 || urlPattern.indexOf('\r') >= 0) {
            log.log(WARNING, LogFacade.URL_PATTERN_CANNOT_BE_MATCHED_EXCEPTION, urlPattern);
            return false;
        }

        if (urlPattern.startsWith("*.")) {
            if (urlPattern.indexOf('/') < 0) {
                checkUnusualURLPattern(urlPattern);
                return true;
            }

            return false;
        }

        if (urlPattern.startsWith("/") && !urlPattern.contains("*.")) {
            checkUnusualURLPattern(urlPattern);
            return true;
        }

        return false;
    }

    /**
     * Check for unusual but valid <code>&lt;url-pattern&gt;</code>s. See Bugzilla 34805, 43079 &amp; 43080
     */
    private void checkUnusualURLPattern(String urlPattern) {
        if (log.isLoggable(INFO)) {
            if (urlPattern.endsWith("*") && (urlPattern.length() < 2 || urlPattern.charAt(urlPattern.length() - 2) != '/')) {
                String msg = "Suspicious url pattern: \"" + urlPattern + "\"" + " in context [" + getName() + "] - see"
                        + " section SRV.11.2 of the Servlet specification";
                log.log(INFO, msg);
            }
        }
    }

    // -------------------- JMX methods --------------------

    /**
     * @return the MBean Names of the set of defined environment entries for this web application
     */
    public String[] getEnvironments() {
        ContextEnvironment[] envs = getNamingResources().findEnvironments();
        List<String> results = new ArrayList<>();
        for (ContextEnvironment env : envs) {
            try {
                ObjectName oname = createObjectName(env);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                IllegalArgumentException iae = new IllegalArgumentException("Cannot create object name for environment " + env);
                iae.initCause(e);
                throw iae;
            }
        }

        return results.toArray(new String[results.size()]);

    }

    /**
     * @return the MBean Names of all the defined resource references for this application.
     */
    public String[] getResourceNames() {
        ContextResource[] resources = getNamingResources().findResources();
        List<String> results = new ArrayList<>();
        for (ContextResource resource : resources) {
            try {
                ObjectName oname = createObjectName(resource);
                results.add(oname.toString());
            } catch (MalformedObjectNameException e) {
                IllegalArgumentException iae = new IllegalArgumentException("Cannot create object name for resource " + resource);
                iae.initCause(e);
                throw iae;
            }
        }

        return results.toArray(new String[results.size()]);
    }

    /**
     * @return the MBean Names of all the defined resource links for this application
     */
    public String[] getResourceLinks() {
        ContextResourceLink[] links = getNamingResources().findResourceLinks();
        List<String> results = new ArrayList<>();
        for (ContextResourceLink link : links) {
            try {
                results.add(createObjectName(link).toString());
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException("Cannot create object name for resource " + link, e);
            }
        }

        return results.toArray(new String[results.size()]);
    }


    /**
     * Add an environment entry for this web application.
     *
     * @param envName New environment entry name
     */
    public String addEnvironment(String envName, String type) throws MalformedObjectNameException {
        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return null;
        }

        ContextEnvironment env = nresources.findEnvironment(envName);
        if (env != null) {
            throw new IllegalArgumentException("Invalid environment name - already exists '" + envName + "'");
        }

        env = new ContextEnvironment();
        env.setName(envName);
        env.setType(type);
        nresources.addEnvironment(env);

        // Return the corresponding MBean name
        return createObjectName(env).toString();
    }

    /**
     * Add a resource reference for this web application.
     *
     * @param resourceName New resource reference name
     */
    public String addResource(String resourceName, String type) throws MalformedObjectNameException {
        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return null;
        }

        ContextResource resource = nresources.findResource(resourceName);
        if (resource != null) {
            throw new IllegalArgumentException("Invalid resource name - already exists'" + resourceName + "'");
        }

        resource = new ContextResource();
        resource.setName(resourceName);
        resource.setType(type);
        nresources.addResource(resource);

        // Return the corresponding MBean name
        return createObjectName(resource).toString();
    }

    /**
     * Add a resource link for this web application.
     *
     * @param resourceLinkName New resource link name
     */
    public String addResourceLink(String resourceLinkName, String global, String name, String type) throws MalformedObjectNameException {
        NamingResources nresources = getNamingResources();
        if (nresources == null) {
            return null;
        }
        ContextResourceLink resourceLink = nresources.findResourceLink(resourceLinkName);
        if (resourceLink != null) {
            throw new IllegalArgumentException("Invalid resource link name - already exists'" + resourceLinkName + "'");
        }
        resourceLink = new ContextResourceLink();
        resourceLink.setGlobal(global);
        resourceLink.setName(resourceLinkName);
        resourceLink.setType(type);
        nresources.addResourceLink(resourceLink);

        // Return the corresponding MBean name
        return createObjectName(resourceLink).toString();
    }

    @Override
    public ObjectName createObjectName(String hostDomain, ObjectName parentName) throws MalformedObjectNameException {
        String onameStr;
        StandardHost hst = (StandardHost) getParent();

        String hostName = getParent().getName();
        String name = "//" + ((hostName == null) ? "DEFAULT" : hostName) + (("".equals(encodedPath)) ? "/" : encodedPath);

        String suffix = ",J2EEApplication=" + getEEApplication() + ",J2EEServer=" + getEEServer();

        onameStr = "j2eeType=WebModule,name=" + name + suffix;
        if (log.isLoggable(FINE)) {
            log.log(FINE, "Registering " + onameStr + " for " + oname);
        }

        // default case - no domain explictely set.
        if (getDomain() == null) {
            domain = hst.getDomain();
        }
        return new ObjectName(getDomain() + ":" + onameStr);
    }

    private void preRegisterJMX() {
        try {
            StandardHost host = (StandardHost) getParent();
            if (oname == null || oname.getKeyProperty("j2eeType") == null) {
                oname = createObjectName(host.getDomain(), host.getJmxName());
                controller = oname;
            }
        } catch (Exception ex) {
            if (log.isLoggable(INFO)) {
                String msg = format(rb.getString(LogFacade.ERROR_UPDATING_CTX_INFO),
                    new Object[] {this, oname, ex.toString()});
                log.log(INFO, msg, ex);
            }
        }
    }

    private void registerJMX() {
        try {
            if (log.isLoggable(FINE)) {
                log.log(FINE, "Checking for " + oname);
            }
            controller = oname;
            // Send j2ee.object.created notification
            if (this.getObjectName() != null) {
                Notification notification = new Notification("j2ee.object.created", this, sequenceNumber++);
                sendNotification(notification);
            }
            for (Container child : findChildren()) {
                ((StandardWrapper) child).registerJMX(this);
            }
        } catch (Exception ex) {
            String msg = format(rb.getString(LogFacade.ERROR_REGISTERING_WRAPPER_INFO),
                new Object[] {this, oname, ex.toString()});
            log.log(INFO, msg, ex);
        }
    }

    public void sendNotification(Notification notification) {

        if (broadcaster == null) {
            broadcaster = ((StandardEngine) getParent().getParent()).getService().getBroadcaster();
        }
        if (broadcaster != null) {
            broadcaster.sendNotification(notification);
        }
        return;
    }

    @Override
    public void init() throws Exception {

        if (this.getParent() == null) {

            ContextConfig config = new ContextConfig();
            this.addLifecycleListener(config);
        }

        // It's possible that addChild may have started us
        if (initialized) {
            return;
        }

        super.init();

        // START GlassFish 2439
        // Notify our interested LifecycleListeners
        lifecycle.fireLifecycleEvent(INIT_EVENT, null);
        // END GlassFish 2439

        // Send j2ee.state.starting notification
        if (this.getObjectName() != null) {
            Notification notification = new Notification("j2ee.state.starting", this, sequenceNumber++);
            sendNotification(notification);
        }

    }

    @Override
    public ObjectName getParentName() throws MalformedObjectNameException {
        // "Life" update
        String path = oname.getKeyProperty("name");
        if (path == null) {
            log.log(SEVERE, LogFacade.MISSING_ATTRIBUTE, getName());
            return null;
        }
        if (!path.startsWith("//")) {
            log.log(SEVERE, LogFacade.MALFORMED_NAME, getName());
        }
        path = path.substring(2);
        int delim = path.indexOf("/");
        hostName = "localhost"; // Should be default...
        if (delim > 0) {
            hostName = path.substring(0, delim);
            path = path.substring(delim);
            if ("/".equals(path)) {
                this.setName("");
            } else {
                this.setName(path);
            }
        } else {
            if (log.isLoggable(FINE)) {
                log.log(FINE, "Setting path " + path);
            }
            this.setName(path);
        }
        // XXX The service and domain should be the same.
        String parentDomain = getEngineName();
        if (parentDomain == null) {
            parentDomain = domain;
        }
        return new ObjectName(parentDomain + ":" + "type=Host,host=" + hostName);
    }

    public void create() throws Exception {
        init();
    }


    /**
     * Create an <code>ObjectName</code> for <code>ContextEnvironment</code> object.
     *
     * @param environment The ContextEnvironment to be named
     * @throws MalformedObjectNameException if a name cannot be created
     */
    public ObjectName createObjectName(ContextEnvironment environment) throws MalformedObjectNameException {
        Object container = environment.getNamingResources().getContainer();
        if (container instanceof Server) {
            return new ObjectName(domain + ":type=Environment" + ",resourcetype=Global,name=" + environment.getName());
        } else if (container instanceof Context) {
            String path = ((Context) container).getPath();
            if (path.isEmpty()) {
                path = "/";
            }
            Host host = (Host) ((Context) container).getParent();
            return new ObjectName(domain + ":type=Environment" + ",resourcetype=Context,path=" + path + ",host="
                + host.getName() + ",name=" + environment.getName());
        }

        return null;
    }


    /**
     * Create an <code>ObjectName</code> for <code>ContextResource</code> object.
     *
     * @param resource The ContextResource to be named
     * @throws MalformedObjectNameException if a name cannot be created
     */
    public ObjectName createObjectName(ContextResource resource) throws MalformedObjectNameException {
        String encodedResourceName = urlEncoder.encode(resource.getName());
        Object container = resource.getNamingResources().getContainer();
        if (container instanceof Server) {
            return new ObjectName(domain + ":type=Resource" + ",resourcetype=Global,class=" + resource.getType()
                + ",name=" + encodedResourceName);
        } else if (container instanceof Context) {
            String path = ((Context) container).getPath();
            if (path.length() < 1) {
                path = "/";
            }
            Host host = (Host) ((Context) container).getParent();
            return new ObjectName(domain + ":type=Resource" + ",resourcetype=Context,path=" + path + ",host="
                + host.getName() + ",class=" + resource.getType() + ",name=" + encodedResourceName);
        }

        return null;
    }


    /**
     * Create an <code>ObjectName</code> for <code>ContextResourceLink</code> object.
     *
     * @param resourceLink The ContextResourceLink to be named
     * @throws MalformedObjectNameException if a name cannot be created
     */
    public ObjectName createObjectName(ContextResourceLink resourceLink) throws MalformedObjectNameException {
        String encodedResourceLinkName = urlEncoder.encode(resourceLink.getName());
        Object container = resourceLink.getNamingResources().getContainer();
        if (container instanceof Server) {
            return new ObjectName(
                domain + ":type=ResourceLink" + ",resourcetype=Global" + ",name=" + encodedResourceLinkName);
        } else if (container instanceof Context) {
            String path = ((Context) container).getPath();
            if (path.length() < 1) {
                path = "/";
            }
            Host host = (Host) ((Context) container).getParent();
            return new ObjectName(domain + ":type=ResourceLink" + ",resourcetype=Context,path=" + path + ",host="
                + host.getName() + ",name=" + encodedResourceLinkName);
        }

        return null;
    }


    @Override
    public Object getAttribute(String name) {
        return context.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return context.getAttributeNames();
    }

    @Override
    public String getContextPath() {
        return getPath();
    }

    @Override
    public ServletContext getContext(String uri) {

        // Validate the format of the specified argument
        if (uri == null || !uri.startsWith("/")) {
            return null;
        }

        Context child = null;
        try {
            Host host = (Host) getParent();
            String mapuri = uri;
            while (true) {
                child = (Context) host.findChild(mapuri);
                if (child != null) {
                    break;
                }
                int slash = mapuri.lastIndexOf('/');
                if (slash < 0) {
                    break;
                }
                mapuri = mapuri.substring(0, slash);
            }
        } catch (Throwable t) {
            return null;
        }

        if (child == null) {
            return null;
        }

        if (getCrossContext()) {
            // If crossContext is enabled, can always return the context
            return child.getServletContext();
        } else if (child == this) {
            // Can still return the current context
            return getServletContext();
        } else {
            // Nothing to return
            return null;
        }
    }

    @Override
    public String getInitParameter(final String name) {
        return context.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return context.getInitParameterNames();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (isContextInitializedCalled) {
            String msg = format(rb.getString(SERVLET_CONTEXT_ALREADY_INIT_EXCEPTION),
                new Object[] {"setInitParameter", getName()});
            throw new IllegalStateException(msg);
        }
        return context.setInitParameter(name, value);
    }

    @Override
    public int getMajorVersion() {
        return context.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return context.getMinorVersion();
    }

    @Override
    public String getMimeType(String file) {
        if (file == null) {
            return null;
        }
        int period = file.lastIndexOf(".");
        if (period < 0) {
            return null;
        }
        String extension = file.substring(period + 1);
        if (extension.length() < 1) {
            return null;
        }
        return findMimeMapping(extension);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        // Validate the name argument
        if (name == null) {
            return null;
        }

        // Create and return a corresponding request dispatcher
        Wrapper wrapper = (Wrapper) findChild(name);
        if (wrapper == null) {
            return null;
        }

        return new ApplicationDispatcher(wrapper, null, null, null, null, null, name);
    }

    @Override
    public String getServletContextName() {
        return getDisplayName();
    }

    @Override
    public void removeAttribute(String name) {
        context.removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        context.setAttribute(name, value);
    }

    @Override
    public String getServerInfo() {
        return context.getServerInfo();
    }

    @Override
    public String getRealPath(String path) {
        if (!(showArchivedRealPathEnabled || directoryDeployed)) {
            return null;
        }

        if (!isFilesystemBased()) {
            return null;
        }

        if (path == null) {
            return null;
        }

        File file = null;
        if (alternateDocBases == null || alternateDocBases.size() == 0) {
            file = new File(getBasePath(getDocBase()), path);
        } else {
            AlternateDocBase match = AlternateDocBase.findMatch(path, alternateDocBases);
            if (match != null) {
                file = new File(match.getBasePath(), path);
            } else {
                // None of the url patterns for alternate doc bases matched
                file = new File(getBasePath(getDocBase()), path);
            }
        }

        if (!file.exists()) {
            try {
                // Try looking up resource in
                // WEB-INF/lib/[*.jar]/META-INF/resources
                File f = getExtractedMetaInfResourcePath(path);
                if (f != null && f.exists()) {
                    file = f;
                }
            } catch (Exception e) {
                // ignore
            }
        }

        if (file.exists()) {
            return file.getAbsolutePath();
        }
        return null;
    }

    @Override
    public void log(String message) {
        message = neutralizeForLog(message);
        org.apache.catalina.Logger logger = getLogger();
        if (logger != null) {
            logger.log(logName() + " ServletContext.log():" + message, org.apache.catalina.Logger.INFORMATION);
        }
    }

    /**
     * Writes the specified exception and message to a servlet log file.
     */
    public void log(Exception exception, String message) {
        message = neutralizeForLog(message);
        org.apache.catalina.Logger logger = getLogger();
        if (logger != null) {
            logger.log(exception, logName() + message);
        }
    }

    @Override
    public void log(String message, Throwable throwable) {
        message = neutralizeForLog(message);
        org.apache.catalina.Logger logger = getLogger();
        if (logger != null) {
            logger.log(logName() + message, throwable);
        }
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        if (path == null || !path.startsWith("/")) {
            return null;
        }

        path = RequestUtil.normalize(path);
        if (path == null) {
            return null;
        }

        final DirContext resources;
        if (alternateDocBases == null || alternateDocBases.isEmpty()) {
            resources = getResources();
        } else {
            AlternateDocBase match = AlternateDocBase.findMatch(path, alternateDocBases);
            if (match == null) {
                // None of the url patterns for alternate doc bases matched
                resources = getResources();
            } else {
                resources = ContextsAdapterUtility.unwrap(match.getResources());
            }
        }

        if (resources != null) {
            try {
                Object resource = resources.lookup(path);
                if (resource instanceof Resource) {
                    return (((Resource) resource).streamContent());
                }
            } catch (Exception e) {
                // do nothing
            }
        }
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        log.log(FINEST, "getResource(path={0})", path);
        if (path == null || path.isEmpty() || path.charAt(0) != '/') {
            String msg = format(rb.getString(LogFacade.INCORRECT_PATH), path);
            throw new MalformedURLException(msg);
        }

        path = RequestUtil.normalize(path);
        if (path == null) {
            return null;
        }

        String libPath = "/WEB-INF/lib/";
        if (path.startsWith(libPath) && path.endsWith(".jar")) {
            File jarFile = null;
            if (isFilesystemBased()) {
                jarFile = new File(getBasePath(docBase), path);
            } else {
                jarFile = new File(getWorkPath(), path);
            }
            if (jarFile.exists()) {
                return jarFile.toURI().toURL();
            }
            return null;

        }
        final DirContext resources;
        if (alternateDocBases == null || alternateDocBases.isEmpty()) {
            resources = context.getResources();
        } else {
            AlternateDocBase match = AlternateDocBase.findMatch(path, alternateDocBases);
            if (match != null) {
                resources = ContextsAdapterUtility.unwrap(match.getResources());
            } else {
                // None of the url patterns for alternate doc bases matched
                resources = getResources();
            }
        }

        if (resources != null) {
            try {
                Object resource = resources.lookup(path);
                if (resource instanceof UrlResource) {
                    UrlResource urlResource = (UrlResource)resource;
                    return urlResource.getUrl();
                }
                String fullPath = getName() + path;
                String hostName = getParent().getName();
                return new URL("jndi", "", 0, getJNDIUri(hostName, fullPath),
                    new DirContextURLStreamHandler(resources));
            } catch (Exception e) {
                // do nothing
            }
        }
        return null;
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        // Validate the path argument
        if (path == null) {
            return null;
        }
        if (!path.startsWith("/")) {
            String msg = format(rb.getString(LogFacade.INCORRECT_PATH), path);
            throw new IllegalArgumentException(msg);
        }

        path = RequestUtil.normalize(path);
        if (path == null) {
            return (null);
        }

        DirContext resources = null;

        if (alternateDocBases == null || alternateDocBases.size() == 0) {
            resources = getResources();
        } else {
            AlternateDocBase match = AlternateDocBase.findMatch(path, alternateDocBases);
            if (match != null) {
                resources = ContextsAdapterUtility.unwrap(match.getResources());
            } else {
                // None of the url patterns for alternate doc bases matched
                resources = getResources();
            }
        }

        if (resources != null) {
            return (getResourcePathsInternal(resources, path));
        }

        return (null);
    }

    /**
     * Internal implementation of getResourcesPath() logic.
     *
     * @param resources Directory context to search
     * @param path Collection path
     */
    private Set<String> getResourcePathsInternal(DirContext resources, String path) {
        HashSet<String> set = new HashSet<>();
        try {
            listCollectionPaths(set, resources, path);
        } catch (NamingException e) {
            // Ignore, need to check for resource paths underneath
            // WEB-INF/lib/[*.jar]/META-INF/resources, see next
        }
        try {
            // Trigger expansion of bundled JAR files
            File file = getExtractedMetaInfResourcePath(path);
            if (file != null) {
                File[] children = file.listFiles();
                StringBuilder sb = null;
                for (File child : children) {
                    sb = new StringBuilder(path);
                    if (!path.endsWith("/")) {
                        sb.append("/");
                    }
                    sb.append(child.getName());
                    if (child.isDirectory()) {
                        sb.append("/");
                    }
                    set.add(sb.toString());
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * @return all the security roles
     */
    public List<String> getSecurityRoles() {
        return securityRoles;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {

        // Validate the path argument
        if (path == null) {
            return null;
        }

        if (!path.startsWith("/") && !path.isEmpty()) {
            String msg = format(rb.getString(LogFacade.INCORRECT_OR_NOT_EMPTY_PATH), path);
            throw new IllegalArgumentException(msg);
        }

        // Get query string
        String queryString = null;
        int pos = path.indexOf('?');
        if (pos >= 0) {
            queryString = path.substring(pos + 1);
            path = path.substring(0, pos);
        }

        path = RequestUtil.normalize(path);
        if (path == null) {
            return (null);
        }

        pos = path.length();

        // Use the thread local URI and mapping data
        DispatchData dd = dispatchData.get();
        if (dd == null) {
            dd = new DispatchData();
            dispatchData.set(dd);
        }

        MessageBytes uriMB = dd.uriMB;
        uriMB.recycle();

        // Retrieve the thread local mapping data
        MappingData mappingData = dd.mappingData;

        // Map the URI
        CharChunk uriCC = uriMB.getCharChunk();
        try {
            uriCC.append(getPath(), 0, getPath().length());
            /*
             * Ignore any trailing path params (separated by ';') for mapping purposes
             */
            int semicolon = path.indexOf(';');
            if (pos >= 0 && semicolon > pos) {
                semicolon = -1;
            }
            uriCC.append(path, 0, semicolon > 0 ? semicolon : pos);
            getMapper().map(uriMB, mappingData);
            if (mappingData.wrapper == null) {
                return (null);
            }
            /*
             * Append any trailing path params (separated by ';') that were ignored for mapping purposes, so that they're reflected
             * in the RequestDispatcher's requestURI
             */
            if (semicolon > 0) {
                uriCC.append(path, semicolon, pos - semicolon);
            }
        } catch (Exception e) {
            // Should never happen
            log.log(WARNING, LogFacade.MAPPING_ERROR_EXCEPTION, e);
            return (null);
        }

        Wrapper wrapper = (Wrapper) mappingData.wrapper;
        String wrapperPath = mappingData.wrapperPath.toString();
        String pathInfo = mappingData.pathInfo.toString();
        HttpServletMapping mappingForDispatch = new MappingImpl(mappingData);

        mappingData.recycle();

        // Construct a RequestDispatcher to process this request
        return new ApplicationDispatcher(wrapper, mappingForDispatch, uriCC.toString(), wrapperPath, pathInfo, queryString, null);
    }


    /**
     * @return the naming resources associated with this web application.
     */
    public DirContext getStaticResources() {
        return getResources();
    }

    /**
     * @return the naming resources associated with this web application.
     * FIXME: Fooling introspection ...
     */
    public DirContext findStaticResources() {
        return getResources();
    }

    /**
     * @return the naming resources associated with this web application.
     */
    public String[] getWelcomeFiles() {
        return findWelcomeFiles();
    }

    @Override
    public void setXmlValidation(boolean webXmlValidation) {
        this.webXmlValidation = webXmlValidation;
    }

    @Override
    public boolean getXmlValidation() {
        return webXmlValidation;
    }

    @Override
    public boolean getXmlNamespaceAware() {
        return webXmlNamespaceAware;
    }

    @Override
    public void setXmlNamespaceAware(boolean webXmlNamespaceAware) {
        this.webXmlNamespaceAware = webXmlNamespaceAware;
    }

    @Override
    public void setTldValidation(boolean tldValidation) {
        this.tldValidation = tldValidation;
    }

    @Override
    public boolean getTldValidation() {
        return tldValidation;
    }

    @Override
    public boolean getTldNamespaceAware() {
        return tldNamespaceAware;
    }

    @Override
    public void setTldNamespaceAware(boolean tldNamespaceAware) {
        this.tldNamespaceAware = tldNamespaceAware;
    }


    /**
     * Sets the list of ordered libs, which will be used as the value of the ServletContext
     * attribute with name jakarta.servlet.context.orderedLibs
     */
    public void setOrderedLibs(List<String> orderedLibs) {
        this.orderedLibs = orderedLibs;
    }

    public void startRecursive() throws LifecycleException {
        // nothing to start recursively, the servlets will be started by
        // load-on-startup
        start();
    }

    public int getState() {
        if (started) {
            return 1; // RUNNING
        }
        if (initialized) {
            return 0; // starting ?
        }
        if (!available) {
            return 4; // FAILED
        }
        // 2 - STOPPING
        return 3; // STOPPED
    }

    boolean isContextInitializedCalled() {
        return isContextInitializedCalled;
    }

    /**
     * Creates an ObjectInputStream that provides special deserialization logic for classes that are normally not
     * serializable (such as javax.naming.Context).
     *
     * @return {@link ObjectInputStream}, never null.
     */
    public ObjectInputStream createObjectInputStream(InputStream is) throws IOException {
        Loader loader = getLoader();
        if (loader != null) {
            ClassLoader classLoader = loader.getClassLoader();
            if (classLoader != null) {
                try {
                    return new CustomObjectInputStream(is, classLoader);
                } catch (IOException ioe) {
                    log.log(SEVERE, LogFacade.CANNOT_CREATE_OBJECT_INPUT_STREAM, ioe);
                }
            }
        }
        return new ObjectInputStream(is);
    }

    /**
     * Creates an ObjectOutputStream that provides special serialization logic for classes that are normally not
     * serializable (such as javax.naming.Context).
     */
    public ObjectOutputStream createObjectOutputStream(OutputStream os) throws IOException {
        return new ObjectOutputStream(os);
    }

    /**
     * Gets the time this context was started.
     *
     * @return Time (in milliseconds since January 1, 1970, 00:00:00) when this context was started
     */
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public boolean isEventProvider() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    /*
     * HTTP session related monitoring events
     */
    public void sessionCreatedEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionDestroyedEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionRejectedEvent(int maxSessions) {
        // Deliberate noop
    }

    public void sessionExpiredEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionPersistedStartEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionPersistedEndEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionActivatedStartEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionActivatedEndEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionPassivatedStartEvent(HttpSession session) {
        // Deliberate noop
    }

    public void sessionPassivatedEndEvent(HttpSession session) {
        // Deliberate noop
    }

    public static class RestrictedServletContextListener implements ServletContextListener {

        /*
         * The ServletContextListener to which to delegate
         */
        private final ServletContextListener delegate;

        /**
         * Constructor
         */
        public RestrictedServletContextListener(ServletContextListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void contextInitialized(ServletContextEvent sce) {
            delegate.contextInitialized(sce);
        }

        @Override
        public void contextDestroyed(ServletContextEvent sce) {
            delegate.contextDestroyed(sce);
        }

        public ServletContextListener getNestedListener() {
            return delegate;
        }
    }

    /**
     * Instantiates the given Servlet class.
     *
     * @return the new Servlet instance
     */
    protected <T extends Servlet> T createServletInstance(Class<T> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * Instantiates the given Filter class.
     *
     * @return the new Filter instance
     */
    protected <T extends Filter> T createFilterInstance(Class<T> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * Instantiates the given EventListener class.
     *
     * @return the new EventListener instance
     */
    public <T extends EventListener> T createListenerInstance(Class<T> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * Instantiates the given HttpUpgradeHandler class.
     *
     * @param clazz
     * @param <T>
     * @return a new T instance
     * @throws Exception
     */
    public <T extends HttpUpgradeHandler> T createHttpUpgradeHandlerInstance(Class<T> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * Custom security manager responsible for enforcing permission check on
     * ServletContext#getClassLoader if necessary.
     */
    private static class MySecurityManager extends SecurityManager {

        /**
         * @return true if the specified class loader <code>cl</code> can be found in the class
         *         loader delegation chain of the <code>start</code> class loader, false otherwise
         */
        boolean isAncestor(ClassLoader start, ClassLoader cl) {
            ClassLoader acl = start;
            do {
                acl = acl.getParent();
                if (cl == acl) {
                    return true;
                }
            } while (acl != null);
            return false;
        }


        /**
         * Checks whether access to the webapp class loader associated with this Context should be
         * granted to the caller of ServletContext#getClassLoader.
         * If no security manager exists, this method returns immediately.
         * Otherwise, it calls the security manager's checkPermission method with the getClassLoader
         * permission if the class loader of the caller of ServletContext#getClassLoader is not the
         * same as, or an ancestor of the webapp class loader associated with this Context.
         */
        void checkGetClassLoaderPermission(ClassLoader webappLoader) {
            SecurityManager sm = System.getSecurityManager();
            if (sm == null) {
                return;
            }

            // Get the current execution stack as an array of classes
            Class<?>[] classContext = getClassContext();

            /*
             * Determine the caller of ServletContext#getClassLoader:
             *
             * classContext[0]: org.apache.catalina.core.StandardContext$MySecurityManager classContext[1]:
             * org.apache.catalina.core.StandardContext classContext[2]: org.apache.catalina.core.StandardContext classContext[3]:
             * org.apache.catalina.core.ApplicationContext classContext[4]: org.apache.catalina.core.ApplicationContextFacade
             * classContext[5]: Caller whose classloader to check
             *
             * NOTE: INDEX MUST BE ADJUSTED WHENEVER EXECUTION STACK CHANGES, E.G., DUE TO CODE BEING REORGANIZED
             */
            ClassLoader ccl = classContext[5].getClassLoader();
            if (ccl != null && ccl != webappLoader && !isAncestor(webappLoader, ccl)) {
                sm.checkPermission(GET_CLASSLOADER_PERMISSION);
            }
        }
    }

    private static class PrivilegedCreateSecurityManager implements PrivilegedAction<MySecurityManager> {
        @Override
        public MySecurityManager run() {
            return new MySecurityManager();
        }
    }

    /**
     * List resource paths (recursively), and store all of them in the given Set.
     */
    private static void listCollectionPaths(Set<String> set, DirContext resources, String path) throws NamingException {
        Enumeration<Binding> childPaths = resources.listBindings(path);
        while (childPaths.hasMoreElements()) {
            Binding binding = childPaths.nextElement();
            String name = binding.getName();
            StringBuilder childPath = new StringBuilder(path);
            if (!"/".equals(path) && !path.endsWith("/")) {
                childPath.append("/");
            }
            childPath.append(name);
            Object object = binding.getObject();
            if (object instanceof DirContext && childPath.charAt(childPath.length() - 1) != '/') {
                childPath.append("/");
            }
            set.add(childPath.toString());
        }
    }

    /**
     * Get full path, based on the host name and the context path.
     */
    private static String getJNDIUri(String hostName, String path) {
        if (!path.startsWith("/")) {
            return "/" + hostName + "/" + path;
        }

        return "/" + hostName + path;
    }

    /**
     * Internal class used as thread-local storage when doing path mapping during dispatch.
     */
    private static final class DispatchData {
        public MessageBytes uriMB;
        public MappingData mappingData;

        private DispatchData() {
            uriMB = MessageBytes.newInstance();
            CharChunk uriCC = uriMB.getCharChunk();
            uriCC.setLimit(-1);
            mappingData = new MappingData();
        }
    }

    /**
     * Get resource from META-INF/resources/ in jars.
     */
    private File getExtractedMetaInfResourcePath(String path) {
        path = META_INF_RESOURCES + path;

        ClassLoader classLoader = getLoader().getClassLoader();
        if (classLoader instanceof WebappClassLoader) {
            return ((WebappClassLoader) classLoader).getExtractedResourcePath(path);
        }

        return null;
    }
}
