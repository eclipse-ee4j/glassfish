/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests;

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.AttributeChanges;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * User: Jerome Dochez
 * Date: Mar 20, 2008
 * Time: 4:48:14 PM
 */
public class DirectCreationTest extends ConfigPersistence {

    @Override
    public void doTest() throws Exception {
        AdminService service = locator.getService(AdminService.class);
        ConfigBean serviceBean = (ConfigBean) Dom.unwrap(service);
        Class<?>[] subTypes = ConfigSupport.getSubElementsTypes(serviceBean);
        ConfigSupport support = locator.getService(ConfigSupport.class);
        assertNotNull(support, "ConfigSupport not found");

        for (Class<?> subType : subTypes) {
            if (subType.getName().endsWith("DasConfig")) {
                Map<String, String> configChanges = new HashMap<>();
                configChanges.put("dynamic-reload-enabled", "true");
                configChanges.put("autodeploy-dir", "funky-dir");
                ConfigSupport.createAndSet(serviceBean, (Class<? extends ConfigBeanProxy>) subType, configChanges);
                break;
            }
        }

        support.createAndSet(serviceBean, DasConfig.class, (List<AttributeChanges>) null);

        List<AttributeChanges> profilerChanges = new ArrayList<>();
        String[] values = { "-Xmx512m", "-RFtrq", "-Xmw24" };
        ConfigSupport.MultipleAttributeChanges multipleChanges = new ConfigSupport.MultipleAttributeChanges("jvm-options", values );
        String[] values1 = { "profile" };
        ConfigSupport.MultipleAttributeChanges multipleChanges1 = new ConfigSupport.MultipleAttributeChanges("name", values1 );
        profilerChanges.add(multipleChanges);
        profilerChanges.add(multipleChanges1);
        support.createAndSet(unwrapConfigBean(), Profiler.class, profilerChanges);
    }

    @Test
    public void directAttributeNameTest() throws Exception {
        Set<String> attributeNames = unwrapConfigBean().model.getAttributeNames();
        assertThat(attributeNames, hasSize(13));
        for (String attrName : attributeNames) {
            assertNotNull(attrName);
        }
    }

    @Override
    public void assertResult(String xml) {
        assertThat(xml, stringContainsInOrder("autodeploy-dir=\"funky-dir\""));
    }


    private ConfigBean unwrapConfigBean() {
        return (ConfigBean) Dom.unwrap(locator.<JavaConfig> getService(JavaConfig.class));
    }
}
