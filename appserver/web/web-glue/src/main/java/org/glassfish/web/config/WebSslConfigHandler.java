/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.config;

import com.sun.enterprise.admin.commands.CreateSsl;
import com.sun.enterprise.admin.commands.DeleteSsl;
import com.sun.enterprise.admin.commands.SslConfigHandler;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.glassfish.api.ActionReport;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.web.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * SSL configuration handler for http-listener protocol
 * @author Jerome Dochez
 */
@Service(name="http-listener")
public class WebSslConfigHandler implements SslConfigHandler {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Override
    public void create(final CreateSsl command, ActionReport report) {

        NetworkConfig netConfig = command.config.getNetworkConfig();
        // ensure we have the specified listener
        NetworkListener listener = netConfig.getNetworkListener(command.listenerId);
        Protocol httpProtocol;
        try {
            if (listener == null) {
                report.setMessage(
                        MessageFormat.format(
                                rb.getString(LogFacade.CREATE_SSL_HTTP_NOT_FOUND),
                                        command.listenerId));
                httpProtocol = command.findOrCreateProtocol(command.listenerId);
            } else {
                httpProtocol = listener.findHttpProtocol();
                Ssl ssl = httpProtocol.getSsl();
                if (ssl != null) {
                    report.setMessage(
                            MessageFormat.format(
                                    rb.getString(LogFacade.CREATE_SSL_HTTP_ALREADY_EXISTS),
                                            command.listenerId));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                        public Object run(Protocol param) throws TransactionFailure {
                            Ssl newSsl = param.createChild(Ssl.class);
                            command.populateSslElement(newSsl);
                            param.setSsl(newSsl);
                            return newSsl;
                        }
                    }, httpProtocol);

        } catch (TransactionFailure e) {
            command.reportError(report, e);
        }
        command.reportSuccess(report);
    }

    @Override
    public void delete(DeleteSsl command, ActionReport report) {

        NetworkConfig netConfig = command.config.getNetworkConfig();
        NetworkListener networkListener =
            netConfig.getNetworkListener(command.listenerId);

        if (networkListener == null) {
            report.setMessage(
                    MessageFormat.format(
                            rb.getString(LogFacade.DELETE_SSL_HTTP_LISTENER_NOT_FOUND), command.listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        Protocol protocol = networkListener.findHttpProtocol();
        if (protocol.getSsl() == null) {
            report.setMessage(
                    MessageFormat.format(
                            rb.getString(LogFacade.DELETE_SSL_ELEMENT_DOES_NOT_EXIST), command.listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Protocol>() {
                public Object run(Protocol param) {
                    param.setSsl(null);
                    return null;
                }
            }, networkListener.findHttpProtocol());
        } catch(TransactionFailure e) {
            command.reportError(report, e);
        }
    }
}
