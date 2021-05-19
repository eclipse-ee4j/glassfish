/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

//import org.glassfish.admin.mbeanserver.LogMessagesResourceBundle;
//import org.glassfish.admin.mbeanserver.LoggerInfo;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModuleChangeListener;

@Service
@Singleton
public class LoggerInfoMetadataService implements LoggerInfoMetadata, ModuleChangeListener {

    private static final String RBNAME = "META-INF/loggerinfo/LoggerInfoMetadata";
    private static final Locale BASE_LOCALE = Locale.ROOT;

    @Inject
    ModulesRegistry modulesRegistry;

    private Map<Locale, LoggersInfoMap> metadataMaps;
    private Set<String> moduleNames;
    private boolean valid;

    // Reset valid flag if teh set of modules changes, so meta-data will be recomputed
    private Set<String> currentModuleNames() {
        Set<String> currentNames = new HashSet<String>();
        for (HK2Module module : modulesRegistry.getModules()) {
            currentNames.add(module.getName());
        }
        // If new modules set changes, force recomputation of logger meta-datas
        if (moduleNames != null) {
            for (String name : moduleNames) {
                if (!currentNames.contains(name)) {
                    valid = false;
                }
            }
            for (String name : currentNames) {
                if (!moduleNames.contains(name)) {
                    valid = false;
                }
            }
        } else {
            valid = false;
        }
        return currentNames;
    }

    private boolean isValid() {
        return (valid && metadataMaps != null);
    }

    private synchronized LoggersInfoMap getLoggersInfoMap(Locale locale) {
        moduleNames = currentModuleNames();
        if (!isValid()) {
            metadataMaps = new HashMap<Locale, LoggersInfoMap>();
        }
        LoggersInfoMap infos = metadataMaps.get(locale);
        if (infos == null) {
            infos = new LoggersInfoMap(locale);
            metadataMaps.put(locale, infos);
        }
        valid = true;
        return infos;
    }

    @Override
    public String getDescription(String logger) {
        LoggersInfoMap infos = getLoggersInfoMap(BASE_LOCALE);
        return infos.getDescription(logger);
    }

    @Override
    public String getDescription(String logger, Locale locale) {
        LoggersInfoMap infos = getLoggersInfoMap(locale);
        return infos.getDescription(logger);
    }

    @Override
    public Set<String> getLoggerNames() {
        LoggersInfoMap infos = getLoggersInfoMap(BASE_LOCALE);
        return infos.getLoggerNames();
    }

    @Override
    public String getSubsystem(String logger) {
        LoggersInfoMap infos = getLoggersInfoMap(BASE_LOCALE);
        return infos.getSubsystem(logger);
    }

    @Override
    public boolean isPublished(String logger) {
        LoggersInfoMap infos = getLoggersInfoMap(BASE_LOCALE);
        return infos.isPublished(logger);
    }

    // If a module changed in any way, reset the valid flag so meta-data will be
    // recomputed when subsequently requested.
    public synchronized void changed(HK2Module sender)  {
        valid = false;
    }

    private class LoggersInfoMap {
        private Locale locale;
        private Map<String, LoggerInfoData> map;

        LoggersInfoMap(Locale locale) {
            this.locale = locale;
            this.map = new HashMap<String, LoggerInfoData>();
            initialize();
        }

        public Set<String> getLoggerNames() {
            return map.keySet();
        }

        public String getDescription(String logger) {
            LoggerInfoData info = map.get(logger);
            return (info != null ? info.getDescription() : null);
        }

        public String getSubsystem(String logger) {
            LoggerInfoData info = map.get(logger);
            return (info != null ? info.getSubsystem() : null);
        }

        public boolean isPublished(String logger) {
            LoggerInfoData info = map.get(logger);
            return (info != null ? info.isPublished() : false);
        }

        private void initialize() {
            ClassLoader nullClassLoader = new NullClassLoader();
            for (HK2Module module : modulesRegistry.getModules()) {
                ModuleDefinition moduleDef = module.getModuleDefinition();
                // FIXME: We may optimize this by creating a manifest entry in the
                // jar file(s) to indicate that the jar contains logger infos. Jar files
                // need not be opened if they don't contain logger infos.
                URI uris[] = moduleDef.getLocations();
                int size = (uris != null ? uris.length : 0);
                if (size == 0) {
                    continue;
                }
                URL urls[] = new URL[size];
                try {
                    for (int i=0; i < size; i++) {
                        urls[i] = uris[i].toURL();
                    }
                    ClassLoader loader = new URLClassLoader(urls, nullClassLoader);
                    ResourceBundle rb = ResourceBundle.getBundle(RBNAME, locale, loader);
                    for (String key : rb.keySet()) {
                        int index = key.lastIndexOf('.');
                        String loggerName = key.substring(0, index);
                        String attribute = key.substring(index+1);
                        String value = rb.getString(key);
                        LoggerInfoData li = findOrCreateLoggerInfoMetadata(loggerName);
                        if (attribute.equals("description")) {
                            li.setDescription(value);
                        } else if (attribute.equals("publish")) {
                            li.setPublished(Boolean.parseBoolean(value));
                        } else if (attribute.equals("subsystem")) {
                            li.setSubsystem(value);
                        } else {
                            continue;
                        }
                    }
                } catch (MalformedURLException mfe) {
                    //FIXME: log message
                    mfe.printStackTrace();
                } catch (MissingResourceException mre) {
                    // Ignore
                }
            }
        }

        private LoggerInfoData findOrCreateLoggerInfoMetadata(String loggerName) {
            LoggerInfoData loggerInfoData = null;
            if (map.containsKey(loggerName)) {
                loggerInfoData = map.get(loggerName);
            } else {
                loggerInfoData = new LoggerInfoData();
                map.put(loggerName, loggerInfoData);
            }
            return loggerInfoData;
        }
    }

    // Null classloader to avoid delegation to parent classloader(s)
    private static class NullClassLoader extends ClassLoader {
        protected URL findResource(String name) {
            return null;
        }
        protected Enumeration findResources(java.lang.String name) throws IOException {
            return null;
        }
        public URL getResource(String name) {
            return null;
        }
        protected Class findClass(java.lang.String name) throws ClassNotFoundException {
            throw new ClassNotFoundException("Class not found: " + name);
        }
        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            throw new ClassNotFoundException("Class not found: " + name);
        }
    }
}
