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

package org.glassfish.admin.amx.core;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Holds meta information useful in generating and/or supplementing the default
 * MBeanInfo as well as other runtime fields or optimizations.
 * <p>
 * Depending on how the implementor generates MBeans, not all of this information is
 * necessarily used; it could be ignored if there is a more authoritative source (eg
 * internal @Configured interfaces that also have AMXConfig proxy interfaces).
 * <p>
 * In general, this annotation is used only by amx-core, amx-config and related built-in
 * AMX modules.
 *
 * @author Lloyd Chambers
 */
@Retention(RUNTIME)
@Target({TYPE, ANNOTATION_TYPE})
@Documented
@org.glassfish.external.arc.Taxonomy(stability = org.glassfish.external.arc.Stability.UNCOMMITTED)
public @interface AMXMBeanMetadata {

    /**
     * If true, states that the MBeanInfo is immutable; that once MBeanInfo is
     * obtained it may be cached, avoiding needless/repeated invocations of getMBeanInfo().
     * Very few MBeans have mutable MBeanInfo, so this defaults to 'true'.
     * The term is a misnomer; it should be invariantMBeanInfo(), but this name
     * is used go be consistent with the JMX standard.
     */
    boolean immutableMBeanInfo() default true;

    public static final String NULL = "\u0000";

    /** overrides default type to be used in ObjectName=, ignored if null or empty */
    public String type() default NULL;


    /** If true, no children are allowed. */
    public boolean leaf() default false;


    /** if true, the MBean is a singleon within its parent's scope */
    public boolean singleton() default false;


    /**
     * if true, the MBean is a global singleton, unique in type among all AMX MBeans.
     * Being a globalSingleton implies being a singleton
     */
    public boolean globalSingleton() default false;

}





