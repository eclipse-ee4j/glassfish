/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.container.common.impl;

import jakarta.inject.Inject;

import javax.naming.NamingException;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamespacePrefixes;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;

@Service
@NamespacePrefixes({ JavaCompNamingProxy.IN_APPCLIENT_CONTAINER })
public final class JavaCompNamingProxy implements NamedNamingObjectProxy {
    static final String IN_APPCLIENT_CONTAINER = JNDI_CTX_JAVA_COMPONENT + "InAppClientContainer";
    @Inject
    private ProcessEnvironment processEnv;

    @Override
    public Object handle(String name) throws NamingException {
        if (IN_APPCLIENT_CONTAINER.equals(name)) {
            Boolean isInAppClientContainer = Boolean.FALSE;
            if (processEnv.getProcessType() == ProcessEnvironment.ProcessType.ACC) {
                isInAppClientContainer = Boolean.TRUE;
            } else if (processEnv.getProcessType() == ProcessEnvironment.ProcessType.Other) {
                throw new NamingException("Lookup failed for '" + name + "'");
            }
            return isInAppClientContainer;
        }
        return null;
    }
}
