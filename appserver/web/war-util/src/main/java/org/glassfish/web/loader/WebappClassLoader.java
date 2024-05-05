/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package org.glassfish.web.loader;

import com.sun.enterprise.loader.ResourceLocator;
import com.sun.enterprise.security.integration.DDPermissionsLoader;
import com.sun.enterprise.security.integration.PermsHolder;
import com.sun.enterprise.util.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.naming.JndiPermission;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.naming.resources.JarFileResourcesProvider;
import org.apache.naming.resources.ProxyDirContext;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.ResourceAttributes;
import org.apache.naming.resources.WebDirContext;
import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.common.util.GlassfishUrlClassLoader;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.web.loader.RepositoryManager.RepositoryResource;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.web.loader.LogFacade.UNABLE_TO_LOAD_CLASS;
import static org.glassfish.web.loader.LogFacade.UNSUPPORTED_VERSION;
import static org.glassfish.web.loader.LogFacade.getString;

/**
 * Specialized web application class loader.
 * <p>
 * This class loader is a full reimplementation of the
 * <code>URLClassLoader</code> from the JDK. It is desinged to be fully
 * compatible with a normal <code>URLClassLoader</code>, although its internal
 * behavior may be completely different.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - This class loader faithfully follows
 * the delegation model recommended in the specification. The system class
 * loader will be queried first, then the local repositories, and only then
 * delegation to the parent class loader will occur. This allows the web
 * application to override any shared class except the classes from Java SE.
 * Special handling is provided from the JAXP XML parser interfaces, the JNDI
 * interfaces, and the classes from the servlet API, which are never loaded
 * from the webapp repository.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Due to limitations in WaSP
 * compilation technology, any repository which contains classes from
 * the servlet API will be ignored by the class loader.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - The class loader generates source
 * URLs which include the full JAR URL when a class is loaded from a JAR file,
 * which allows setting security permission at the class level, even when a
 * class is contained inside a JAR.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Local repositories are searched in
 * the order they are added via the initial constructor and/or any subsequent
 * calls to <code>addRepository()</code> or <code>addJar()</code>.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - No check for sealing violations or
 * security is made unless a security manager is present.
 *
 * @author Remy Maucherat
 * @author Craig R. McClanahan
 * @since 2007/08/17 15:46:27 $
 */
public final class WebappClassLoader extends GlassfishUrlClassLoader
    implements Reloader, InstrumentableClassLoader, DDPermissionsLoader, JarFileResourcesProvider, PreDestroy {

    static {
        registerAsParallelCapable();
    }

    /** First try parent classloader, then own resources. */
    public static final boolean DELEGATE_DEFAULT = true;

    private static final Logger LOG = LogFacade.getSysLogger(WebappClassLoader.class);

    private static final Function<String, String> PACKAGE_TO_PATH = pkg -> pkg.replace('.', '/');

    /** The path which will be monitored for added Jar files. */
    private static final String WEB_INF_LIB = "/WEB-INF/lib";

    /**
     * Set of package names which are not allowed to be loaded from a webapp
     * class loader without delegating first.
     */
    private static final Set<String> DELEGATED_PACKAGES = Set.of(
        "jakarta",                                   // Jakarta EE classes
        "javax",                                     // Java extensions
        "sun",                                       // Sun classes (JRE internals)
        "org.xml.sax",                               // SAX 1 & 2 (JRE, jrt-fs.jar)
        "org.w3c.dom",                               // DOM 1 & 2 (JRE, jrt-fs.jar)
        "org.glassfish.wasp.standard",               // wasp.jar
        "com.sun.faces"                              // jakarta.faces.jar
    );
    private static final Set<String> DELEGATED_RESOURCE_PATHS = DELEGATED_PACKAGES.stream()
        .map(PACKAGE_TO_PATH).collect(Collectors.toUnmodifiableSet());

    /** Instance of the SecurityManager installed. */
    private static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();

    private final ReferenceCleaner cleaner;

    /** The cache of ResourceEntry for classes and resources we have loaded, keyed by resource name. */
    private final ConcurrentHashMap<String, ResourceEntry> resourceEntryCache = new ConcurrentHashMap<>();

    /** The list of not found resources to avoid slow repeated searches. */
    private final Set<String> notFoundResources = ConcurrentHashMap.newKeySet();

    /* Name of the application this class loader is for */
    private String webappName;

    /** Associated directory context giving access to the resources in this webapp. */
    private DirContext jndiResources;

    /**
     * Should this class loader delegate to the parent class loader
     * <strong>before</strong> searching its own repositories (i.e. the
     * usual Java2 delegation model)?  If set to <code>false</code>,
     * this class loader will search its own repositories first, and
     * delegate to the parent only if the class or resource is not
     * found locally.
     */
    private boolean delegate = DELEGATE_DEFAULT;

    /** Use anti JAR locking code, which does URL rerouting when accessing resources. */
    private boolean antiJARLocking;

    private final RepositoryManager repositoryManager = new RepositoryManager();

    /**
     * JAR files cache manager. When files are not used, they can be automatically closed.
     */
    private final JarFileManager jarFiles = new JarFileManager();

    /**
     * The list of JARs last modified dates, in the order they should be
     * searched for locally loaded classes or resources.
     */
    private final ConcurrentLinkedQueue<PathTimestamp> pathTimestamps = new ConcurrentLinkedQueue<>();

    /**
     * The list of JARs in {@link #WEB_INF_LIB}, in the order they should be searched
     * for locally loaded classes or resources. This list serves to check if files changed.
     */
    private List<String> jarNames = new ArrayList<>();

    private boolean packageDefinitionSecurityEnabled;

    /**
     * A list of read File and Jndi Permission's required if this loader
     * is for a web application context.
     */
    private final ConcurrentLinkedQueue<Permission> permissionList = new ConcurrentLinkedQueue<>();

    /** holder for declared and ee permissions */
    private PermsHolder permissionsHolder;

    /** Path where resources loaded from JARs will be extracted. */
    private File loaderDir;

    /** The PermissionCollection for each CodeSource for a web application context. */
    private final ConcurrentHashMap<String, PermissionCollection> loaderPC = new ConcurrentHashMap<>();

    /** The system class loader. */
    private final ClassLoader system;

    private LifeCycleStatus status = LifeCycleStatus.NEW;

    /** Has external repositories. */
    private boolean hasExternalRepositories;

    /** List of byte code pre-processors per webapp class loader. */
    private final List<ClassFileTransformer> transformers = new CopyOnWriteArrayList<>();

    /** myfaces-api uses jakarta.faces packages */
    private boolean useMyFaces;

    /**
     * Set of packages that may always be overridden by the application,
     * regardless of whether they belong to a protected namespace
     * (i.e., a namespace that may never be overridden by any webapp)
     */
    private Set<String> overridablePackages = Set.of();

    /**
     * Should we attempt to null out any static final fields from loaded
     * classes when a web application is stopped as a work around for apparent
     * garbage collection bugs and application coding errors?
     * <p>There have been some issues reported with log4j when this option is true.
     * Applications without memory leaks using recent JVMs should operate correctly with this
     * option set to <code>false</code>.
     * <p>If not specified, the default value of <code>false</code> will be used.
     */
    private boolean clearReferencesStatic;

    /**
     * Name of associated context used with logging and JMX to associate with
     * the right web application. Particularly useful for the clear references
     * messages. Defaults to unknown but if standard Tomcat components are used
     * it will be updated during initialisation from the resources.
     */
    private String contextName = "unknown";

    /** Repositories URLs, used to cache the result of getURLs. */
    private List<URL> repositoryURLs;

    /**
     * Construct a new ClassLoader with the given parent ClassLoader,
     * but no defined repositories.
     */
    public WebappClassLoader(ClassLoader parent) {
        this(parent, "unknown");
    }

    public WebappClassLoader(ClassLoader parent, String webappName) {
        super(new URL[0], parent);
        this.webappName = webappName;
        this.cleaner = new ReferenceCleaner(this);
        this.system = WebappClassLoader.class.getClassLoader();
        if (SECURITY_MANAGER != null) {
            refreshPolicy();
        }
        this.permissionsHolder = new PermsHolder();
    }


    /**
     * Set the "delegate first" flag for this class loader.
     *
     * @param delegate The new "delegate first" flag
     */
    public void setDelegate(boolean delegate) {
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        LOG.log(DEBUG, "setDelegate(delegate={0})", delegate);
        this.delegate = delegate;
    }


    /**
     * Sets the given package names that may always be overriden, regardless of whether they belong
     * to a protected namespace
     */
    public void setOverridablePackages(Set<String> packageNames) {
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        overridablePackages = packageNames;
    }


    /**
     * Set associated resources.
     */
    public void setResources(DirContext resources) {
        LOG.log(DEBUG, "setResources(resources={0})", resources);
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        this.jndiResources = resources;
        final DirContext dirCtx;
        if (resources instanceof ProxyDirContext) {
            ProxyDirContext proxyRes = (ProxyDirContext) resources;
            contextName = proxyRes.getContextName();
            dirCtx = proxyRes.getDirContext();
        } else {
            dirCtx = resources;
        }
        if (dirCtx instanceof WebDirContext) {
            ((WebDirContext) dirCtx).setJarFileResourcesProvider(this);
        }
    }


    /**
     * Sets the anti JAR locking flag, which does URL rerouting when accessing resources.
     *
     * @param enable The new "anti JAR locking" flag
     */
    public void setAntiJARLocking(boolean enable) {
        checkStatus(LifeCycleStatus.NEW);
        this.antiJARLocking = enable;
    }


    /**
     * Enables checks for the package definition permissions.
     *
     * @param enable
     */
    public void setPackageDefinitionSecurityEnabled(boolean enable) {
        if (enable && SECURITY_MANAGER == null) {
            throw new IllegalArgumentException("The Security Manager is disabled.");
        }
        LOG.log(DEBUG, "setPackageDefinitionSecurityEnabled(enable={0})", enable);
        this.packageDefinitionSecurityEnabled = enable;
    }


    /**
     * If there is a Java SecurityManager create a read FilePermission
     * or JndiPermission for URL.
     *
     * @param url URL for a file or directory on local system
     */
    public void addPermission(URL url) {
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        if (url != null) {
            addPermission(url.toString());
        }
    }


    /**
     * If there is a Java SecurityManager create a read FilePermission
     * or JndiPermission for the file directory path.
     *
     * @param path file directory path
     */
    public void addPermission(final String path) {
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        if (path == null || SECURITY_MANAGER == null) {
            return;
        }
        SECURITY_MANAGER.checkSecurityAccess(DDPermissionsLoader.SET_EE_POLICY);
        if (path.startsWith("jndi:") || path.startsWith("jar:jndi:")) {
            final String jndiPath = path.endsWith("/") ? path : path + "/";
            permissionList.add(new JndiPermission(jndiPath + "*"));
        } else {
            final String filePath;
            if (path.endsWith(File.separator)) {
                filePath = path;
            } else {
                permissionList.add(new FilePermission(path, "read"));
                filePath = path + File.separator;
            }
            permissionList.add(new FilePermission(filePath + "-", "read"));
        }
    }


    /**
     * If there is a Java SecurityManager create a Permission.
     *
     * @param permission permission to add
     */
    public void addPermission(Permission permission) {
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        if (SECURITY_MANAGER != null && permission != null) {
            SECURITY_MANAGER.checkSecurityAccess(DDPermissionsLoader.SET_EE_POLICY);
            permissionList.add(permission);
        }
    }


    @Override
    public void addDeclaredPermissions(PermissionCollection declaredPc) throws SecurityException {
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        if (SECURITY_MANAGER != null) {
            SECURITY_MANAGER.checkSecurityAccess(DDPermissionsLoader.SET_EE_POLICY);
            permissionsHolder.setDeclaredPermissions(declaredPc);
        }
    }

    @Override
    public void addEEPermissions(PermissionCollection eePc) throws SecurityException {
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        if (SECURITY_MANAGER != null) {
            SECURITY_MANAGER.checkSecurityAccess(DDPermissionsLoader.SET_EE_POLICY);
            permissionsHolder.setEEPermissions(eePc);
        }
    }


    /**
     * @return {@value #WEB_INF_LIB}
     */
    public String getLibJarPath() {
        return WEB_INF_LIB;
    }


    /**
     * Change the work directory.
     */
    public void setWorkDir(File workDir) {
        checkStatus(LifeCycleStatus.NEW);
        this.loaderDir = new File(workDir, "loader_" + this.hashCode());
    }


    public void setUseMyFaces(boolean useMyFaces) {
        checkStatus(LifeCycleStatus.NEW);
        this.useMyFaces = useMyFaces;
    }


    /**
     * Set the clearReferencesStatic feature for this Context.
     *
     * @param clearReferencesStatic The new flag value
     */
    public void setClearReferencesStatic(boolean clearReferencesStatic) {
        checkStatus(LifeCycleStatus.NEW);
        this.clearReferencesStatic = clearReferencesStatic;
    }


    @Override
    public void addRepository(String repository) throws IllegalArgumentException {
        LOG.log(DEBUG, "addRepository(repository={0})", repository);
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        // Ignore any of the standard repositories, as they are set up using
        // either addJar or addRepository
        if (repository.startsWith(WEB_INF_LIB) || repository.startsWith("/WEB-INF/classes")) {
            return;
        }

        // Add this repository to our underlying class loader
        try {
            super.addURL(new URL(repository));
            hasExternalRepositories = true;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid repository: " + repository, e);
        }
    }


    /**
     * Add a new repository to the set of places this ClassLoader can look for
     * classes to be loaded.
     *
     * @param repository Name of a source of classes to be loaded, such as a directory pathname,
     *            a JAR file pathname, or a ZIP file pathname, relative to the
     *            {@link #setResources(DirContext)}
     * @param directory
     */
    public void addRepository(String repository, File directory) {
        LOG.log(DEBUG, "addRepository(repository={0}, file={1})", repository, directory);
        checkStatus(LifeCycleStatus.NEW);
        repositoryManager.addRepository(repository, directory);
    }


    public void addJar(String filePath, File file) {
        LOG.log(DEBUG, "addJar(filePath={0}, file={1})", filePath, file);
        checkStatus(LifeCycleStatus.NEW);

        super.addURL(toURL(file));

        if (filePath.startsWith(WEB_INF_LIB)) {
            String jarName = filePath.substring(WEB_INF_LIB.length());
            while (jarName.charAt(0) == '/') {
                jarName = jarName.substring(1);
            }
            jarNames.add(jarName);
        }

        try {
            final long lastModified = getResourceAttributes(filePath).getLastModified();
            pathTimestamps.add(new PathTimestamp(filePath, lastModified));
        } catch (NamingException e) {
            LOG.log(DEBUG, "Could not get resource attributes from JNDI for " + filePath, e);
        }

        jarFiles.addJarFile(file);
    }


    /**
     * Add a new ClassFileTransformer to this class loader.
     * This transfomer should be called for each class loading event.
     *
     * @param transformer new class file transformer to do byte code enhancement.
     */
    @Override
    public void addTransformer(final ClassFileTransformer transformer) {
        checkStatus(LifeCycleStatus.NEW, LifeCycleStatus.RUNNING);
        transformers.add(transformer);
    }


    /**
     * Start the class loader.
     */
    public void start() {
        LOG.log(DEBUG, "start()");
        checkStatus(LifeCycleStatus.NEW);
        jarNames = Collections.unmodifiableList(jarNames);
        status = LifeCycleStatus.RUNNING;
    }


    /**
     * Find the specified class in our local repositories, if possible.
     * If not found, throw {@link ClassNotFoundException}.
     *
     * @param name Name of the class to be loaded
     *
     * @throws ClassNotFoundException if the class was not found
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        LOG.log(DEBUG, "findClass(name={0})", name);
        checkStatus(LifeCycleStatus.RUNNING);

        // (1) Permission to define this class when using a SecurityManager
        if (packageDefinitionSecurityEnabled) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    SECURITY_MANAGER.checkPackageDefinition(name.substring(0, i));
                } catch (Exception se) {
                    throw new ClassNotFoundException(name, se);
                }
            }
        }

        // Ask our superclass to locate this class, if possible
        // (throws ClassNotFoundException if it is not found)
        Class<?> clazz = null;
        try {
            try {
                ResourceEntry entry = findClassInternal(name);
                // Create the code source object
                CodeSource codeSource = new CodeSource(entry.codeBase, entry.certificates);
                synchronized (getClassLoadingLock(name)) {
                    if (entry.loadedClass == null) {
                        // We use a temporary byte[] so that we don't change
                        // the content of entry in case bytecode preprocessing takes place.
                        byte[] binaryContent = entry.binaryContent;
                        if (!transformers.isEmpty()) {
                            String internalClassName = name.replace('.', '/');
                            for (ClassFileTransformer transformer : transformers) {
                                byte[] transformedBytes = transformer.transform(this, internalClassName, null, null, binaryContent);
                                // ClassFileTransformer returns null if no transformation took place.
                                if (transformedBytes != null) {
                                    binaryContent = transformedBytes;
                                    LOG.log(TRACE, "Transformed {0}", internalClassName);
                                }
                            }
                        }
                        clazz = defineClass(name, binaryContent, 0, binaryContent.length, codeSource);
                        entry.loadedClass = clazz;
                        entry.binaryContent = null;
                        entry.source = null;
                        entry.codeBase = null;
                        entry.manifest = null;
                        entry.certificates = null;
                    } else {
                        clazz = entry.loadedClass;
                    }
                }
            } catch (IllegalClassFormatException icfe) {
                throw new IllegalStateException("Could not preprocess " + toClassFilePath(name), icfe);
            } catch (ClassNotFoundException cnfe) {
                if (!hasExternalRepositories) {
                    throw cnfe;
                }
            } catch (UnsupportedClassVersionError ucve) {
                throw new UnsupportedClassVersionError(getString(UNSUPPORTED_VERSION, name, getJavaVersion()));
            } catch (AccessControlException ace) {
                throw new ClassNotFoundException(name, ace);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new RuntimeException(getString(UNABLE_TO_LOAD_CLASS, name, t.toString()), t);
            }
            if (clazz == null && hasExternalRepositories) {
                try {
                    clazz = super.findClass(name);
                } catch (AccessControlException ace) {
                    throw new ClassNotFoundException(name, ace);
                } catch (RuntimeException e) {
                    throw e;
                }
            }
            if (clazz == null) {
                throw new ClassNotFoundException(name);
            }
        } catch (ClassNotFoundException e) {
            // This is because some callers just swallow the CNFE.
            LOG.log(TRACE, "Rethrowing exception for " + name, e);
            throw e;
        }

        LOG.log(TRACE, "Returning {0}", clazz);
        return clazz;
    }


    /**
     * Find the specified resource in our local repository, and return a {@link URL} referring
     * to it, or <code>null</code> if this resource cannot be found.
     *
     * @param name Name of the resource to be found
     */
    @Override
    public URL findResource(String name) {
        LOG.log(DEBUG, "findResource(name={0})", name);
        checkStatus(LifeCycleStatus.RUNNING);
        if (".".equals(name)) {
            name = "";
        }
        ResourceEntry entry = resourceEntryCache.get(name);
        if (entry == null) {
            entry = findResourceInternal(name, name);
        }
        URL url = null;
        if (entry != null) {
            url = entry.source;
        }
        if (url == null && hasExternalRepositories) {
            url = super.findResource(name);
        }
        LOG.log(TRACE, "Returning {0} for name={1}", url, name);
        return url;

    }


    /**
     * Return an enumeration of <code>URLs</code> representing all of the
     * resources with the given name.  If no resources with this name are
     * found, return an empty enumeration.
     *
     * @param name Name of the resources to be found
     *
     * @throws IOException if an input/output error occurs
     */
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        LOG.log(DEBUG, "findResources(name={0})", name);
        checkStatus(LifeCycleStatus.RUNNING);
        if (jndiResources == null) {
            return super.findResources(name);
        }
        List<URL> foundResources = new ArrayList<>();
        List<RepositoryResource> resources = repositoryManager.getResources(name);
        for (RepositoryResource resource : resources) {
            try {
                jndiResources.lookup(resource.name);
                foundResources.add(toURL(resource.file));
            } catch (NamingException e) {
                // ignore, we continue searching
            }
        }

        Enumeration<URL> otherResourcePaths = super.findResources(name);
        while (otherResourcePaths.hasMoreElements()) {
            foundResources.add(otherResourcePaths.nextElement());
        }
        return Collections.enumeration(foundResources);
    }


    /**
     * Find the resource with the given name.  A resource is some data
     * (images, audio, text, etc.) that can be accessed by class code in a
     * way that is independent of the location of the code.  The name of a
     * resource is a "/"-separated path name that identifies the resource.
     * If the resource cannot be found, return <code>null</code>.
     * <p>
     * This method searches according to the following algorithm, returning
     * as soon as it finds the appropriate URL.  If the resource cannot be
     * found, returns <code>null</code>.
     * <ul>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     *     call the <code>getResource()</code> method of the parent class
     *     loader, if any.</li>
     * <li>Call <code>findResource()</code> to find this resource in our
     *     locally defined repositories.</li>
     * <li>Call the <code>getResource()</code> method of the parent class
     *     loader, if any.</li>
     * </ul>
     *
     * @param name Name of the resource to return a URL for
     */
    @Override
    public URL getResource(String name) {
        LOG.log(DEBUG, "getResource(name={0})", name);
        checkStatus(LifeCycleStatus.RUNNING);
        // (1) Delegate to parent if requested, or if the requested resource
        // belongs to one of the packages that are part of the Jakarta EE platform
        if (isDelegateFirstResource(name)) {
            URL url = getDelegateClassLoader().getResource(name);
            if (url != null) {
                LOG.log(TRACE, "Returning {0} for name={1}", url, name);
                return url;
            }
        }

        // (2) Search local repositories
        URL url = findResource(name);
        if (url != null) {
            if (antiJARLocking && !name.endsWith(".class") && !name.endsWith(".jar")) {
                // Locating the repository for special handling in the case of a JAR
                ResourceEntry entry = resourceEntryCache.get(name);
                try {
                    String repository = entry.codeBase.toString();
                    if (repository.endsWith(".jar")) {
                        File resourceFile = new File(loaderDir, name);
                        url = resourceFile.toURI().toURL();
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            LOG.log(TRACE, "Returning {0} for name={1}", url, name);
            return url;
        }

        // (3) Delegate to parent unconditionally if not already attempted
        if (!delegate) {
            url = getDelegateClassLoader().getResource(name);
            if (url != null) {
                LOG.log(TRACE, "Returning {0} for name={1}", url, name);
                return url;
            }
        }

        // (4) Resource was not found
        LOG.log(TRACE, "Resource {0} not found, returning null", name);
        return null;
    }


    /**
     * Find the resource with the given name, and return an input stream
     * that can be used for reading it.  The search order is as described
     * for <code>getResource()</code>, after checking to see if the resource
     * data has been previously cached.  If the resource cannot be found,
     * return <code>null</code>.
     *
     * @param name Name of the resource to return an input stream for
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        LOG.log(DEBUG, "getResourceAsStream(name={0})", name);
        checkStatus(LifeCycleStatus.RUNNING);
        // (0) Check for a cached copy of this resource
        InputStream stream = findLoadedResource(name);
        if (stream != null) {
            return stream;
        }

        // (1) Delegate to parent if requested, or if the requested resource
        // belongs to one of the packages that are part of the Jakarta EE platform
        if (isDelegateFirstResource(name)) {
            stream = getDelegateClassLoader().getResourceAsStream(name);
            if (stream != null) {
                return stream;
            }
        }

        // (2) Search local repositories
        URL url = findResource(name);
        if (url != null) {
            stream = findLoadedResource(name);
            try {
                if (hasExternalRepositories && stream == null) {
                    stream = url.openStream();
                }
            } catch (IOException e) {
                 // Ignore
            }
            if (stream != null) {
                return stream;
            }
        }

        // (3) Delegate to parent unconditionally
        if (!delegate) {
            stream = getDelegateClassLoader().getResourceAsStream(name);
            if (stream != null) {
                return stream;
            }
        }

        // (4) Resource was not found
        LOG.log(TRACE, "Resource {0} not found, returning null", name);
        return null;
    }


    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        checkStatus(LifeCycleStatus.RUNNING);
        final ClassLoader loader = getDelegateClassLoader();
        final ResourceLocator locator = new ResourceLocator(this, loader, isDelegateFirstResource(name));
        return locator.getResources(name);
    }


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }


    /**
     * Load the class with the specified name, searching using the following
     * algorithm until it finds and returns the class.  If the class cannot
     * be found, returns <code>ClassNotFoundException</code>.
     * <ul>
     * <li>Call <code>findLoadedClass(String)</code> to check if the
     *     class has already been loaded.  If it has, the same
     *     <code>Class</code> object is returned.</li>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     *     call the <code>loadClass()</code> method of the parent class
     *     loader, if any.</li>
     * <li>Call <code>findClass()</code> to find this class in our locally
     *     defined repositories.</li>
     * <li>Call the <code>loadClass()</code> method of our parent
     *     class loader, if any.</li>
     * </ul>
     * If the class was found using the above steps, and the
     * <code>resolve</code> flag is <code>true</code>, this method will then
     * call <code>resolveClass(Class)</code> on the resulting Class object.
     *
     * @param name Name of the class to be loaded
     * @param resolve If <code>true</code> then resolve the class
     *
     * @throws ClassNotFoundException if the class was not found
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name == null) {
            return null;
        }
        LOG.log(DEBUG, "loadClass(name={0}, resolve={1})", name, resolve);
        checkStatus(LifeCycleStatus.RUNNING);

        synchronized (getClassLoadingLock(name)) {
            // (0) Check our previously loaded local class cache
            Class<?> clazz = findLoadedClass0(name);
            if (clazz != null) {
                return resolveIfRequired(resolve, clazz);
            }

            // (0.1) Check our previously loaded class cache
            clazz = findLoadedClass(name);
            if (clazz != null) {
                return resolveIfRequired(resolve, clazz);
            }

            // (0.5) Permission to access this class when using a SecurityManager
            if (packageDefinitionSecurityEnabled) {
                int i = name.lastIndexOf('.');
                if (i >= 0) {
                    try {
                        SECURITY_MANAGER.checkPackageAccess(name.substring(0, i));
                    } catch (SecurityException se) {
                        String error = getString(LogFacade.SECURITY_EXCEPTION, name);
                        LOG.log(INFO, error, se);
                        throw new ClassNotFoundException(error, se);
                    }
                }
            }

            final ClassLoader delegateLoader = getDelegateClassLoader();
            boolean delegateLoad = isDelegateFirstClass(name);

            // (1) Delegate to our parent if requested
            if (delegateLoad) {
                // Check delegate first
                try {
                    clazz = delegateLoader.loadClass(name);
                    if (clazz != null) {
                        return resolveIfRequired(resolve, clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore
                }
            }

            // (2) Search local repositories
            try {
                clazz = findClass(name);
                if (clazz != null) {
                    return resolveIfRequired(resolve, clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }

            // (3) Delegate if class was not found locally
            if (!delegateLoad) {
                try {
                    clazz = delegateLoader.loadClass(name);
                    if (clazz != null) {
                        return resolveIfRequired(resolve, clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore
                }
            }
        }
        throw new ClassNotFoundException(name);
    }


    /**
     * Returns the context name or null
     */
    @Override
    public String getName() {
        return this.contextName;
    }


    /**
     * Get the Permissions for a CodeSource.  If this instance
     * of WebappClassLoader is for a web application context,
     * add read FilePermission or JndiPermissions for the base
     * directory (if unpacked),
     * the context URL, and jar file resources.
     *
     * @param codeSource where the code was loaded from
     * @return PermissionCollection for CodeSource
     */
    @Override
    protected PermissionCollection getPermissions(CodeSource codeSource) {
        LOG.log(TRACE, "getPermissions(codeSource={0})", codeSource);
        checkStatus(LifeCycleStatus.RUNNING);
        String codeUrl = codeSource.getLocation().toString();
        PermissionCollection pc = loaderPC.get(codeUrl);
        if (pc != null) {
            return pc;
        }
        pc = new Permissions();
        PermissionCollection spc = super.getPermissions(codeSource);
        Enumeration<Permission> permsa = spc.elements();
        while (permsa.hasMoreElements()) {
            Permission p = permsa.nextElement();
            pc.add(p);
        }
        for (Permission p : permissionList) {
            pc.add(p);
        }

        // get the declared and EE perms
        PermissionCollection pc1 = permissionsHolder.getPermissions(codeSource, null);
        if (pc1 != null) {
            Enumeration<Permission> dperms = pc1.elements();
            while (dperms.hasMoreElements()) {
                Permission p = dperms.nextElement();
                pc.add(p);
            }
        }
        PermissionCollection tmpPc = loaderPC.putIfAbsent(codeUrl, pc);
        return tmpPc == null ? pc : tmpPc;
    }


    @Override
    public synchronized URL[] getURLs() {
        if (this.status == LifeCycleStatus.CLOSED) {
            return new URL[0];
        }
        if (repositoryURLs != null) {
            return repositoryURLs.toArray(URL[]::new);
        }
        final ArrayList<URL> urls = new ArrayList<>();
        for (File directory : repositoryManager.getDirectories()) {
            urls.add(toURL(directory));
        }
        for (File file : jarFiles.getJarRealFiles()) {
            urls.add(toURL(file));
        }
        for (URL url : super.getURLs()) {
            urls.add(url);
        }
        repositoryURLs = urls.stream().distinct().collect(Collectors.toList());
        return repositoryURLs.toArray(URL[]::new);
    }


    public File getExtractedResourcePath(String path) {
        if (antiJARLocking) {
            jarFiles.extractResources(loaderDir, path);
        }
        File extractedResource = new File(loaderDir, path);
        return extractedResource.exists() ? extractedResource : null;
    }


    @Override
    public JarFile[] getJarFiles() {
        checkStatus(LifeCycleStatus.RUNNING);
        return jarFiles.getJarFiles();
    }


    /**
     * Have one or more classes or resources been modified so that a {@link #reload()} is appropriate?
     */
    @Override
    public boolean modified() {
        checkStatus(LifeCycleStatus.RUNNING);
        if (jndiResources == null) {
            return false;
        }
        // Checking for modified loaded resources
        for (PathTimestamp pathTimestamp : pathTimestamps) {
            try {
                long currentLastModified = getResourceAttributes(pathTimestamp.path).getLastModified();
                long oldLastModified = pathTimestamp.timestamp;
                if (currentLastModified != oldLastModified) {
                    if (LOG.isLoggable(DEBUG)) {
                        LOG.log(DEBUG, "Resource {0} was modified at {1}, old time stamp was {2}.", pathTimestamp.path,
                            Instant.ofEpochMilli(currentLastModified), Instant.ofEpochMilli(oldLastModified));
                    }
                    return true;
                }
            } catch (NamingException e) {
                LOG.log(ERROR, LogFacade.MISSING_RESOURCE, pathTimestamp.path);
                return true;
            }
        }

        try {
            final int jarNamesLength = jarNames.size();
            final NamingEnumeration<Binding> bindings = jndiResources.listBindings(WEB_INF_LIB);
            int i = 0;
            while (bindings.hasMoreElements() && i < jarNamesLength) {
                NameClassPair ncPair = bindings.nextElement();
                String name = ncPair.getName();
                // Ignore non JARs present in the lib folder
                if (!name.endsWith(".jar") && !name.endsWith(".zip")) {
                    continue;
                }
                if (!name.equals(jarNames.get(i))) {
                    LOG.log(TRACE, "JAR files changed: {0}", name);
                    return true;
                }
                i++;
            }
            if (bindings.hasMoreElements()) {
                while (bindings.hasMoreElements()) {
                    NameClassPair ncPair = bindings.nextElement();
                    String name = ncPair.getName();
                    // Additional non-JAR files are allowed
                    if (name.endsWith(".jar") || name.endsWith(".zip")) {
                        LOG.log(TRACE, "Additional JARs have been added: {0}", name);
                        return true;
                    }
                }
            } else if (i < jarNamesLength) {
                LOG.log(TRACE, "Some JAR file was removed.");
                return true;
            }
        } catch (NamingException | ClassCastException e) {
            LOG.log(ERROR, LogFacade.FAILED_TRACKING_MODIFICATIONS, WEB_INF_LIB, e.getMessage());
        }

        // No classes have been modified
        return false;

    }


    /**
     * Used to signal to the classloader to release JAR resources because of reload.
     */
    public void reload() {
        checkStatus(LifeCycleStatus.RUNNING);
        jarFiles.closeJarFiles();
    }


    @Override
    public ClassLoader copy() {
        LOG.log(DEBUG, "copy()");
        // set getParent() as the parent of the cloned class loader
        PrivilegedAction<URLClassLoader> action = () -> new GlassfishUrlClassLoader(getURLs(), getParent());
        return AccessController.doPrivileged(action);
    }


    @Override
    public void close() throws IOException {
        if (status == LifeCycleStatus.CLOSED) {
            return;
        }
        LOG.log(INFO, () -> "close(), " + this.getClass().getSimpleName()
                + " classloader for application: " + webappName);
        LOG.log(DEBUG, () -> "close(), this:\n" + this);

        cleaner.clearReferences(clearReferencesStatic ? resourceEntryCache.values() : null);
        status = LifeCycleStatus.CLOSED;

        try {
            super.close();
        } catch (Exception e) {
            LOG.log(WARNING, "Parent close method failed.", e);
        }

        notFoundResources.clear();
        resourceEntryCache.clear();
        pathTimestamps.clear();

        jndiResources = null;
        repositoryURLs = null;
        hasExternalRepositories = false;
        repositoryManager.close();

        permissionList.clear();
        permissionsHolder = null;
        loaderPC.clear();
        jarFiles.close();

        if (loaderDir != null) {
            deleteDir(loaderDir);
        }

        DirContextURLStreamHandler.unbind(this);
    }


    /**
     * Constructs a short description of the classloader.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(4096);
        sb.append(super.toString());
        sb.append("[webAppName=").append(webappName);
        sb.append(", delegate=").append(delegate);
        sb.append(", context=").append(contextName);
        sb.append(", status=").append(status);
        sb.append(", antiJARLocking=").append(antiJARLocking);
        sb.append(", securityManager=").append(SECURITY_MANAGER != null);
        sb.append(", packageDefinitionSecurityEnabled=").append(packageDefinitionSecurityEnabled);
        sb.append(", repositories=").append(repositoryManager);
        sb.append(", notFound.size=").append(notFoundResources.size());
        sb.append(", pathTimestamps.size=").append(pathTimestamps.size());
        sb.append(", resourceEntryCache.size=").append(resourceEntryCache.size());
        sb.append(']');
        return sb.toString();
    }


    public void preDestroy() {
        LOG.log(TRACE, "preDestroy()");
        try {
            close();
        } catch (Exception e) {
            throw new IllegalStateException("There were issues with closing " + this, e);
        }
    }


    private ClassLoader getDelegateClassLoader() {
        ClassLoader parent = getParent();
        final ClassLoader delegateLoader = parent == null ? system : parent;
        return delegateLoader;
    }


    private Class<?> resolveIfRequired(boolean resolve, Class<?> clazz) {
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }


    /**
     * Find specified class in local repositories.
     *
     * @return the loaded class, never null
     */
    private ResourceEntry findClassInternal(String name) throws ClassNotFoundException {
        LOG.log(TRACE, "findClassInternal(name={0})", name);
        if (!validate(name)) {
            throw new ClassNotFoundException(name);
        }

        final ResourceEntry entry = findResourceInternal(name, toClassFilePath(name));
        if (entry == null) {
            throw new ClassNotFoundException(name);
        }

        synchronized (getClassLoadingLock(name)) {
            if (entry.loadedClass != null) {
                return entry;
            }
            if (entry.binaryContent == null) {
                throw new ClassNotFoundException(name);
            }

            // Looking up the package
            final int pos = name.lastIndexOf('.');
            final String packageName = pos == -1 ? null : name.substring(0, pos);
            final Package pkg;
            if (packageName == null) {
                pkg = null;
            } else {
                pkg = getDefinedPackage(packageName);

                // Define the package (if null)
                if (pkg == null) {
                    if (entry.manifest == null) {
                        definePackage(packageName, null, null, null, null, null, null, null);
                    } else {
                        definePackage(packageName, entry.manifest, entry.codeBase);
                    }
                }
            }

            if (SECURITY_MANAGER != null) {
                // Checking sealing
                if (pkg != null) {
                    final boolean sealCheck;
                    if (pkg.isSealed()) {
                        sealCheck = pkg.isSealed(entry.codeBase);
                    } else {
                        sealCheck = entry.manifest == null || !isPackageSealed(packageName, entry.manifest);
                    }
                    if (!sealCheck) {
                        throw new SecurityException(
                            "Sealing violation loading " + name + ": Package " + packageName + " is sealed.");
                    }
                }
            }
            return entry;
        }
    }


    /**
     * Attempts to find the specified resource in local repositories.
     *
     * @return the loaded resource, or null if the resource isn't found
     */
    private ResourceEntry findResourceInternal(String name, String path) {
        LOG.log(TRACE, "findResourceInternal(name={0}, path={1})", name, path);

        checkStatus(LifeCycleStatus.RUNNING);

        if (name == null || path == null) {
            return null;
        }

        ResourceEntry entry = resourceEntryCache.get(name);
        if (entry != null) {
            return entry;
        }
        if (notFoundResources.contains(name)) {
            return null;
        }

        entry = findResourceInternalFromRepositories(name, path);
        if (entry == null) {
            entry = jarFiles.findResource(name, path, loaderDir, antiJARLocking);
        }

        if (entry == null) {
            notFoundResources.add(name);
            return null;
        }

        // Add the entry in the local resource repository
        // Ensures that all the threads which may be in a race to load
        // a particular class all end up with the same ResourceEntry instance
        ResourceEntry alreadyPresentEntry = resourceEntryCache.putIfAbsent(name, entry);
        return alreadyPresentEntry == null ? entry : alreadyPresentEntry;
    }


    /**
     * Attempts to load the requested resource from this classloader's internal repositories.
     *
     * @return The requested resource, or null if not found
     */
    private ResourceEntry findResourceInternalFromRepositories(String name, String path) {
        LOG.log(TRACE, "findResourceInternalFromRepositories(name={0}, path={1})", name, path);
        if (jndiResources == null) {
            return null;
        }
        for (RepositoryResource repoResource : repositoryManager.getResources(path)) {
            try {
                final Object lookupResult = jndiResources.lookup(repoResource.name);
                if (lookupResult instanceof Resource) {
                    return toResourceEntry(name, repoResource, (Resource) lookupResult);
                } else if (lookupResult instanceof WebDirContext) {
                    ResourceAttributes attributes = getResourceAttributes(repoResource.name);
                    return toResourceEntry(name, repoResource, attributes);
                } else {
                    continue;
                }
            } catch (NamingException e) {
                // not found, search continues.
            }
        }
        return null;
    }


    private ResourceEntry toResourceEntry(String name, RepositoryResource repoResource, Resource resource)
        throws NamingException {
        final ResourceAttributes attributes = getResourceAttributes(repoResource.name);
        final ResourceEntry entry = toResourceEntry(name, repoResource, attributes);
        final int contentLength = (int) attributes.getContentLength();
        try (InputStream binaryStream = resource.streamContent()) {
            if (binaryStream != null) {
                entry.readEntryData(name, binaryStream, contentLength, null);
            }
        } catch (IOException e) {
            LOG.log(DEBUG, "Could not read entry data for " + name, e);
            return null;
        }
        return entry;
    }


    private ResourceEntry toResourceEntry(String name, RepositoryResource repoResource, ResourceAttributes attributes) {
        final ResourceEntry entry;
        if (SECURITY_MANAGER == null) {
            entry = new ResourceEntry(toURL(repoResource.file));
        } else {
            PrivilegedAction<ResourceEntry> action = () -> new ResourceEntry(toURL(repoResource.file));
            entry = AccessController.doPrivileged(action);
        }
        entry.lastModified = attributes.getLastModified();
        pathTimestamps.add(new PathTimestamp(repoResource.name, entry.lastModified));
        return entry;
    }


    private ResourceAttributes getResourceAttributes(String fullPath) throws NamingException {
        return (ResourceAttributes) jndiResources.getAttributes(fullPath);
    }


    /**
     * @return true if the specified package name is sealed according to the given manifest.
     */
    private boolean isPackageSealed(String name, Manifest man) {
        String path = name.replace('.', '/') + '/';
        Attributes attr = man.getAttributes(path);
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Name.SEALED);
        }
        if (sealed == null) {
            attr = man.getMainAttributes();
            if (attr != null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }


    /**
     * Finds the resource with the given name if it has previously been
     * loaded and cached by this class loader, and return an input stream
     * to the resource data.  If this resource has not been cached, return
     * <code>null</code>.
     *
     * @param name Name of the resource to return
     */
    private InputStream findLoadedResource(String name) {
        ResourceEntry entry = resourceEntryCache.get(name);
        if (entry != null) {
            if (entry.binaryContent != null) {
                return new ByteArrayInputStream(entry.binaryContent);
            }
        }
        return null;
    }


    /**
     * Finds the class with the given name if it has previously been
     * loaded and cached by this class loader, and return the Class object.
     * If this class has not been cached, return <code>null</code>.
     *
     * @param name Name of the resource to return
     */
    private Class<?> findLoadedClass0(String name) {
        ResourceEntry entry = resourceEntryCache.get(name);
        if (entry != null) {
            return entry.loadedClass;
        }
        return null;
    }


    /**
     * Refresh the system policy file, to pick up eventual changes.
     */
    private void refreshPolicy() {
        try {
            // The policy file may have been modified to adjust
            // permissions, so we're reloading it when loading or
            // reloading a Context
            Policy policy = Policy.getPolicy();
            policy.refresh();
        } catch (AccessControlException e) {
            // Some policy files may restrict this, even for the core,
            // so this exception is ignored
            LOG.log(TRACE, "The policy refresh failed.", e);
        }
    }


    /**
     * Validate a classname. As per SRV.9.7.2, we must restrict loading of
     * classes from J2SE (java.*) and classes of the servlet API
     * (jakarta.servlet.*). That should enhance robustness and prevent a number
     * of user error (where an older version of servlet.jar would be present
     * in /WEB-INF/lib).
     *
     * @param name class name
     * @return true if the name is valid
     */
    private boolean validate(String name) {
        if (name == null) {
            return false;
        }
        if (name.startsWith("java.")) {
            return false;
        }
        return true;
    }


    private static URL toURL(File file) {
        File realFile;
        try {
            realFile = file.getCanonicalFile();
        } catch (IOException e) {
            realFile = file;
        }
        try {
            return realFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Could not convert " + file + " to URL!", e);
        }
    }


    /**
     * Delete the specified directory, including all of its contents and
     * subdirectories recursively.
     *
     * @param dir File object representing the directory to be deleted
     */
    private static void deleteDir(File dir) {
        String[] files = dir.list();
        if (files == null) {
            files = new String[0];
        }
        for (String fileName : files) {
            File file = new File(dir, fileName);
            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                if (!FileUtils.deleteFileMaybe(file)) {
                    LOG.log(WARNING, LogFacade.UNABLE_TO_DELETE, file);
                }

            }
        }
        if (!FileUtils.deleteFileMaybe(dir)) {
            LOG.log(WARNING, LogFacade.UNABLE_TO_DELETE, dir);
        }
    }


    private String getJavaVersion() {
        if (SECURITY_MANAGER == null) {
            return System.getProperty("java.version");
        }
        PrivilegedAction<String> action = () -> System.getProperty("java.version");
        return AccessController.doPrivileged(action);
    }


    /**
     * @return true if the class should be first located by the delegating classloader.
     */
    private boolean isDelegateFirstClass(String className) {
        if (delegate) {
            return true;
        }
        // Special case for performance reason.
        if (className.startsWith("java.")) {
            return true;
        }
        final String packageName = getPackageName(className);
        if (overridablePackages.stream().anyMatch(packageName::startsWith)) {
            return false;
        }
        if (className.startsWith("jakarta.faces.")) {
            // myfaces-api uses jakarta.faces packages
            return !useMyFaces;
        }
        if (DELEGATED_PACKAGES.stream().anyMatch(packageName::startsWith)) {
            return true;
        }
        return false;
    }


    /**
     * @return true if the resource should be first located by the delegating classloader.
     */
    private boolean isDelegateFirstResource(String name) {
        if (delegate) {
            return true;
        }
        if (name.startsWith("java/")) {
            return true;
        }
        if (overridablePackages.stream().map(PACKAGE_TO_PATH).anyMatch(name::startsWith)) {
            return false;
        }
        if (name.startsWith("jakarta/faces/")) {
            // myfaces-api uses jakarta.faces packages
            return !useMyFaces;
        }
        if (DELEGATED_RESOURCE_PATHS.stream().anyMatch(name::startsWith)) {
            return true;
        }
        return false;
    }


    private void checkStatus(LifeCycleStatus... expected) {
        // FIXME The CL is used with unfinished configuration, so some methods allow two states.
        // Final refactored state should probably be some prepared data object unusable as CL,
        // but which could be set via constructor. The CL would be just usable or closed (it is closeable).
        // That will need more work and more time.
        for (LifeCycleStatus expectedStatus : expected) {
            if (this.status == expectedStatus) {
                return;
            }
        }
        throw new IllegalStateException("ClassLoader is not in expected state: " + this);
    }


    /**
     * @return ie. java/lang/Object.class
     */
    private static String toClassFilePath(String name) {
        return name.replace('.', '/') + ".class";
    }


    private static String getPackageName(final String className) {
        int pos = className.lastIndexOf('.');
        if (pos == -1) {
            // same as Class.getPackageName
            return "";
        }
        return className.substring(0, pos);
    }

    private enum LifeCycleStatus {
        NEW, RUNNING, CLOSED
    }


    private static class PathTimestamp {
        final String path;
        final long timestamp;

        PathTimestamp(String path, long timestamp) {
            this.path = path;
            this.timestamp = timestamp;
        }
    }
}
