/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.domain;

import java.util.Properties;

import org.testng.annotations.Test;

import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainException;

public class TestDomainPortValidator {

    private DomainPortValidator _portValidator = null;

    @Test (expectedExceptions = DomainException.class)
    public void testForNullPorts() throws Exception {
        DomainConfig domainConfig = new DomainConfig("test", null);
        domainConfig.add(DomainConfig.K_VALIDATE_PORTS, Boolean.TRUE);
        _portValidator = new DomainPortValidator(domainConfig, new Properties());
        _portValidator.validateAndSetPorts();
    }

    @Test (expectedExceptions = DomainException.class)
    public void testForNonNumericPort() throws Exception {
        DomainConfig domainConfig = new DomainConfig("test", null);
        domainConfig.add(DomainConfig.K_VALIDATE_PORTS, Boolean.TRUE);
        domainConfig.add(DomainConfig.K_ADMIN_PORT, "admin2");
        _portValidator = new DomainPortValidator(domainConfig, new Properties());
        _portValidator.validateAndSetPorts();
    }

    @Test (expectedExceptions = DomainException.class)
    public void testForNegativePort() throws Exception {
        DomainConfig domainConfig = new DomainConfig("test", null);
        domainConfig.add(DomainConfig.K_VALIDATE_PORTS, Boolean.TRUE);
        domainConfig.add(DomainConfig.K_ADMIN_PORT, "-2");
        _portValidator = new DomainPortValidator(domainConfig, new Properties());
        _portValidator.validateAndSetPorts();
    }

    @Test (expectedExceptions = DomainException.class)
    public void testForPortValueZero() throws Exception {
        DomainConfig domainConfig = new DomainConfig("test", null);
        domainConfig.add(DomainConfig.K_VALIDATE_PORTS, Boolean.TRUE);
        domainConfig.add(DomainConfig.K_ADMIN_PORT, "0");
        _portValidator = new DomainPortValidator(domainConfig, new Properties());
        _portValidator.validateAndSetPorts();
    }

    @Test (expectedExceptions = DomainException.class)
    public void testForMaxPort() throws Exception {
        DomainConfig domainConfig = new DomainConfig("test", null);
        domainConfig.add(DomainConfig.K_VALIDATE_PORTS, Boolean.TRUE);
        domainConfig.add(DomainConfig.K_ADMIN_PORT, String.valueOf((DomainPortValidator.PORT_MAX_VAL + 1)));
        _portValidator = new DomainPortValidator(domainConfig, new Properties());
        _portValidator.validateAndSetPorts();
    }
}
