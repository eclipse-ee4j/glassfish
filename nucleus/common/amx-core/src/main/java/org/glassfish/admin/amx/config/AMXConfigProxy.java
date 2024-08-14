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

import java.util.Map;

import javax.management.AttributeList;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Param;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.StdAttributesAccess;
import org.glassfish.external.amx.AMX;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
 * @deprecated Extending this proxy interface implies that the class is part of the MBean API for
 *             configuration,
 *             that the interface is a dynamic proxy to a config MBean.
 *             <p>
 *             Note that considerable metadata is available for config MBeans, via
 *             MBeanInfo.getDescriptor().
 * @see AMXProxy
 * @see AMXConfigConstants
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@Deprecated
public interface AMXConfigProxy extends AMXProxy, AttributeResolver {

    /**
     * Return a Map of default values for the specified child type.
     * The resulting Map is keyed by the attribute name, either the AMX attribute name or the xml
     * attribute name.
     *
     * @since Glassfish V3.
     * @param type the J2EEType of the child
     * @param useAMXAttributeName whether to key the values by the the AMX Attribute name or XML
     *            attribute name
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Get the default values for child type")
    Map<String, String> getDefaultValues(
        @Param(name = "type")
        String type,
        @Param(name = "useAMXAttributeName")
        @Description("true to use Attribute names, false to use XML names")
        boolean useAMXAttributeName);


    /**
     * Return a Map of default values for this MBean.
     *
     * @param useAMXAttributeName whether to key the values by the XML attribute name vs the AMX
     *            Attribute name
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Get the available default values")
    Map<String, String> getDefaultValues(
        @Param(name = "useAMXAttributeName")
        @Description("true to use Attribute names, false to use XML names")
        boolean useAMXAttributeName);


    /**
     * Generic creation of an {@link AMXConfigProxy} based on the desired XML element type, which
     * must be legitimate for the containing element.
     * <p>
     * Required attributes must be specified, and should all be 'String' (The Map value is declared
     * with a type of of 'Object' anticipating future extensions).
     * <p>
     * Use the {@link AMX#ATTR_NAME} key for the name.
     *
     * @param childType the XML element type
     * @param params Map containing attributes which are required by the @Configured and any
     *            optional attributes (as desired).
     * @return proxy interface to the newly-created AMXConfigProxy
     */
    @ManagedOperation
    @Description("Create a child of the specified type")
    AMXConfigProxy createChild(
        @Param(name = "childType")
        String childType,
        @Param(name = "params")
        @Description("name/value pairs for attributes")
        Map<String, Object> params);


    /**
     * Create one or more children of any type(s). Outer map is keyed by type.
     * Inner maps are the attributes of each child. At the same time, attributes can be set
     * on the parent element via 'attrs'. The entire operation is transactional (all or none).
     */
    @ManagedOperation
    AMXConfigProxy[] createChildren(
        @Param(name = "childrenMaps")
        @Description("Keyed by type, then one Map per child of that type, with each map containing name/value pairs for attributes")
        Map<String, Map<String, Object>[]> childrenMaps,
        @Param(name = "attrs")
        @Description("Attributes to be set on the parent element")
        Map<String, Object> attrs);


    /**
     * Remove a config by type and name.
     *
     * @param childType the AMX j2eeType as defined
     * @param name the name of the child
     * @return the ObjectName of the removed child, or null if not found
     */
    @ManagedOperation
    ObjectName removeChild(
        @Param(name = "childType")
        String childType,
        @Param(name = "name")
        String name);


    /**
     * Generically remove a config by type (child must be a singleton)
     *
     * @param childType the AMX j2eeType as defined
     * @return the ObjectName of the removed child, or null if not found
     */
    @ManagedOperation
    ObjectName removeChild(
        @Param(name = "childType")
        String childType);


    /**
     * Direct access to the MBeanServer, calls conn.setAttributes(objectName, attrs).
     * Unlike {@link StdAttributesAccess#setAttributes}, this method throws a generic Exception if
     * there is a transaction failure.
     */
    @ManagedOperation
    AttributeList setAttributesTransactionally(
        @Param(name = "attrs")
        AttributeList attrs)
    throws Exception;

}
