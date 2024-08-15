/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbcruntime.config.validation;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.tests.utils.junit.DomainXml;
import org.glassfish.tests.utils.junit.HK2JUnit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author mmares
 */
@ExtendWith(HK2JUnit5Extension.class)
@DomainXml(value = "DomainTest.xml")
// FIXME: This test probably belongs to another module: code coverage here: 0.0%
public class ReferenceConstrainTest {

    @Inject
    private ServiceLocator habitat;

    private ConstraintViolationException findConstrViolation(Throwable thr) {
        if (thr == null) {
            return null;
        }
        if (thr instanceof ConstraintViolationException) {
            return (ConstraintViolationException) thr;
        }
        return findConstrViolation(thr.getCause());
    }


    @Test
    public void doChangeToValidPool() throws TransactionFailure {
        Domain domain = habitat.getService(Domain.class);
        //Find JdbcResource to chenge its values
        Iterator<JdbcResource> iterator = domain.getResources().getResources(JdbcResource.class).iterator();
        JdbcResource jdbc = null;
        while (iterator.hasNext()) {
            JdbcResource res = iterator.next();
            if ("__TimerPool".equals(res.getPoolName())) {
                jdbc = res;
                break;
            }
        }
        assertNotNull(jdbc);
        ConfigBean poolConfig = (ConfigBean) Dom.unwrap(jdbc);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<>();
        Map<String, String> configChanges = new HashMap<>();
        configChanges.put("pool-name", "DerbyPool");
        changes.put(poolConfig, configChanges);
        ConfigSupport cs = this.habitat.getService(ConfigSupport.class);
        cs.apply(changes);
    }

    @Test
    public void doChangeToInValidPool() throws TransactionFailure {
        Domain domain = habitat.getService(Domain.class);
        // Find JdbcResource to chenge its values
        Iterator<JdbcResource> iterator = domain.getResources().getResources(JdbcResource.class).iterator();
        JdbcResource jdbc = null;
        while (iterator.hasNext()) {
            JdbcResource res = iterator.next();
            if ("DerbyPool".equals(res.getPoolName())) {
                jdbc = res;
                break;
            }
        }
        assertNotNull(jdbc);
        ConfigBean poolConfig = (ConfigBean) Dom.unwrap(jdbc);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<>();
        Map<String, String> configChanges = new HashMap<>();
        configChanges.put("pool-name", "WrongPointer");
        changes.put(poolConfig, configChanges);
        ConfigSupport cs = this.habitat.getService(ConfigSupport.class);
        TransactionFailure tf = assertThrows(TransactionFailure.class, () -> cs.apply(changes));
        ConstraintViolationException cv = findConstrViolation(tf);
        assertNotNull(cv);
    }
}
