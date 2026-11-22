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


package org.glassfish.orb.http.server;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;

/** Test support: whether anything a client would be sent is a container or CORBA type. */
final class InternalTypes {

    private InternalTypes() {
    }

    /** @return whether the throwable, its causes or its suppressed exceptions include such a type */
    static boolean inChain(Throwable root) {
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Throwable> pending = new ArrayDeque<>();
        pending.add(root);
        while (!pending.isEmpty()) {
            Throwable t = pending.poll();
            if (!seen.add(t)) {
                continue;
            }
            String name = t.getClass().getName();
            if (name.startsWith("org.omg.") || name.startsWith("com.sun.")) {
                return true;
            }
            if (t.getCause() != null) {
                pending.add(t.getCause());
            }
            pending.addAll(Arrays.asList(t.getSuppressed()));
        }
        return false;
    }
}
