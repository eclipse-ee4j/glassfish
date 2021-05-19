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

import org.glassfish.admin.amx.core.proxy.AMXProxyHandler;
import org.glassfish.external.amx.AMX;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

import javax.management.ObjectName;
import java.util.Map;
import java.util.Set;

/**
 * @deprecated
 * <p>
 * An AMXProxy offers generic access to any AMX-compliant MBean, including the ability to navigate
 * upwards to the Parent MBean, find all children or those of a particular type or name, to get
 * all metadata, atttributes, or to invoke any method.
 * <p>
 * Various sub-interfaces offer additional functionality, such as explicit methods for getting
 * children of a particular type, creating new children (eg configuration), attribute getter/setter
 * methods, etc. The most notable sub-interface is
 * {@link org.glassfish.admin.amx.config.AMXConfigProxy} and
 * its sub-interfaces.
 * <p>
 * <b>Implementing handler&mdash;</b> an AMXProxy is implemented by {@link AMXProxyHandler}, but the
 * handler should be considered
 * private: do not use it as it is subject to change.
 * <p>
 * <b>Sub interfaces&mdash;</b> the base AMXProxy interface can and should be extended for specific
 * MBeans, but in
 * most cases it will not be appropriate or convenient for MBean <em>implementors</em> to
 * 'implement' the interface because it is for use by a <i>proxy to the MBean</i>, not the MBean
 * itself.
 * In particular, it makes no sense for an MBean to implement the proxy interface because the proxy
 * interface demands the use of AMXProxy and sub-types, whereas the MBean must return ObjectName.
 * <p>
 * <b>Method name convention&mdash;</b> a convention followed in AMXProxy is that convenience
 * "getter" methods
 * (non-remote methods implemented directly by the proxy itself) do not use the <code>get</code>
 * prefix,
 * in order to distinguish them from the usual getter pattern for real MBean attributes.
 * For example, {@link #parent} returns an AMXProxy, but {@link #getParent} returns the value of the
 * {@code Parent} attribute (an ObjectName).
 * The same convention is followed for {@link #childrenSet}, etc / {@link #getChildren}.
 * <p>
 * <b>Not authoritative&mdash;</b> <em>proxy interfaces should not be considered authoritative,
 * meaning that an underlying MBean
 * implementation determines what the MBean actually provides, possibly ignoring
 * the proxy interface</em> (this is the case with config MBeans, which derive their metadata from
 * the ConfigBean
 * <code>@Configured</code> interface).
 * Therefore, it is possible for the proxy interface to completely misrepresent the actual MBean
 * functionality,
 * should the interface get out of sync with the actual MBean.
 * Only at runtime would errors between the interface and the MBean would emerge.
 * <p>
 * <b>Methods in sub-interfaces of AMXProxy&mdash;</b> To mininimize issues with tracking
 * implementation changes over time (eg addition or removal of attributes),
 * sub-interfaces of {@code AMXProxy} might choose to <em>omit</em>
 * getter/setter methods for attributes, and instead manifest the <i>containment relationships</i>
 * (children),
 * which form the core of usability of navigating the hierarchy.
 * The methods {@link #attributeNames} and {@link #attributesMap} can be used to generically
 * obtain all available attributes, and of course {@link MetaGetters#mbeanInfo} provides extensive
 * metadata.
 * <p>
 * <b>Auto-mapping of ObjectName&mdash;</b> An AMXProxy automatically maps various ObjectName
 * constructs
 * to the equivalent AMXProxy(ies).
 * <p>
 * For example, an MBean providing an Attribute named <code>Item</code>
 * should declare it as an <code>ObjectName</code>, or for a plurality <code>Items</code>, declaring
 * an <code>ObjectName[]</code>.
 * Any of the following proxy methods (declared in a sub-interface of AMXProxy) will automatically
 * convert the resulting
 * ObjectName(s) into the corresponding AMXProxy or plurality of AMXProxy:
 *
 * <pre>
 *
 * AMXProxy getItem();
 *
 *
 * AMXProxy[] getItems();
 *
 * Set&lt;AMXProxy> getItems();
 *
 * List&lt;AMXProxy> getItems();
 *
 * Map&lt;String, AMXProxy> getItems();
 * </pre>
 *
 * The same approach is used in the generic {@link #child}, {@link #childrenSet},
 * {@link #childrenMap} methods.
 * <p>
 * <b>Invoking operations generically&mdash;</b> Use the {@link #invokeOp} methods to invoke an
 * arbitrary
 * operation by name.
 *
 * @see Extra
 * @see MetaGetters
 * @see org.glassfish.admin.amx.config.AMXConfigProxy
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@Deprecated
public interface AMXProxy extends AMX_SPI {

    /** MBean MUST return an ObjectName. May be null for DomainRoot only. */
    AMXProxy parent();


    /**
     * Value of the name property of the ObjectName. Could differ from getName(), which returns
     * the internal name, which might not be legal in an ObjectName, or could have changed.
     */
    String nameProp();


    /** The value of the {@link AMX#PARENT_PATH_KEY} property in the ObjectName */
    String parentPath();


    /** The value of the {@link AMX#TYPE_KEY} property in the ObjectName */
    String type();


    /**
     * A proxy can become invalid if its corresponding MBean is unregistered, the connection is
     * lost, etc.
     * If currently marked as valid, a trip to the server is made to verify validity.
     *
     * @return true if this proxy is valid
     */
    boolean valid();


    /**
     * Get all existing children of all types. Returns null if the MBean is a leaf node (cannot have
     * children) .
     */
    Set<AMXProxy> childrenSet();


    /**
     * Get all children of a specified type, keyed by the name as found in the ObjectName.
     */
    Map<String, AMXProxy> childrenMap(String type);


    /**
     * Get all children of the same type.
     * The Map is keyed by the name as found in the ObjectName.
     *
     * @param intf the proxy interface, type is deduced from it
     */
    <T extends AMXProxy> Map<String, T> childrenMap(Class<T> intf);


    /**
     * Get Maps keyed by type, with a Map keyed by name.
     */
    Map<String, Map<String, AMXProxy>> childrenMaps();


    /**
     * Get a singleton child of the specified type. An exception is thrown if the child is not
     * a singleton. If children do not exist, or there is no such child, then null is returned.
     */
    AMXProxy child(String type);


    /**
     * Get a singleton child. Its type is deduced from the interface using {@link Util#deduceType}.
     */
    <T extends AMXProxy> T child(Class<T> intf);


    /**
     * Return a proxy implementing the specified interface. Clients with access to
     * a sub-interface of {@link AMXProxy} can specialized it with this method; the proxy
     * by default will implement only the base {@link AMXProxy} interface.
     * <p>
     * This method is needed
     * when crossing module boundaries where the desired class is not available to the
     * AMXProxyHandler
     * through its own classloader and/or when a generic proxy has been obtained through other
     * means.
     * When sub-interfaces of AMXProxy already return the appropriate type there is no reason or
     * need
     * to use this method.
     */
    <T extends AMXProxy> T as(Class<T> intf);


    /**
     * Get a Map keyed by Attribute name of all Attribute values. Requires a trip to the server.
     */
    Map<String, Object> attributesMap();


    /**
     * Get a Map keyed by Attribute name of the specified Attribute values.
     */
    Map<String, Object> attributesMap(Set<String> attrNames);


    /**
     * Get all available Attributes names, no trip to server needed. Requires a trip to the server.
     */
    Set<String> attributeNames();


    /**
     * Get this MBean's pathname. Its parent path can be obtained by calling {@code path}
     * on {@link #parent}
     */
    String path();


    /**
     * The ObjectName of this MBean.
     */
    ObjectName objectName();


    /** Return a Java interface representing this MBean, suitable for display or compilation */
    String java();


    /** additional capabilities, including direct JMX access */
    Extra extra();


    /** Invoke an operation by name, no arguments. */
    Object invokeOp(String operationName);


    /** Invoke an operation by name, JMX style params and signature. */
    Object invokeOp(String operationName, Object[] params, String[] signature);
}
