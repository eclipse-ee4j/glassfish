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

import com.sun.enterprise.config.serverbeans.Domain;

import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigBean;


/**
 * User: Jerome Dochez
 * Date: Mar 20, 2008
 * Time: 2:44:48 PM
 */
public class SubTypesTest extends ConfigApiTest {

    // not testing all the sub types, just a few to be sure it works ok.
    String expectedClassNames[] = {
        "com.sun.enterprise.config.serverbeans.Applications",
        "com.sun.enterprise.config.serverbeans.Configs",
        "com.sun.enterprise.config.serverbeans.Clusters"
    };


    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void testSubTypesOfDomain() {
        Domain domain = super.getHabitat().getService(Domain.class);
        try {
            Class<?>[] subTypes = ConfigSupport.getSubElementsTypes((ConfigBean) ConfigBean.unwrap(domain));
            for (Class subType : subTypes) {
                Logger.getAnonymousLogger().fine("Found class" + subType);
            }
            for (String expectedClassName : expectedClassNames) {
                boolean found=false;
                for (Class<?> subType : subTypes)  {
                    if (subType.getName().equals(expectedClassName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Logger.getAnonymousLogger().severe("Cannot find " + expectedClassName + " from list of subtypes");
                    for (Class subType : subTypes) {
                        Logger.getAnonymousLogger().severe("Found class" + subType);
                    }
                    throw new RuntimeException("Cannot find " + expectedClassName);
                }
            }
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
