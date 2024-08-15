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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.MethodDescriptor;

import java.util.LinkedList;
import java.util.List;

import org.glassfish.deployment.common.Descriptor;


/**
 * Contains a single interceptor binding entry.
 */
public class InterceptorBindingDescriptor extends Descriptor
{
    public enum BindingType {

        DEFAULT,
        CLASS,
        METHOD

    }

    // Only applies to CLASS and METHOD
    private String ejbName;

    // Only applies to METHOD
    private MethodDescriptor businessMethod;

    // Ordered list of interceptor classes.
    private LinkedList<String> interceptors = new LinkedList<String>();

    // True if interceptor list represents a total ordering.
    private boolean isTotalOrdering;

    // Only applies to CLASS or METHOD
    private boolean excludeDefaultInterceptors;

    // Only applies to METHOD
    private boolean excludeClassInterceptors;

    private boolean needsOverloadResolution;

    public InterceptorBindingDescriptor() {
    }

    public BindingType getBindingType() {
        if( "*".equals(ejbName) ) {
            return BindingType.DEFAULT;
        } else if( businessMethod == null ) {
            return BindingType.CLASS;
        } else {
            return BindingType.METHOD;
        }
    }

    public void setNeedsOverloadResolution(boolean flag) {
        needsOverloadResolution = flag;
    }

    public boolean getNeedsOverloadResolution() {
        return needsOverloadResolution;
    }

    public void setEjbName(String ejb) {
        ejbName = ejb;
    }

    public String getEjbName() {
        return ejbName;
    }

    public void setBusinessMethod(MethodDescriptor desc) {
        businessMethod = desc;
    }

    public MethodDescriptor getBusinessMethod() {
        return businessMethod;
    }

    public void appendInterceptorClass(String interceptor) {
        interceptors.addLast(interceptor);
    }

    public List<String> getInterceptorClasses() {
        return new LinkedList<String>(interceptors);
    }

    public void setIsTotalOrdering(boolean flag) {
        isTotalOrdering = flag;
    }

    public boolean getIsTotalOrdering() {
        return isTotalOrdering;
    }

    public void setExcludeDefaultInterceptors(boolean flag) {
        excludeDefaultInterceptors = flag;
    }

    public boolean getExcludeDefaultInterceptors() {
        return excludeDefaultInterceptors;
    }

    public void setExcludeClassInterceptors(boolean flag) {
        excludeClassInterceptors = flag;
    }

    public boolean getExcludeClassInterceptors() {
        return excludeClassInterceptors;
    }

}
