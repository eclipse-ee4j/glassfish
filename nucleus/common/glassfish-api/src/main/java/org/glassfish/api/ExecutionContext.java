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

import java.util.logging.Logger;

/**
 * Any execution type invocation should be passed with an instance of this context. Sub-classes should add specialized
 * information required by the operation type.
 *
 * @author Jerome Dochez
 */
public interface ExecutionContext {

    /**
     * Returns the logger services implementation should use to log useful information about their execution.
     *
     * @retun the services logger
     */
    Logger getLogger();

}
