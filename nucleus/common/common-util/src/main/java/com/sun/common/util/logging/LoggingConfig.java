/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.common.util.logging;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.jvnet.hk2.annotations.Contract;

/**
 * Interface for Logging Commands
 *
 * @author Naman Mehta
 */
@Contract
public interface LoggingConfig {

    /**
     * Set propertyName to the propertyValue.
     * <p>
     * The logManager readConfiguration is not called in this method.
     */
    String setLoggingProperty(String propertyName, String propertyValue) throws IOException;

    /**
     * Set propertyName to the propertyValue.
     * <p>
     * The logManager readConfiguration is not called in this method.
     */
    String setLoggingProperty(String propertyName, String propertyValue, String targetServer) throws IOException;

    /**
     * Update the properties to new values. properties is a Map of names of properties and
     * their cooresponding value. If the property does not exist then it is added to the
     * logging.properties file.
     * The readConfiguration method is called on the logManager after updating the properties.
     */
    Map<String, String> updateLoggingProperties(Map<String, String> properties) throws IOException;

    /**
     * Update the properties to new values for given target. properties is a Map of names of
     * properties and their coresponding value. If the property does not exist then it is added to
     * the logging.properties file.
     *
     * @param properties Map of the name and value of property to add or update
     * @throws IOException
     */
    Map<String, String> updateLoggingProperties(Map<String, String> properties, String targetServer) throws IOException;

    /**
     * @return the properties and corresponding values in the logging.properties file for given
     *         target server.
     */
    Map<String, String> getLoggingProperties(String targetServer) throws IOException;

    /**
     * @return the properties and corresponding values in the logging.properties file.
     */
    Map<String, String> getLoggingProperties() throws IOException;

    /**
     * Creates zip file for given sourceDirectory
     */
    String createZipFile(String sourceDir) throws IOException;

    /**
     * Delete the properties from logging.properties file.
     *
     * @throws IOException
     */
    Map<String, String> deleteLoggingProperties(Collection<String> properties) throws IOException;

    /**
     * Delete the properties from logging.properties file.
     *
     * @throws IOException
     */
    Map<String, String> deleteLoggingProperties(Collection<String> properties, String target) throws IOException;
}
