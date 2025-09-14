/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.util.io.ServerDirs;

import java.util.Map;

/**
 * Represents an abstract Service. This interface defines sufficient methods for any platform integration of application
 * server with various service control mechanisms on various platforms.
 *
 * @since SJSAS 9.1
 * @see #isConfigValid
 * @author Kedar Mhaswade
 */
public interface Service {

    /**
     * get the dirs with this thread-safe immutable guaranteed object. It saves a LOT of error checking... You should set
     * the variable in the constructor. You are not allowed to change it later
     *
     * @param dirs
     */
    ServerDirs getServerDirs();

    /**
     * Returns the additional properties of the Service.
     *
     * @return String representing addtional properties of the service. May return default properties as well.
     */
    String getServiceProperties();

    /**
     * Sets the additional service properties that are specific to it.
     *
     * @param must be a colon separated String, if not null. No effect, if null is passed.
     */
    void setServiceProperties(String cds);

    /**
     * Returns the tokens and values of the service as a map. This method converts a service into corresponding tokens and
     * their values.
     *
     * @return tokens and values as a Map<String, String>.
     */
    Map<String, String> tokensAndValues();

    /**
     * Creates an arbitrary service, specified by certain parameters. The implementations should dictate the mappings in the
     * parameters received. The creation of service is either successful or not. In other words, the implementations must
     * retain the original state of the operating platform if the service creation is not successful completely.
     *
     * @param params a Map between Strings that represents the name value pairs required to create the service
     * @throws RuntimeException if there is any error is creation of service
     */
    void createService();

    String getSuccessMessage();

    void writeReadmeFile(String msg);

    String getLocationArgsStart();

    String getLocationArgsRestart();

    String getLocationArgsStop();

    boolean isDomain();

    boolean isInstance();

    PlatformServicesInfo getInfo();

    void initializeInternal();

    void createServiceInternal();

    void deleteService();

    void deleteServiceInternal();
}
