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

package org.glassfish.web.deployment.descriptor;

import com.sun.enterprise.deployment.web.AppListenerDescriptor;

import org.glassfish.deployment.common.Descriptor;

/**
 * Objects exhibiting this interface represent an event listener descriptor.
 * This represents the <listener-class> XML element defined in the
 * Servlet 2.3 spec.
 * @author Vivek Nagar
 */
public class AppListenerDescriptorImpl extends Descriptor implements AppListenerDescriptor {

    private String listenerClass;
    private String displayName;

    /** The default constructor.
     */
    public AppListenerDescriptorImpl() {
    }

    /**
     * Create an instance of the descriptor with the specified listener class.
     * @param the listener class name.
     */
    public AppListenerDescriptorImpl(String clz) {
        this.listenerClass = clz;
    }

    /**
     * Return the listener class.
     * @return the listener class name or empty string if none.
     */
    @Override
    public String getListener() {
        if (this.listenerClass == null) {
            this.listenerClass = "";
        }
        return this.listenerClass;
    }

    /**
     * Sets the listener class.
     * @param the listener class name.
     */
    @Override
    public void setListener(String clz) {
        this.listenerClass = clz;
    }

    /** set display name */
    @Override
    public void setDisplayName(String name) {
        this.displayName = (name != null)? name : "";
    }

    /** get display name */
    @Override
    public String getDisplayName() {
        String n = this.displayName;
        if ((n == null) || n.equals("")) {
            n = this.getName();
        }
        return n;
    }

    /**
     * Test for equals
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof AppListenerDescriptorImpl)
            ? this.getListener().equals(((AppListenerDescriptorImpl) obj).getListener())
            : super.equals(obj);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37*result + getListener().hashCode();
        return result;
    }


    /**
     * A formatted version of the state as a String.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("Listener Class ").append(this.getListener());
    }
}

