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
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * SSL configuration handler for iiop-listener configuration
 * @author Jerome Dochez
 *
 */
@Service(name="iiop-listener")
public class IiopSslConfigHandler implements SslConfigHandler {

    final private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(CreateSsl.class);


    @Override
    public void create(final CreateSsl command, ActionReport report) {
       IiopService iiopService = command.config.getExtensionByType(IiopService.class);
        // ensure we have the specified listener
        IiopListener iiopListener = null;
        for (IiopListener listener : iiopService.getIiopListener()) {
            if (listener.getId().equals(command.listenerId)) {
                iiopListener = listener;
            }
        }
        if (iiopListener == null) {
            report.setMessage(
                localStrings.getLocalString("create.ssl.iiop.notfound",
                    "IIOP Listener named {0} to which this ssl element is " +
                        "being added does not exist.", command.listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        if (iiopListener.getSsl() != null) {
            report.setMessage(
                localStrings.getLocalString("create.ssl.iiop.alreadyExists",
                    "IIOP Listener named {0} to which this ssl element is " +
                        "being added already has an ssl element.", command.listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        try {
            ConfigSupport.apply(new SingleConfigCode<IiopListener>() {
                        public Object run(IiopListener param)
                                throws PropertyVetoException, TransactionFailure {
                            Ssl newSsl = param.createChild(Ssl.class);
                            command.populateSslElement(newSsl);
                            param.setSsl(newSsl);
                            return newSsl;
                        }
                    }, iiopListener);

        } catch (TransactionFailure e) {
            command.reportError(report, e);
        }
        command.reportSuccess(report);
    }

    @Override
    public void delete(DeleteSsl command, ActionReport report) {

        IiopService iiopService = command.config.getExtensionByType(IiopService.class);
        IiopListener iiopListener = null;
        for (IiopListener listener : iiopService.getIiopListener()) {
            if (listener.getId().equals(command.listenerId)) {
                iiopListener = listener;
            }
        }

        if (iiopListener == null) {
            report.setMessage(localStrings.getLocalString(
                    "delete.ssl.iiop.listener.notfound",
                    "Iiop Listener named {0} not found", command.listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (iiopListener.getSsl() == null) {
            report.setMessage(localStrings.getLocalString(
                    "delete.ssl.element.doesnotexist", "Ssl element does " +
                    "not exist for Listener named {0}", command.listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        try {
            ConfigSupport.apply(new SingleConfigCode<IiopListener>() {
                public Object run(IiopListener param)
                throws PropertyVetoException {
                    param.setSsl(null);
                    return null;
                }
            }, iiopListener);
        } catch(TransactionFailure e) {
            command.reportError(report, e);
        }
    }
}
