/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
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

    private static final int NO_ASSOCIATED_THREAD = -1;
    private boolean associated;
    private long threadId = NO_ASSOCIATED_THREAD;
    private boolean unusable;

    public AssocWithThreadResourceHandle(ManagedConnection resource, ResourceSpec spec, ResourceAllocator alloc) {
        super(resource, spec, alloc);
    }

    /**
     * @return true if the resource should not be used again.
     */
    public boolean isUnusable() {
        return unusable;
    }

    /**
     * Mark the resource to not to be provided again.
     * Usually because it is somehow broken or the pool will be destroyed.
     */
    public void setUnusable() {
        this.unusable = true;
    }

    /**
     * Associated resource is owned by a thread and will not be put back to the pool.
     *
     * @return true if associated with thread.
     */
    public boolean isAssociated() {
        return associated;
    }

    /**
     * @param associated true to associate with thread.
     */
    public void setAssociated(boolean associated) {
        this.associated = associated;
        this.threadId = associated ? Thread.currentThread().getId() : NO_ASSOCIATED_THREAD;
    }

    /**
     * @return id of the associated thread, -1 if the resource is NOT associated with a thread.
     */
    public long getThreadId() {
        return threadId;
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
