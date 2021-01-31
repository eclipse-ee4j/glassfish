/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.invocation;

import org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.jvnet.hk2.annotations.Contract;

/**
 * InvocationManager provides interface to keep track of component context on a per-thread basis
 */

@Contract
public interface InvocationManager {

    /**
     * To be called by a Container to indicate that the Container is about to invoke a method on a component. The preInvoke
     * and postInvoke must be called in pairs and well-nested.
     *
     * @param inv the Invocation object
     */
    <T extends ComponentInvocation> void preInvoke(T inv) throws InvocationException;

    /**
     * To be called by a Container to indicate that the Container has just completed the invocation of a method on a
     * component. The preInvoke and postInvoke must be called in pairs and well-nested.
     *
     * @param inv the Invocation object
     */
    <T extends ComponentInvocation> void postInvoke(T inv) throws InvocationException;

    /**
     * Returns the current Invocation object associated with the current thread
     */
    <T extends ComponentInvocation> T getCurrentInvocation();

    /**
     * Returns the previous Invocation object associated with the current thread. Returns null if there is none. This is
     * typically used when a component A calls another component B within the same VM. In this case, it might be necessary
     * to obtain information related to both component A using getPreviousInvocation() and B using getCurrentInvocation()
     */
    <T extends ComponentInvocation> T getPreviousInvocation() throws InvocationException;

    /**
     * return true iff no invocations on the stack for this thread
     */
    boolean isInvocationStackEmpty();

    java.util.List<? extends ComponentInvocation> getAllInvocations();

    void registerComponentInvocationHandler(ComponentInvocationType type, RegisteredComponentInvocationHandler handler);

    /**
     * To be called by the infrastructure to indicate that some user code not associated with any Java EE specification may
     * be called. In particular must be called by the Weld integration layer to indicate the application environment in
     * which the portable extensions are running
     * <p>
     * The pushAppEnvironment and popAppEnvironment must be called in pairs and well-nested.
     *
     * @param env may not be null. Information about the application environment
     */
    void pushAppEnvironment(ApplicationEnvironment env);

    /**
     * Gets the current application environment on the current thread
     *
     * @return The current ApplicationEnvironment, or null if there is none
     */
    ApplicationEnvironment peekAppEnvironment();

    /**
     * To be called by the infrastructure to indicate that some user code not associated with any Java EE specification is
     * finished being called. In particular must be called by the Weld integration layer to indicate the application
     * environment in which the portable extensions are running
     * <p>
     * The pushAppEnvironment and popAppEnvironment must be called in pairs and well-nested.
     *
     * @param env may not be null. Information about the application environment
     */
    void popAppEnvironment();

}
