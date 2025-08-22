/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.admin.progress;

import org.glassfish.api.admin.ProgressStatus;

/**
 * Progress method was called.
 *
 * @author martinmares
 */
public class ProgressStatusEventCreateChild extends ProgressStatusEvent {

    private String childId;
    private String name;
    private int allocatedSteps;
    private int totalSteps;

    public ProgressStatusEventCreateChild(String progressStatusId, String name, String childId, int allocatedSteps, int totalSteps) {
        super(progressStatusId);
        this.name = name;
        this.childId = childId;
        this.allocatedSteps = allocatedSteps;
        this.totalSteps = totalSteps;
    }

    public ProgressStatusEventCreateChild(String progressStatusId) {
        super(progressStatusId);
    }

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    public int getAllocatedSteps() {
        return allocatedSteps;
    }

    public void setAllocatedSteps(int allocatedSteps) {
        this.allocatedSteps = allocatedSteps;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ProgressStatus apply(ProgressStatus ps) {
        ProgressStatus chld;
        if (ps instanceof ProgressStatusBase) {
            ProgressStatusBase psb = (ProgressStatusBase) ps;
            chld = psb.createChild(name, allocatedSteps, totalSteps);
        } else {
            chld = ps.createChild(name, allocatedSteps);
            if (totalSteps >= 0) {
                chld.setTotalStepCount(totalSteps);
            }
        }
        return chld;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.childId != null ? this.childId.hashCode() : 0);
        hash = 19 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 19 * hash + this.allocatedSteps;
        hash = 19 * hash + this.totalSteps;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProgressStatusEventCreateChild other = (ProgressStatusEventCreateChild) obj;
        if (this.childId == null ? other.childId != null : !this.childId.equals(other.childId)) {
            return false;
        }
        if (this.name == null ? other.name != null : !this.name.equals(other.name)) {
            return false;
        }
        if (this.allocatedSteps != other.allocatedSteps) {
            return false;
        }
        if (this.totalSteps != other.totalSteps) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + getSourceId()
            + ", progress=" + getAllocatedSteps() + '/' + getTotalSteps() + ']';
    }
}
