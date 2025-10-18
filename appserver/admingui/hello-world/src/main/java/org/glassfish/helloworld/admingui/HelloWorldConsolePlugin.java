/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.helloworld.admingui;

import java.net.URL;

import org.glassfish.api.admingui.ConsoleProvider;
import org.jvnet.hk2.annotations.Service;

/**
 * HK2 service that registers this module as an Admin Console plugin.
 * 
 * This class serves as the entry point for the plugin system to discover
 * and integrate this module with the Admin Console. Key aspects:
 * 
 * 1. @Service annotation makes this discoverable by HK2 service locator
 * 2. ConsoleProvider interface is the contract for console plugins
 * 3. getConfiguration() returning null means use default config file location
 * 4. The plugin system will look for META-INF/admingui/console-config.xml
 * 
 * The plugin architecture allows modular extensions to the console without
 * modifying core console code. Each plugin can contribute:
 * - Navigation nodes (tree items)
 * - Integration points (masthead, content areas)
 * - Resources (JSF pages, CSS, images)
 * - Java components (CDI beans, HK2 services)
 * 
 * This particular plugin demonstrates the "simplified plugin" approach where
 * the plugin JAR is included directly in the admin WAR rather than deployed
 * as a separate OSGi bundle.
 */
@Service
public class HelloWorldConsolePlugin implements ConsoleProvider {

    /**
     * Returns the URL to the plugin configuration file.
     * 
     * Returning null tells the plugin system to look for the default
     * configuration file at META-INF/admingui/console-config.xml within
     * this plugin's JAR file.
     * 
     * Alternative implementations could return a specific URL to a
     * configuration file located elsewhere.
     * 
     * @return null to use default config file location
     */
    @Override
    public URL getConfiguration() {
        return null;
    }
}
