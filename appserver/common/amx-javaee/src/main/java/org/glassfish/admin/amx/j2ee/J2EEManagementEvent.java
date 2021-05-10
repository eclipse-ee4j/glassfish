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

package org.glassfish.admin.amx.j2ee;

/**
 */
public interface J2EEManagementEvent {

    /**
     * The name of the managed object that generated this event.
     *
     * @return the ObjectName of the object, as a String
     */
    String getSource();


    /**
     * The time of the event represented as a long, whose value is
     * the number of milliseconds since January 1, 1970, 00:00:00.
     */
    long getWhen();


    /**
     * The sequence number of the event.
     * Identifies the position of the event in a stream
     * of events. The sequence number provides a means of
     * determining the order of sequential events that
     * occurred with the same timestamp (within the
     * minimum supported unit of time).
     */
    long getSequence();


    /**
     * The type of the event. State manageable objects generate a
     * J2EEEvent object with the type attribute set to "STATE"
     * whenever they reach the RUNNING, STOPPED or FAILED states.
     */
    String getType();


    /**
     * An informational message about the event.
     */
    String getMessage();
}
