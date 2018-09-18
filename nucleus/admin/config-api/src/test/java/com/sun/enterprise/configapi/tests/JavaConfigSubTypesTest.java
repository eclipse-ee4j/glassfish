/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.junit.Test;
import org.junit.Assert;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.SingleConfigCode;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import com.sun.enterprise.config.serverbeans.JavaConfig;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.beans.PropertyVetoException;

/**
 * User: Jerome Dochez
 * Date: Apr 7, 2008
 * Time: 11:13:22 AM
 */
public class JavaConfigSubTypesTest extends ConfigPersistence {


    @Test
    public void testSubTypesOfDomain() {
        JavaConfig config = super.getHabitat().getService(JavaConfig.class);
        try {
            Class<?>[] subTypes = ConfigSupport.getSubElementsTypes((ConfigBean) ConfigBean.unwrap(config));
            boolean found=false;
            for (Class subType : subTypes) {
                Logger.getAnonymousLogger().fine("Found class " + subType);
                if (subType.getName().equals(List.class.getName())) {
                    found=true;
                }
            }
            Assert.assertTrue(found);;
        } catch(ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    ServiceLocator habitat = Utils.instance.getHabitat(this);

    /**
     * Returns the file name without the .xml extension to load the test configuration
     * from. By default, it's the name of the TestClass.
     *
     * @return the configuration file name
     */
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
    
    @Test
    public void doTest() throws TransactionFailure {


        JavaConfig javaConfig = habitat.getService(JavaConfig.class);

        ConfigSupport.apply(new SingleConfigCode<JavaConfig>() {
            public Object run(JavaConfig param) throws PropertyVetoException, TransactionFailure {
                List<String> jvmOptions = param.getJvmOptions();
                jvmOptions.add("-XFooBar=true");
                return jvmOptions;
            }
        }, javaConfig);

    }

    public boolean assertResult(String s) {
        return s.indexOf("-XFooBar")!=-1;
    }
    
}
