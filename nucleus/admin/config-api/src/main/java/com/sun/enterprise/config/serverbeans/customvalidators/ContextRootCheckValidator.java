/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.UnexpectedTypeException;

import java.util.Arrays;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.Dom;

/**
 * Validation logic for ContextRootCheck constraint
 *
 * @author Amy Roh
 */
public class ContextRootCheckValidator implements ConstraintValidator<ContextRootCheck, Application> {

    @Override
    public void initialize(ContextRootCheck constraintAnnotation) {
        //no initialization needed
    }

    @Override
    public boolean isValid(Application app, ConstraintValidatorContext cvc) throws UnexpectedTypeException {

        if (app == null) {
            return true;
        }
        Dom dom = Dom.unwrap(app);

        ServiceLocator locator = dom.getHabitat();
        if (locator == null)
            return true;

        ConfigBeansUtilities cbu = locator.getService(ConfigBeansUtilities.class);
        if (cbu == null)
            return true;

        Domain domain = cbu.getDomain();
        String appName = app.getName();

        String contextRoot = app.getContextRoot();

        if (contextRoot != null && !contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }

        boolean result = true;

        List<String> targets = domain.getAllReferencedTargetsForApplication(appName);
        for (String target : targets) {
            List<Server> servers = domain.getServersInTarget(target);
            for (Server server : servers) {
                ApplicationRef applicationRef = domain.getApplicationRefInServer(server.getName(), appName);

                if (applicationRef != null) {
                    for (Application application : domain.getApplications().getApplications()) {
                        if (isSameApp(appName, application.getName())) {
                            // skip the check if the validation is for different versions of the same application
                        } else if ((application.getContextRoot() != null) && application.getContextRoot().equals(contextRoot)) {

                            String virtualServers = applicationRef.getVirtualServers();
                            List<String> vsList = Arrays.asList(virtualServers.split(","));

                            ApplicationRef thisAppRef = domain.getApplicationRefInServer(server.getName(), application.getName());

                            if (thisAppRef != null) {

                                virtualServers = thisAppRef.getVirtualServers();
                                List<String> thisVsList = Arrays.asList(virtualServers.split(","));

                                for (String vs : thisVsList) {
                                    if (vsList.contains(vs)) {
                                        result = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;

    }

    private static final boolean isSameApp(String name1, String name2) {
        try {
            if (name1.equals(name2)) {
                return true;
            } else if (name1.equals(getUntaggedName(name2))) {
                return true;
            } else if (name2.equals(getUntaggedName(name1))) {
                return true;
            } else if (getUntaggedName(name1).equals(getUntaggedName(name2))) {
                return true;
            }
        } catch (Exception ex) {
            // ignore
        }
        return false;
    }

    private static final String getUntaggedName(String appName) throws Exception {

        if (appName != null && !appName.isEmpty()) {
            int colonIndex = appName.indexOf(":");
            // if the appname contains a EXPRESSION_SEPARATOR
            if (colonIndex >= 0) {
                if (colonIndex == 0) {
                    // if appName is starting with a colon
                    throw new Exception("excepted application name before colon: " + appName);
                } else if (colonIndex == (appName.length() - 1)) {
                    // if appName is ending with a colon
                    throw new Exception("excepted version identifier after colon: " + appName);
                }
                // versioned
                return appName.substring(0, colonIndex);
            }
        }
        // not versioned
        return appName;
    }

}
