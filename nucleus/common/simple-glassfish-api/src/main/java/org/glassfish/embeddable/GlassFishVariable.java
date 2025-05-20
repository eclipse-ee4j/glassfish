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
    /** Derby database main directory, containing lib directory with jar files. */
    DERBY_ROOT("AS_DERBY_INSTALL", "com.sun.aas.derbyRoot"),
    /** OSGi implementation selector. */
    OSGI_PLATFORM("GlassFish_Platform"),
    /** Which installation root the GlassFish should run with. */
    INSTALL_ROOT("AS_INSTALL", "com.sun.aas.installRoot"),
    /** Instance directory */
    INSTANCE_ROOT(null, "com.sun.aas.instanceRoot"),
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
