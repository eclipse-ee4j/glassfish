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

package org.glassfish.loadbalancer.admin.cli.reader.impl;

import com.sun.enterprise.config.serverbeans.HealthChecker;

import org.glassfish.loadbalancer.admin.cli.LbLogUtil;
import org.glassfish.loadbalancer.admin.cli.reader.api.HealthCheckerReader;
import org.glassfish.loadbalancer.admin.cli.reader.api.LbReaderException;
import org.glassfish.loadbalancer.admin.cli.transform.HealthCheckerVisitor;
import org.glassfish.loadbalancer.admin.cli.transform.Visitor;

/**
 * Provides health checker information relavant to Load balancer tier.
 *
 * @author Kshitiz Saxena
 */
public class HealthCheckerReaderImpl implements HealthCheckerReader {

    static HealthCheckerReader getDefaultHealthChecker() {
        return defaultHCR;
    }

    public HealthCheckerReaderImpl() {
        _hc = null;
    }

    /**
     * Constructor
     */
    public HealthCheckerReaderImpl(HealthChecker hc) {
        if (hc == null) {
            String msg = LbLogUtil.getStringManager().getString("ConfigBeanAndNameNull");
            throw new IllegalArgumentException(msg);
        }
        _hc = hc;
    }

    /**
     * Return health checker url
     *
     * @return String           health checker url, it shoudld conform to
     *                          RFC 2396. java.net.URI.resolve(url) shoudl
     *                          return a valid URI.
     */
    @Override
    public String getUrl() throws LbReaderException {
        if (_hc == null) {
            return defaultURL;
        }
        return _hc.getUrl();
    }

    /**
     * Health checker runs in the specified interval time.
     *
     * @return String           value must be > 0
     */
    @Override
    public String getIntervalInSeconds() throws LbReaderException {
        if (_hc == null) {
            return defaultInterval;
        }
        return _hc.getIntervalInSeconds();
    }

    /**
     *  Timeout where a server is considered un healthy.
     *
     * @return String           value must be > 0
     */
    @Override
    public String getTimeoutInSeconds() throws LbReaderException {
        if (_hc == null) {
            return defaultTimeout;
        }
        return _hc.getTimeoutInSeconds();
    }

    // --- VISITOR IMPLEMENTATION ---
    @Override
    public void accept(Visitor v) throws Exception {
        if (v instanceof HealthCheckerVisitor) {
            HealthCheckerVisitor pv = (HealthCheckerVisitor) v;
            pv.visit(this);
        }
    }
    //--- PRIVATE VARIABLES ------
    HealthChecker _hc = null;
    private static final HealthCheckerReader defaultHCR =
            new HealthCheckerReaderImpl();
    private static final String defaultURL = "/";
    private static final String defaultInterval = "10";
    private static final String defaultTimeout = "30";
}
