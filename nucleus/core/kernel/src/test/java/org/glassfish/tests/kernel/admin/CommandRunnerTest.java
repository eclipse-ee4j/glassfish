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

package org.glassfish.tests.kernel.admin;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.single.SingleModulesRegistry;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import junit.framework.Assert;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import jakarta.inject.Inject;
import org.glassfish.internal.api.InternalSystemAdministrator;
import org.jvnet.hk2.testing.junit.HK2Runner;

/**
 * Test the command runner implementation.
 *
 * @author Jerome Dochez
 */
@Ignore
public class CommandRunnerTest extends HK2Runner {

    @Inject
    CommandRunner commandRunner;

    @Inject
    InternalSystemAdministrator kernelIdentity;

    @BeforeClass
    public void setup() {

        /*
         * The CommandRunnerImpl now injects Domain but these tests do not
         * exercise the code path that requires the domain.  So register a
         * dummy Domain instance with the habitat so injection will work.
         */
        ServiceLocatorUtilities.addOneDescriptor(testLocator,
                BuilderHelper.createConstantDescriptor(simpleDomain(), null, Domain.class));
        ServiceLocatorUtilities.addOneConstant(testLocator, new StartupContext());
        ServiceLocatorUtilities.addOneDescriptor(testLocator,
                BuilderHelper.createConstantDescriptor(new SingleModulesRegistry(CommandRunnerTest.class.getClassLoader()),
                        null, ModulesRegistry.class));
    }

    private static Domain simpleDomain() {
        InvocationHandler handler = new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                throw new UnsupportedOperationException("Feature-free dummy implementation for injection only");
            }
        };
        Domain d = (Domain) Proxy.newProxyInstance(Domain.class.getClassLoader(),
                                          new Class[] { Domain.class },
                                          handler);
        return d;
    }

    @Test
    public void tryOut() {
        Assert.assertTrue(commandRunner!=null);
        try {
            ActionReport report = commandRunner.getActionReport("plain");
            CommandRunner.CommandInvocation inv = commandRunner.getCommandInvocation("list-contracts", report, kernelIdentity.getSubject());
            inv.execute();
            System.out.println(report.getTopMessagePart().getMessage());
            for (ActionReport.MessagePart child : report.getTopMessagePart().getChildren()) {
                System.out.println(child.getMessage());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
