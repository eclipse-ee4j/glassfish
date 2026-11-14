/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.orb.http.glassfish;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import jakarta.resource.spi.XATerminator;

import java.lang.reflect.Proxy;
import java.util.List;

import javax.transaction.xa.Xid;

/**
 * A transaction manager and terminator that record what they are asked.
 * <p>
 * Test scaffolding for the bridge: the manager answers nothing except its
 * terminator, and the terminator answers every vote with {@code XA_OK}.
 */
final class TerminatorRecorder {

    private TerminatorRecorder() {
    }

    /**
     * @param terminatorCalls receives the name of every terminator call, in order
     * @param managerCalls receives the name of every call on the manager, in order
     * @param listed what the terminator answers to {@code recover}
     * @return a manager to hand to the bridge
     */
    static JavaEETransactionManager manager(List<String> terminatorCalls, List<String> managerCalls,
            Xid... listed) {
        XATerminator terminator = (XATerminator) Proxy.newProxyInstance(
                TerminatorRecorder.class.getClassLoader(), new Class<?>[] { XATerminator.class },
                (proxy, method, args) -> {
                    terminatorCalls.add(method.getName());
                    if (method.getName().equals("recover")) {
                        return listed;
                    }
                    return method.getReturnType() == int.class ? Integer.valueOf(0) : null;
                });
        return (JavaEETransactionManager) Proxy.newProxyInstance(
                TerminatorRecorder.class.getClassLoader(), new Class<?>[] { JavaEETransactionManager.class },
                (proxy, method, args) -> {
                    managerCalls.add(method.getName());
                    return method.getName().equals("getXATerminator") ? terminator : null;
                });
    }
}
