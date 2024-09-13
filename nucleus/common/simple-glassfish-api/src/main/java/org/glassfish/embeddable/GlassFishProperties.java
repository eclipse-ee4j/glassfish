/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

/**
 * Encapsulates the set of properties required to create a new GlassFish instance.
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
public class GlassFishProperties {

    /** Key for specifying which instance root (aka domain dir) GlassFish should run with. */
    public static final String INSTANCE_ROOT_PROP_NAME = "com.sun.aas.instanceRoot";
    /** Key for specifying which configuration file (domain.xml) GlassFish should run with. */
    public static final String CONFIG_FILE_URI_PROP_NAME = "org.glassfish.embeddable.configFileURI";
    /**
     * Key for specifying whether the specified configuration file (domain.xml) or config/domain.xml
     * at the user specified instanceRoot should be operated by GlassFish in read only mode or not.
     */
    public static final String CONFIG_FILE_READ_ONLY = "org.glassfish.embeddable.configFileReadOnly";
    private static final String NETWORK_LISTENER_KEY = "embedded-glassfish-config.server.network-config.network-listeners.network-listener.%s";

    private final Properties gfProperties;

    /**
     * Create GlassFishProperties with default properties.
     */
    public GlassFishProperties() {
        gfProperties = new Properties();
    }


    /**
     * Create GlassFishProperties with custom properties.
     * This method does not take a copy of the passed in properties object; instead it just
     * maintains a reference to it, so all semantics of "pass-by-reference" applies.
     * <p/>
     * Custom properties can include values for all or some of the keys
     * defined as constants in this class. Eg., a value for com.sun.aas.instanceRoot
     * can be included in the custom properties.
     * <p/>
     * Custom properties can also include additional properties which are required
     * for the plugged in {@link GlassFishRuntime} (if any)
     *
     * @param props Properties object which will back this GlassFishProperties object.
     */
    public GlassFishProperties(Properties props) {
        gfProperties = props;
    }


    /**
     * Get the underlying Properties object which backs this GlassFishProperties.
     * <p/>
     * If getProperties().setProperty(key,value) is called, then it will
     * add a property to this GlassFishProperties.
     *
     * @return The Properties object that is backing this GlassFishProperties.
     */
    public Properties getProperties() {
        return gfProperties;
    }

    /**
     * Set any custom glassfish property. May be required for the plugged in
     * {@link GlassFishRuntime} (if any)
     *
     * @param key   the key to be placed into this glassfish properties.
     * @param value the value corresponding to the key.
     */
    public void setProperty(String key, String value) {
        gfProperties.setProperty(key, value);
    }


    /**
     * Optionally set the instance root (aka domain dir) using which the
     * GlassFish should run.
     * <p/>
     * Make sure to specify a valid GlassFish instance directory
     * (eg., GF_INSTALL_DIR/domains/domain1).
     * <p/>
     * By default, the config/domain.xml at the specified instance root is operated in
     * read only mode. To writeback changes to it, call
     * {@link #setConfigFileReadOnly(boolean)} by passing 'false'
     * <p/>
     * If the instance root is not specified, then a small sized temporary
     * instance directory is created in the current directory. The temporary
     * instance directory will get deleted when the glassfish.dispose() is called.
     *
     * @param instanceRoot Location of the instance root.
     */
    public void setInstanceRoot(String instanceRoot) {
        gfProperties.setProperty(INSTANCE_ROOT_PROP_NAME, instanceRoot);
    }

    /**
     * Get the location instance root set using {@link #setInstanceRoot(String)}
     *
     * @return Location of instance root set using {@link #setInstanceRoot(String)}
     */
    public String getInstanceRoot() {
        return gfProperties.getProperty(INSTANCE_ROOT_PROP_NAME);
    }

    /**
     * Optionally set the location of configuration file (i.e., domain.xml) using
     * which the GlassFish should run.
     * <p/>
     * Unless specified, the configuration file is operated on read only mode.
     * To writeback any changes, call {@link #setConfigFileReadOnly(boolean)} with 'false'.
     *
     * @param configFileURI Location of configuration file.
     */
    public void setConfigFileURI(String configFileURI) {
        gfProperties.setProperty(CONFIG_FILE_URI_PROP_NAME, configFileURI);
    }

    /**
     * Get the configurationFileURI set using {@link #setConfigFileURI(String)}
     *
     * @return The configurationFileURI set using {@link #setConfigFileURI(String)}
     */
    public String getConfigFileURI() {
        return gfProperties.getProperty(CONFIG_FILE_URI_PROP_NAME);
    }

    /**
     * Check whether the specified configuration file or config/domain.xml at
     * the specified instance root is operated read only or not.
     *
     * @return true if the specified configurator file or config/domain.xml at the
     *         specified instance root remains unchanged when the glassfish runs, false otherwise.
     */
    public boolean isConfigFileReadOnly() {
        return Boolean.parseBoolean(gfProperties.getProperty(CONFIG_FILE_READ_ONLY, "true"));
    }

    /**
     * Mention whether or not the GlassFish should writeback any changes to specified
     * configuration file or config/domain.xml at the specified instance root.
     * <p/>
     * <p/> By default readOnly is true.
     *
     * @param readOnly false to writeback any changes.
     */
    public void setConfigFileReadOnly(boolean readOnly) {
        gfProperties.setProperty(CONFIG_FILE_READ_ONLY, Boolean.toString(readOnly));
    }

    /**
     * Set the port number for a network listener that the GlassFish server
     * should use.
     *
     * In the default configuration, all listeners are disabled. This method will enable the listener if it's disabled.
     * If the port is 0 or a negative value, it will disable the network listener.
     *
     * <p/>
     * Examples:
     * <p/>
     * 1. When the custom configuration file is not used, the ports can be set using:
     * <p/>
     * <pre>
     *      setPort("http-listener", 8080); // GlassFish will listen on HTTP port 8080
     *      setPort("https-listener", 8181); // GlassFish will listen on HTTPS port 8181
     *      setPort("http-listener", 0); // GlassFish will disable the HTTP listener
     * </pre>
     * <p/>
     * 2. When the custom configuration file (domain.xml) is used, then the
     * name of the network listener specified here will point to the
     * network-listener element in the domain.xml. For example:
     * <p/>
     * <pre>
     *      setPort("joe", 8080);
     * </pre>
     * <p/>
     * will point to server.network-config.network-listeners.network-listener.joe. Hence the
     * GlassFish server will use "joe" network listener with its port set to 8080.
     * <p/>
     * If there is no such network-listener by name "joe" in the supplied domain.xml,
     * then the server will throw an exception and fail to start.
     *
     * @param networkListener Name of the network listener.
     * @param port            Port number
     */
    public void setPort(String networkListener, int port) {
        if (networkListener != null) {
            String key = String.format(NETWORK_LISTENER_KEY, networkListener);
            if (key != null) {
                if (port <= 0) {
                    gfProperties.setProperty(key + ".enabled", "false");
                } else {
                    gfProperties.setProperty(key + ".port", Integer.toString(port));
                    gfProperties.setProperty(key + ".enabled", "true");
                }
            }
        }
    }

    /**
     * Get the port number set using {@link #setPort(String, int)}
     *
     * @param networkListener Name of the listener
     * @return Port number which was set using {@link #setPort(String, int)}.
     *         -1 if it was not set previously.
     */
    public int getPort(String networkListener) {
        int port = -1;
        if (networkListener != null) {
            String key = String.format(NETWORK_LISTENER_KEY, networkListener);
            if (key != null) {
                String portStr = gfProperties.getProperty(key + ".port");
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException nfe) {
                    // ignore and return -1;
                }
            }
        }
        return port;
    }
}
