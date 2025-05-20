/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlassFish variables used as environment options or system properties and their mapping.
 */
public enum GlassFishVariable {
    /** Directory with asenv.conf, {@link #INSTALL_ROOT}/config */
    CONFIG_ROOT("AS_CONFIG", "com.sun.aas.configRoot"),
    /** Derby database main directory, containing lib directory with jar files. */
    DERBY_ROOT("AS_DERBY_INSTALL", "com.sun.aas.derbyRoot"),
    /** Directory where we have domains */
    DOMAINS_ROOT("AS_DEF_DOMAINS_PATH", "com.sun.aas.domainsRoot"),
    /** Autodetected host name. */
    HOST_NAME(null, "com.sun.aas.hostName"),
    /** Java home directory set by JVM automatically via <code>java.home</code> or via JAVA_HOME by user. */
    JAVA_HOME("JAVA_HOME", "java.home"),
    /** Java home set by AS_JAVA, has higher priority than JAVA_HOME. */
    JAVA_ROOT("AS_JAVA", "com.sun.aas.javaRoot"),
    /** OSGi implementation selector. */
    OSGI_PLATFORM("GlassFish_Platform"),
    /** JMS Message Broker bin directory */
    IMQ_BIN("AS_IMQ_BIN", "com.sun.aas.imqBin"),
    /** JMS Message Broker lib directory */
    IMQ_LIB("AS_IMQ_LIB", "com.sun.aas.imqLib"),
    /**
     * Which installation root the GlassFish should run with.
     * Usually <code>glassfish[x]/glassfish</code>
     */
    INSTALL_ROOT("AS_INSTALL", "com.sun.aas.installRoot"),
    /** Instance directory */
    INSTANCE_ROOT(null, "com.sun.aas.instanceRoot"),
    /** Node agents directory */
    NODES_ROOT("AS_DEF_NODES_PATH", "com.sun.aas.agentRoot"),
    /** Install root parent, resolved from {@link #INSTANCE_ROOT}. */
    PRODUCT_ROOT(null, "com.sun.aas.productRoot")
    ;

    private final String envName;
    private final String sysPropName;

    GlassFishVariable(String name) {
        this(name, name);
    }


    GlassFishVariable(String envName, String sysPropName) {
        this.envName = envName;
        this.sysPropName = sysPropName;
    }

    /**
     * @return name used when configuring GlassFish from the operating system side or asenv.conf.
     */
    public String getEnvName() {
        return this.envName;
    }


    /**
     * @return <code>${{@link #getPropertyName()}}</code>, for example:
     *         <code>${com.sun.aas.installRoot}</code>
     */
    public String toExpression() {
        return "${" + getPropertyName() + '}';
    }


    /**
     * @return name used when configuring GlassFish using properties; this name is same as for
     *         {@link #getSystemPropertyName()}.
     */
    public String getPropertyName() {
        return getSystemPropertyName();
    }


    /**
     * @return name used when configuring GlassFish using {@link System#getProperties()}.
     */
    public String getSystemPropertyName() {
        return this.sysPropName;
    }


    public static Map<String, String> getEnvToSystemPropertyMapping() {
        return Arrays.stream(GlassFishVariable.values())
            .filter(m -> m.getEnvName() != null && m.getSystemPropertyName() != null)
            .collect(Collectors.toMap(GlassFishVariable::getEnvName, GlassFishVariable::getSystemPropertyName));
    }
}
