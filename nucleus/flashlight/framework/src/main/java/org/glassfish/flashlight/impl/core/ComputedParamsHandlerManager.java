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

package org.glassfish.flashlight.impl.core;

import java.util.ArrayList;
import java.util.List;

import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 */
@Service
public class ComputedParamsHandlerManager {

    private static ComputedParamsHandlerManager _me = new  ComputedParamsHandlerManager();

    List<ComputedParamHandler> handlers = new ArrayList<ComputedParamHandler>();

    private ComputedParamsHandlerManager() {}

    public static ComputedParamsHandlerManager getInstance() {
        return _me;
    }

    public synchronized void addComputedParamHandler(ComputedParamHandler handler) {
        handlers.add(handler);
    }

    public Object computeValue(String param) {
        Object value = null;
        for (ComputedParamHandler h : handlers) {
            if (h.canHandle(param)) {
                value = h.compute(param);
                break;
            }
        }
        return value;
    }
}
