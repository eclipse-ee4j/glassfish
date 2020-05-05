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

import java.util.Set;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.spi.Bean;

public class TestBeanInWebInfLib {
    @Inject
    BeanManager bm;

    @Inject
    TestBean tb;

    public String testInjection() {
        if (bm == null) {
            return "Bean Manager not injected into the TestBean in WEB-INF/lib";
        }
        System.out.println("BeanManager in WEB-INF/lib bean is " + bm);

        if (tb == null) {
            return "Injection of WAR's TestBean into the TestBean in WEB-INF/lib failed";
        }

        Set<Bean<?>> webinfLibBeans = bm.getBeans(TestBeanInWebInfLib.class,
                                                  new AnnotationLiteral<Any>() {});
        if (webinfLibBeans.size() != 1){
            return "TestBean in WEB-INF/lib is not available via the WEB-INF/lib Bean's BeanManager";
        }

        // success
        return "";
    }

}
