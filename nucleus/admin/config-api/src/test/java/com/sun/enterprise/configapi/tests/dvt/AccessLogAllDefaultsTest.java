/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.configapi.tests.dvt;

import com.sun.enterprise.config.serverbeans.AccessLog;

import jakarta.inject.Inject;

import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.tests.utils.junit.DomainXml;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author kedar
 */
@ExtendWith(ConfigApiJunit5Extension.class)
@DomainXml("AccessLogAllDefaultsTest.xml")
public class AccessLogAllDefaultsTest {

    @Inject
    private AccessLog al;

    @Test
    public void testAllDefaults() {
        assertEquals("true", al.getRotationEnabled());
        assertEquals("1440", al.getRotationIntervalInMinutes());
        assertEquals("time", al.getRotationPolicy());
    }
}
