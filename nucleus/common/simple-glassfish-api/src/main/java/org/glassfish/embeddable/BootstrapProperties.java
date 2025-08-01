/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable;

import java.util.Properties;

import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;

/**
 * Encapsulates the set of properties required to bootstrap GlassFishRuntime.
 *
 * <pre>
 * ...
 * GlassFishRuntime runtime = GlassFishRuntime.bootstrap(new BootstrapProperties());
 * GlassFish glassfish = runtime.newGlassFish(glassFishProperties);
 * ...
 * </pre>
 *
 * @author bhavanishankar@dev.java.net
 * @author Prasad.Subramanian@Sun.COM
 */
public class BootstrapProperties {

    private final Properties properties;

    /**
     * Create BootstrapProperties with default properties.
     */
    public BootstrapProperties() {
        properties = new Properties();
    }

    /**
     * Create BootstrapProperties with custom properties.
     * This method does not take a copy of the passed in properties object; instead it just maintains a reference to
     * it, so all semantics of "pass-by-reference" applies.
     * <p/>
     * <p/>Custom properties can include GlassFish_Platform,
     * com.sun.aas.installRoot, com.sun.aas.installRootURI
     * <p/>
     * <p/>Custom properties can also include additional properties which are required
     * for the plugged in {@link org.glassfish.embeddable.spi.RuntimeBuilder} (if any)
     *
     * @param props Properties object which will back this BootstrapProperties object.
     */
    public BootstrapProperties(Properties props) {
        this.properties = props;
    }

    /**
     * Get the underlying Properties object which backs this BootstrapProperties.
     * <p/>
     * <p/> If getProperties().setProperty(key,value) is called, then it will
     * add a property to this bootstrap properties.
     *
     * @return The Properties object that is backing this BootstrapProperties.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Optionally set the installation root using which the GlassFish should run.
     *
     * @param installRoot Location of installation root.
     */
    public void setInstallRoot(String installRoot) {
        properties.setProperty(INSTALL_ROOT.getPropertyName(), installRoot);
    }

    /**
     * Get the location installation root set using {@link #setInstallRoot}
     *
     * @return Location of installation root set using {@link #setInstallRoot}
     */
    public String getInstallRoot() {
        return properties.getProperty(INSTALL_ROOT.getPropertyName());
    }

    /**
     * Set any custom bootstrap property. May be required for the plugged in
     * {@link org.glassfish.embeddable.spi.RuntimeBuilder} (if any)
     *
     * @param key   the key to be placed into this bootstrap properties.
     * @param value the value corresponding to the key.
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Get the value of the property with the specified key.
     *
     * @param key the property key
     * @return value of the property for the specified key, null if there is no such property.
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String toString() {
        return super.toString() + "[" + properties + "]";
    }
}
