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

package com.sun.enterprise.config.modularity.command;

import com.sun.enterprise.config.modularity.ConfigModularityUtils;
import com.sun.enterprise.config.modularity.customization.ConfigBeanDefaultValue;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.glassfish.api.admin.AccessRequired;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * @author Masoud Kalali
 */
@Service
public class AbstractConfigModularityCommand {
    @Inject
    private ConfigModularityUtils configModularityUtils;
    @Inject
    ServiceLocator locator;
    protected final static String LINE_SEPARATOR = System.getProperty("line.separator");

    protected String replaceExpressionsWithValues(String location) {
        StringTokenizer tokenizer = new StringTokenizer(location, "/", false);
        while (tokenizer.hasMoreElements()) {
            String level = tokenizer.nextToken();
            if (level.contains("[$")) {
                String expr = location.substring(location.indexOf("$"), location.indexOf("]"));
                String value = configModularityUtils.resolveExpression(expr);
                location = location.replace(expr, value);
            }
        }
        return location;
    }

    protected Config getConfigForName(String targetName, ServiceLocator serviceLocator, Domain domain) {

        if (CommandTarget.CONFIG.isValid(serviceLocator, targetName)) {
            return domain.getConfigNamed(targetName);
        }
        if (CommandTarget.DAS.isValid(serviceLocator, targetName)
                || CommandTarget.STANDALONE_INSTANCE.isValid(serviceLocator, targetName)) {
            Server s = domain.getServerNamed(targetName);
            return s == null ? null : domain.getConfigNamed(s.getConfigRef());
        }

        if (CommandTarget.CLUSTER.isValid(serviceLocator, targetName)) {
            Cluster cl = domain.getClusterNamed(targetName);
            return cl == null ? null : domain.getConfigNamed(cl.getConfigRef());

        }
        return null;
    }

    protected Collection<AccessRequired.AccessCheck> getAccessChecksForDefaultValue(List<ConfigBeanDefaultValue> values, String target,
            List<String> actions) {
        Collection<AccessRequired.AccessCheck> checks = new ArrayList<AccessRequired.AccessCheck>();
        for (ConfigBeanDefaultValue val : values) {
            String location = val.getLocation();
            for (String s : actions) {
                AccessRequired.AccessCheck check = new AccessRequired.AccessCheck(configModularityUtils.getOwningObject(location), s, true);
                checks.add(check);
            }
        }
        return checks;
    }

    protected Collection<AccessRequired.AccessCheck> getAccessChecksForConfigBean(ConfigBeanProxy cbProxy, String target,
            List<String> actions) {
        Collection<AccessRequired.AccessCheck> checks = new ArrayList<AccessRequired.AccessCheck>();
        for (String s : actions) {
            AccessRequired.AccessCheck check = new AccessRequired.AccessCheck(cbProxy, s, true);
            checks.add(check);
        }
        return checks;
    }

    protected Collection<AccessRequired.AccessCheck> getAccessChecksForLocation(String location, List<String> actions) {
        Collection<AccessRequired.AccessCheck> checks = new ArrayList<AccessRequired.AccessCheck>();
        for (String s : actions) {
            AccessRequired.AccessCheck check = new AccessRequired.AccessCheck(replaceExpressionsWithValues(location), s);
            checks.add(check);
        }
        return checks;
    }
}
