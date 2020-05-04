/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.beans.wbinflib;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class TestBeanInWebInfLib {
    @Inject
    BeanManager bm;
    
//    @Inject //@TestDatabase 
//    EntityManager emf_at_inj;

    @PersistenceContext(unitName="pu1")  
    EntityManager emf_at_pu;

    //This test injection method would be called in the context of the servlet in WAR
    //which does not have the alternative bean enabled in it.
    public String testInjection() {
        if (bm == null)
            return "Bean Manager not injected into the TestBean in WEB-INF/lib";
        System.out.println("BeanManager injected in WEB-INF/lib bean is " + bm);

        System.out.println("EMF injected in WEB-INF/lib bean is " + emf_at_pu);
        if (emf_at_pu == null)
            return "EMF injected via @PersistenceContext is not injected into " +
            		"the TestBean packaged in WEB-INF/lib";
        
        Set<Bean<?>> webinfLibBeans = bm.getBeans(TestBeanInWebInfLib.class, new AnnotationLiteral<Any>() {});
        if (webinfLibBeans.size() != 2) //Bean and enabled Alternative
            return "TestBean in WEB-INF/lib is not available via the WEB-INF/lib "
                    + "Bean's BeanManager";
        System.out.println("***********************************************************");
        printBeans(webinfLibBeans, "BeanManager.getBeans(TestBeanInWebInfLib, Any):");
        
        Set<Bean<?>> webinfLibAltBeans = bm.getBeans(TestAlternativeBeanInWebInfLib.class, new AnnotationLiteral<Any>() {});
        if (webinfLibBeans.size() != 1) //enabled Alternative
            return "TestAlternativeBean in WEB-INF/lib is not available via the WEB-INF/lib "
                    + "Bean's BeanManager";
        printBeans(webinfLibAltBeans, "BeanManager.getBeans(TestAlternativeBeanInWebInfLib, Any):");
        
        
        Iterable<Bean<?>> accessibleBeans = ((org.jboss.weld.manager.BeanManagerImpl) bm).getAccessibleBeans();
        printBeans(accessibleBeans, "BeanManagerImpl.getAccessibleBeans:");

        Iterable<Bean<?>> beans = ((org.jboss.weld.manager.BeanManagerImpl) bm).getBeans();
        printBeans(beans, "BeanManagerImpl.getBeans");
        System.out.println("***********************************************************");

        // success
        return "";
    }

    private void printBeans(Iterable<Bean<?>> beans, String msg) {
        System.out.println(msg + ":");
        for (Bean b : beans) {
            debug(b);
        }
        System.out.println();
    }

    private void debug(Bean b) {
        String name = b.getBeanClass().getName();
        if (name.indexOf("Test") != -1) {
            System.out.print(name);
        }

    }
}
