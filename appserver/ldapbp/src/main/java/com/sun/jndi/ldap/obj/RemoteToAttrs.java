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

import com.sun.jndi.ldap.LdapCtxFactory;

import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.server.RemoteStub;
import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.spi.DirStateFactory;

/**
  * An DirStateFactory that returns the object and Attributes
  * given a java.rmi.Remote object. It handles the following different scenarios:
  *<ul>
  *<li>
  * If object is a JRMP stub (RemoteStub), return its MarshalledObject.
  *<li>
  * If object is a CORBA Object or a CORBA stub,
  * store as CORBA IOR (see CorbaToAttrs).
  *<li>
  * If all else fails, store as MarshalledObject.
  *</ol>
  *<p>
  * The form of <tt>getStateToBind()</tt> that does not accept an
  * <tt>Attributes</tt> parameter always return null because this
  * factory needs to return <tt>Attributes</tt>.
  * The caller should always use the form of <tt>getStateToBind()</tt>
  * that accepts an <tt>Attributes</tt> parameter. This is the case if
  * the service provider uses <tt>DirectoryManager</tt>.
  *<p>
  * This factory only works on JDK1.2 systems because it needs MarshalledObject
  * and CORBA classes, which are in JDK1.2.
  *
  * @author Rosanna Lee
  */
public class RemoteToAttrs implements DirStateFactory {
    private static final String CLASSNAME_ATTRID = "javaClassName";
    private static final String CLASSNAMES_ATTRID = "javaClassNames";
    private static final String STRUCTURAL_OCID = "javacontainer";
    private static final String MARSHALLED_OCID = "javamarshalledobject";

    public RemoteToAttrs() {
    }

    /**
     * Returns an object and attributes for storing into LDAP that represents
     * the Remote object. If the input is not a Remote object, or if
     * the Remote object cannot be transformed, return null.
     *
     * @param orig The object to store; if not Remote; return null.
     * @param name Ignored
     * @param ctx  Ignored
     * @param env A possibly null environment. Used to get the ORB to use
     *   when getting the CORBA object for the Remote object using RMI-IIOP.
     * @param inAttrs The possibly null attributes included with the bind.
     * @return {obj, attrs} where obj is either null or the RemoteStub of the
     * Remote object. It is the stub if <tt>orig</tt> is already a stub, or if
     * <tt>orig</tt> has a JRMP stub. <tt>attrs</tt> is a union of
     * <tt>inAttrs</tt> and and some other attributes depending on how
     * <tt>orig</tt> has been transformed.
     * If <tt>orig</tt> is transformed into a CORBA object, then it will
     * have CORBA-related attributes (see CorbaToAttrs).
     * @exception ConfigurationException If problem calling RemoteObject.toStub()
     *    or if problem transforming Remote object to CORBA object.
     * @exception NamingException If some other problem occurred transforming
     *    the object.
     */
    @Override
    public DirStateFactory.Result getStateToBind(Object orig, Name name, Context ctx, Hashtable env, Attributes inAttrs)
        throws NamingException {

        if (!(orig instanceof java.rmi.Remote)) {
            return null; // Only handles Remote objects
        }

        // A JRMP stub, just bind
        if (orig instanceof RemoteStub) {
            return jrmpObject(orig, inAttrs);
        } else {
            // Try doing CORBA mapping
            try {
                DirStateFactory.Result answer = RemoteToCorbaToAttrs.remoteToCorbaToAttrs((Remote) orig, env, inAttrs);

                if (answer != null) {
                    return answer;
                }

            } catch (ClassNotFoundException e) {
                // RMI-IIOP package not available.
                // Ignore and continue because we don't want RMI/JRMP to
                // have dependency on RMI-IIOP packages being available
            }
        }

        // Otherwise, marshal object
        return jrmpObject(orig, inAttrs);
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
        // Cannot just return obj; need to return Attributes too
        return null;
    }

    /**
      * Returns a pair consisting of a MarshalledObject and attributes to
      * be bound with the stub.
      *
      * @param obj The non-null object to store.
      * @param inAttrs The possible null attributes to store with object.
      * @return A non-null Result consisting of the MarshalledObject and attributes.
      */
    private static DirStateFactory.Result jrmpObject(Object obj, Attributes inAttrs) throws NamingException {
        try {
            Object mobj = new MarshalledObject(obj);

            Attributes outAttrs = null;
            Attribute cname = null;
            Attribute tnames = null;
            Attribute objectClass = null;

            if (inAttrs != null) {
                // Get existing objectclass attribute
                objectClass = inAttrs.get("objectClass");
                if (objectClass == null && !inAttrs.isCaseIgnored()) {
                    // %%% workaround
                    objectClass = inAttrs.get("objectclass");
                }

                // No objectclasses supplied, use "top" to start
                if (objectClass == null) {
                    objectClass =  new BasicAttribute("objectClass", "top");
                } else {
                    objectClass = (Attribute)objectClass.clone();
                }

                cname = inAttrs.get(CLASSNAME_ATTRID);
                tnames = inAttrs.get(CLASSNAMES_ATTRID);

                outAttrs = (Attributes)inAttrs.clone();
            } else {
                outAttrs = new BasicAttributes(true);
                objectClass = new BasicAttribute("objectClass", "top");
            }

            if (cname == null) {
                outAttrs.put(CLASSNAME_ATTRID, obj.getClass().getName());
            }
            if (tnames == null) {
                Attribute tAttr = LdapCtxFactory.createTypeNameAttr(obj.getClass());
                if (tAttr != null) {
                    outAttrs.put(tAttr);
                }
            }

            boolean structural = objectClass.size() == 0 || (objectClass.size() == 1 && objectClass.contains("top"));

            if (structural) {
                objectClass.add(STRUCTURAL_OCID);
            }
            objectClass.add(MARSHALLED_OCID);
            outAttrs.put(objectClass);

            return new DirStateFactory.Result(mobj, outAttrs);

        } catch (java.io.IOException e) {
            NamingException ne = new NamingException("Cannot create MarshallObject for " + obj);
            ne.setRootCause(e);
            throw ne;
        }
    }
}
