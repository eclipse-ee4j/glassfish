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

package org.glassfish.api;

import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Contract;

/**
 * Do NOT use this interface anymore, it no longer does anything
 *
 * @deprecated Use the {@link RunLevel} annotation on the Service instead of implementing this interface.
 *
 * @author Jerome Dochez
 */
@Deprecated
@Contract
public interface Startup {

    /**
     * A startup service may be useful during the lifetime of the application server, while others need to process a task
     * and stop running at the end of the server startup. A startup service should indicate if it needs to be running during
     * the START sequence only or during the SERVER lifetime.
     */
    public enum Lifecycle {
        START, SERVER
    }

    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    Lifecycle getLifecycle();
}
