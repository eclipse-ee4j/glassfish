/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.modularity;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import java.util.StringTokenizer;

import org.glassfish.api.naming.DefaultResourceProxy;
import org.glassfish.config.support.Singleton;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * The main driver to make the getset commands compatible with the config modularity.
 *
 * @author Masoud Kalali
 */
@Service
@Singleton
public class GetSetModularityHelper {

    @Inject
    private ConfigModularityUtils configModularityUtils;

    @Inject
    private Domain domain;

    @Inject
    private ServiceLocator serviceLocator;

    @PostConstruct
    public void initDefaultResources() {
        serviceLocator.getAllServices(DefaultResourceProxy.class);
    }

    /**
     * @param prefix the entire . separated string
     * @param position starts with one
     * @return the configbean class matching the element in the given position or null
     */
    private Class getElementClass(String prefix, int position) {

        StringTokenizer tokenizer = new StringTokenizer(prefix, ".");
        String token = null;
        for (int i = 0; i < position; i++) {
            if (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();
            } else {
                return null;
            }
        }
        if (token != null) {
            return configModularityUtils.getClassFor(token);
        }
        return null;
    }

    public Class getClassFor(String serviceName) {
        return configModularityUtils.getClassFor(serviceName);
    }

    /**
     * @param string the entire . separated string
     * @param position starts with one
     * @return String in that position
     */
    private String getElement(String string, int position) {

        StringTokenizer tokenizer = new StringTokenizer(string, ".");
        String token = null;
        for (int i = 0; i < position; i++) {
            if (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();
            } else {
                return null;
            }
        }
        return token;
    }

    public void getLocationForDottedName(String dottedName) {
        //        TODO temporary hard coded service names till all elements are supported, being tracked as part of FPP-121
        if (dottedName.contains("monitor"))
            return;
        if (dottedName.contains("mdb-container") || dottedName.contains("ejb-container.") || dottedName.endsWith("ejb-container") || dottedName.contains("web-container.")
                || dottedName.contains("cdi-service") || dottedName.contains("batch-runtime-configuration")
                || dottedName.contains("managed-job-config")) {
            //TODO improve performance to improve command execution time
            checkForDependentElements(dottedName);
            if (dottedName.startsWith("configs.config.")) {
                Config c = null;
                if ((getElement(dottedName, 3) != null)) {
                    c = getConfigForName(getElement(dottedName, 3));

                }
                if (c != null && getElementClass(dottedName, 4) != null) {
                    c.getExtensionByType(getElementClass(dottedName, 4));
                }

            } else if (!dottedName.startsWith("domain.")) {
                Config c = null;
                if ((getElement(dottedName, 1) != null)) {
                    c = getConfigForName(getElement(dottedName, 1));

                }
                if (c != null && getElementClass(dottedName, 2) != null) {
                    c.getExtensionByType(getElementClass(dottedName, 2));
                }
            }

        }
    }

    private void checkForDependentElements(String dottedName) {
        //Go over the dependent elements of custom configured config beans and try finding if the dependent elements match the dottedName
    }

    private Config getConfigForName(String name) {
        if (domain.getConfigNamed(name) != null)
            return domain.getConfigNamed(name);
        if (domain.getServerNamed(name) != null)
            return domain.getServerNamed(name).getConfig();
        return null;
    }
}
