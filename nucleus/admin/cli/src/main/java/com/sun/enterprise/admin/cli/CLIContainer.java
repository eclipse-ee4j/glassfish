/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import com.sun.enterprise.admin.remote.Metrix;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.config.InjectionManager;
import org.jvnet.hk2.config.InjectionResolver;

/**
 * This exists mainly due performance reason. After construct it starts hk2 descriptors parsing because is is
 * significantly more effective then HK2 initialization. <br/>
 * It retrieve list of CLICommands and use it to determine if requested command is local or remote. <br/>
 * For local CLICommand which injects just ProgramOptions or Environment it can also create requested instance. For
 * other command uses lazy loaded HK2 ServiceLocator.
 *
 * @author martinmares
 */
public final class CLIContainer {

    class SimpleInjectionResolver extends InjectionResolver<Inject> {

        public SimpleInjectionResolver(Class<Inject> type) {
            super(type);
        }

        @Override
        public <V> V getValue(Object o, AnnotatedElement ae, Type genricType, Class<V> type) throws MultiException {
            if (type.isAssignableFrom(ProgramOptions.class)) {
                return (V) getProgramOptions();
            }
            if (type.isAssignableFrom(Environment.class)) {
                return (V) getEnvironment();
            }
            if (type.isAssignableFrom(CLIContainer.class)) {
                return (V) CLIContainer.this;
            }
            throw new IllegalStateException();
        }

    }

    private static final InjectionManager injectionMgr = new InjectionManager();

    private final Set<File> extensions;
    private final ClassLoader classLoader;
    private final Logger logger;

    private ServiceLocator serviceLocator;
    private ProgramOptions programOptions;
    private Environment environment;

    private Map<String, String> cliCommandsNames;

    public CLIContainer(final ClassLoader classLoader, final Set<File> extensions, final Logger logger) {
        this.classLoader = classLoader;
        this.extensions = extensions;
        this.logger = logger;
        try {
            cliCommandsNames = parseHk2Locators();
        } catch (IOException ex) {
            logger.log(Level.FINER, "Can't fast parse hk2 locators! HK2 ServiceLocator must be used");
        }
    }

    private Object createInstance(String name)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalStateException {
        if (name == null) {
            return null;
        }
        Class<?> clazz = Class.forName(name);
        if (clazz.getAnnotation(PerLookup.class) == null) {
            //Other scopes => HK2
            return null;
        }
        Object result = clazz.newInstance();
        InjectionResolver<Inject> injector = new SimpleInjectionResolver(Inject.class);
        injectionMgr.inject(result, injector);
        return result;
    }

    private void parseInHk2LocatorOrig(BufferedReader reader, Map<String, String> cliCommandNames) throws IOException {
        DescriptorImpl desc = new DescriptorImpl();
        while (desc.readObject(reader)) {
            if (StringUtils.ok(desc.getName()) && desc.getAdvertisedContracts().contains(CLICommand.class.getName())) {
                cliCommandNames.put(desc.getName(), desc.getImplementation());
            }
        }
    }

    private Set<File> expandExtensions() throws IOException {
        Set<File> result = new HashSet<File>();
        for (File file : extensions) {
            if (file.isDirectory()) {
                File[] lf = file.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase(Locale.ENGLISH).endsWith(".jar");
                    }
                });
                result.addAll(Arrays.asList(lf));
            } else {
                result.add(file);
            }
        }
        File inst = new File(System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
        File adminCliJar = new File(new File(inst, "modules"), "admin-cli.jar");
        if (!adminCliJar.exists()) {
            throw new IOException(adminCliJar.getCanonicalPath());
        }
        result.add(adminCliJar);
        return result;
    }

    private Map<String, String> parseHk2Locators() throws IOException {
        Map<String, String> result = new HashMap<String, String>();
        Set<File> extFiles = expandExtensions();
        for (File file : extFiles) {
            BufferedReader reader = null;
            JarFile jar = null;
            try {
                jar = new JarFile(file);
                ZipEntry entry = jar.getEntry("META-INF/hk2-locator/default");
                if (entry != null) {
                    reader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry)));
                    parseInHk2LocatorOrig(reader, result);
                }
            } finally {
                try {
                    reader.close();
                } catch (Exception ex) {
                }
                try {
                    jar.close();
                } catch (Exception ex) {
                }
            }
        }
        return result;
    }

    private String getCommandClassName(String name) throws InterruptedException, IllegalStateException {
        if (cliCommandsNames == null) {
            throw new IllegalStateException();
        } else {
            return cliCommandsNames.get(name);
        }
    }

    public ServiceLocator getServiceLocator() {
        if (serviceLocator == null) {
            Metrix.event("Init hk2 - start");
            ModulesRegistry registry = new StaticModulesRegistry(this.classLoader);
            serviceLocator = registry.createServiceLocator("default");
            if (programOptions != null) {
                ServiceLocatorUtilities.addOneConstant(serviceLocator, programOptions);
            }
            if (environment != null) {
                ServiceLocatorUtilities.addOneConstant(serviceLocator, environment);
            }
            ServiceLocatorUtilities.addOneConstant(serviceLocator, this);
            Metrix.event("Init hk2 - done");
        }
        return serviceLocator;
    }

    public CLICommand getLocalCommand(String name) {
        if (serviceLocator == null) {
            //First hard chack if it is local command
            try {
                String className = getCommandClassName(name);
                if (className == null) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.log(Level.FINER, "CLICommand not found for name {0}", name);
                    }
                    return null;
                }
                CLICommand result = (CLICommand) createInstance(className);
                if (result != null) {
                    if (logger.isLoggable(Level.FINER)) {
                        logger.log(Level.FINER, "CLIContainer creates instance for command {0}", name);
                    }
                    return result;
                }
            } catch (Exception ex) {
                //Not special case.
            }
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "HK2 Service locator will be used for command {0}", name);
        }
        return getServiceLocator().getService(CLICommand.class, name);
    }

    public Set<String> getLocalCommandsNames() {
        return cliCommandsNames.keySet();
    }

    public void setProgramOptions(ProgramOptions programOptions) {
        this.programOptions = programOptions;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public ProgramOptions getProgramOptions() {
        return programOptions;
    }

    public Environment getEnvironment() {
        return environment;
    }

}
