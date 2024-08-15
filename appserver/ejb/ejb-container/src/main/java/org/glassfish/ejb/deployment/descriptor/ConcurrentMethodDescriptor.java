/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.TimeUnit;

import org.glassfish.deployment.common.Descriptor;

public class ConcurrentMethodDescriptor extends Descriptor {

    private MethodDescriptor method;

    private Boolean writeLock = null;

    private TimeoutValueDescriptor accessTimeout;

    public void setAccessTimeout(TimeoutValueDescriptor t) {
        accessTimeout = t;
    }

    public void setConcurrentMethod(MethodDescriptor m) {
        method = m;
    }

    public MethodDescriptor getConcurrentMethod() {
        return method;
    }

    public void setWriteLock(boolean flag) {
        writeLock = flag;
    }

    public boolean hasLockMetadata() {
        return (writeLock != null);
    }

    public boolean isWriteLocked() {
        return writeLock;
    }

    public boolean isReadLocked() {
        return !writeLock;
    }

    public boolean hasAccessTimeout() {
        return (accessTimeout != null);
    }

    public long getAccessTimeoutValue() {
        return accessTimeout != null ? accessTimeout.getValue() : 0;
    }

    public TimeUnit getAccessTimeoutUnit() {
        return accessTimeout != null ? accessTimeout.getUnit() : null;
    }


}
