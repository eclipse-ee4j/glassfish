/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.web.loader;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.annotation.HandlesTypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.deployment.common.ClassDependencyBuilder;
import org.glassfish.hk2.classmodel.reflect.AnnotatedElement;
import org.glassfish.hk2.classmodel.reflect.AnnotationType;
import org.glassfish.hk2.classmodel.reflect.ClassModel;
import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.classmodel.reflect.Member;
import org.glassfish.hk2.classmodel.reflect.Parameter;
import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.main.jdke.cl.GlassfishUrlClassLoader;

import static java.lang.Runtime.version;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.zip.ZipFile.OPEN_READ;
import static org.glassfish.web.loader.LogFacade.CLASS_LOADING_ERROR;
import static org.glassfish.web.loader.LogFacade.INVALID_URL_CLASS_LOADER_PATH;
import static org.glassfish.web.loader.LogFacade.IO_ERROR;
import static org.glassfish.web.loader.LogFacade.WRONG_CLASSLOADER_TYPE;

/**
 * Utility class - contains util methods used for implementation of
 * pluggable Shared Library features.
 *
 *  @author Vijay Ramachandran
 */
public class ServletContainerInitializerUtil {

    private static final Logger LOG = LogFacade.getLogger();

    private static final ResourceBundle rb = LOG.getResourceBundle();

    public interface LogContext {

        default Level getNonCriticalClassloadingErrorLogLevel() {
            return WARNING;
        }
    }

    /**
     * Given a class loader, check for ServletContainerInitializer
     * implementations in any JAR file in the classpath.
     *
     * @param cl The ClassLoader to be used to find JAR files
     *
     * @return Iterable over all ServletContainerInitializers that were found
     */
    public static ServiceLoader<ServletContainerInitializer> getServletContainerInitializers(String containerName,
        Map<String, String> webFragmentMap, List<Object> absoluteOrderingList, boolean hasOthers, ClassLoader cl) {
        /*
         * If there is an absoluteOrderingList specified, then make sure that
         * any ServletContainerInitializers included in fragment JARs
         * NOT listed in the absoluteOrderingList will be ignored.
         * For this, we remove any unwanted fragment JARs from the class
         * loader's URL
         */
        if((absoluteOrderingList != null) && !hasOthers) {
            if (!(cl instanceof URLClassLoader)) {
                LOG.log(WARNING, WRONG_CLASSLOADER_TYPE, cl.getClass().getCanonicalName());
                return null;
            }
            final URLClassLoader webAppCl = (URLClassLoader) cl;

            // Create a new List of URLs with missing fragments removed from
            // the currentUrls
            List<URL> newClassLoaderUrlList = new ArrayList<>();
            for (URL classLoaderUrl : webAppCl.getURLs()) {
                // Check that the URL is using file protocol, else ignore it
                if (!"file".equals(classLoaderUrl.getProtocol())) {
                    continue;
                }
                File file = new File(classLoaderUrl.getFile());
                try {
                    file = file.getCanonicalFile();
                } catch (IOException e) {
                    // Ignore
                }
                if (!file.exists()) {
                    continue;
                }
                String path = file.getAbsolutePath();
                if (!path.endsWith(".jar")) {
                    continue;
                }
                if (!isFragmentMissingFromAbsoluteOrdering(file.getName(),
                        webFragmentMap, absoluteOrderingList)) {
                    newClassLoaderUrlList.add(classLoaderUrl);
                }
            }

            // Create temporary classloader for ServiceLoader#load
            // TODO: Have temporary classloader honor delegate flag from sun-web.xml
            final URL[] urlArray = newClassLoaderUrlList.toArray(URL[]::new);
            PrivilegedAction<URLClassLoader> action = () -> new GlassfishUrlClassLoader(
                "ServletContainerInitializer(" + containerName + ")", urlArray, webAppCl.getParent());
            cl = AccessController.doPrivileged(action);
        }

        return ServiceLoader.load(ServletContainerInitializer.class, cl);
    }


    /**
     * Builds a mapping of classes to the list of ServletContainerInitializers
     * interested in them.
     *
     * @param initializers an Iterable over all ServletContainerInitializers
     * that need to be considered
     *
     * @return Mapping of classes to list of ServletContainerInitializers
     * interested in them
     */
    public static Map<Class<?>, List<Class<? extends ServletContainerInitializer>>> getInterestList(Iterable<ServletContainerInitializer> initializers) {

        if (null == initializers) {
            return null;
        }

        Map<Class<?>, List<Class<? extends ServletContainerInitializer>>> interestList = null;

        // Build a list of the classes / annotations in which the
        // initializers are interested
        for (ServletContainerInitializer sc : initializers) {
            if (interestList == null) {
                interestList = new HashMap<>();
            }
            Class<? extends ServletContainerInitializer> sciClass = sc.getClass();
            HandlesTypes ann = sciClass.getAnnotation(HandlesTypes.class);
            if (ann == null) {
                // This initializer does not contain @HandlesTypes
                // This means it should always be called for all web apps
                // So map it with a special token
                List<Class<? extends ServletContainerInitializer>> currentInitializerList =
                        interestList.get(ServletContainerInitializerUtil.class);
                if (currentInitializerList == null) {
                    List<Class<? extends ServletContainerInitializer>> arr = new ArrayList<>();
                    arr.add(sciClass);
                    interestList.put(ServletContainerInitializerUtil.class, arr);
                } else {
                    currentInitializerList.add(sciClass);
                }
            } else {
                Class<?>[] interestedClasses = ann.value();
                if (interestedClasses != null) {
                    for (Class<?> c : interestedClasses) {
                        List<Class<? extends ServletContainerInitializer>> currentInitializerList = interestList.get(c);
                        if (currentInitializerList == null) {
                            List<Class<? extends ServletContainerInitializer>> arr = new ArrayList<>();
                            arr.add(sciClass);
                            interestList.put(c, arr);
                        } else {
                            currentInitializerList.add(sciClass);
                        }
                    }
                }
            }
        }

        return interestList;
    }

    /**
     * Given an interestList that was built above, and a class loader, scan the entire web app's classes and libraries
     * looking for classes that extend/implement/use the annotations of a class present in the interest list.
     *
     * @param initializers Iterable over all ServletContainerInitializers that were discovered
     * @param interestList The interestList built by the previous util method
     * @param cl The classloader to be used to load classes in WAR
     * @return Map&lt;Class&lt;? extends ServletContainerInitializer&gt;, Set&lt;Class&lt;?&gt;&gt;&gt;
     *                          A Map of ServletContainerInitializer classes to be called and arguments to be passed
     *                          to them
     */
    public  static Map<Class<? extends ServletContainerInitializer>, Set<Class<?>>> getInitializerList(
            Iterable<ServletContainerInitializer> initializers,
            Map<Class<?>, List<Class<? extends ServletContainerInitializer>>> interestList,
            Types types,
            ClassLoader cl, LogContext logContext) {

        if (interestList == null) {
            return null;
        }

        // This contains the final list of initializers and the set of
        // classes to be passed to them as arg
        Map<Class<? extends ServletContainerInitializer>, Set<Class<?>>> initializerList = null;

        // If an initializer was present without any @HandleTypes, it
        // must be called with a null set of classes
        if(interestList.containsKey(ServletContainerInitializerUtil.class)) {
            initializerList = new HashMap<>();
            List<Class<? extends ServletContainerInitializer>> initializersWithoutHandleTypes =
                    interestList.get(ServletContainerInitializerUtil.class);
            for (Class<? extends ServletContainerInitializer> c : initializersWithoutHandleTypes) {
                initializerList.put(c, null);
            }
        }

        /*
         * Now scan every class in this app's WEB-INF/classes and WEB-INF/lib
         * to see if any class uses the annotation or extends/implements a
         * class in our interest list.
         * Do this scanning only if we have ServletContainerInitializers that
         * have expressed specific interest
         */
        if( (interestList.keySet().size() > 1) ||
                ((interestList.keySet().size() == 1) &&
                (!interestList.containsKey(ServletContainerInitializerUtil.class)))) {
            /*
             * Create an instance of ClassDependencyBuilder that looks at the byte code and keeps
             * the information for every class in this app
             *
             */
            if (types==null || Boolean.getBoolean("org.glassfish.web.parsing")) {
                ClassDependencyBuilder classInfo = new ClassDependencyBuilder();
                if (cl instanceof URLClassLoader) {
                    URLClassLoader ucl = (URLClassLoader) cl;
                    for (URL u : ucl.getURLs()) {
                        String path = u.getPath();
                        File file = new File(path);
                        try {
                            if (path.endsWith(".jar")) {
                                try (JarFile jarFile = new JarFile(file, true, OPEN_READ, version())){
                                    Iterator<JarEntry> entries = jarFile.versionedStream()
                                            .filter(jarEntry -> !jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")).iterator();
                                    while (entries.hasNext()) {
                                        JarEntry anEntry = entries.next();
                                        try (InputStream jarInputStream = jarFile.getInputStream(anEntry)) {
                                            classInfo.loadClassData(jarInputStream.readAllBytes());
                                        } catch (Throwable t) {
                                            if (LOG.isLoggable(FINE)) {
                                                LOG.log(FINE, CLASS_LOADING_ERROR, new Object[] {anEntry.getName(), t.toString()});
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (file.exists()) {
                                    if (file.isDirectory()) {
                                        scanDirectory(file, classInfo);
                                    } else {
                                        LOG.log(WARNING, INVALID_URL_CLASS_LOADER_PATH, path);
                                    }
                                }
                            }
                        } catch (IOException ioex) {
                            String msg = MessageFormat.format(rb.getString(IO_ERROR), path);
                            LOG.log(SEVERE, msg, ioex);
                            return null;
                        }
                    }
                }

                initializerList = checkAgainstInterestList(classInfo, interestList, initializerList, cl, logContext);
            } else {
                initializerList = checkAgainstInterestList(types, interestList, initializerList, cl, logContext);
            }
        }

        /*
         * If a ServletContainerInitializer was annotated with HandlesTypes,
         * but none of the application classes match, we must still invoke
         * it at its onStartup method, passing in a null Set of classes
         */
        for (ServletContainerInitializer initializer : initializers) {
            if (!initializerList.containsKey(initializer.getClass())) {
                initializerList.put(initializer.getClass(), null);
            }
        }

        return initializerList;
    }

    /**
     * Checks if a given JAR file is to be excluded while searching for ServletContainerInitializer implementations.
     *
     * @param jarName the JAR file
     * @param webFragmentMap fragment information from deployment desc
     * @param absoluteOrderingList give ordering list
     * @return true if the given JAR file is NOT present in the absolute ordering list
     */
    private static boolean isFragmentMissingFromAbsoluteOrdering(
           String jarName, Map<String, String> webFragmentMap, List<Object> absoluteOrderingList) {
        return (webFragmentMap != null && absoluteOrderingList != null &&
            !absoluteOrderingList.contains(webFragmentMap.get(jarName)));
   }

    /**
     * Given a directory, scan all subdirectories looking for classes and
     * build the interest list.
     *
     * @param dir the directory to be scanned
     * @param classInfo the ClassDependencyBuilder that holds info on all classes
     */
    private static void scanDirectory(File dir, ClassDependencyBuilder classInfo) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getPath();
                if (fileName.endsWith(".class")) {
                    try (InputStream fileStream = new FileInputStream(fileName)) {
                        classInfo.loadClassData(fileStream.readAllBytes());
                    } catch (Throwable t) {
                        if (LOG.isLoggable(WARNING)) {
                            LOG.log(WARNING, CLASS_LOADING_ERROR, new Object[] {fileName, t.toString()});
                        }
                    }
                }
            } else {
                scanDirectory(file, classInfo);
            }
        }
    }

    /**
     * Given the interestList, checks in the Types metadata if a given class
     * uses any of the annotations, subclasses any of the type; If so, builds
     * the initializer list.
     *
     */
    private static Map<Class<? extends ServletContainerInitializer>, Set<Class<?>>> checkAgainstInterestList(
                                Types classInfo,
                                Map<Class<?>, List<Class<? extends ServletContainerInitializer>>> interestList,
                                Map<Class<? extends ServletContainerInitializer>, Set<Class<?>>> initializerList,
                                ClassLoader cl, LogContext logContext) {

        if (classInfo == null) {
            return initializerList;
        }
        for (Map.Entry<Class<?>, List<Class<? extends ServletContainerInitializer>>> e : interestList.entrySet()) {
            Class<?> c = e.getKey();
            Type type = classInfo.getBy(c.getName());
            if (type == null) {
                continue;
            }

            Set<Class<?>> resultSet = new HashSet<>();
            if (type instanceof AnnotationType) {
                for (AnnotatedElement element : ((AnnotationType) type).allAnnotatedTypes()) {
                    if (element instanceof Member) {
                        element = ((Member) element).getDeclaringType();
                    } else if (element instanceof Parameter) {
                        element = ((Parameter) element).getMethod().getDeclaringType();
                    }
                    if (element instanceof Type) {
                        try {
                            resultSet.add(cl.loadClass(element.getName()));
                        } catch (Throwable t) {
                            if (LOG.isLoggable(logContext.getNonCriticalClassloadingErrorLogLevel())) {
                                LOG.log(logContext.getNonCriticalClassloadingErrorLogLevel(),
                                    CLASS_LOADING_ERROR, new Object[] {element.getName(), t.toString()});
                            }
                        }
                    }
                }
            } else {
                Collection<ClassModel> classes;
                if (type instanceof InterfaceModel) {
                    classes = ((InterfaceModel) type).allImplementations();
                } else {
                    classes = ((ClassModel) type).allSubTypes();
                }
                for (ClassModel classModel : classes) {
                    try {
                        resultSet.add(cl.loadClass(classModel.getName()));
                    } catch (Throwable t) {
                        if (LOG.isLoggable(logContext.getNonCriticalClassloadingErrorLogLevel())) {
                            LOG.log(logContext.getNonCriticalClassloadingErrorLogLevel(),
                                CLASS_LOADING_ERROR, new Object[] {classModel.getName(), t.toString()});
                        }
                    }
                }
            }
            if (initializerList == null) {
                initializerList = new HashMap<>();
            }
            List<Class<? extends ServletContainerInitializer>> containerInitializers = e.getValue();
            for (Class<? extends ServletContainerInitializer> initializer : containerInitializers) {
                Set<Class<?>> classSet = initializerList.get(initializer);
                if (classSet == null) {
                    classSet = new HashSet<>();
                }
                classSet.addAll(resultSet);
                initializerList.put(initializer, classSet);
            }

        }

        return initializerList;
    }
    /**
     * Given the interestList, checks if a given class uses any of the
     * annotations; If so, builds the initializer list
     *
     * @param classInfo the ClassDependencyBuilder instance that holds info on all classes
     * @param interestList the interestList built earlier
     * @param initializerList the initializerList built so far
     * @param cl the ClassLoader to be used to load the class
     * @return the updated initializer list
     */
    private static Map<Class<? extends ServletContainerInitializer>, Set<Class<?>>> checkAgainstInterestList(
                                ClassDependencyBuilder classInfo,
                                Map<Class<?>, List<Class<? extends ServletContainerInitializer>>> interestList,
                                Map<Class<? extends ServletContainerInitializer>, Set<Class<?>>> initializerList,
                                ClassLoader cl, LogContext logContext) {
        for(Map.Entry<Class<?>, List<Class<? extends ServletContainerInitializer>>> e : interestList.entrySet()) {
            Class<?> c = e.getKey();
            Set<String> resultFromClassInfo = classInfo.computeResult(c.getName());
            if (resultFromClassInfo.isEmpty()) {
                continue;
            }
            Set<Class<?>> resultSet = new HashSet<>();
            for (String string : resultFromClassInfo) {
                String className = string.replace('/', '.');
                try {
                    Class<?> aClass = cl.loadClass(className);
                    resultSet.add(aClass);
                } catch (Throwable t) {
                    if (LOG.isLoggable(logContext.getNonCriticalClassloadingErrorLogLevel())) {
                        LOG.log(logContext.getNonCriticalClassloadingErrorLogLevel(),
                            CLASS_LOADING_ERROR, new Object[] {className, t.toString()});
                    }
                }
            }
            if (initializerList == null) {
                initializerList = new HashMap<>();
            }
            List<Class<? extends ServletContainerInitializer>> containerInitializers = e.getValue();
            for (Class<? extends ServletContainerInitializer> initializer : containerInitializers) {
                Set<Class<?>> classSet = initializerList.get(initializer);
                if(classSet == null) {
                    classSet = new HashSet<>();
                }
                classSet.addAll(resultSet);
                initializerList.put(initializer, classSet);
            }
        }
        return initializerList;
    }
}
