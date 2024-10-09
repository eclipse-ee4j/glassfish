/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.net.NetUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sun.enterprise.util.SystemPropertyConstants.AGENT_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.CONFIG_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.DERBY_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.DOMAINS_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.HOST_NAME_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.IMQ_BIN_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.IMQ_LIB_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.JAVA_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.JAVA_ROOT_PROPERTY_ASENV;
import static com.sun.enterprise.util.SystemPropertyConstants.PRODUCT_ROOT_PROPERTY;
import static com.sun.enterprise.util.SystemPropertyConstants.UNIX_ASENV_FILENAME;
import static com.sun.enterprise.util.SystemPropertyConstants.WINDOWS_ASENV_FILENAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Class ASenvPropertyReader
 *
 * This class converts the variables stored in asenv.conf (UNIX)
 * or asenv.bat (WINDOWS) into their equivalent system properties.
 * <p>This class <strong>guarantees</strong> that no Exception will get thrown back.
 * You may however, have a bad javaRoot set even though we tried everything to find
 * one
 */
public class ASenvPropertyReader {

    private static final Map<String, String> ENV_TO_SYS_PROPERTY = Map.of(
        "AS_DERBY_INSTALL", DERBY_ROOT_PROPERTY,
        "AS_IMQ_LIB", IMQ_LIB_PROPERTY,
        "AS_IMQ_BIN", IMQ_BIN_PROPERTY,
        "AS_CONFIG", CONFIG_ROOT_PROPERTY,
        "AS_INSTALL", INSTALL_ROOT_PROPERTY,
        "AS_JAVA", JAVA_ROOT_PROPERTY_ASENV,
        "AS_DEF_DOMAINS_PATH", DOMAINS_ROOT_PROPERTY,
        "AS_DEF_NODES_PATH", AGENT_ROOT_PROPERTY);


    /**
     * Typically, only one asenv file will be read, even though there may be many
     * ASenvPropertyReader objects.  So for each unique File, only one ASenvMap
     * is created, and all ASenvPropertyReader objects that reference the file
     * will share the same map. The key to the propsMap is the install dir that
     * is passed to the constructor.
     */
    private static final HashMap<File, ASenvMap> propsMap = new HashMap<>();
    private ASenvMap props;

    /**
     * Read and process the information in asenv
     * There are no arguments because the installation directory is calculated
     * relative to the jar file you are calling from.
     * Unlike V2 this class will not set any System Properties.  Instead it will
     * give you a Map<String,String> containing the properties.
     * <p>To use the class, create an instance and then call getProps().
     */
    public ASenvPropertyReader() {
        this(GFLauncherUtils.getInstallDir());
    }

    /**
     * Read and process the information in asenv.[bat|conf]
     * This constructor should normally not be called.  It is designed for
     * unit test classes that are not running from an official installation.
     * @param installDir The Glassfish installation directory
     */
    public ASenvPropertyReader(File installDir) {
        synchronized (propsMap) {
            installDir = SmartFile.sanitize(installDir);
            props = propsMap.get(installDir);
            if (props == null) {
                props = new ASenvMap(installDir);
                propsMap.put(installDir, props);
            }
        }
    }

    /**
     * Returns the properties that were processed.  This includes going to a bit of
     * trouble setting up the hostname and java root.
     * @return A Map<String,String> with all the properties
     */
    public Map<String, String> getProps() {
        return props;
    }

    /**
     * Returns a string representation of the properties in the Map<String,String>.
     * Format:  name=value\nname2=value2\n etc.
     * @return the string representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Set<String> keys = props.keySet();

        for (String key : keys) {
            sb.append(key).append("=").append(props.get(key)).append('\n');
        }
        return sb.toString();
    }


    /**
     * ASenvMap is a "lazy-evaluation" map, i.e., for values that are
     * expensive to calculate, the value is not calculated until it is actually
     * used.
     */
    static class ASenvMap extends HashMap<String, String> {
        // If we find a token in a set property, this is set to true.
        boolean foundToken;

        ASenvMap(File installDir) {
            put(INSTALL_ROOT_PROPERTY, installDir.getPath());
            File configDir = SmartFile.sanitize(new File(installDir, "config"));
            put(CONFIG_ROOT_PROPERTY, configDir.getPath());
            setProperties(configDir);
            postProcess(configDir);
            // Product root is defined to be the parent of the install root.
            // While tempting to just use installDir.getParent() we go through
            // these gyrations just in case setProperties() changed the value
            // of the INSTALL_ROOT_PROPERTY property.
            File installRoot = new File(super.get(INSTALL_ROOT_PROPERTY));
            put(PRODUCT_ROOT_PROPERTY, installRoot.getParent());
        }

        @Override
        public String get(Object k) {
            String v = super.get(k);
            if (v != null) {
                return v;
            }
            if (k.equals(HOST_NAME_PROPERTY)) {
                v = getHostname();
                put(HOST_NAME_PROPERTY, v);
            }
            else if (k.equals(JAVA_ROOT_PROPERTY)) {
                v = getJavaRoot(super.get(JAVA_ROOT_PROPERTY_ASENV));
                put(JAVA_ROOT_PROPERTY, v);
            }
            return v;
        }

        @Override
        public Set<String> keySet() {
            completeMap();
            return super.keySet();
        }

        @Override
        public Set<Map.Entry<String, String>> entrySet() {
            completeMap();
            return super.entrySet();
        }

        @Override
        public boolean containsKey(Object k) {
            completeMap();
            return super.containsKey(k);
        }

        @Override
        public Collection<String> values() {
            completeMap();
            return super.values();
        }

        /**
         * Add the "lazy" items to the map so that the map is complete.
         */
        private void completeMap() {
            get(HOST_NAME_PROPERTY);
            get(JAVA_ROOT_PROPERTY);
        }


        /**
         * <ol>
         * <li>change relative paths to absolute
         * <li>change env. variables to either the actual values in the environment
`            * or to another prop in asenv
         * </ol>
         */
        private void postProcess(File configDir) {
            if (foundToken) {
                final Map<String, String> env = System.getenv();
                //put env props in first
                Map<String, String> all = new HashMap<>(env);
                // now override with our props
                all.putAll(this);
                TokenResolver tr = new TokenResolver(all);
                tr.resolve(this);
            }

            // props have all tokens replaced now (if they exist)
            // now make the paths absolute.
            // Call super.keySet here so that the lazy values are not added
            // to the map at this point.
            Set<String> keys = super.keySet();

            for (String key : keys) {
                String value = super.get(key);
                if (GFLauncherUtils.isRelativePath(value)) {
                    // we have to handle both of these:
                    // /x/y/../z
                    // ../x/y/../z

                    File f;
                    if (value.startsWith(".")) {
                        f = SmartFile.sanitize(new File(configDir, value));
                    }
                    else {
                        f = SmartFile.sanitize(new File(value));
                    }

                    put(key, f.getPath());
                }
            }
        }

        private void setProperties(File configDir) {
            //Read in asenv.conf/bat and set system properties accordingly
            File asenv = new File(configDir,
                GFLauncherUtils.isWindows() ? WINDOWS_ASENV_FILENAME : UNIX_ASENV_FILENAME);
            if (!asenv.exists()) {
                return;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(asenv, UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    setProperty(line);
                }
            } catch (Exception e) {
                throw new IllegalStateException("Could not parse " + asenv, e);
            }
        }

        /**
         * Method setProperty
         * Parses a single line of asenv.conf or asenv.bat and attempt to
         * set the corresponding property. Note that if the system
         * property is already set (e.g. via -D on the command line), then
         * we will not clobber its existing value.
         *
         * @param line
         *
         */
        private void setProperty(String line) {
            int pos = line.indexOf('=');
            if (pos > 0) {
                String lhs = (line.substring(0, pos)).trim();
                String rhs = (line.substring(pos + 1)).trim();
                String rhsExpression = "\"${" + lhs + ":-";
                if (!GFLauncherUtils.isWindows() && rhs.contains(rhsExpression) && rhs.contains("}")) {
                    String env = System.getenv(lhs);
                    int start = rhs.indexOf(rhsExpression);
                    int end = rhs.indexOf('}');
                    StringBuilder value = new StringBuilder();
                    if (start > 0) {
                        value.append(rhs.subSequence(0, start));
                    }
                    if (env == null) {
                        value.append('"').append(rhs.substring(start + rhsExpression.length(), end));
                    } else {
                        value.append(env);
                    }
                    if (rhs.length() > end + 1) {
                        value.append(rhs.substring(end + 1));
                    }
                    rhs = value.toString();
                }

                if (GFLauncherUtils.isWindows()) {
                    // trim off the "set "
                    lhs = (lhs.substring(3)).trim();
                } else {
                    // take the quotes out
                    pos = rhs.indexOf('"');
                    if (pos != -1) {
                        rhs = (rhs.substring(pos + 1)).trim();
                        pos = rhs.indexOf('"');
                        if (pos != -1) {
                            rhs = (rhs.substring(0, pos)).trim();
                        }
                    }
                }

                String systemPropertyName = ENV_TO_SYS_PROPERTY.get(lhs);

                if (systemPropertyName != null) {
                    if (TokenResolver.hasToken(rhs)) {
                        foundToken = true;
                    }
                    put(systemPropertyName, rhs);
                }
            }
        }

        private static String getHostname() {
            String hostname = "localhost";
            try {
                // canonical name checks to make sure host is proper
                hostname = NetUtils.getCanonicalHostName();
            } catch (Exception ex) {
                // ignore, go with "localhost"
            }
            return hostname;
        }

        /**
         * Get a value for the Java installation directory.  The value that is
         * passed in should be the value from the ASenv config file. If it is valid
         * it is returned.  Otherwise, this method checks the following:
         * - JAVA_HOME environment variable
         * - java.home system property
         */
        private static String getJavaRoot(String fileValue) {
            // make sure we have a folder with java in it!
            // note that we are not in a position to set it from domain.xml yet

            // first choice -- whatever is in asenv
            String javaRootName = fileValue;

            if (isValidJavaRoot(javaRootName)) {
                return javaRootName; // we are already done!
            }

            // try JAVA_HOME
            javaRootName = System.getenv("JAVA_HOME");

            if (isValidJavaRoot(javaRootName)) {
                javaRootName = SmartFile.sanitize(new File(javaRootName)).getPath();
                return javaRootName;
            }
            // try java.home with ../
            // usually java.home is pointing at jre and ".." goes to the jdk
            javaRootName = System.getProperty("java.home") + "/..";

            if (isValidJavaRoot(javaRootName)) {
                javaRootName = SmartFile.sanitize(new File(javaRootName)).getPath();
                return javaRootName;
            }

            // try java.home as-is
            javaRootName = System.getProperty("java.home");

            if (isValidJavaRoot(javaRootName)) {
                javaRootName = SmartFile.sanitize(new File(javaRootName)).getPath();
                return javaRootName;
            }
            // TODO - should this be an Exception? A log message?
            return null;
        }

        private static boolean isValidJavaRoot(String javaRootName) {
            if (!GFLauncherUtils.ok(javaRootName)) {
                return false;
            }

            // look for ${javaRootName}/bin/java[.exe]
            File f = new File(javaRootName);

            if (GFLauncherUtils.isWindows()) {
                f = new File(f, "bin/java.exe");
            } else {
                f = new File(f, "bin/java");
            }

            return f.exists();
        }

    }
}
