/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Change some value in ProgressStatus using set method.
 *
 * @author martinmares
 */
public class ProgressStatusEventSet extends ProgressStatusEvent {

    private Integer totalStepCount;
    private Integer currentStepCount;

    public ProgressStatusEventSet(String id, Integer totalStepCount, Integer currentStepCount) {
        super(id);
        this.totalStepCount = totalStepCount;
        this.currentStepCount = currentStepCount;
    }

    public ProgressStatusEventSet(String progressStatusId) {
        super(progressStatusId);
    }

    public Integer getTotalStepCount() {
        return totalStepCount;
    }

    public Integer getCurrentStepCount() {
        return currentStepCount;
    }

    public void setTotalStepCount(Integer totalStepCount) {
        this.totalStepCount = totalStepCount;
    }

    public void setCurrentStepCount(Integer currentStepCount) {
        this.currentStepCount = currentStepCount;
    }

    @Override
    public ProgressStatus apply(ProgressStatus ps) {
        if (totalStepCount != null) {
            ps.setTotalStepCount(totalStepCount);
        }
        if (currentStepCount != null) {
            ps.setCurrentStepCount(currentStepCount);
        }
        return ps;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.totalStepCount != null ? this.totalStepCount.hashCode() : 0);
        hash = 53 * hash + (this.currentStepCount != null ? this.currentStepCount.hashCode() : 0);
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
        final ProgressStatusEventSet other = (ProgressStatusEventSet) obj;
        if (this.totalStepCount == null ? other.totalStepCount != null : !this.totalStepCount.equals(other.totalStepCount)) {
            return false;
        }
        if (this.currentStepCount == null ? other.currentStepCount != null : !this.currentStepCount.equals(other.currentStepCount)) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + getSourceId()
            + ", progress=" + getCurrentStepCount() + '/' + getTotalStepCount() + ']';
    }

}
