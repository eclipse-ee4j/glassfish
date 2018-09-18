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

package com.sun.enterprise.tools.verifier;

import java.util.EventObject;

/**
 * <p/>
 * This listener interface is used for tests that need to be notified of the
 * verifier execution life cycle
 * </p>
 *
 * @author Jerome Dochez
 */
public interface VerifierEventsListener {

    /**
     * <p/>
     * Individual test completion notification event
     * </p>
     *
     * @param e event object which source is the result of the individual test
     */
    void testFinished(EventObject e);

    /**
     * <p/>
     * Notification that all tests pertinent to a verifier check manager have
     * been completed
     * </p>
     *
     * @param e event object which source is the check manager for the
     * completed tests
     */
    void allTestsFinished(EventObject e);
}
