/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.logging;

import com.sun.enterprise.server.logging.FormatterDelegate;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.admin.monitor.callflow.ThreadLocalData;

import static com.sun.enterprise.server.logging.UniformLogFormatter.*;

import java.util.logging.Level;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: May 29, 2007
 * Time: 4:13:03 PM
 * To change this template use File | Settings | File Templates.
 */

public class AgentFormatterDelegate implements FormatterDelegate {

    Agent agent;

    public AgentFormatterDelegate(Agent agent) {
        this.agent = agent;
    }

    public void format(StringBuilder buf, Level level) {


        ThreadLocalData tld = agent.getThreadLocalData();
        if (tld==null) {
            return;
        }

        if (level.equals(Level.INFO) || level.equals(Level.CONFIG)) {

            if (tld.getApplicationName() != null) {
                buf.append("_ApplicationName").append(NV_SEPARATOR).
                        append(tld.getApplicationName()).
                        append(NVPAIR_SEPARATOR);
            }

        } else {

            if (tld.getRequestId() != null) {
                buf.append("_RequestID").append(NV_SEPARATOR).
                        append(tld.getRequestId()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getApplicationName() != null) {
                buf.append("_ApplicationName").append(NV_SEPARATOR).
                        append(tld.getApplicationName()).
                        append(NVPAIR_SEPARATOR);
            }

            if (tld.getModuleName() != null) {
                buf.append("_ModuleName").append(NV_SEPARATOR).
                        append(tld.getModuleName()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getComponentName() != null) {
                buf.append("_ComponentName").append(NV_SEPARATOR).
                        append(tld.getComponentName()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getComponentType() != null) {
                buf.append("_ComponentType").append(NV_SEPARATOR).
                        append(tld.getComponentType()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getMethodName() != null) {
                buf.append("_MethodName").append(NV_SEPARATOR).
                        append(tld.getMethodName()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getTransactionId() != null) {
                buf.append("_TransactionId").append(NV_SEPARATOR).
                        append(tld.getTransactionId()).append(NVPAIR_SEPARATOR);
            }

            if (tld.getSecurityId() != null) {
                buf.append("_CallerId").append(NV_SEPARATOR).
                        append(tld.getSecurityId()).append(NVPAIR_SEPARATOR);
            }
        }
    }
}
