/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.beans.PropertyChangeEvent;

/**
    Carries the PropertyChangeEvent and the reason it could not be processed.
 */
public final class UnprocessedChangeEvent {
    final PropertyChangeEvent mEvent;
    final String              mReason;
    final long                mWhen;

    public UnprocessedChangeEvent(final PropertyChangeEvent event, final String reason ) {
        mEvent  = event;
        mReason = reason;
        mWhen = System.currentTimeMillis();
    }
    public String getReason()             { return mReason; }
    public PropertyChangeEvent getEvent() { return mEvent; }
    public long getWhen()                 { return mWhen; }


    private static String toString( final PropertyChangeEvent e ) {
        return "PropertyName=" + e.getPropertyName() + ", OldValue = " + e.getOldValue() +
            ", NewValue = " + e.getNewValue() + ", Source = " + e.getSource();
    }

    public String toString() {
        return "UnprocessedChangeEvent{" + toString(mEvent) +
                "}, reason = " + mReason +
                ", when = " + getWhen();
    }
}
