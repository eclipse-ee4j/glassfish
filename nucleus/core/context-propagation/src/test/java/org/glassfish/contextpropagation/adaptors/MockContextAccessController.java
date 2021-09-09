/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.contextpropagation.adaptors;

import org.glassfish.contextpropagation.bootstrap.ContextAccessController;
import org.glassfish.contextpropagation.internal.AccessControlledMap;
import org.glassfish.contextpropagation.internal.AccessControlledMap.ContextAccessLevel;

public class MockContextAccessController extends ContextAccessController {

    @Override
    public boolean isAccessAllowed(String key, AccessControlledMap.ContextAccessLevel type) {
        if (type == ContextAccessLevel.READ && isEveryoneAllowedToRead(key)) {
            return true; // First do a quick check for read access
        }
        return true;
    }


    @Override
    public boolean isEveryoneAllowedToRead(String key) {
        return false;
    }

}
