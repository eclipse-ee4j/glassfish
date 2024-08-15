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
 * LogHelperModel.java
 *
 * Created on May 28, 2002, 5:00 PM
 */

package com.sun.jdo.api.persistence.model.util;

import com.sun.jdo.api.persistence.model.Model;
import com.sun.jdo.spi.persistence.utility.logging.LogHelper;
import com.sun.jdo.spi.persistence.utility.logging.Logger;

/**
 *
 * @author Rochelle Raccah
 * @version %I%
 */
public class LogHelperModel
{
    /** The component name for this component
     */
    private static final String _componentName = "model"; // NOI18N

    /** The class loader for this component
     */
    private static final ClassLoader _loader =
        LogHelperModel.class.getClassLoader();

    /** Return the logger for the model component
     */
    public static Logger getLogger ()
    {
        return LogHelper.getLogger (_componentName, Model.messageBase, _loader);
    }

}
