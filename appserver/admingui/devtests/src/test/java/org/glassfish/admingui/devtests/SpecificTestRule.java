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

package org.glassfish.admingui.devtests;

import org.glassfish.admingui.devtests.util.SeleniumHelper;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Ignore;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 *
 * @author jasonlee
 */
public class SpecificTestRule implements MethodRule {
    protected static boolean debug;
    public SpecificTestRule() {
        debug = Boolean.parseBoolean(SeleniumHelper.getParameter("debug", "false"));
    }

    @Override
    public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object o) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                boolean runMethod = false;
                final Logger logger = Logger.getLogger(BaseSeleniumTestClass.class.getName());
                String method = System.getProperty("method");
                boolean skipTest = false;
                if(BaseSeleniumTestClass.IS_SECURE_ADMIN_ENABLED) {
                    String className = frameworkMethod.getMethod().getDeclaringClass().getName();
                    if (className.contains(".SecurityTest") ||
                       (className.contains(".AdminServiceTest") &&
                        frameworkMethod.getName().equals("testSsl"))) {
                        skipTest = true;
                    }
                }
                Set<String> methods = new HashSet<String>();
                if (method != null) {
                    String[] parts = method.split(",");
                    methods.addAll(Arrays.asList(parts));
                }
                Ignore ignore = frameworkMethod.getAnnotation(Ignore.class);

                if (methods.contains(frameworkMethod.getName()) && !skipTest) {
                    runMethod = true;
                } else if ((method == null) && (ignore == null) && !skipTest) {
                    runMethod = true;
                }

                if (debug) {
                    logger.log(Level.INFO, "Processing test {0} at {1}",
                            new String[]{
                                frameworkMethod.getName(),
                                (new SimpleDateFormat("yyyy-MM-dd-hh.mm.ss")).format(new Date())
                            });
                }
                if (runMethod) {
                    if (debug) {
                        logger.log(Level.INFO, "\tExecuting.");
                    }
                    try {
                        statement.evaluate();
                    } catch (Throwable t) {
                        SeleniumHelper.captureScreenshot(frameworkMethod.getName());
                        throw t; // rethrow
                        // No explanation as to why this was done, so we'll disable
                        // it and see what happens
                        //statement.evaluate(); // try again. Ugly hack, but if it works...
                    }
                } else {
                    logger.log(Level.INFO, "\tSkipping.");
                }
            }
        };
    }
}
