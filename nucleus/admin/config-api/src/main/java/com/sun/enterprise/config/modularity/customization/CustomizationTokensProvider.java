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

package com.sun.enterprise.config.modularity.customization;

import com.sun.enterprise.config.modularity.ConfigModularityUtils;
import com.sun.enterprise.config.modularity.annotation.HasCustomizationTokens;
import com.sun.enterprise.config.util.ConfigApiLoggerInfo;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import org.glassfish.hk2.api.ServiceLocator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Masoud Kalali
 */

public class CustomizationTokensProvider {

    private static final Logger LOG = ConfigApiLoggerInfo.getLogger();

    private static final LocalStringManager strings = new LocalStringManagerImpl(CustomizationTokensProvider.class);
    private ServiceLocator locator;
    ConfigModularityUtils mu;

    public List<ConfigCustomizationToken> getPresentConfigCustomizationTokens() throws NoSuchFieldException, IllegalAccessException {
        String runtimeType = "admin";
        initializeLocator();
        mu = locator.getService(ConfigModularityUtils.class);
        List<Class> l = mu.getAnnotatedConfigBeans(HasCustomizationTokens.class);
        List<ConfigCustomizationToken> ctk = new ArrayList<ConfigCustomizationToken>();
        Set s = new HashSet();
        for (Class cls : l) {
            if (!s.contains(cls)) {
                ctk.addAll(getTokens(cls, runtimeType));
                s.add(cls);
            }
        }
        return ctk;
    }

    /**
     * The tokens that are returned by this method will be used directly without consulting the portbase, etc. e.g if the
     * value is 24848 then that is to be used as the system-property value.
     *
     * @return List of tokens to be used for default-config.
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public List<ConfigCustomizationToken> getPresentDefaultConfigCustomizationTokens() throws
    //TODO it is required to change the file format so that default tokens can be introduced at file level
    NoSuchFieldException, IllegalAccessException {
        String runtimeType = "admin";
        initializeLocator();
        mu = locator.getService(ConfigModularityUtils.class);
        List<Class> l = mu.getAnnotatedConfigBeans(HasCustomizationTokens.class);
        List<ConfigCustomizationToken> ctk = new ArrayList<ConfigCustomizationToken>();
        Set s = new HashSet();
        for (Class cls : l) {
            if (!s.contains(cls)) {
                ctk.addAll(getTokens(cls, runtimeType));
                s.add(cls);
            }
        }
        Iterator<ConfigCustomizationToken> it = ctk.iterator();
        while (it.hasNext()) {
            ConfigCustomizationToken c = it.next();
            if (c.getCustomizationType().equals(ConfigCustomizationToken.CustomizationType.FILE)
                    || c.getCustomizationType().equals(ConfigCustomizationToken.CustomizationType.STRING)) {
                it.remove();
                continue;
            }
            int defaultPortNumberForDefaultConfig = Integer.parseInt(c.getValue()) + 20000;
            c.setValue(String.valueOf(defaultPortNumberForDefaultConfig));
        }

        return ctk;
    }

    private List<ConfigCustomizationToken> getTokens(Class configBean, String runtimeType) {
        List<ConfigCustomizationToken> ctk = new ArrayList<ConfigCustomizationToken>();
        List<ConfigBeanDefaultValue> defaultValues = mu.getDefaultConfigurations(configBean, runtimeType);
        for (ConfigBeanDefaultValue def : defaultValues) {
            ctk.addAll(def.getCustomizationTokens());
        }
        return ctk;
    }

    protected void initializeLocator() {
        final ClassLoader ecl = CustomizationTokensProvider.class.getClassLoader();
        File inst = new File(System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY));
        final File ext = new File(inst, "modules");
        LOG.log(Level.FINE, "asadmin modules directory: {0}", ext);
        if (ext.isDirectory()) {
            AccessController.doPrivileged(new PrivilegedAction() {
                @Override
                public Object run() {
                    try {
                        URLClassLoader pl = new URLClassLoader(getJars(ext));
                        ModulesRegistry registry = new StaticModulesRegistry(pl);
                        locator = registry.createServiceLocator("default");
                        return pl;
                    } catch (IOException ex) {
                        // any failure here is fatal
                        LOG.log(Level.SEVERE, ConfigApiLoggerInfo.MODULES_CL_FAILED, ex);
                    }
                    return ecl;
                }
            });
        } else {
            LOG.log(Level.FINER, "Modules directory does not exist");
        }
    }

    private static URL[] getJars(File dir) throws IOException {
        File[] fjars = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        if (fjars == null)
            throw new IOException("No Jar Files in the HK2Modules Directory!");
        URL[] jars = new URL[fjars.length];
        for (int i = 0; i < fjars.length; i++)
            jars[i] = fjars[i].toURI().toURL();
        return jars;
    }
}
