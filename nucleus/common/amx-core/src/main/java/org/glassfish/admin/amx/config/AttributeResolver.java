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

package org.glassfish.admin.amx.config;

import javax.management.AttributeList;
import javax.management.MBeanOperationInfo;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Param;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
 * @deprecated Interface implemented by MBeans which can resolve a variable to a value.
 *             Variable attributes are strings of the form ${...} and
 *             are returned as the values of certain Attributes. This interface is intended for use
 *             only with config MBeans.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@Deprecated
public interface AttributeResolver {

    /**
     * Resolve an attribute <em>value</em> to a literal. The value should have been
     * previously obtained from an Attribute of the same AMXConfig MBean.
     * <p>
     * If the String is not a template string, return the string unchanged.
     * <p>
     * If the String is a template string, resolve its value if it can be resolved, or 'null'
     * if it cannot be resolved.
     * <p>
     * Examples:</br>
     *
     * <pre>
     "${com.sun.aas.installRoot}" => "/glassfish"
     "${does-not-exist}" => null
     "${com.myco.moonIsBlue}" => "true"
     "8080" => "8080"
     "hello" => "hello"
     * </pre>
     *
     * @param value any String
     * @return resolved value
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Resolve a (possible) ${...} attribute *value* to a real value")
    String resolveAttributeValue(
        @Param(name = "value")
        String value);


    /** calls getAttribute(), then returns the resolved value or null */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Get and resolve a (possible) ${...} attribute to a real value")
    String resolveAttribute(
        @Param(name = "attributeName")
        String attributeName);


    /** Get the Attribute and resolve it to a Boolean or null */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Get and resolve a (possible)  ${...} attribute to a Boolean, returns null if not found")
    Boolean resolveBoolean(
        @Param(name = "attributeName")
        String attributeName);


    /** Get the Attribute and resolve it to a Long or null */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Get and resolve a (possible)  ${...} attribute to a Long, returns null if not found")
    Long resolveLong(
        @Param(name = "attributeName")
        String attributeName);


    /**
     * Calls getAttributes(), then returns all resolved values. If the attributes
     * have been annotated with @ResolveTo, then the value is of the correct type
     * (eg String, Boolean, Integer).
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Get and resolve attributes to values")
    AttributeList resolveAttributes(
        @Param(name = "attributeNames")
        String[] attributeNames);
}
