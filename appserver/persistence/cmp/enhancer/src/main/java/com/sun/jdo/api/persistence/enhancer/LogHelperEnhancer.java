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

package com.sun.jdo.api.persistence.enhancer;

import com.sun.jdo.spi.persistence.utility.logging.LogHelper;
import com.sun.jdo.spi.persistence.utility.logging.Logger;

/**
 *
 * @author  Piali Nag
 */
public class LogHelperEnhancer {

    /** The component name for this component
     */
    protected static final String componentName = "enhancer"; // NOI18N

    /** The class loader for this component
     */
    protected static final ClassLoader loader =
        LogHelperEnhancer.class.getClassLoader();

    /** The bundle name for this component
     */
    protected static final String bundleName =
        "com.sun.jdo.api.persistence.enhancer.Bundle"; // NOI18N

    /** Return the logger for the enhancer component
     */
    public static Logger getLogger() {
        return LogHelper.getLogger (componentName, bundleName, loader);
    }

}
