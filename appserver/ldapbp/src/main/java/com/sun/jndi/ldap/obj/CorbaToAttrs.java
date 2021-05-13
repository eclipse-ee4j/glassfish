/*
 * Copyright (c) 1999, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jndi.ldap.obj;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.spi.DirStateFactory;

/**
  * A DirStateFactory that returns an Attributes when
  * given a omg.org.CORBA.Object.
  * The form of <tt>getStateToBind()</tt> that does not accept an
  * <tt>Attributes</tt> parameter always return null because this
  * factory needs to return <tt>Attributes</tt>.
  * The caller should always use the form of <tt>getStateToBind()</tt>
  * that accepts an <tt>Attributes</tt> parameter. This is the case if
  * the service provider uses <tt>DirectoryManager</tt>.
  *<p>
  * The LDAP schema for CORBA objects is:
  *<blockquote>
  * objectClass: top, corbaObject, corbaContainer, corbaObjectReference
  * corbaIor: IOR of CORBA object
  *</blockquote>
  *
  * @author Rosanna Lee
  */
public class CorbaToAttrs implements DirStateFactory {
    public CorbaToAttrs() {
    }

    /**
     * Returns attributes required for storing a CORBA object.
     * Get the IOR from <tt>orig</tt> and use it for the "corbaIor" attribute.
     * Add "corbaObject" to "objectclass" attribute. If there are no
     * other objectclass attribute values, the entry needs a structural
     * objectclass: add "corbaContainer" as an additional objectclass.
     *
     * @param orig The CORBA object to bind. If not an instance of
     *            org.omg.CORBA.portable.ObjectImpl, return null.
     * @param name Ignored
     * @param ctx  Ignored
     * @param env  Ignored
     * @param inAttrs A possibly null set of attributes that will accompany
     *         this bind. These attributes are combined with those required
     *        for storing <tt>orig</tt>.
     * @return {null, attrs} where <tt>attrs</tt> is the union of
     * <tt>inAttrs</tt> and attributes that represent the CORBA object
     * <tt>orig</tt>. null if <tt>orig</tt> is not an instance of
     * <tt>ObjectImpl</tt>.
     * @exception NamingException Not thrown.
     */
    @Override
    public DirStateFactory.Result getStateToBind(Object orig, Name name, Context ctx, Hashtable env, Attributes inAttrs)
        throws NamingException {
        if (orig instanceof org.omg.CORBA.portable.ObjectImpl) {

        // Turn org.omg.CORBA.Object into attrs
        return new DirStateFactory.Result(null,
            corbaToAttrs((org.omg.CORBA.portable.ObjectImpl)orig, inAttrs));
        }
        return null; // pass and let next state factory try
    }

    /**
     * Always return null.
     * @param orig Ignored
     * @param name Ignored
     * @param ctx Ignored
     * @param env Ignored
     * @exception NamingException Not thrown
     */
    @Override
    public Object getStateToBind(Object orig, Name name, Context ctx, Hashtable env) throws NamingException {
        // Cannot just return obj; needs to return Attributes
        return null;
    }

    /**
     * Returns attributes required for storing a CORBA object.
     * Get the IOR from <tt>orig</tt> and use it for the "corbaIor" attribute.
     * Add "corbaObject" to "objectclass" attribute. If there are no
     * other objectclass attribute values, the entry needs a structural
     * objectclass: add "corbaContainer" as an additional objectclass.
     *
     * @param orig The non-null ObjectImpl from which to get the IOR
     * @param inAttrs The possibly attribute set that is to be merged with the
     *         CORBA attributes.
     * @return A non-null Attributes containing the incoming attribute merged
     * with the CORBA attributes.
     */
    static Attributes
    corbaToAttrs(org.omg.CORBA.portable.ObjectImpl orig, Attributes inAttrs) {

        // Get holder for outgoing attributes
        Attributes outAttrs = (inAttrs != null) ? (Attributes) inAttrs.clone() : new BasicAttributes(true);

        // Put IOR
        String ior = orig._orb().object_to_string(orig);
        outAttrs.put("corbaIor", ior);

        // Put appropriate object class
        Attribute objectClass = outAttrs.get("objectClass");
        if (objectClass == null && !outAttrs.isCaseIgnored()) {
            // %%% workaround
            objectClass = outAttrs.get("objectclass");
        }

        if (objectClass == null) {
            // No objectclasses supplied
            objectClass =  new BasicAttribute("objectClass", "top");
            objectClass.add("corbaContainer");
        } else {
            // Clone existing objectclass
            objectClass = (Attribute)objectClass.clone();
        }

        objectClass.add("corbaObject");
        objectClass.add("corbaObjectReference");
        outAttrs.put(objectClass);

        return outAttrs;
    }
}
