/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.loader.util;

import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModulesRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.logging.annotation.LogMessageInfo;

import static com.sun.enterprise.util.Utility.isEmpty;
import static com.sun.enterprise.util.io.FileUtils.isJar;
import static com.sun.enterprise.util.io.FileUtils.isZip;
import static java.io.File.pathSeparator;
import static java.io.File.separator;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

public class ASClassLoaderUtil {

    public static final Logger deplLogger = DeploymentContextImpl.deplLogger;

    @LogMessageInfo(message = "Cannot convert classpath to URL {0}", level = "WARNING")
    private static final String CLASSPATH_ERROR = "NCLS-DEPLOYMENT-00045";

    @LogMessageInfo(message = "Exception:  {0}", level = "WARNING")
    private static final String EXCEPTION = "NCLS-DEPLOYMENT-00017";

    @LogMessageInfo(message = "unexpected error in getting urls", level = "WARNING")
    private static final String UNEXPECTED_EXCEPTION = "NCLS-DEPLOYMENT-00018";

    private static String modulesClassPath;

    /** The manifest file name from an archive. */
    private static final String MANIFEST_ENTRY = "META-INF" + separator + "MANIFEST.MF";

    /**
     * Gets the classpath associated with a module, suffixing libraries defined [if any] for the application
     *
     * @param serviceLocator the habitat the application resides in.
     * @param moduleId HK2Module id of the module
     * @param deploymentLibs libraries option passed through deployment
     * @return A <code>File.pathSeparator</code> separated list of classpaths for the passed in module, including the module
     * specified "libraries" defined for the module.
     */
    public static String getModuleClassPath(ServiceLocator serviceLocator, String moduleId, String deploymentLibs) {
        deplLogger.log(FINE, () -> "ASClassLoaderUtil.getModuleClassPath " + "for module Id : " + moduleId);

        StringBuilder classpath = new StringBuilder(getModulesClasspath(serviceLocator));

        String commonClassPath = serviceLocator.getService(ClassLoaderHierarchy.class).getCommonClassPath();
        if (!isEmpty(commonClassPath)) {
            classpath.append(commonClassPath).append(pathSeparator);
        }

        addDeployParamLibrariesForModule(classpath, moduleId, deploymentLibs, serviceLocator);
        deplLogger.log(FINE, () -> "Final classpath: " + classpath.toString());

        return classpath.toString();
    }

    public static String getModuleClassPath(ServiceLocator habitat, DeploymentContext context) {
        DeployCommandParameters params = context.getCommandParameters(DeployCommandParameters.class);

        return getModuleClassPath(habitat, params.name(), params.libraries());
    }

    private static void addDeployParamLibrariesForModule(StringBuilder sb, String moduleId, String deploymentLibs, ServiceLocator serviceLocator) {
        if (moduleId.indexOf("#") != -1) {
            moduleId = moduleId.substring(0, moduleId.indexOf("#"));
        }

        if (deploymentLibs == null) {
            ApplicationInfo appInfo = serviceLocator.getService(ApplicationRegistry.class).get(moduleId);
            if (appInfo == null) {
                // This might be an internal container app, like _default_web_app, ignore.
                return;
            }
            deploymentLibs = appInfo.getLibraries();
        }

        final URL[] libs = getDeployParamLibrariesAsURLs(deploymentLibs, serviceLocator);
        if (libs != null) {
            for (URL libUrl : libs) {
                sb.append(libUrl.getPath());
                sb.append(pathSeparator);
            }
        }
    }

    private static URL[] getDeployParamLibrariesAsURLs(String librariesStr, ServiceLocator serviceLocator) {
        return getDeployParamLibrariesAsURLs(librariesStr, serviceLocator.getService(ServerEnvironment.class));
    }

    /**
     * converts libraries specified via EXTENSION_LIST entry in MANIFEST.MF of all of the libraries of the deployed archive
     * to The libraries are made available to the application in the order specified.
     *
     * @param libraries is a comma-separated list of library JAR files
     * @param env the server environment
     * @return array of URL
     */
    public static URL[] getLibrariesAsURLs(Set<String> libraries, ServerEnvironment env) {
        if (libraries == null) {
            return null;
        }

        return getDeployParamLibrariesAsURLs(env, libraries.toArray(new String[libraries.size()]), new URL[libraries.size()]);
    }

    /**
     * converts libraries specified via the --libraries deployment option to URL[]. The library JAR files are specified by
     * either relative or absolute paths. The relative path is relative to instance-root/lib/applibs. The libraries are made
     * available to the application in the order specified.
     *
     * @param librariesStr is a comma-separated list of library JAR files
     * @param env the server environment
     * @return array of URL
     */
    public static URL[] getDeployParamLibrariesAsURLs(String librariesStr, ServerEnvironment env) {
        if (librariesStr == null) {
            return null;
        }

        String[] librariesStrArray = librariesStr.split(",");
        return getDeployParamLibrariesAsURLs(env, librariesStrArray, new URL[librariesStrArray.length]);
    }

    private static URL[] getDeployParamLibrariesAsURLs(ServerEnvironment env, String[] libraries, URL[] urls) {
        final String appLibsDir = env.getLibPath() + File.separator + "applibs";

        int i = 0;
        for (final String libraryStr : libraries) {
            try {
                File libraryFile = new File(libraryStr);
                if (!libraryFile.isAbsolute()) {
                    libraryFile = new File(appLibsDir, libraryStr);
                }

                URL url = libraryFile.toURI().toURL();
                urls[i++] = url;
            } catch (MalformedURLException malEx) {
                deplLogger.log(WARNING, CLASSPATH_ERROR, libraryStr);
                LogRecord lr = new LogRecord(WARNING, EXCEPTION);
                lr.setParameters(new Object[] { malEx.getMessage() });
                lr.setThrown(malEx);
                deplLogger.log(lr);
            }
        }

        return urls;
    }

    private static synchronized String getModulesClasspath(ServiceLocator serviceLocator) {
        synchronized (ASClassLoaderUtil.class) {
            if (modulesClassPath == null) {
                final StringBuilder modulesClassPathBuilder = new StringBuilder();
                ModulesRegistry modulesRegistry = serviceLocator.getService(ModulesRegistry.class);
                if (modulesRegistry != null) {
                    for (HK2Module module : modulesRegistry.getModules()) {
                        for (URI uri : module.getModuleDefinition().getLocations()) {
                            modulesClassPathBuilder.append(uri.getPath());
                            modulesClassPathBuilder.append(pathSeparator);
                        }
                    }
                }

                // Set shared classpath for module so that it doesn't need to be
                // recomputed for every other invocation
                modulesClassPath = modulesClassPathBuilder.toString();
            }
        }

        return modulesClassPath;
    }

    /**
     * Returns an array of urls that contains ..
     *
     * <pre>
     *    i.   all the valid directories from the given directory (dirs) array
     *    ii.  all jar files from the given directory (jarDirs) array
     *    iii. all zip files from the given directory (jarDirs) array if
     *         not ignoring zip file (ignoreZip is false).
     * </pre>
     *
     * @param dirs array of directory path names
     * @param jarDirs array of path name to directories that contains JAR & ZIP files.
     * @param ignoreZip whether to ignore zip files
     * @return an array of urls that contains all the valid dirs, *.jar & *.zip
     *
     * @throws IOException if an i/o error while constructing the urls
     */
    public static URL[] getURLs(File[] dirs, File[] jarDirs, boolean ignoreZip) throws IOException {
        return convertURLListToArray(getURLsAsList(dirs, jarDirs, ignoreZip));
    }

    /**
     * Returns a list of urls that contains ..
     *
     * <pre>
     *    i.   all the valid directories from the given directory (dirs) array
     *    ii.  all jar files from the given directory (jarDirs) array
     *    iii. all zip files from the given directory (jarDirs) array if
     *         not ignoring zip file (ignoreZip is false).
     * </pre>
     *
     * @param dirs array of directory path names
     * @param jarDirs array of path name to directories that contains JAR & ZIP files.
     * @param ignoreZip whether to ignore zip files
     * @return an array of urls that contains all the valid dirs, *.jar & *.zip
     *
     * @throws IOException if an i/o error while constructing the urls
     */
    public static List<URL> getURLsAsList(File[] dirs, File[] jarDirs, boolean ignoreZip) throws IOException {
        List<URL> list = new ArrayList<URL>();

        // Adds all directories
        if (dirs != null) {
            for (int i = 0; i < dirs.length; i++) {
                File dir = dirs[i];
                if (dir.isDirectory() || dir.canRead()) {
                    URL url = dir.toURI().toURL();
                    list.add(url);

                    if (deplLogger.isLoggable(FINE)) {
                        deplLogger.log(FINE, "Adding directory to class path:" + url.toString());
                    }
                }
            }
        }

        // Adds all the jars
        if (jarDirs != null) {
            for (int i = 0; i < jarDirs.length; i++) {
                File jarDir = jarDirs[i];

                if (jarDir.isDirectory() || jarDir.canRead()) {
                    File[] files = jarDir.listFiles();

                    for (int j = 0; j < files.length; j++) {
                        File jar = files[j];

                        if (isJar(jar) || (!ignoreZip && isZip(jar))) {
                            list.add(jar.toURI().toURL());

                            if (deplLogger.isLoggable(FINE)) {
                                deplLogger.log(FINE, "Adding jar to class path:" + jar.toURI().toURL());
                            }
                        }
                    }
                }
            }
        }

        return list;
    }

    public static URL[] convertURLListToArray(List<URL> list) {
        // converts the list to an array
        URL[] urls = new URL[0];
        if (list != null && list.size() > 0) {
            urls = new URL[list.size()];
            urls = list.toArray(urls);
        }
        return urls;
    }

    /**
     * get URL list from classpath
     *
     * @param classpath classpath string containing the classpaths
     * @param delimiter delimiter to separate the classpath components in the classpath string
     * @param rootPath root path of the classpath if the paths are relative
     *
     * @return urlList URL list from the given classpath
     */
    public static List<URL> getURLsFromClasspath(String classpath, String delimiter, String rootPath) {
        final List<URL> urls = new ArrayList<URL>();

        if (isEmpty(classpath)) {
            return urls;
        }

        // Tokenize classpath
        final StringTokenizer classpathTokenizer = new StringTokenizer(classpath, delimiter);
        while (classpathTokenizer.hasMoreTokens()) {
            try {
                String path = classpathTokenizer.nextToken();
                try {
                    // Try to see if the path is absolute
                    URI uri = new URL(path).toURI();
                    if (uri.isAbsolute()) {
                        urls.add(uri.toURL());
                        continue;
                    }
                } catch (Exception ie) {
                    // ignore
                }

                if (rootPath != null && rootPath.length() != 0) {
                    path = rootPath + File.separator + path;
                }

                urls.add(new File(path).toURI().toURL());
            } catch (Exception e) {
                deplLogger.log(WARNING, UNEXPECTED_EXCEPTION, e);
            }
        }

        return urls;
    }

    /**
     * Returns the manifest file for the given root path.
     *
     * <xmp> Example: |--repository/ | |--applications/ | |--converter/ | |--ejb-jar-ic_jar/ <---- rootPath | |--META-INF/ |
     * |--MANIFEST.MF </xmp>
     *
     * @param rootPath absolute path to the module
     *
     * @return the manifest file for the given module
     */
    public static Manifest getManifest(String rootPath) {
        // Gets the input stream to the MANIFEST.MF file
        try (InputStream in = new FileInputStream(rootPath + separator + MANIFEST_ENTRY)) {
           return new Manifest(in);
        } catch (IOException ioe) {
            // ignore
            return null;
        }
    }

    /**
     * Returns the class path (if any) from the given manifest file as an URL list.
     *
     * @param manifest manifest file of an archive
     * @param rootPath root path to the module
     *
     * @return a list of URLs an empty list if given manifest is null
     */
    public static List<URL> getManifestClassPathAsURLs(Manifest manifest, String rootPath) {
        List<URL> urls = new ArrayList<>();
        if (manifest == null) {
            return urls;
        }

        Attributes mainAttributes = manifest.getMainAttributes();

        for (Entry<Object, Object> entry : mainAttributes.entrySet()) {
            Name next = (Name) entry.getKey();

            if (next.equals(Name.CLASS_PATH)) {
                String classpathString = (String) entry.getValue();
                urls = getURLsFromClasspath(classpathString, " ", rootPath);
            }
        }

        return urls;
    }

    /**
     * add all the libraries packaged in the application library directory
     *
     * @param appRoot the application root
     * @param appLibDir the Application library directory
     * @param compatibilityProp the version of the release that we need to maintain backward compatibility
     * @return an array of URL
     */
    public static URL[] getAppLibDirLibraries(File appRoot, String appLibDir, String compatibilityProp) throws IOException {
        return convertURLListToArray(getAppLibDirLibrariesAsList(appRoot, appLibDir, compatibilityProp));
    }

    public static List<URL> getAppLibDirLibrariesAsList(File appRoot, String appLibDir, String compatibilityProp) throws IOException {
        URL[] libDirLibraries = new URL[0];
        // get all the app lib dir libraries
        if (appLibDir != null) {
            String libPath = appLibDir.replace('/', File.separatorChar);
            libDirLibraries = getURLs(null, new File[] { new File(appRoot, libPath) }, true);
        }

        List<URL> allLibDirLibraries = new ArrayList<URL>();
        for (URL url : libDirLibraries) {
            allLibDirLibraries.add(url);
        }

        // If the compatibility property is set to "v2", we should add all the
        // jars under the application root to maintain backward compatibility
        // of v2 jar visibility
        if (compatibilityProp != null && compatibilityProp.equals("v2")) {
            List<URL> appRootLibraries = getURLsAsList(null, new File[] { appRoot }, true);
            allLibDirLibraries.addAll(appRootLibraries);
        }

        return allLibDirLibraries;
    }

    public static List<URI> getLibDirectoryJarURIs(File moduleLibDirectory) throws Exception {
        List<URI> libLibraryURIs = new ArrayList<URI>();
        File[] jarFiles = moduleLibDirectory.listFiles(pathname -> pathname.getAbsolutePath().endsWith(".jar"));

        if (jarFiles != null && jarFiles.length > 0) {
            for (File jarFile : jarFiles) {
                libLibraryURIs.add(Util.toURI(jarFile.toURL()));
            }
        }

        return libLibraryURIs;
    }
}
