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

package org.glassfish.api.admin;

import org.glassfish.api.ActionReport;

/**
 * @author Martin Mares
 * @author Bhakti Mehta
 */
public interface AdminCommandState {

    String EVENT_STATE_CHANGED = "AdminCommandState/stateChanged";

    public enum State {
        /** Created new job, ready to be executed */
        PREPARED,
        /** Started and not completed */
        RUNNING,
        /** Completed successfully or unsuccessfully */
        COMPLETED,
        /**
         * Completed and recorded
         * @deprecated Never set
         */
        RECORDED,
        RUNNING_RETRYABLE,
        FAILED_RETRYABLE,
        REVERTING,
        REVERTED;

        /**
         * Checks if the transition from one state to another is allowed.
         *
         * @param from
         * @param to
         * @return true if yes, false otherwise
         */
        public static boolean isAllowedTransition(State from, State to) {
            switch (from) {
                case PREPARED:
                    return to == RUNNING;
                case RUNNING:
                    return to == COMPLETED || to == FAILED_RETRYABLE || to == REVERTING;
                case COMPLETED:
                    return to == RECORDED;
                case RECORDED:
                    // No further transitions allowed
                    return false;
                case RUNNING_RETRYABLE:
                    return to == COMPLETED || to == FAILED_RETRYABLE || to == REVERTING;
                case FAILED_RETRYABLE:
                    return to == RUNNING_RETRYABLE || to == REVERTING;
                case REVERTING:
                    return to == REVERTED;
                case REVERTED:
                    // No further transitions allowed
                    return false;
                default:
                    throw new IllegalArgumentException("Unknown state: " + from);
            }
        }
    }


    /**
     * @return true if the job is in one of states: PREPARED, RUNNING, RUNNING_RETRYABLE
     */
    default boolean isJobStillActive() {
        return State.PREPARED.equals(getState())
            || State.RUNNING.equals(getState())
            || State.RUNNING_RETRYABLE.equals(getState());
    }

    /**
     * @return current state of the command. Never null.
     */
    State getState();


    /**
     * Action report is created on Job creation and is just updated for its lifetime.
     *
     * @return {@link ActionReport}, never null
     */
    ActionReport getActionReport();

    /**
     * @return true if there are data in outbound payload.
     */
    boolean isOutboundPayloadEmpty();

    /**
     * @return job id. Can be null.
     */
    String getId();


    /**
     * @return job name. Can be null.
     */
    String getName();

}
