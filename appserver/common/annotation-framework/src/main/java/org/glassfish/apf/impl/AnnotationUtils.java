/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.apf.impl;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LoggerInfo;

/**
 * Bag for utility methods
 *
 * @author Jerome Dochez
 */
public class AnnotationUtils {

    @LoggerInfo(subsystem = "AnnotationFramework", description = "Logger for dol module", publish = true)
    private static final String ANNOTATION_LOGGER = "org.glassfish.apf";

    private static final Logger LOG = Logger.getLogger(ANNOTATION_LOGGER);

    public static Logger getLogger() {
        return LOG;
    }
}
