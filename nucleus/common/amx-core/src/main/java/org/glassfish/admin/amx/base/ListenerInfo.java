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

package org.glassfish.admin.amx.base;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
Provide information about who's listening.
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
public interface ListenerInfo
{
    /**
    Get the number of listeners which are listening for the
    specified type of Notification.  If there are anonymous listeners (those
    that have no filter or a non-standard filter) then this routine will
    return the value 1.

    @param notificationType  any Notification type, should usually be one advertised via MBeanInfo
    @return count of listeners
     */
    public int getNotificationTypeListenerCount(final String notificationType);

    /**
    Get the total number of listeners listening for anything.

    @return count of listeners
     */
    public int getListenerCount();

}
