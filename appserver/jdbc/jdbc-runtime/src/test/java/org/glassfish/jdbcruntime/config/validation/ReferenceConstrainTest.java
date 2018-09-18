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

package org.glassfish.jdbcruntime.config.validation;

import com.sun.enterprise.config.serverbeans.Domain;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.validation.ConstraintViolationException;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jdbc.config.JdbcResource;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Before;
import org.glassfish.tests.utils.Utils;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;
import org.glassfish.jdbcruntime.config.ConfigApiTest;

import static org.junit.Assert.*;

/**
 *
 * @author mmares
 */
public class ReferenceConstrainTest extends ConfigApiTest {
    
//    private Logger logger = Logger.getLogger(ReferenceConstrainTest.class.getName());
    private ServiceLocator habitat;

    @Override
    public String getFileName() {
        return "DomainTest";
    }

    @Override
    public ServiceLocator getHabitat() {
        return habitat;
    }
    
    private ConstraintViolationException findConstrViolation(Throwable thr) {
        if (thr == null) {
            return null;
        }
        if (thr instanceof ConstraintViolationException) {
            return (ConstraintViolationException) thr;
        }
        return findConstrViolation(thr.getCause());
    }
    
    @Before
    public void createNewHabitat() {
        this.habitat = Utils.instance.getHabitat(this);
    }
    
    @Test // @Ignore
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
        ConfigBean poolConfig = (ConfigBean) ConfigBean.unwrap(jdbc);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<ConfigBean, Map<String, String>>();
        Map<String, String> configChanges = new HashMap<String, String>();
        configChanges.put("pool-name", "DerbyPool");
        changes.put(poolConfig, configChanges);
        try {
            ConfigSupport cs = getHabitat().getService(ConfigSupport.class);
            cs.apply(changes);
        } catch (TransactionFailure tf) {
            fail();
        }
    }
   
    @Test
    public void doChangeToInValidPool() throws TransactionFailure {
        Domain domain = habitat.getService(Domain.class);
        //Find JdbcResource to chenge its values
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
        ConfigBean poolConfig = (ConfigBean) ConfigBean.unwrap(jdbc);
        Map<ConfigBean, Map<String, String>> changes = new HashMap<ConfigBean, Map<String, String>>();
        Map<String, String> configChanges = new HashMap<String, String>();
        configChanges.put("pool-name", "WrongPointer");
        changes.put(poolConfig, configChanges);
        try {
            ConfigSupport cs = getHabitat().getService(ConfigSupport.class);
            cs.apply(changes);
            fail("Can not reach this point");
        } catch (TransactionFailure tf) {
            ConstraintViolationException cv = findConstrViolation(tf);
//            cv.printStackTrace(System.out);
            assertNotNull(cv);
        }
    }
    
}
