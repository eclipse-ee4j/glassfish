/*
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

package org.glassfish.tests.utils;

// import com.sun.enterprise.module.bootstrap.Populator;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import com.sun.enterprise.module.single.StaticModulesRegistry;

/**
 * Utilities to create a configured Habitat and cache them
 *
 * @author Jerome Dochez
 */
public class Utils {

    final static String habitatName = "default";
    final static String inhabitantPath = "META-INF/inhabitants";

    private static Map<String, ServiceLocator> habitats = new HashMap<>();
    public static final Utils instance = new Utils();

    public synchronized ServiceLocator getHabitat(ConfigApiTest test) {

        final String fileName = test.getFileName();
        // we cache the habitat per xml file

        if (habitats.containsKey(fileName))  {
            return habitats.get(fileName);
        }

        ServiceLocator habitat = getNewHabitat(test);
        habitats.put(fileName, habitat);
        return habitat;
    }

    private static synchronized ServiceLocator getNewHabitat(final ConfigApiTest test) {
        final ServiceLocator sl = getNewHabitat();

        final String fileName = test.getFileName();
        ConfigParser configParser = new ConfigParser(sl);

        long now = System.currentTimeMillis();
        URL url = Utils.class.getClassLoader().getResource(fileName + ".xml");
        if (url != null) {
            try {
                DomDocument testDocument = test.getDocument(sl);
                DomDocument document = configParser.parse(url, testDocument);
                ServiceLocatorUtilities.addOneConstant(sl, document);
                test.decorate(sl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Logger.getAnonymousLogger().fine(
                "time to parse domain.xml : "
                    + String.valueOf(System.currentTimeMillis() - now));
        }

        return sl;
    }

    public static ServiceLocator getNewHabitat() {
        final String root =  Utils.class.getResource("/").getPath();
        return getNewHabitat(root);
    }

    public static ServiceLocator getNewHabitat(String root) {

        Properties p = new Properties();
        p.put(com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_PROP_NAME, root);
        p.put(com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_PROP_NAME, root);
        ModulesRegistry registry = new StaticModulesRegistry(Utils.class.getClassLoader(), new StartupContext(p));
        ServiceLocator defaultSL = registry.createServiceLocator("default");
        return defaultSL;
    }

    public void shutdownServiceLocator(
        final ConfigApiTest test) {
        String fileName = test.getFileName();

        if (habitats.containsKey(fileName))  {
            ServiceLocator locator = habitats.remove(fileName);
            ServiceLocatorFactory.getInstance().destroy(locator);
        }
    }
}
