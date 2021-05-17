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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Profiler;
import com.sun.enterprise.config.serverbeans.AdminService;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import static org.junit.Assert.*;
import org.junit.Test;
import org.jvnet.hk2.config.AttributeChanges;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Jerome Dochez
 * Date: Mar 20, 2008
 * Time: 4:48:14 PM
 */
public class DirectCreationTest extends ConfigPersistence {

    ServiceLocator habitat = Utils.instance.getHabitat(this);

    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
    @Override
    public String getFileName() {
        return "DomainTest";
    }

    @Override
    public ServiceLocator getBaseServiceLocator() {
        return habitat;
    }

    @Override
    public ServiceLocator getHabitat() {
        return getBaseServiceLocator();
    }

    public void doTest() throws TransactionFailure {

        AdminService service = habitat.getService(AdminService.class);

        ConfigBean serviceBean = (ConfigBean) ConfigBean.unwrap(service);
        Class<?>[] subTypes = null;
        try {
            subTypes = ConfigSupport.getSubElementsTypes(serviceBean);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new RuntimeException(e);
        }

        ConfigSupport support = getBaseServiceLocator().getService(ConfigSupport.class);

        assertNotNull("ConfigSupport not found", support);

        for (Class<?> subType : subTypes) {

            // TODO:  JL force compilation error to mark this probably edit point for grizzly config
            if (subType.getName().endsWith("DasConfig")) {
                Map<String, String> configChanges = new HashMap<String, String>();
                configChanges.put("dynamic-reload-enabled", "true");
                configChanges.put("autodeploy-dir", "funky-dir");
                support.createAndSet(serviceBean, (Class<? extends ConfigBeanProxy>)subType, configChanges);
                break;
            }
        }

        support.createAndSet(serviceBean, DasConfig.class, (List) null);

        List<AttributeChanges> profilerChanges = new ArrayList<AttributeChanges>();
        String[] values = { "-Xmx512m", "-RFtrq", "-Xmw24" };
        ConfigSupport.MultipleAttributeChanges multipleChanges = new ConfigSupport.MultipleAttributeChanges("jvm-options", values );
        String[] values1 = { "profile" };
        ConfigSupport.MultipleAttributeChanges multipleChanges1 = new ConfigSupport.MultipleAttributeChanges("name", values1 );
        profilerChanges.add(multipleChanges);
        profilerChanges.add(multipleChanges1);
        support.createAndSet((ConfigBean) ConfigBean.unwrap(habitat.<JavaConfig>getService(JavaConfig.class))
                , Profiler.class, profilerChanges);
    }

    @Test
    public void directAttributeNameTest() throws ClassNotFoundException {

        boolean foundOne=false;
        for (String attrName :
                ((ConfigBean) ConfigBean.unwrap(habitat.<JavaConfig>getService(JavaConfig.class))).model.getAttributeNames()) {
            assertTrue(attrName!=null);
            foundOne=true;
        }
        assertTrue(foundOne);
    }

    public boolean assertResult(String s) {
        return s.contains("autodeploy-dir=\"funky-dir\"");
    }
}
