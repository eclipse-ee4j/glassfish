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
 * CaptureSchemaWrapper.java
 *
 * Created on October 5, 2004
 */

package com.sun.jdo.spi.persistence.support.ejb.util;

import com.sun.enterprise.util.Utility;
import com.sun.jdo.api.persistence.mapping.ejb.CaptureSchema;

/**
 * This class is used to set the required infrastructure
 * for DataDirect drivers support and delegate the actual implementation
 * to the <code>CaptureSchema<\code>
 * @see CaptureSchema
 *
 * @author  Marina Vatkina
 */

public final class CaptureSchemaWrapper {
    public static void main(String args[]) {
        Utility.setEnvironment();
        CaptureSchema.main(args);
    }
}
