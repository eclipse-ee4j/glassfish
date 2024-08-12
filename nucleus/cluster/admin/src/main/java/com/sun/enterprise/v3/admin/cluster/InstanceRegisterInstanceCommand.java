/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.util.InstanceRegisterInstanceCommandParameters;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.util.Map;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;


/**
 * The _register-instance (and _create-node) command that runs on instance
 * @author Jennifer Chou
 */
@Service(name="_register-instance-at-instance")
@PerLookup
@ExecuteOn(value={RuntimeType.INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_register-instance-at-instance",
        description="_register-instance-at-instance")
})
public class InstanceRegisterInstanceCommand extends InstanceRegisterInstanceCommandParameters implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(InstanceRegisterInstanceCommand.class);

    @Inject
    Domain domain;

    @Inject
    ServerEnvironment env;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    @Override
    public void execute(AdminCommandContext ctxt) {
        final ActionReport report = ctxt.getActionReport();

        try {
            // create node if it doesn't exist
            Node n = domain.getNodes().getNode(node);
            if (n == null) {
                ConfigSupport.apply(new SingleConfigCode<Nodes>() {

                    @Override
                    public Object run(Nodes param) throws PropertyVetoException, TransactionFailure {

                        Node newNode = param.createChild(Node.class);
                        newNode.setName(node);
                        if(installdir != null && !"".equals(installdir))
                            newNode.setInstallDir(installdir);
                        if (nodedir != null && !"".equals(nodedir))
                             newNode.setNodeDir(nodedir);
                        if (nodehost != null && !"".equals(nodehost))
                            newNode.setNodeHost(nodehost);
                        newNode.setType(type);
                        //comment out - not needed
                        /*if (type.equals("SSH")) {
                            SshConnector sshC = param.createChild(SshConnector.class);
                            if (sshHost != null && sshHost != "") {
                                sshC.setSshHost(sshHost);

                            }
                            if (sshPort != "-1" && sshPort != "") {
                                sshC.setSshPort(sshPort);

                            }
                            if (sshuser != null || sshkeyfile != null || sshpassword != null
                                    || sshkeypassphrase != null) {
                                SshAuth sshA = sshC.createChild(SshAuth.class);
                                if (sshuser != null && sshuser != "") {
                                    sshA.setUserName(sshuser);
                                }
                                if (sshkeyfile != null && sshkeyfile != "") {
                                    sshA.setKeyfile(sshkeyfile);
                                }
                                if (sshpassword != null && sshpassword != "") {
                                    sshA.setPassword(sshpassword);
                                }
                                if (sshkeypassphrase != null && sshkeypassphrase != "") {
                                    sshA.setKeyPassphrase(sshkeypassphrase);
                                }
                                sshC.setSshAuth(sshA);
                            }
                            if (sshC != null) {
                                newNode.setSshConnector(sshC);
                            }
                        }*/

                        param.getNode().add(newNode);
                        return newNode;
                    }
                }, domain.getNodes());
            }

            // create server if it doesn't exist
            Server s = domain.getServers().getServer(instanceName);
            if (s == null) {
                ConfigSupport.apply(new SingleConfigCode<Servers>() {

                    public Object run(Servers param) throws PropertyVetoException, TransactionFailure {

                        Server newServer = param.createChild(Server.class);

                        newServer.setConfigRef(config);
                        //newServer.setLbWeight(lbWeight);
                        newServer.setName(instanceName);
                        newServer.setNodeRef(node);

                        // comment out - not needed
                        /*if (resourceRefs != null && !resourceRefs.isEmpty()) {
                            for (String ref: resourceRefs) {
                                ResourceRef newRR = newServer.createChild(ResourceRef.class);
                                newRR.setRef(ref);
                                newServer.getResourceRef().add(newRR);
                            }
                        }

                        if (appRefs != null && !appRefs.isEmpty()) {
                            for (String ar : appRefs) {
                                ApplicationRef newAR = newServer.createChild(ApplicationRef.class);
                                newAR.setRef(ar);
                                newServer.getApplicationRef().add(newAR);
                            }
                        }*/

                        if (systemProperties != null) {
                            for (final Map.Entry<Object, Object> entry : systemProperties.entrySet()) {
                                final String propName = (String) entry.getKey();
                                final String propValue = (String) entry.getValue();
                                SystemProperty newSP = newServer.createChild(SystemProperty.class);
                                //newSP.setDescription(sp.getDescription());
                                newSP.setName(propName);
                                newSP.setValue(propValue);
                                newServer.getSystemProperty().add(newSP);
                            }
                        }

                        param.getServer().add(newServer);
                        return newServer;
                    }
                }, domain.getServers());

                // create server-ref on cluster
                Cluster thisCluster = domain.getClusterNamed(clusterName);
                if (thisCluster != null) {
                    ConfigSupport.apply(new SingleConfigCode<Cluster>() {

                        public Object run(Cluster param) throws PropertyVetoException, TransactionFailure {

                            ServerRef newServerRef = param.createChild(ServerRef.class);
                            newServerRef.setRef(instanceName);
                            newServerRef.setLbEnabled(lbEnabled);
                            param.getServerRef().add(newServerRef);
                            return param;
                        }
                    }, thisCluster);
                }
            }


            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure tfe) {
            report.setMessage(localStrings.getLocalString("register.instance.failed",
                    "Instance {0} registration failed on {1}", instanceName, server.getName()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
            return;
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("register.instance.failed",
                    "Instance {0} registration failed on {1}", instanceName, server.getName()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }



    }


}
