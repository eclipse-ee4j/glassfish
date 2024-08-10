/*
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

package org.glassfish.security.services.commands;

import com.sun.enterprise.config.serverbeans.Domain;

import org.glassfish.api.ActionReport;
import org.glassfish.security.services.config.SecurityConfiguration;
import org.glassfish.security.services.config.SecurityConfigurations;
import org.glassfish.security.services.config.SecurityProvider;

/**
 *
 * @author tjquinn
 */
public class CLIUtil {

    public static SecurityConfiguration findSecurityConfiguration(
            final Domain domain,
            final String serviceName,
            final ActionReport report) {
        // Lookup the security configurations
        SecurityConfigurations secConfigs = domain.getExtensionByType(SecurityConfigurations.class);
        if (secConfigs == null) {
            report.setMessage("Unable to locate security configurations");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return null;
        }

        // Get the security service
        SecurityConfiguration ssc = secConfigs.getSecurityServiceByName(serviceName);
        if (ssc == null) {
            report.setMessage("Unable to locate security service: " + serviceName);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return null;
        }
        return ssc;
    }

    public static SecurityProvider findSecurityProvider(
            final Domain domain,
            final String serviceName,
            final String providerName,
            final ActionReport report) {
        // Get the security provider config
        final SecurityConfiguration sc = findSecurityConfiguration(domain, serviceName, report);
        if (sc == null) {
            return null;
        }
        SecurityProvider provider = sc.getSecurityProviderByName(providerName);
        if (provider == null) {
            report.setMessage("Unable to locate security provider: " + providerName);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return null;
        }
        return provider;
    }
}
