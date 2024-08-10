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

package com.sun.enterprise.admin.servermgmt.pe;

import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.util.TokenValue;
import com.sun.enterprise.admin.util.TokenValueSet;

/**
 * This class defines the tokens required by the startserv & stopserv scripts.
 */
public final class PEScriptsTokens {
    public static final String CONFIG_HOME = "CONFIG_HOME";
    public static final String INSTANCE_ROOT = "INSTANCE_ROOT";
    public static final String SERVER_NAME = "SERVER_NAME";
    public static final String DOMAIN_NAME = "DOMAIN_NAME";

    /**
     * @return Returns the TokenValueSet that has the (token, value) pairs for startserv & stopserv scripts.
     * @param domainConfig
     */
    public static TokenValueSet getTokenValueSet(DomainConfig domainConfig) {
        final PEFileLayout layout = new PEFileLayout(domainConfig);

        final TokenValueSet tokens = new TokenValueSet();

        final String configRootDir = domainConfig.getConfigRoot();
        TokenValue tv = new TokenValue(CONFIG_HOME, configRootDir);
        tokens.add(tv);

        final String instanceRoot = layout.getRepositoryDir().getAbsolutePath();
        tv = new TokenValue(INSTANCE_ROOT, instanceRoot);
        tokens.add(tv);

        final String instanceName = (String) domainConfig.get(DomainConfig.K_SERVERID);
        if ((instanceName == null) || (instanceName.equals("")))
            tv = new TokenValue(SERVER_NAME, PEFileLayout.DEFAULT_INSTANCE_NAME);
        else
            tv = new TokenValue(SERVER_NAME, instanceName);
        tokens.add(tv);

        tv = new TokenValue(DOMAIN_NAME, domainConfig.getDomainName());
        tokens.add(tv);

        return (tokens);
    }
}
