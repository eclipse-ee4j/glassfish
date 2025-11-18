/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.web.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.Transport;
import org.glassfish.grizzly.config.dom.Transports;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Command to create transport element within network-config
 *
 * Sample Usage : create-transport [--acceptorThreads no_of_acceptor_threads] [--bufferSizeBytes buff_size_bytes]
 * [--classname class_name] [--enableSnoop true|false][--selectionKeyHandler true|false] [--displayConfiguration
 * true|false][--maxConnectionsCount count] [--idleKeyTimeoutSeconds idle_key_timeout] [--tcpNoDelay true|false]
 * [--readTimeoutMillis read_timeout][--writeTimeoutMillis write_timeout] [--byteBufferType buff_type]
 * [--selectorPollTimeoutMillis true|false] transport_name
 *
 * domain.xml element example <transports> <transport name="tcp" /> </transports>
 *
 * @author Nandini Ektare
 */
@Service(name = "create-transport")
@PerLookup
@I18n("create.transport")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class CreateTransport implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name = "transportname", primary = true)
    String transportName;
    @Param(name = "acceptorthreads", alias="acceptorThreads", optional = true, defaultValue = "-1")
    String acceptorThreads;
    @Param(name = "buffersizebytes", alias="bufferSizeBytes", optional = true, defaultValue = "8192")
    String bufferSizeBytes;
    @Param(name = "bytebuffertype", alias="byteBufferType", optional = true, defaultValue = "HEAP")
    String byteBufferType;
    @Param(name = "classname", optional = true,
        defaultValue = "org.glassfish.grizzly.TCPSelectorHandler")
    String className;
    @Param(name = "displayconfiguration", alias="displayConfiguration", optional = true, defaultValue = "false")
    Boolean displayConfiguration;
    @Param(name = "enablesnoop", alias="enableSnoop", optional = true, defaultValue = "false")
    Boolean enableSnoop;
    @Param(name = "idlekeytimeoutseconds", alias="idleKeyTimeoutSeconds", optional = true, defaultValue = "30")
    String idleKeyTimeoutSeconds;
    @Param(name = "maxconnectionscount", alias="maxConnectionsCount", optional = true, defaultValue = "4096")
    String maxConnectionsCount;
    @Param(name = "readtimeoutmillis", alias="readTimeoutMillis", optional = true, defaultValue = "30000")
    String readTimeoutMillis;
    @Param(name = "writetimeoutmillis", alias="writeTimeoutMillis", optional = true, defaultValue = "30000")
    String writeTimeoutMillis;
    @Param(name = "selectionkeyhandler", alias="selectionKeyHandler", optional = true)
    String selectionKeyHandler;
    @Param(name = "selectorpolltimeoutmillis", alias="selectorPollTimeoutMillis", optional = true, defaultValue = "1000")
    String selectorPollTimeoutMillis;
    @Param(name = "tcpnodelay", alias="tcpNoDelay", optional = true, defaultValue = "false")
    Boolean tcpNoDelay;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    Domain domain;
    @Inject
    ServiceLocator services;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and
     * the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            config = newConfig;
        }
        final ActionReport report = context.getActionReport();
        // check for duplicates
        NetworkConfig networkConfig = config.getNetworkConfig();
        Transports transports = networkConfig.getTransports();
        for (Transport transport : transports.getTransport()) {
            if (transportName != null &&
                transportName.equalsIgnoreCase(transport.getName())) {
                report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_TRANSPORT_FAIL_DUPLICATE), transportName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
        // Add to the <network-config>
        try {
            ConfigSupport.apply(new SingleConfigCode<Transports>() {
                public Object run(Transports param)
                    throws PropertyVetoException, TransactionFailure {
                    boolean docrootAdded = false;
                    boolean accessLogAdded = false;
                    Transport newTransport = param.createChild(Transport.class);
                    newTransport.setName(transportName);
                    newTransport.setAcceptorThreads(acceptorThreads);
                    newTransport.setBufferSizeBytes(bufferSizeBytes);
                    newTransport.setByteBufferType(byteBufferType);
                    newTransport.setClassname(className);
                    newTransport.setDisplayConfiguration(displayConfiguration.toString());
                    newTransport.setEnableSnoop(enableSnoop.toString());
                    newTransport.setIdleKeyTimeoutSeconds(idleKeyTimeoutSeconds);
                    newTransport.setMaxConnectionsCount(maxConnectionsCount);
                    newTransport.setName(transportName);
                    newTransport.setReadTimeoutMillis(readTimeoutMillis);
                    newTransport.setSelectionKeyHandler(selectionKeyHandler);
                    newTransport.setSelectorPollTimeoutMillis(
                        selectorPollTimeoutMillis);
                    newTransport.setWriteTimeoutMillis(writeTimeoutMillis);
                    newTransport.setTcpNoDelay(tcpNoDelay.toString());
                    param.getTransport().add(newTransport);
                    return newTransport;
                }
            }, transports);
        } catch (TransactionFailure e) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_TRANSPORT_FAIL), transportName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
