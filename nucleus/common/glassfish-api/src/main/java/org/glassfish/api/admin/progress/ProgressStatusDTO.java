/*
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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mmares
 */
public class ProgressStatusDTO {

    public static class ChildProgressStatusDTO {

        private final int allocatedSteps;
        private final ProgressStatusDTO progressStatus;

        public ChildProgressStatusDTO(int allocatedSteps, ProgressStatusDTO progressStatus) {
            this.allocatedSteps = allocatedSteps;
            this.progressStatus = progressStatus;
        }

        public int getAllocatedSteps() {
            return allocatedSteps;
        }

        public ProgressStatusDTO getProgressStatus() {
            return progressStatus;
        }

        @Override
        public String toString() {
            return "ChildProgressStatusDTO{" + "allocatedSteps=" + allocatedSteps + ", progressStatus=" + progressStatus + '}';
        }

    }

    protected String name;
    protected String id;
    protected int totalStepCount = -1;
    protected int currentStepCount = 0;
    protected boolean completed = false;
    protected Set<ChildProgressStatusDTO> children = new HashSet<>();

    public ProgressStatusDTO() {
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getCurrentStepCount() {
        return currentStepCount;
    }

    public void setCurrentStepCount(int currentStepCount) {
        this.currentStepCount = currentStepCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if ("null".equals(name)) {
            this.name = null; // TODO: Debug to find out where is time to time "null" from
        }
    }

    public int getTotalStepCount() {
        return totalStepCount;
    }

    public void setTotalStepCount(int totalStepCount) {
        this.totalStepCount = totalStepCount;
    }

    public Set<ChildProgressStatusDTO> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "ProgressStatusDTO{" + "name=" + name + ", id=" + id + ", totalStepCount=" + totalStepCount + ", currentStepCount=" + currentStepCount
                + ", completed=" + completed + ", children=" + children + '}';
    }

}
