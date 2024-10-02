/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.resource;

import com.sun.enterprise.resource.allocator.ResourceAllocator;

import jakarta.resource.spi.ManagedConnection;

/**
 * ResourceHandle with state related to assoc-with-thread pool
 *
 * @author Jagadish Ramu
 */
public class AssocWithThreadResourceHandle extends ResourceHandle {

    private boolean associated_;
    private long threadId_;
    private boolean dirty_;

    public AssocWithThreadResourceHandle(ManagedConnection resource, ResourceSpec spec, ResourceAllocator alloc) {
        super(resource, spec, alloc);
    }

    public boolean isDirty() {
        return dirty_;
    }

    public void setDirty() {
        dirty_ = true;
    }

    public boolean isAssociated() {
        return associated_;
    }

    public void setAssociated(boolean flag) {
        associated_ = flag;
    }

    public long getThreadId() {
        return threadId_;
    }

    public void setThreadId(long threadId) {
        threadId_ = threadId;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        // The equals method for the subclass just return the result of invoking super.equals()
        return super.equals(other);
    }

}
