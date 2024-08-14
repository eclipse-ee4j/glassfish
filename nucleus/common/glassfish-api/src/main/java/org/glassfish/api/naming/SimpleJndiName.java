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

    public static final String JNDI_CTX_JAVA_APP_NS_ID = JNDI_CTX_JAVA + "app";
    public static final String JNDI_CTX_JAVA_APP = JNDI_CTX_JAVA_APP_NS_ID + '/';
    public static final String JNDI_CTX_JAVA_APP_ENV = JNDI_CTX_JAVA_APP + "env/";

    public static final String JNDI_CTX_JAVA_COMPONENT_NS_ID = JNDI_CTX_JAVA + "comp";
    public static final String JNDI_CTX_JAVA_COMPONENT = JNDI_CTX_JAVA_COMPONENT_NS_ID + '/';
    public static final String JNDI_CTX_JAVA_COMPONENT_ENV = JNDI_CTX_JAVA_COMPONENT + "env/";

    public static final String JNDI_CTX_JAVA_MODULE_NS_ID = JNDI_CTX_JAVA + "module";
    public static final String JNDI_CTX_JAVA_MODULE = JNDI_CTX_JAVA_MODULE_NS_ID + '/';
    public static final String JNDI_CTX_JAVA_MODULE_ENV = JNDI_CTX_JAVA_MODULE + "env/";

    public static final String JNDI_CTX_JAVA_GLOBAL_NS_ID = JNDI_CTX_JAVA + "global";
    public static final String JNDI_CTX_JAVA_GLOBAL = JNDI_CTX_JAVA_GLOBAL_NS_ID + '/';

    private final String jndiName;

    /**
     * Does simple validation and creates the instance.
     *
     * @param jndiName must not be null and if it is not a <code>corbaname:</code> jndi name, must
     *            not contain more than one colon.
     */
    public SimpleJndiName(final String jndiName) {
        if (!isValidJndiName(jndiName)) {
            throw new IllegalArgumentException(
                "Invalid JNDI name: '" + jndiName + "'. The JNDI name must not be null.");
            // FIXME dmatej added and commented out
//            IllegalArgumentException e = new IllegalArgumentException(
//                "Invalid JNDI name: '" + jndiName + "'. The JNDI name must not be null. The '" + JNDI_CTX_JAVA
//                    + "' JNDI name is not allowed to contain more than one colon and the prefix cannot be found"
//                    + " in the middle of an unprefixed JNDI name.");
//            throw e;
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
     * @return true if the JNDI name starts with {@value #JNDI_CTX_JAVA_GLOBAL_NS_ID}.
     */
    public boolean isJavaGlobal() {
        return jndiName.startsWith(JNDI_CTX_JAVA_GLOBAL_NS_ID);
    }


    /**
     * @return true if the JNDI name starts with {@value #JNDI_CTX_JAVA_APP_NS_ID}.
     */
    public boolean isJavaApp() {
        return jndiName.startsWith(JNDI_CTX_JAVA_APP_NS_ID);
    }


    /**
     * @return true if the JNDI name starts with {@value #JNDI_CTX_JAVA_MODULE_NS_ID}.
     */
    public boolean isJavaModule() {
        return jndiName.startsWith(JNDI_CTX_JAVA_MODULE_NS_ID);
    }


    /**
     * @return true if the JNDI name starts with {@value #JNDI_CTX_JAVA_COMPONENT_NS_ID}.
     */
    public boolean isJavaComponent() {
        return jndiName.startsWith(JNDI_CTX_JAVA_COMPONENT_NS_ID);
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
     * or <code>a:</code> or <code>x://</code>
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
            if (isKnownNamespaceId()) {
                return jndiName;
            }
            return jndiName.substring(0, colonIndex + 1);
        }
        if (jndiName.charAt(slashIndex + 1) == '/') {
            // if the JNDI name is in the URL form, like http://host:80
            return jndiName.substring(0, slashIndex + 2);
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
        if (jndiName.startsWith(prefix)) {
            return new SimpleJndiName(jndiName.substring(prefix.length()));
        }
        return this;
    }


    /**
     * Returns the JNDI name without the prefix.
     * If there is no prefix, returns this instance unchanged.
     * <p>
     * Prefix in this case is understood as any string in the form <code>a:b/</code>
     * or <code>a:</code> or <code>x://</code>
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
            if (isKnownNamespaceId()) {
                return new SimpleJndiName("");
            }
            return new SimpleJndiName(jndiName.substring(colonIndex + 1));
        }
        if (jndiName.charAt(slashIndex + 1) == '/') {
            // if the JNDI name is in the URL form, like http://host:80
            return new SimpleJndiName(jndiName.substring(slashIndex + 2));
        }
        return new SimpleJndiName(jndiName.substring(slashIndex + 1));
    }


    /**
     * Changes the simple JNDI name prefix.
     * <p>
     * Prefix in this case is understood as any string in the form <code>a:b/</code>
     * or <code>a:</code> or <code>x://</code>
     * <p>
     * For JNDI names starting with {@value #JNDI_CTX_CORBA} returns this instance unchanged.
     * <p>
     * If there is no prefix, just sets the new prefix.
     * If the name is a Corba name doesn't do anything.
     * If the name has <code>java:ccc/</code> prefix or is an URL
     * (protocol://xxx:8888/...), sets the new prefix instead of the <code>java:ccc/</code> or the
     * <code>protocol://</code>
     *
     * @param newPrefix must end with the colon or slash.
     * @return new instance, never null.
     * @throws IllegalArgumentException if it is not possible to create valid JNDI name with the new prefix
     */
    public SimpleJndiName changePrefix(final String newPrefix) throws IllegalArgumentException {
        if (hasCorbaPrefix()) {
            return this;
        }
        final char lastChar = newPrefix.charAt(newPrefix.length() - 1);
        if (lastChar != ':' && lastChar != '/') {
            throw new IllegalArgumentException(
                "The new prefix doesn't end with colon nor slash character: " + newPrefix);
        }
        SimpleJndiName noPrefix = removePrefix();
        return new SimpleJndiName(newPrefix + noPrefix);
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


    private boolean isKnownNamespaceId() {
        return jndiName.equals(JNDI_CTX_JAVA_GLOBAL_NS_ID) || jndiName.equals(JNDI_CTX_JAVA_APP_NS_ID)
            || jndiName.equals(JNDI_CTX_JAVA_MODULE_NS_ID) || jndiName.equals(JNDI_CTX_JAVA_COMPONENT_NS_ID);
    }


    /**
     * Returns true if the parameter is not null and doesn't start with the {@value #JNDI_CTX_JAVA},
     * or if it does, it must contain just one colon character.
     *
     * @param jndiName any string or null
     * @return true if the jndiName can be used in the constructor.
     */
    public static boolean isValidJndiName(final String jndiName) {
        if (jndiName == null) {
            return false;
        }
        // FIXME dmatej added and commented out
//        if (jndiName.startsWith(JNDI_CTX_JAVA)) {
//            return jndiName.indexOf(':') == jndiName.lastIndexOf(':');
//        }
//        if (jndiName.indexOf(':') > 10 && jndiName.contains(JNDI_CTX_JAVA)) {
//            // The java: prefix in the middle of the name is not allowed.
//            // It is quite common mistake when names are concatenated.
//            // However for JNDI names of other type it is possible (http://, corbaname:, ...)
//            return false;
//        }
        return true;
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
