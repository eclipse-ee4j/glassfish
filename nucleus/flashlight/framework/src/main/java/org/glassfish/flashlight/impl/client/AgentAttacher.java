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

package org.glassfish.flashlight.impl.client;

/**
 * created May 26, 2011
 * @author Byron Nevins
 */
public final class AgentAttacher {
    public synchronized static boolean canAttach() {
        return canAttach;
    }

    public synchronized static boolean isAttached() {
        try {
            if (!canAttach)
                return false;

            return AgentAttacherInternal.isAttached();
        }
        catch (Throwable t) {
            return false;
        }
    }

    public synchronized static boolean attachAgent() {

        try {
            if (!canAttach)
                return false;

            return attachAgent(-1, "");
        }
        catch (Throwable t) {
            return false;
        }
    }

    public synchronized static boolean attachAgent(int pid, String options) {
        try {
            if (!canAttach)
                return false;

            return AgentAttacherInternal.attachAgent(pid, options);
        }
        catch (Throwable t) {
            return false;
        }
    }

    private final static boolean canAttach;

    static {
        boolean b = false;
        try {
            // this will cause a class not found error if tools.jar is missing
            // this is a distinct possibility in embedded mode.
            AgentAttacherInternal.isAttached();
            b = true;
        }
        catch (Throwable t) {
            b = false;
        }
        canAttach = b;
    }
}
