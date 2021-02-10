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

import org.glassfish.api.admin.ProgressStatus;

/**
 * This implementation is used for modeling of command execution with supplemental commands. It only mirrors all sizes
 * (total steps and current steps from its children.
 *
 *
 * @author mmares
 */
//TODO: Move to kernel if possible. It is now in API only because ProgressStatusImpl is here, too
public class ProgressStatusMirroringImpl extends ProgressStatusBase {

    // private Collection<ProgressStatusBase> mirroreds = new ArrayList<ProgressStatusBase>();

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ProgressStatusMirroringImpl(String name, ProgressStatusBase parent, String id) {
        super(name, -1, parent, id);
    }

    @Override
    protected ProgressStatusBase doCreateChild(String name, int totalStepCount) {
        String childId = (id == null ? "" : id) + "." + (children.size() + 1);
        return new ProgressStatusImpl(name, totalStepCount, this, childId);
    }

    @Override
    public void setTotalStepCount(int totalStepCount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void progress(int steps, String message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCurrentStepCount(int stepCount) {
        throw new UnsupportedOperationException();
    }

    /**
     * Ignores allocated steps. It's mirroring implementation.
     */
    @Override
    public synchronized ProgressStatus createChild(String name, int allocatedSteps, int totalStepCount) {
        ProgressStatusBase result = doCreateChild(name, totalStepCount);
        children.add(new ChildProgressStatus(allocatedSteps, result));
        fireEvent(new ProgressStatusEventCreateChild(id, name, result.getId(), 0, totalStepCount));
        return result;
    }

    @Override
    protected synchronized void fireEvent(ProgressStatusEvent event) {
        recount();
        super.fireEvent(event);
    }

    private void recount() {
        int newTotalStepCount = 0;
        boolean unknown = false;
        int newCurrentStepCount = 0;
        for (ChildProgressStatus child : children) {
            ProgressStatusBase mirr = child.getProgressStatus();
            if (!unknown) {
                int tsc = mirr.getTotalStepCount();
                if (tsc < 0) {
                    unknown = true;
                    newTotalStepCount = -1;
                } else {
                    newTotalStepCount += tsc;
                }
            }
            newCurrentStepCount += mirr.getCurrentStepCount();
        }
        // Event
        ProgressStatusEventSet event = new ProgressStatusEventSet(id);
        if (newCurrentStepCount != currentStepCount) {
            currentStepCount = newCurrentStepCount;
            event.setCurrentStepCount(currentStepCount);
        }
        if (newTotalStepCount != totalStepCount) {
            totalStepCount = newTotalStepCount;
            event.setTotalStepCount(totalStepCount);
        }
        if (event.getCurrentStepCount() != null || event.getTotalStepCount() != null) {
            super.fireEvent(event);
        }
    }

    @Override
    protected synchronized float computeCompleteSteps() {
        return currentStepCount;
    }

    @Override
    public synchronized float computeCompletePortion() {
        if (isComplete()) {
            return 1;
        }
        if (totalStepCount == 0) {
            if (currentStepCount == 0) {
                return 0;
            } else {
                return 1;
            }
        }
        return (float) currentStepCount / (float) totalStepCount;
    }

}
