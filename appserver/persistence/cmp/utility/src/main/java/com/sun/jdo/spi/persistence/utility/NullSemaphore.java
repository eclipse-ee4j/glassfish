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

package com.sun.jdo.spi.persistence.utility;

import com.sun.jdo.spi.persistence.utility.logging.Logger;

/** Implements a simple semaphore that does not do <em>any</em>
 * semaphore-ing.  That is, the methods just immediately return.
 *
 * @author Dave Bristor
 */
// db13166: I would rather we use Doug Lea's stuff, but don't want to
// introduce that magnitude of change at this point in time.
public class NullSemaphore implements Semaphore {
    /** Where to log messages about locking operations
     */
    private static final Logger _logger = LogHelperUtility.getLogger();

    /** For logging, indicates on whose behalf locking is done.
     */
    private final String _owner;

    public NullSemaphore(String owner) {
        _owner = owner;

        if (_logger.isLoggable(Logger.FINEST)) {
            Object[] items = new Object[] {_owner};
            _logger.finest("utility.nullsemaphore.constructor",items); // NOI18N
        }
    }

    /** Does nothing.
     */
    public void acquire() {

        if (_logger.isLoggable(Logger.FINEST)) {
            Object[] items = new Object[] {_owner};
            _logger.finest("utility.nullsemaphore.acquire",items); // NOI18N
        }
    }

    /** Does nothing.
     */
    public void release() {

        if (_logger.isLoggable(Logger.FINEST)) {
            Object[] items = new Object[] {_owner};
            _logger.finest("utility.nullsemaphore.release",items); // NOI18N
        }
    }
}
