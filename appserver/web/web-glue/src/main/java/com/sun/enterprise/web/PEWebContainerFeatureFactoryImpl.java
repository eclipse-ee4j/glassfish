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

package com.sun.enterprise.web;

import com.sun.enterprise.web.pluggable.WebContainerFeatureFactory;

import org.jvnet.hk2.annotations.Service;

/**
 * Implementation of WebContainerFeatureFactory which returns web container
 * feature implementations for PE.
 */
@Service(name="pe")
public class PEWebContainerFeatureFactoryImpl
        implements WebContainerFeatureFactory {

    @Override
    public WebContainerStartStopOperation getWebContainerStartStopOperation() {
        return new PEWebContainerStartStopOperation();
    }

    @Override
    public HealthChecker getHADBHealthChecker(WebContainer webContainer) {
        return new PEHADBHealthChecker(webContainer);
    }

    @Override
    public ReplicationReceiver getReplicationReceiver(EmbeddedWebContainer embedded) {
        return new PEReplicationReceiver(embedded);
    }

    @Override
    public SSOFactory getSSOFactory() {
        return new PESSOFactory();
    }

    @Override
    public VirtualServer getVirtualServer() {
        return new VirtualServer();
    }

    @Override
    public String getSSLImplementationName(){
        return null;
    }

    /**
     * Gets the default access log file prefix.
     *
     * @return The default access log file prefix
     */
    @Override
    public String getDefaultAccessLogPrefix() {
        return "_access_log.";
    }

    /**
     * Gets the default access log file suffix.
     *
     * @return The default access log file suffix
     */
    @Override
    public String getDefaultAccessLogSuffix() {
        return ".txt";
    }

    /**
     * Gets the default datestamp pattern to be applied to access log files.
     *
     * @return The default datestamp pattern to be applied to access log files
     */
    @Override
    public String getDefaultAccessLogDateStampPattern() {
        return "yyyy-MM-dd";
    }

    /**
     * Returns true if the first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation.
     *
     * @return true if first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation.
     */
    @Override
    public boolean getAddDateStampToFirstAccessLogFile() {
        return true;
    }

    /**
     * Gets the default rotation interval in minutes.
     *
     * @return The default rotation interval in minutes
     */
    @Override
    public int getDefaultRotationIntervalInMinutes() {
        return 15;
    }
}
