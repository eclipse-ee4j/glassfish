/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.auth;

import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 * validate Timestamp received in messages.
 *
 * @author k.venugopal@sun.com
 */
public interface TimestampValidator {

    /**
     * validate given time against current time.
     *
     * @param created created time
     * @param maxClockSkew maximum difference allowed between the system clocks of the sender and recipient.
     * @param freshnessLimit maximum duration of time after which the Timestamp becomes stale
     * @throws java.util.concurrent.TimeoutException
     */
    void validate(Date created, long maxClockSkew, long freshnessLimit) throws TimeoutException;
}
