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

import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.SshConnector;
import com.sun.enterprise.config.serverbeans.SshAuth;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.util.StringUtils;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sun.enterprise.util.net.NetUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;

import jakarta.inject.Inject;


import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.*;
import java.util.logging.Logger;

/**
 * Remote AdminCommand to update a config node.  This command is run only on DAS.
 *  Update the config node on DAS
 *
 * @author Carla Mott
 */
@Service(name = "_update-node")
@I18n("update.node")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Node.class,
        opType=RestEndpoint.OpType.POST,
        path="_update-node",
        description="Update Node",
        params={
            @RestParam(name="name", value="$parent")
        })
})
public class UpdateNodeCommand implements AdminCommand {

    @Inject
    Nodes nodes;

    @Inject
    Domain domain;

    @Param(name="name", primary = true)
    String name;

    @Param(name="nodedir", optional=true)
    String nodedir;

    @Param(name="nodehost", optional=true)
    String nodehost;

    @Param(name = "installdir", optional=true)
    String installdir;

    @Param(name="sshport", optional=true)
    String sshport;

    @Param(name="sshuser", optional=true)
    String sshuser;

    @Param(name="sshnodehost", optional=true)
    String sshnodehost;

    @Param(name="sshkeyfile", optional=true)
    String sshkeyfile;

    @Param(name = "sshpassword", optional = true, password=true)
     String sshpassword;

    @Param(name = "sshkeypassphrase", optional = true, password=true)
     String sshkeypassphrase;

    @Param(name = "windowsdomain", optional = true)
     String windowsdomain;

    @Param(name = "type", optional=true)
     String type;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Logger logger= context.getLogger();

        Node node= nodes.getNode(name);
        if (node == null) {
            //node doesn't exist
            String msg = Strings.get("noSuchNode", name);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
        //validate installdir if passed and running on localhost
        if (StringUtils.ok(nodehost)){
            if (NetUtils.isThisHostLocal(nodehost) && StringUtils.ok(installdir)){
                TokenResolver resolver = null;

                // Create a resolver that can replace system properties in strings
                Map<String, String> systemPropsMap =
                        new HashMap<String, String>((Map)(System.getProperties()));
                resolver = new TokenResolver(systemPropsMap);
                String resolvedInstallDir = resolver.resolve(installdir);
                File actualInstallDir = new File( resolvedInstallDir+"/" + NodeUtils.LANDMARK_FILE);


                if (!actualInstallDir.exists()){
                    report.setMessage(Strings.get("invalid.installdir",installdir));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }
        // If the node is in use then we can't change certain attributes
        // like the install directory or node directory.
        if (node.nodeInUse()) {
            String badparam = null;
            String configNodedir = node.getNodeDir();
            String configInstalldir = node.getInstallDir();

            if (!allowableChange(nodedir, configNodedir)){
                badparam = "nodedir";
            }

            if (!allowableChange(installdir, configInstalldir)) {
                badparam = "installdir";
            }

            if (StringUtils.ok(badparam)) {
                String msg = Strings.get("noUpdate.nodeInUse", name, badparam);
                logger.warning(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(msg);
                return;
            }

        }

        try {
            updateNodeElement(name);
        } catch(TransactionFailure e) {
            logger.warning("failed.to.update.node " + name);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
        }


    }


    public void updateNodeElement(final String nodeName) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode() {
            @Override
            public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure {
                // get the transaction
                Transaction t = Transaction.getTransaction(param);
                if (t!=null) {
                   Nodes nodes = ((Domain)param).getNodes();
                    Node node = nodes.getNode(nodeName);
                    Node writeableNode = t.enroll(node);
                    if (windowsdomain != null)
                        writeableNode.setWindowsDomain(windowsdomain);
                    if (nodedir != null)
                        writeableNode.setNodeDir(nodedir);
                    if (nodehost != null)
                        writeableNode.setNodeHost(nodehost);
                    if (installdir != null)
                        writeableNode.setInstallDir(installdir);
                    if (type != null)
                        writeableNode.setType(type);
                    if (sshport != null || sshnodehost != null ||sshuser != null || sshkeyfile != null){
                        SshConnector sshC = writeableNode.getSshConnector();
                        if (sshC == null)  {
                            sshC =writeableNode.createChild(SshConnector.class);
                        }else
                            sshC = t.enroll(sshC);

                        if (sshport != null)
                            sshC.setSshPort(sshport);
                        if(sshnodehost != null)
                            sshC.setSshHost(sshnodehost);

                        if (sshuser != null || sshkeyfile != null || sshpassword != null || sshkeypassphrase != null ) {
                            SshAuth sshA = sshC.getSshAuth();
                            if (sshA == null) {
                               sshA = sshC.createChild(SshAuth.class);
                            } else
                                sshA = t.enroll(sshA);

                            if (sshuser != null)
                                sshA.setUserName(sshuser);
                            if (sshkeyfile != null)
                                sshA.setKeyfile(sshkeyfile);
                            if(sshpassword != null)
                                sshA.setPassword(sshpassword);
                            if(sshkeypassphrase != null)
                                sshA.setKeyPassphrase(sshkeypassphrase);
                            sshC.setSshAuth(sshA);
                        }
                        writeableNode.setSshConnector(sshC);

                    }

                }
                return Boolean.TRUE;
            }

        }, domain);
    }

    /**
     * If the node is in use, is it OK to change currentvalue to newvalue?
     */
    private static boolean allowableChange(String newvalue, String currentvalue) {

        // If the new value is not specified, then we aren't changing anything
        if (newvalue == null) {
            return true;
        }

        // If the current (config) value is null or "" then let it be changed.
        // We need to do this for the offline config case where the user has
        // created a config node with no values, created instances using those
        // nodes, then updates the values later. This has the undersireable
        // effect of letting you, for example, set a nodedir on a node
        // that was created without one.
        if (!StringUtils.ok(currentvalue)) {
            return true;
        }

        // If the values are the same, then we aren't changing anything.
        if (newvalue.equals(currentvalue)) {
            return true;
        }

        if (newvalue.contains("$") || currentvalue.contains("$")) {
            // One or both of the values may contain an unexpanded
            // property. Expand them then compare
            Map<String, String> systemPropsMap =
                        new HashMap<String, String>((Map)(System.getProperties()));
            TokenResolver resolver = new TokenResolver(systemPropsMap);
            newvalue = resolver.resolve(newvalue);
            currentvalue = resolver.resolve(currentvalue);
            return newvalue.equals(currentvalue);
        }

        // Values don't match.
        return false;
    }

}
