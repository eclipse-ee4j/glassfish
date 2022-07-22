/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul;

import java.util.logging.Logger;

/**
 * This enum represents states of the GlassFish Logging lifecycle.
 *
 * @author David Matejcek
 */
public enum GlassFishLoggingStatus {
    /**
     * The logging is not available, requires initialization first.
     * The initialization usually starts by the first usage of any {@link Logger} instance
     * or by the first LogManager.getManager call.
     */
    UNINITIALIZED,
    /**
     * The initialization is done - the global LogManager instance is set and cannot be changed
     * any more, but it's configuration is not completed.
     */
    UNCONFIGURED,
    /**
     * GlassFish Logging reconfiguration is executed.
     * <p>
     * This part is extremely fragile - if your logging configuration is incorrect, it is not
     * guaranteed that the logging will work.
     */
    CONFIGURING,
    /**
     * The reconfiguration is finished.
     * <p>
     * Now it is time to flush all buffers in logging.
     */
    FLUSHING_BUFFERS,
    /**
     * Logging is configured and provides full service.
     */
    FULL_SERVICE,
    ;
}
