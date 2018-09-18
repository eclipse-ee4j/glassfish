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

package org.glassfish.flashlight.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author Mahesh Kannan
 * @author Byron Nevins
 * Created: May 30, 2008
 */

public class ProbeAgentMain {

    public static void agentmain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }
    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
    private static Instrumentation instrumentation;
}

