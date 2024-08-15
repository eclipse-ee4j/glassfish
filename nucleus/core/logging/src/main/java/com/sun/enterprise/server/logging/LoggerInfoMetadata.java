/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging;

import java.util.Locale;
import java.util.Set;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface LoggerInfoMetadata {

    /**
     * Provides a set of logger names for which additional metadata is available.
     * @return
     */
    public Set<String> getLoggerNames();

    /**
     * Gets the description for the named logger.
     * @return
     */
    public String getDescription(String logger);

    /**
     * Get description for the named logger for given locale
     * @param logger
     * @param locale
     * @return
     */
    public String getDescription(String logger, Locale locale);

    /**
     * Gets the subsystem grouping to which a specified logger belongs.
     * @param logger
     * @return
     */
    public String getSubsystem(String logger);

    /**
     * Gets whether the specified logger is to be included in the public doc.
     * @param logger
     * @return <code>true</code> if the logger is to be documented otherwise
     * <code>false</code> for internal fine-grained loggers.
     */
    public boolean isPublished(String logger);

}
