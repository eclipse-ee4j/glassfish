/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.web.pluggable;

import com.sun.enterprise.web.EmbeddedWebContainer;
import com.sun.enterprise.web.HealthChecker;
import com.sun.enterprise.web.ReplicationReceiver;
import com.sun.enterprise.web.SSOFactory;
import com.sun.enterprise.web.VirtualServer;
import com.sun.enterprise.web.WebContainer;
import com.sun.enterprise.web.WebContainerStartStopOperation;

import org.jvnet.hk2.annotations.Contract;

/**
 * Interface for getting webcontainer specific pluggable features.
 */
@Contract
public interface WebContainerFeatureFactory {

    WebContainerStartStopOperation getWebContainerStartStopOperation();

    HealthChecker getHADBHealthChecker(WebContainer webContainer);

    ReplicationReceiver getReplicationReceiver(EmbeddedWebContainer embedded);

    VirtualServer getVirtualServer();

    SSOFactory getSSOFactory();

    String getSSLImplementationName();

    /**
     * Gets the default access log file prefix.
     *
     * @return The default access log file prefix
     */
    String getDefaultAccessLogPrefix();

    /**
     * Gets the default access log file suffix.
     *
     * @return The default access log file suffix
     */
    String getDefaultAccessLogSuffix();

    /**
     * Gets the default datestamp pattern to be applied to access log files.
     *
     * @return The default datestamp pattern to be applied to access log files
     */
    String getDefaultAccessLogDateStampPattern();

    /**
     * Returns true if the first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation.
     *
     * @return true if first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation.
     */
    boolean getAddDateStampToFirstAccessLogFile();

    /**
     * Gets the default rotation interval in minutes.
     *
     * @return The default rotation interval in minutes
     */
    int getDefaultRotationIntervalInMinutes();
}
