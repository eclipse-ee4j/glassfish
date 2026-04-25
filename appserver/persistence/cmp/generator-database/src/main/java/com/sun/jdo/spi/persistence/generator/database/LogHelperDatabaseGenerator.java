/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.jdo.spi.persistence.generator.database;

/**
 *
 * @author Jie Leng 2003
 */
class LogHelperDatabaseGenerator {

    /** The component name for this component */
    private static final String componentName = "databaseGenerator";

    /** The class loader for this component */
    private static final ClassLoader loader =
            LogHelperDatabaseGenerator.class.getClassLoader();

    /** The bundle name for this component */
    private static final String bundleName =
            "com.sun.jdo.spi.persistence.generator.database.Bundle";

}
