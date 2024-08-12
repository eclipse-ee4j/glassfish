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

package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.util.io.ServerDirs;

import java.util.Map;

/**
 * Represents an abstract Service. This interface defines sufficient methods for any platform integration of application
 * server with various service control mechanisms on various platforms. An example is SMF for Solaris.
 *
 * @since SJSAS 9.1
 * @see #isConfigValid
 * @see ServiceHandler
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

    int getTimeoutSeconds();

    /**
     * Sets timeout in seconds before the master boot restarter should give up starting this service.
     *
     * @param number a non-negative integer representing timeout. A value of zero implies infinite timeout.
     */
    void setTimeoutSeconds(final int number);

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
    void setServiceProperties(final String cds);

    /**
     * Determines if the configuration of the method is valid. When this class is constructed, appropriate defaults are
     * used. But before attempting to create the service in the Solaris platform, it is important that the necessary
     * configuration is done by the users via various mutator methods of this class. This method must be called to guard
     * against some abnormal failures before creating the service. It makes sure that the caller has set all the necessary
     * parameters reasonably. Note that it does not validate the actual values.
     *
     * @throws RuntimeException if the configuration is not valid
     * @return true if the configuration is valid, an exception is thrown otherwise
     */
    boolean isConfigValid();

    /**
     * Returns the tokens and values of the service as a map. This method converts a service into corresponding tokens and
     * their values.
     *
     * @return tokens and values as a Map<String, String>.
     */
    Map<String, String> tokensAndValues();

    /**
     * Returns the absolute location of the manifest file as service understands it. It takes into account the name, type
     * and configuration location of the service. It is expected that these are set before calling this method. If the <b>
     * Fully Qualified Service Name </b> is invalid, a RuntimeException results.
     */
    String getManifestFilePath();

    /**
     * Returns the absolute location of the template for the given service. The type of the service must be set before
     * calling this method, otherwise a runtime exception results.
     */
    String getManifestFileTemplatePath();

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
