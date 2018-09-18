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

package com.sun.enterprise.deployment.runtime.ws;

/**
 * @author Rama Pulavarthi
 */
public class ReliabilityConfig {
    String inactivityTimeout;
    String baseRetransmissionInterval;
    String retransmissionExponentialBackoff;
    String acknowledgementInterval;
    String sequenceExpiration;
    String bufferRetryCount;
    String bufferRetryDelay;

    public String getInactivityTimeout() {
        return inactivityTimeout;
    }

    public void setInactivityTimeout(String inactivityTimeout) {
        this.inactivityTimeout = inactivityTimeout;
    }

    public String getBaseRetransmissionInterval() {
        return baseRetransmissionInterval;
    }

    public void setBaseRetransmissionInterval(String baseRetransmissionInterval) {
        this.baseRetransmissionInterval = baseRetransmissionInterval;
    }

    public String getRetransmissionExponentialBackoff() {
        return retransmissionExponentialBackoff;
    }

    public void setRetransmissionExponentialBackoff(String retransmissionExponentialBackoff) {
        this.retransmissionExponentialBackoff = retransmissionExponentialBackoff;
    }

    public String getAcknowledgementInterval() {
        return acknowledgementInterval;
    }

    public void setAcknowledgementInterval(String acknowledgementInterval) {
        this.acknowledgementInterval = acknowledgementInterval;
    }

    public String getSequenceExpiration() {
        return sequenceExpiration;
    }

    public void setSequenceExpiration(String sequenceExpiration) {
        this.sequenceExpiration = sequenceExpiration;
    }

    public String getBufferRetryCount() {
        return bufferRetryCount;
    }

    public void setBufferRetryCount(String bufferRetryCount) {
        this.bufferRetryCount = bufferRetryCount;
    }

    public String getBufferRetryDelay() {
        return bufferRetryDelay;
    }

    public void setBufferRetryDelay(String bufferRetryDelay) {
        this.bufferRetryDelay = bufferRetryDelay;
    }

}
