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

import com.sun.enterprise.deployment.web.ServletFilterMapping;

import jakarta.servlet.DispatcherType;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;

/**
 * Deployment object representing the servlet filter mapping spec
 * @author Martin D. Flynn
 */
public final class ServletFilterMappingDescriptor
extends Descriptor
implements com.sun.enterprise.deployment.web.ServletFilterMapping
{

    private EnumSet<DispatcherType> dispatchers;
    private List<String> servletNames;
    private List<String> urlPatterns;

    /** generic constructor */
    public ServletFilterMappingDescriptor() {
        super(""/*name*/, ""/*description*/);
    }

    /** copy constructor */
    public ServletFilterMappingDescriptor(ServletFilterMappingDescriptor other) {
        super(other);
        dispatchers = (other.dispatchers != null)?
            EnumSet.copyOf(other.dispatchers) : null;
    }

    public void addServletName(String servletName) {
        getServletNames().add(servletName);
    }

    public List<String> getServletNames() {
        if (servletNames == null) {
            servletNames = new LinkedList<String>();
        }
        return servletNames;
    }

    public void addURLPattern(String urlPattern) {
        getUrlPatterns().add(urlPattern);
    }

    public List<String> getUrlPatterns() {
        if (urlPatterns == null) {
            urlPatterns = new LinkedList<String>();
        }
        return urlPatterns;
    }

    public void addDispatcher(String dispatcher) {
        if (dispatchers == null) {
            dispatchers = EnumSet.noneOf(DispatcherType.class);
        }
        dispatchers.add(Enum.valueOf(DispatcherType.class, dispatcher));
    }

    public void removeDispatcher(String dispatcher) {
        if (dispatchers == null) {
            return;
        }
        dispatchers.remove(Enum.valueOf(DispatcherType.class, dispatcher));
    }

    public Set<DispatcherType> getDispatchers() {
        if (dispatchers == null) {
            dispatchers = EnumSet.noneOf(DispatcherType.class);
        }
        return dispatchers;
    }


    /** compare equals */
    public boolean equals(Object obj) {
        if (obj instanceof ServletFilterMapping) {
            ServletFilterMapping o = (ServletFilterMapping) obj;
            Set<DispatcherType> otherDispatchers = o.getDispatchers();
            boolean sameDispatchers =
                ( (dispatchers == null &&
                (otherDispatchers == null || otherDispatchers.size() == 0)) ||
                    (dispatchers != null && dispatchers.equals(otherDispatchers)) );
            if ( this.getName().equals(o.getName())
                && this.getServletNames().equals(o.getServletNames())
                && this.getUrlPatterns().equals(o.getUrlPatterns())
                && sameDispatchers ) {
                return true;
            }
        }

        return false;
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + getName().hashCode();
        result = 37*result + getServletNames().hashCode();
        result = 37*result + getUrlPatterns().hashCode();
        return result;
    }
}
