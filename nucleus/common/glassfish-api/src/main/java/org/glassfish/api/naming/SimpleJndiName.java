/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.api.naming;

import java.io.Serializable;
import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

/**
 * Value object for JNDI names used in GlassFish.
 *
 * @author David Matejcek
 */
public class SimpleJndiName implements Serializable, Comparable<SimpleJndiName> {

    private static final long serialVersionUID = -6969478638009057579L;

    public static final String JNDI_CTX_CORBA = "corbaname:";
    public static final String JNDI_CTX_JAVA = "java:";
    public static final String JNDI_CTX_JAVA_APP = JNDI_CTX_JAVA + "app/";
    public static final String JNDI_CTX_JAVA_APP_ENV = JNDI_CTX_JAVA_APP + "env/";
    public static final String JNDI_CTX_JAVA_COMPONENT = JNDI_CTX_JAVA + "comp/";
    public static final String JNDI_CTX_JAVA_COMPONENT_ENV = JNDI_CTX_JAVA_COMPONENT + "env/";
    public static final String JNDI_CTX_JAVA_MODULE = JNDI_CTX_JAVA + "module/";
    public static final String JNDI_CTX_JAVA_MODULE_ENV = JNDI_CTX_JAVA_MODULE + "env/";
    public static final String JNDI_CTX_JAVA_GLOBAL = JNDI_CTX_JAVA + "global/";

    private final String jndiName;

    /**
     * Does simple validation and creates the instance.
     *
     * @param jndiName must not be null and if it is not a <code>corbaname:</code> jndi name, must
     *            not contain more than one colon.
     */
    public SimpleJndiName(final String jndiName) {
        if (jndiName.startsWith(JNDI_CTX_JAVA)) {
            int firstColon = jndiName.indexOf(':');
            if (firstColon >= 0 && firstColon != jndiName.lastIndexOf(':')) {
                throw new IllegalArgumentException(
                    "The " + JNDI_CTX_JAVA + " JNDI name is not allowed to contain more than one colon: " + jndiName);
            }
        }
        this.jndiName = jndiName;
    }


    /**
     * @return true if the JNDI name starts with {@value #JNDI_CTX_JAVA}
     */
    public boolean hasJavaPrefix() {
        return jndiName.startsWith(JNDI_CTX_JAVA);
    }


    /**
     * @return true if the JNDI name starts with {@value #JNDI_CTX_CORBA}
     */
    public boolean hasCorbaPrefix() {
        return jndiName.startsWith(JNDI_CTX_CORBA);
    }


    /**
     * @return true if the JNDI name starts with {@value #JNDI_CTX_JAVA_GLOBAL} without the trailing slash.
     */
    public boolean isJavaGlobal() {
        return jndiName.startsWith("java:global");
    }


    /**
     * @return true if the JNDI name starts with {@value #JNDI_CTX_JAVA_APP} without the trailing slash.
     */
    public boolean isJavaApp() {
        return jndiName.startsWith("java:app");
    }


    /**
     * @return true if the JNDI name starts with {@value #JNDI_CTX_JAVA_MODULE} without the trailing slash.
     */
    public boolean isJavaModule() {
        return jndiName.startsWith("java:module");
    }


    /**
     * @return true if the JNDI name starts with {@value #JNDI_CTX_JAVA_COMPONENT} without the trailing slash.
     */
    public boolean isJavaComponent() {
        return jndiName.startsWith("java:comp");
    }


    @Override
    public int compareTo(SimpleJndiName other) {
        return other == null ? -1 : this.jndiName.compareTo(other.jndiName);
    }


    @Override
    public int hashCode() {
        return this.jndiName.hashCode();
    }


    @Override
    public boolean equals(Object object) {
        return object instanceof SimpleJndiName && jndiName.equals(((SimpleJndiName) object).jndiName);
    }


    /**
     * @return true if the JNDI name is an empty string
     */
    public boolean isEmpty() {
        return jndiName.isEmpty();
    }


    /**
     * @param part must not be null.
     * @return true if the JNDI name contains the parameter.
     */
    public boolean contains(String part) {
        return jndiName.contains(part);
    }


    /**
     * Prefix in this case is understood as any string in the form <code>a:b/</code>
     * or <code>a:</code>
     * <p>
     * For JNDI names starting with {@value #JNDI_CTX_CORBA} returns null.
     *
     * @return substring containing <code>:</code> and ending by <code>/</code>
     */
    public String getPrefix() {
        if (hasCorbaPrefix()) {
            return null;
        }
        final int colonIndex = jndiName.indexOf(':');
        if (colonIndex == -1) {
            return null;
        }
        final int slashIndex = jndiName.indexOf('/', colonIndex + 1);
        if (slashIndex == -1) {
            return jndiName.substring(0, colonIndex + 1);
        }
        return jndiName.substring(0, slashIndex + 1);
    }


    /**
     * @param prefix must not be null.
     * @return true if the JNDI name starts with the parameter.
     */
    public boolean hasPrefix(String prefix) {
        return jndiName.startsWith(prefix);
    }


    /**
     * @param suffix must not be null.
     * @return true if the JNDI name ends with the parameter.
     */
    public boolean hasSuffix(String suffix) {
        return jndiName.endsWith(suffix);
    }


    /**
     * @param prefix can be null, then returns this instance unchanged.
     * @return new instance with the prefix removed or this instance unchanged
     *         if the prefix was not present.
     */
    public SimpleJndiName removePrefix(String prefix) {
        if (prefix == null) {
            return this;
        }
        int index = jndiName.indexOf(prefix);
        if (index >= 0) {
            return new SimpleJndiName(jndiName.substring(index + prefix.length()));
        }
        return this;
    }


    /**
     * Returns the JNDI name without the prefix.
     * If there is no prefix, returns this instance unchanged.
     * <p>
     * Prefix in this case is understood as any string in the form <code>a:b/</code>
     * or <code>a:</code>
     * <p>
     * For JNDI names starting with {@value #JNDI_CTX_CORBA} returns this instance unchanged.
     *
     * @return new instance with the prefix removed or this instance unchanged, never null.
     */
    public SimpleJndiName removePrefix() {
        if (hasCorbaPrefix()) {
            return this;
        }
        final int colonIndex = jndiName.indexOf(':');
        if (colonIndex == -1) {
            return this;
        }
        final int slashIndex = jndiName.indexOf('/', colonIndex + 1);
        if (slashIndex == -1) {
            return new SimpleJndiName(jndiName.substring(colonIndex + 1));
        }
        return new SimpleJndiName(jndiName.substring(slashIndex + 1));
    }


    /**
     * @param suffix can be null, then returns this instance unchanged.
     * @return new instance with the suffix removed or this instance unchanged
     *         if the suffix was not present.
     */
    public SimpleJndiName removeSuffix(String suffix) {
        if (suffix == null) {
            return this;
        }
        int index = jndiName.lastIndexOf(suffix);
        if (index >= 0) {
            return new SimpleJndiName(jndiName.substring(0, index));
        }
        return this;
    }


    /**
     * Returns the JNDI name.
     */
    // don't change, it is heavily used!
    @Override
    public String toString() {
        return this.jndiName;
    }


    /**
     * @return {@link CompositeName} representing the same JNDI name.
     * @throws InvalidNameException if the conversions was not possible.
     */
    public Name toName() throws InvalidNameException {
        return new CompositeName(jndiName);
    }


    /**
     * @param name can be null, then returns null.
     * @return null or a new instance of {@link SimpleJndiName}.
     */
    public static SimpleJndiName of(Name name) {
        return name == null ? null : new SimpleJndiName(name.toString());
    }


    /**
     * @param name can be null, then returns null.
     * @return null or a new instance of {@link SimpleJndiName}.
     */
    public static SimpleJndiName of(String name) {
        return name == null ? null : new SimpleJndiName(name);
    }
}
