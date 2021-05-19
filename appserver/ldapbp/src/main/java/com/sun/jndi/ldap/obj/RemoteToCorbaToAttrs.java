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

import java.rmi.Remote;
import java.util.Hashtable;

import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.spi.DirStateFactory;

import org.glassfish.jndi.toolkit.corba.CorbaUtils;
import org.omg.CORBA.ORB;

/**
  * An DirStateFactory that returns an Attributes when
  * given an RMI-IIOP object.
  * This factory requires CORBA classes and RMI-IIOP for getting
  * the CORBA object of an Remote object.
  * <p>
  * Try to get the CORBA object for the Remote object (using RMI-IIOP),
  * and turn that CORBA object into attributes. Return null if the Remote
  * objectis a JRMP implementation or JRMP stub.
  * <p>
  * The LDAP schema for storing CORBA objects is:
  *<blockquote>
  * objectClass: top, corbaObject, corbaContainer
  * corbaIor: IOR of CORBA object
  *</blockquote>
  *
  * @author Rosanna Lee
  */

public class RemoteToCorbaToAttrs implements DirStateFactory {
    public RemoteToCorbaToAttrs() {
    }

    /**
      * Returns the attributes required for storing a CORBA object.
      * Uses the utility supplied by CorbaToAttrs.
      * @param orig A non-null Remote object.
      * @param name Ignored
      * @param ctx Ignored
      * @param env A possibly null environment. Used to get the ORB to use
      *   when getting the CORBA object for the Remote object using RMI-IIOP.
      * @param inAttrs The possibly null attributes included with the bind.
      * @return {null, attrs} where <tt>attrs</tt> is the union of
      * <tt>inAttrs</tt> and attributes that represent the CORBA object (of)
      * <tt>orig</tt>. null if <tt>orig</tt> cannot be turned into a
      * <tt>ObjectImpl</tt>, or if RMI-IIOP is not available.
      * @exception ConfigurationException If configuration problems encountered
      *   in getting <tt>orig</tt>'s CORBA object, such as RMI-IIOP not available.
      * @exception NamingException If some other error occurred.
      */
    @Override
    public DirStateFactory.Result getStateToBind(Object orig, Name name, Context ctx, Hashtable env, Attributes inAttrs)
        throws NamingException {
        if (orig instanceof Remote) {
            try {
                return remoteToCorbaToAttrs((Remote) orig, env, inAttrs);
            } catch (ClassNotFoundException e) {
                // RMI-IIOP library not available
                throw new ConfigurationException("javax.rmi packages not available");
            }
        }
        return null;
    }


    /**
      * Gets the connected IIOP stub for Remote object.
      */
    static DirStateFactory.Result remoteToCorbaToAttrs(Remote orig, Hashtable env, Attributes inAttrs)
        throws ClassNotFoundException, NamingException {
        org.omg.CORBA.Object corbaObj;

        if (orig instanceof org.omg.CORBA.Object) {
            // Implements both CORBA and Remote; no need for conversion
            corbaObj = (org.omg.CORBA.Object) orig;
        } else {

            // Use ORB supplied or default ORB
            ORB orb = (env != null) ? (ORB) env.get("java.naming.corba.orb") : null;
            if (orb == null) {
                orb = getDefaultOrb(env);
            }

            // Convert Remote to CORBA using RMI/IIOP
            corbaObj = CorbaUtils.remoteToCorba(orig, orb);
            if (corbaObj == null) {
                return null; // Cannot get CORBA obj; must be JRMP obj
            }
        }

        // Convert CORBA object to attributes
        return new DirStateFactory.Result(null,
            CorbaToAttrs.corbaToAttrs((org.omg.CORBA.portable.ObjectImpl) corbaObj, inAttrs));
    }

    /**
     * Always returns null.
     * @param orig Ignored
     * @param name Ignored
     * @param ctx Ignored
     * @param env Ignored
     * @return null
     * @exception NamingException Not thrown.
     */
    @Override
    public Object getStateToBind(Object orig, Name name, Context ctx, Hashtable env) throws NamingException {
        // Cannot just return obj; needs to return Attributes
        return null;
    }


    /**
     * Return default ORB.
     * The ORB is used for getting the tie of a RMI-IIOP object.
     * %%%
     * The assumption is that any ORB will *NOT* do. That's why we can't maintain
     * this as a static. If this assumption is false, save result in static
     * to avoid calling it each time.
     *
     * @param env The possibly null environment properties to use when
     *            creating the default ORB.
     * @return A non-null ORB.
     */
    private static ORB getDefaultOrb(Hashtable env) {
        return CorbaUtils.getOrb(null, -1, env);
    }
}

