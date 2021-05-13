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

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.spi.DirObjectFactory;

/**
  * An DirObjectFactory that returns the unmarshalled object from a
  * MarshalledObject.
  * For example, a Remote/JRMP object is stored as MarshalledObject.
  * Use this factory to return its unmarshalled form (e.g., the Remote object).
  *
  * @author Rosanna Lee
  */
public class MarshalledToObject implements DirObjectFactory {

    public MarshalledToObject() {
    }

    /**
     * Unmarshals a MarshalledObject.
     *
     * @param orig The possibly null object to check.
     * @param name Ignored
     * @param ctx Ignored
     * @param env Ignored
     * @param attrs The possibly attributes containing the "objectclass"
     * @return The non-null unmarshalled object if <tt>orig</tt> is a
     *     MarshalledObject; otherwise null
     * @exception IOException If problem unmarshalling the object
     * @exception ClassNotFoundException If cannot find class required to unmarshal.
     */
    @Override
    public Object getObjectInstance(Object orig, Name name, Context ctx, Hashtable env, Attributes attrs)
        throws Exception {
        Attribute oc;
        if (orig instanceof MarshalledObject && attrs != null
            && (oc = attrs.get("objectclass")) != null
            && (oc.contains("javaMarshalledObject") || oc.contains("javamarshalledobject"))) {
            return ((MarshalledObject) orig).get();
        }
        return null;
    }


    /**
     * Unmarshals a MarshalledObject.
     *
     * @param orig The possibly null object to check.
     * @param name Ignored
     * @param ctx Ignored
     * @param env Ignored
     * @return The non-null unmarshalled object if <tt>orig</tt> is a MarshalledObject; otherwise null
     * @exception IOException If problem unmarshalling the object
     * @exception ClassNotFoundException If cannot find class required to unmarshal.
     */
    @Override
    public Object getObjectInstance(Object orig, Name name, Context ctx, Hashtable env) throws Exception {
        if (orig instanceof MarshalledObject) {
            return ((MarshalledObject) orig).get();
        }
        return null;
    }
}
