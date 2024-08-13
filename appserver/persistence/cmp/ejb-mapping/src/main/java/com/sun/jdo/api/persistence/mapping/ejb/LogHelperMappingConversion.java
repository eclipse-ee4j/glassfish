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

/*
 * LogHelperMappingConversion.java
 *
 * Created on June 9, 2004
 */


package com.sun.jdo.api.persistence.mapping.ejb;

import com.sun.jdo.spi.persistence.utility.logging.LogHelper;
import com.sun.jdo.spi.persistence.utility.logging.Logger;

/**
 *
 * @author Jie Leng
 */
class LogHelperMappingConversion {

    /** The component name for this component */
    private static final String componentName = "mapping.conversion"; // NOI18N

    /** The class loader for this component */
    private static final ClassLoader loader =
            LogHelperMappingConversion.class.getClassLoader();

    /** The bundle name for this component */
    private static final String bundleName =
            "com.sun.jdo.api.persistence.mapping.ejb.Bundle"; // NOI18N

    /**
     * @return The logger for the mapping generator component.
     */
    static Logger getLogger() {
        return LogHelper.getLogger(componentName, bundleName, loader);
    }
}
