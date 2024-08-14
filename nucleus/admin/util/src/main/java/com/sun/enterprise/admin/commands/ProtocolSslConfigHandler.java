/*
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

package com.sun.enterprise.admin.commands;

import com.sun.enterprise.util.LocalStringManagerImpl;

import org.glassfish.api.ActionReport;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Ssl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

@Service(name = "protocol")
public class ProtocolSslConfigHandler implements SslConfigHandler {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ProtocolSslConfigHandler.class);

    // ------------------------------------------- Methods from SslConfigHandler

    @Override
    public void create(final CreateSsl command, final ActionReport report) {
        try {
            final Protocol protocol = command.findOrCreateProtocol(command.listenerId, false);
            if (protocol == null) {
                report.setMessage(localStrings.getLocalString("create.ssl.protocol.notfound.fail", "Unable to find protocol {0}.",
                        command.listenerId));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            } else {
                ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                    public Object run(Protocol param) throws TransactionFailure {
                        Ssl newSsl = param.createChild(Ssl.class);
                        param.setSecurityEnabled("true");
                        command.populateSslElement(newSsl);
                        param.setSsl(newSsl);
                        return newSsl;
                    }
                }, protocol);
            }
        } catch (TransactionFailure transactionFailure) {
            command.reportError(report, transactionFailure);
            return;
        }
        command.reportSuccess(report);
    }

    @Override
    public void delete(final DeleteSsl command, final ActionReport report) {
        try {
            NetworkConfig networkConfig = command.config.getNetworkConfig();
            final Protocol protocol = networkConfig.findProtocol(command.listenerId);
            if (protocol != null) {

                ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                    public Object run(Protocol param) {
                        param.setSecurityEnabled("false");
                        param.setSsl(null);
                        return null;
                    }
                }, protocol);
            }
        } catch (TransactionFailure transactionFailure) {
            command.reportError(report, transactionFailure);
        }
    }
}
