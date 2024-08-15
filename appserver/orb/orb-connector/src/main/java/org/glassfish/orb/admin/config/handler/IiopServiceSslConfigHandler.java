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

package org.glassfish.orb.admin.config.handler;

import com.sun.enterprise.admin.commands.CreateSsl;
import com.sun.enterprise.admin.commands.DeleteSsl;
import com.sun.enterprise.admin.commands.SslConfigHandler;
import com.sun.enterprise.config.serverbeans.SslClientConfig;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.orb.admin.config.IiopService;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * SSL configuration handler for iiop-service.
 * @author Jerome Dochez
 */
@Service(name="iiop-service")
public class IiopServiceSslConfigHandler implements SslConfigHandler {

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateSsl.class);

    @Override
    public void create(final CreateSsl command, ActionReport report) {
        IiopService iiopSvc = command.config.getExtensionByType(IiopService.class);
        if (iiopSvc.getSslClientConfig() != null) {
            report.setMessage(
                localStrings.getLocalString(
                    "create.ssl.iiopsvc.alreadyExists", "IIOP Service " +
                        "already has been configured with SSL configuration."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        try {
            ConfigSupport.apply(new SingleConfigCode<IiopService>() {
                        public Object run(IiopService param)
                                throws PropertyVetoException, TransactionFailure {
                            SslClientConfig newSslClientCfg =
                                    param.createChild(SslClientConfig.class);
                            Ssl newSsl = newSslClientCfg.createChild(Ssl.class);
                            command.populateSslElement(newSsl);
                            newSslClientCfg.setSsl(newSsl);
                            param.setSslClientConfig(newSslClientCfg);
                            return newSsl;
                        }
                    }, iiopSvc);

        } catch (TransactionFailure e) {
            command.reportError(report, e);
        }
        command.reportSuccess(report);
    }

    @Override
    public void delete(DeleteSsl command, ActionReport report) {
        if (command.config.getExtensionByType(IiopService.class).getSslClientConfig() == null) {
            report.setMessage(localStrings.getLocalString(
                    "delete.ssl.element.doesnotexistforiiop",
                    "Ssl element does not exist for IIOP service"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<IiopService>() {
                    public Object run(IiopService param)
                            throws PropertyVetoException {
                        param.setSslClientConfig(null);
                        return null;
                    }
                }, command.config.getExtensionByType(IiopService.class));
        } catch (TransactionFailure e) {
            command.reportError(report, e);
        }
    }
}
