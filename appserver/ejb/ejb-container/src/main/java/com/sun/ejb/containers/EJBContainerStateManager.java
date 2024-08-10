/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers;

import com.sun.ejb.Container;
import com.sun.ejb.EjbInvocation;

import jakarta.ejb.EJBObject;

/**
 *
 * @author mvatkina
 */

public class EJBContainerStateManager {

    Container container;

    EJBContainerStateManager(Container c) {
        container = c;
    }

    public boolean isNullEJBObject(EJBContextImpl context) {
        return context.getEJBObjectImpl() == null;
    }

    public boolean isNullEJBLocalObject(EJBContextImpl context) {
        return context.getEJBLocalObjectImpl() == null;
    }

    public boolean isRemovedEJBObject(EjbInvocation inv) {
        return inv.ejbObject.isRemoved();
    }

    public boolean isRemovedEJBObject(EJBContextImpl context) {
        return !isNullEJBObject(context) && context.getEJBObjectImpl().isRemoved();
    }

    public boolean isRemovedEJBLocalObject(EJBContextImpl context) {
        return !isNullEJBLocalObject(context) && context.getEJBLocalObjectImpl().isRemoved();
    }

    /**
     * Associate EJB Object with this invocation and this Context
     * Note that some of the calls do not have Context assosiated with this
     * invocation, so Context object is passed in separately
     */
    public void attachObject(EjbInvocation inv, EJBContextImpl context,
            EJBObjectImpl ejbObjImpl, EJBLocalObjectImpl localObjImpl) {
        if ( ejbObjImpl != null && container.isRemoteObject() && (!inv.isLocal) ) {
            // associate the context with the ejbObject
            context.setEJBObjectImpl(ejbObjImpl);
            context.setEJBStub((EJBObject)ejbObjImpl.getStub());
        }

        if ( localObjImpl != null && container.isLocalObject() ) {
            // associate the context with the ejbLocalObject
            context.setEJBLocalObjectImpl(localObjImpl);
        }

        if ( inv.isLocal && localObjImpl != null ) {
            inv.ejbObject = localObjImpl;
        } else if (ejbObjImpl != null) {
            inv.ejbObject = ejbObjImpl;
        }
    }

    /**
     * Mark EJB Object associated with this Context as removed or not
     */
    public void markObjectRemoved(EJBContextImpl context, boolean removed) {
        if ( !isNullEJBObject(context) ) {
            context.getEJBObjectImpl().setRemoved(removed);
        }

        if ( !isNullEJBLocalObject(context) ) {
            context.getEJBLocalObjectImpl().setRemoved(removed);
        }
    }

    /**
     * Disconnect context from EJB(Local)Object so that
     * context.getEJBObject() will throw exception.
     */
    public void disconnectContext(EJBContextImpl context) {
        if ( !isNullEJBObject(context) ) {
            // reset flag in case EJBObject is used again
            context.getEJBObjectImpl().setRemoved(false);
            context.setEJBObjectImpl(null);
            context.setEJBStub(null);
        }

        if ( !isNullEJBLocalObject(context) ) {
            // reset flag in case EJBLocalObject is used again
            context.getEJBLocalObjectImpl().setRemoved(false);
            context.setEJBLocalObjectImpl(null);
        }
    }

    /**
     * Clear EJB Object references in the context
     */
    public void clearContext(EJBContextImpl context) {
        context.setEJBLocalObjectImpl(null);
        context.setEJBObjectImpl(null);
        context.setEJBStub(null);
    }

}
