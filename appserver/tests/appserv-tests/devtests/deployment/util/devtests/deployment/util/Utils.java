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

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;

import java.net.URL;
import java.util.logging.Logger;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

/**
 * Utilities to create a configured Habitat and cache them
 *
 * Shamelessly copied from nucleus/tests/util
 *
 * @author Jerome Dochez
 */
public class Utils {

    final static String habitatName = "default";
    final static String inhabitantPath = "META-INF/inhabitants";

    public static final Utils instance = new Utils();

    public static ServiceLocator getServiceLocator() {

        Properties p = new Properties();
        p.put(com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_PROP_NAME, System.getProperty("java.io.tmpdir"));
        p.put(com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_PROP_NAME, System.getProperty("java.io.tmpdir"));
        ModulesRegistry registry = new StaticModulesRegistry(Utils.class.getClassLoader(), new StartupContext(p));
        ServiceLocator serviceLocator = registry.createServiceLocator("default");

        return serviceLocator;
    }
}

