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

package test.extension;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.enterprise.context.spi.*;

import test.servlet.PortableExtensionInjectionTargetTestServlet;

import java.util.Set;

/**
 * Test for issue: 11135
 *
 * Extension sets its own injection target and check if that object is used
 * for injection
 *
 * @author Jitendra Kotamraju
 */
public class MyExtension implements Extension {
    public static boolean processAnnotatedTypeCalled = false;

    public void observe(final @Observes ProcessInjectionTarget<PortableExtensionInjectionTargetTestServlet> pit) {
        PortableExtensionInjectionTargetTestServlet.pitCalled = true;

        final InjectionTarget<PortableExtensionInjectionTargetTestServlet> it = pit.getInjectionTarget();
        pit.setInjectionTarget(new InjectionTarget<PortableExtensionInjectionTargetTestServlet>() {

            public void inject(PortableExtensionInjectionTargetTestServlet instance, CreationalContext<PortableExtensionInjectionTargetTestServlet> ctx) {
                it.inject(instance, ctx);
                instance.pitsInjectionTargetUsed = true;
            }

            public void postConstruct(PortableExtensionInjectionTargetTestServlet instance) {
                it.postConstruct(instance);
            }

            public void preDestroy(PortableExtensionInjectionTargetTestServlet instance) {
                it.preDestroy(instance);
            }

            public void dispose(PortableExtensionInjectionTargetTestServlet instance) {        
            }

            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

            public PortableExtensionInjectionTargetTestServlet produce(CreationalContext<PortableExtensionInjectionTargetTestServlet> ctx) {
                return it.produce(ctx);
            }

        });

    }
}
