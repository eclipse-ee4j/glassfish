/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static com.sun.enterprise.util.FelixPrettyPrinter.prettyPrintExceptionMessage;
import static com.sun.enterprise.util.SystemPropertyConstants.DEBUG_MODE_PROPERTY;
import static java.util.Collections.enumeration;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModuleLifecycleListener;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.CompositeEnumeration;

import jakarta.inject.Inject;

/**
 * This class is responsible for creating a ClassLoader that can load classes exported by the system for public use. We
 * call those classes public APIs and the corresponding class loader is called APIClassLoader. Such classes include Java
 * EE API, AMX API, appserv-ext API, etc. CommonClassLoader delegates to this class loader. This class has a punch-in
 * mechanism to do special handling of META-INF/mailcap and META-INF/services resources.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class APIClassLoaderServiceImpl implements PostConstruct {

    /**
     * This is the module that we delegate to.
     */
    private static final String APIExporterModuleName = "GlassFish-Application-Common-Module"; // NOI18N
    private static final String MAILCAP = "META-INF/mailcap";
    private static final String META_INF_SERVICES = "META-INF/services/"; // NOI18N

    private static final String PUNCHIN_MODULE_STATE_PROP = "glassfish.kernel.apicl.punchin.module.state"; // NOI18N

    private static final Enumeration<URL> EMPTY_URLS = new Enumeration<>() {

        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public URL nextElement() {
            throw new NoSuchElementException();
        }
    };

    final static Logger logger = KernelLoggerInfo.getLogger();

    private HK2Module APIModule;

    /*
     * Implementation Note:
     *
     * 1. This class currently depends on a special which is configured such that it can load all
     * public APIs. The APIClassLoader is a wrapper around such a module's loader. This is how we are independent of actual
     * module system like OSGi. So far it has worked when we run in OSGi mode as well as when we run in a single classpath
     * mode.
     *
     * 2. APIClassLoader maintains a blacklist, i.e., classes and resources that could not be loaded to avoid
     * unnecessary delegation. It flushes that list every time a new bundle is installed in the system. This takes care of
     * performance problem in typical production use of GlassFish.
     *
     * TODO: 1. We need to add an upper threshold for blacklist to avoid excessive use of memory. 2. Externalize punch-in
     * facility. We don't want to know about things like MAILCAP file in this class.
     */

    private ClassLoader theAPIClassLoader;

    @Inject
    ModulesRegistry modulesRegistry;

    @Override
    public void postConstruct() {
        try {
            createAPIClassLoader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createAPIClassLoader() throws IOException {

        // The default module to load from is the common module, this dynamically loads from every other module.
        APIModule = modulesRegistry.getModules(APIExporterModuleName).iterator().next();
        assert APIModule != null;

        /*
         * We don't directly return APIModule's class loader, because that class loader does not delegate to the parent.
         *
         * Instead, it relies on OSGi bundle or some such module implementation to load the classes. That behavior is fine if we
         * want to mimic underlying module system's classloading semantics. But, APIClassLoader has a slightly different
         * requirement. It has to use classic delegation model as well so that deployed applications can use classes made
         * available via extension class loader.
         *
         * Since the parent of bundle classloader will have glassfish launching classes,
         * felix or any other OSGi framework classes and their dependencies, we don't want to delegate to such a class loader.
         *
         * Instead, we delegate to JRE's extension class loader if we don't find any class via APIModuleLoader. With this, user
         * can actually embed a different version of Felix as part of their app.
         */
        theAPIClassLoader = new APIClassLoader(APIModule.getClassLoader(), getExtensionClassLoader());
        logger.logp(FINE, "APIClassLoaderService", "createAPIClassLoader", "APIClassLoader = {0}", new Object[] { theAPIClassLoader });
    }

    private ClassLoader getExtensionClassLoader() {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return ClassLoader.getSystemClassLoader().getParent();
                }
            });
        } else {
            return ClassLoader.getSystemClassLoader().getParent();
        }
    }

    public ClassLoader getAPIClassLoader() {
        return theAPIClassLoader;
    }

    private class APIClassLoader extends ClassLoader {

        private final ClassLoader apiModuleLoader;

        // list of not found classes and resources.
        // the string represents resource name, so foo/Bar.class for foo.Bar
        private final Set<String> blacklist;

        // set to NEW to maintain backward compatibility. We should change it to RESOLVED after we have
        // done enough testing to make sure there are no regressions.
        private final ModuleState punchInModuleState = ModuleState
            .valueOf(System.getProperty(PUNCHIN_MODULE_STATE_PROP, ModuleState.NEW.toString()));

        /**
         * This method takes two classloaders which is unusual. Both the class loaders are consulted, so they both are
         * delegates, but depending on the class/resource being requested, one is preferred over the other. The second argument
         * is the classic parent class loader, where as the first one is the module system gateway classloader. For all java.*
         * names, we consult only the parent loader. For any other names, we first consult the gateway loader and then parent.
         * See more comments in {@link #loadClass(String)} method implementation of this class.
         *
         * @param apiModuleLoader ClassLoader corresponding to the APIModule
         * @param parent ClassLoader that's consulted for all java.* classes and for classes not found via apiModuleLoader
         */
        public APIClassLoader(ClassLoader apiModuleLoader, ClassLoader parent) {
            super(parent);
            this.apiModuleLoader = apiModuleLoader;
            blacklist = new HashSet<>();

            // add a listener to manage blacklist in APIClassLoader
            modulesRegistry.register(new ModuleLifecycleListener() {
                @Override
                public void moduleInstalled(HK2Module module) {
                    clearBlackList();
                }

                @Override
                public void moduleResolved(HK2Module module) {
                }

                @Override
                public void moduleStarted(HK2Module module) {
                }

                @Override
                public void moduleStopped(HK2Module module) {
                }

                @Override
                public void moduleUpdated(HK2Module module) {
                    clearBlackList();
                }
            });

        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return loadClass(name, false);
        }



        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // First check if we know this can't be loaded
            String resourceName = convertToResourceName(name);
            if (isBlackListed(resourceName)) {
                throw new ClassNotFoundException(name);
            }

            // Then check if the class has already been loaded
            Class<?> loadedClass = findLoadedClass(name);

            if (loadedClass == null) {
                if (!name.startsWith("java.")) { // java classes always come from parent
                    try {
                        loadedClass = apiModuleLoader.loadClass(name); // we ignore the resolution flag
                    } catch (ClassNotFoundException cnfe) {

                        // Punch in.
                        // Find the module that provides the class, no matter where we are.
                        HK2Module classProvidingModule = modulesRegistry.getProvidingModule(name);

                        if (classProvidingModule != null) {
                            if (select(classProvidingModule)) {
                                try {
                                    return classProvidingModule.getClassLoader().loadClass(name); // abort search if we fail to load.
                                } catch (ClassNotFoundException cnfe2) {

                                    // Only in debug mode, since the child classloader can legitimately  try other souces to load a class
                                    if ((Boolean.valueOf(System.getProperty(DEBUG_MODE_PROPERTY)))) {
                                        logger.logp(SEVERE, "APIClassLoaderServiceImpl$APIClassLoader", "loadClass", name, cnfe2);

                                        if (cnfe2.getCause() != null) {
                                            String message = prettyPrintExceptionMessage(cnfe2.getCause().getMessage());
                                            if (message != null && !message.isEmpty()) {
                                                logger.logp(SEVERE, "APIClassLoaderServiceImpl$APIClassLoader", "loadClass",
                                                    "\nFailed loading class " + name + " by API Class Loader\n\n" +
                                                    message + "\n");
                                            }
                                        }
                                    }

                                    throw cnfe2;

                                }
                            } else {
                                logger.logp(FINE, "APIClassLoaderServiceImpl$APIClassLoader", "loadClass",
                                        "Skipping loading {0} from module {1} as this module is not yet resolved.", new Object[] { name, classProvidingModule });
                            }
                        }
                    }
                }

                // This would be called for javax.* classes, and in the rare(?) case there's no providing module.
                if (loadedClass == null) {
                    // Call super class implementation which takes care of delegating to parent.
                    try {
                        loadedClass = super.loadClass(name, resolve);
                    } catch (ClassNotFoundException e) {
                        addToBlackList(resourceName);
                        throw e;
                    }
                }
            }

            return loadedClass;
        }

        /**
         * Select this module if it meets punch-in criteria. At this point of implementation, the criteria is very simple. It
         * checks to see if the module's state is greater than equal to what is configured in {@link #punchInModuleState}.
         *
         * @param module
         * @return
         */
        private boolean select(HK2Module module) {
            ModuleState state = module.getState();

            return state.compareTo(punchInModuleState) >= 0 && state != ModuleState.ERROR;
        }

        @Override
        public URL getResource(String name) {
            if (isBlackListed(name)) {
                return null;
            }

            URL url = null;
            if (!name.startsWith("java/")) {
                url = apiModuleLoader.getResource(name);
                if (url != null) {
                    return url;
                }

                // now punch-ins for various cases that require special handling
                if (name.equals(MAILCAP)) {
                    // punch in for META-INF/mailcap files.
                    // see issue #8426
                    for (HK2Module module : modulesRegistry.getModules()) {
                        if (!select(module)) {
                            continue;
                        }
                        if ((url = module.getClassLoader().getResource(name)) != null) {
                            return url;
                        }
                    }
                } else if (name.startsWith(META_INF_SERVICES)) {
                    // punch in to find the service loader from any module
                    // If this is a META-INF/services lookup, search in every
                    // modules that we know of.
                    String serviceName = name.substring(META_INF_SERVICES.length());

                    for (HK2Module module : modulesRegistry.getModules()) {
                        if (!select(module)) {
                            continue;
                        }

                        List<URL> list = module.getMetadata().getDescriptors(serviceName);
                        if (!list.isEmpty()) {
                            return list.get(0);
                        }
                    }
                }
            }

            // Either requested resource belongs to java/ namespace or
            // it was not found in any of the bundles, so call
            // super class implementation which will delegate to parent.
            url = super.getResource(name);
            if (url == null) {
                addToBlackList(name);
            }

            return url;
        }

        /**
         * This method is required as {@link ClassLoader#getParent} is a privileged method
         */
        private ClassLoader getParent_() {
            if (System.getSecurityManager() != null) {
                return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                    @Override
                    public ClassLoader run() {
                        return getParent();
                    }
                });
            } else {
                return getParent();
            }
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            List<Enumeration<URL>> enumerators = new ArrayList<>();
            enumerators.add(findResources(name));
            if (getParent_() != null) {
                enumerators.add(getParent_().getResources(name));
            }

            return new CompositeEnumeration(enumerators);
        }

        // This method is needed to be compatible with ClassLoader implementation in IBM JRE.
        // In IBM JRE, ClassLoader.getResources() does not call parent.getResources(); instead it
        // recurssively calls findResources() for all parents in the delegation hierarchy.
        // See issue #16364 for details.
        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            if (!name.startsWith("java/")) {

                List<Enumeration<URL>> enumerations = new ArrayList<>();
                Enumeration<URL> apiResources = apiModuleLoader.getResources(name);
                if (apiResources.hasMoreElements()) {
                    enumerations.add(apiResources);
                }

                // Now punch-ins for various cases that require special handling

                if (name.equals(MAILCAP)) {

                    // Punch in for META-INF/mailcap files. see issue #8426
                    for (HK2Module module : modulesRegistry.getModules()) {
                        if (!select(module) || (module == APIModule)) {
                            continue; // we have already looked up resources in apiModuleLoader
                        }

                        enumerations.add(module.getClassLoader().getResources(name));
                    }
                } else if (name.startsWith(META_INF_SERVICES)) {

                    // Punch in. find the service loader from any module

                    String serviceName = name.substring(META_INF_SERVICES.length());
                    List<URL> punchedInURLs = new ArrayList<>();
                    for (HK2Module module : modulesRegistry.getModules()) {
                        if (!select(module) || (module == APIModule)) {
                            continue; // we have already looked up resources in apiModuleLoader
                        }
                        punchedInURLs.addAll(module.getMetadata().getDescriptors(serviceName));
                    }

                    if (!punchedInURLs.isEmpty()) {
                        enumerations.add(enumeration(punchedInURLs));
                    }
                }

                // Now assemble the result and return
                switch (enumerations.size()) {
                case 0:
                    return EMPTY_URLS;
                case 1:
                    return enumerations.get(0);
                default:
                    return new CompositeEnumeration(enumerations);
                }
            }
            return EMPTY_URLS;
        }

        @Override
        public String toString() {
            return "APIClassLoader";
        }

        /**
         * Takes a class name as used in Class.forName and converts it to a resource name as used in ClassLoader.getResource
         *
         * @param className className to be converted
         * @return equivalent resource name
         */
        private String convertToResourceName(String className) {
            return className.replace('.', '/').concat(".class");
        }

        private synchronized boolean isBlackListed(String name) {
            return blacklist.contains(name);
        }

        private synchronized void addToBlackList(String name) {
            blacklist.add(name);
        }

        private synchronized void clearBlackList() {
            blacklist.clear();
        }

    }
}
