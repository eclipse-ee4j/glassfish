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

import com.sun.enterprise.config.serverbeans.*;

import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.net.NetUtils;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;

import jakarta.inject.Inject;


import org.jvnet.hk2.annotations.Service;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.util.Set;
import java.util.HashSet;

/**
 * Remote AdminCommand to validate a config Node. This command is run only on DAS.
 * This command does the following:
 *
 * If the node does not exist it returns an error
 *
 * If parameters are passed to the command, such as nodehost, then it verifies
 * the parameters passed to the command match what is in the config. If the
 * config does not match the passed parameters then it is an error -- unless
 * the config has no value.
 *
 * If the node exists, but does not have some of the attributes passed to
 * the command, then the node is updated with the values passed to the command.
 *
 * @author Joe Di Pol
 */
@Service(name = "_validate-node")
@I18n("validate.node")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_validate-node",
        description="_validate-node")
})
public class ValidateNodeCommand implements AdminCommand {

    @Inject
    private Nodes nodes;

    @Inject
    private CommandRunner cr;

    @Param(name="name", primary = true)
    private String name;

    @Param(name="nodedir", optional=true)
    private String nodedir;

    @Param(name="nodehost", optional=true)
    private String nodehost;

    @Param(name = "installdir", optional=true)
    private String installdir;

    @Param(name="sshport", optional=true)
    private String sshport;

    @Param(name="sshuser", optional=true)
    private String sshuser;

    @Param(name="sshnodehost", optional=true)
    private String sshnodehost;

    @Param(name="sshkeyfile", optional=true)
    private String sshkeyfile;

    private Set<String> excludeFromUpdate = new HashSet<String>();

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Logger logger= context.getLogger();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        if (logger.isLoggable(Level.FINE))
            logger.fine(Strings.get("Validating node {0}", name));
        Node node = nodes.getNode(name);
        if (node == null) {
            //node doesn't exist
            String msg = Strings.get("noSuchNode", name);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        try {
            validateNode(node);
        } catch (CommandValidationException e) {
            logger.warning(e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
            return;
        }

        if (logger.isLoggable(Level.FINE))
            logger.fine(Strings.get(
                            "Node {0} is valid. Updating if needed", name));

        // What is there in the node is valid. Now go update anything that
        // was not there.
        CommandInvocation ci = cr.getCommandInvocation("_update-node", report, context.getSubject());
        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", name);
        if (! excludeFromUpdate.contains("installdir"))
            map.add("installdir", installdir);
        if (! excludeFromUpdate.contains("nodehost"))
            map.add("nodehost", nodehost);
        if (! excludeFromUpdate.contains("nodedir"))
            map.add("nodedir", nodedir);
        if (! excludeFromUpdate.contains("sshport"))
            map.add("sshport", sshport);
        if (! excludeFromUpdate.contains("sshuser"))
            map.add("sshuser", sshuser);
        if (! excludeFromUpdate.contains("sshkeyfile"))
            map.add("sshkeyfile", sshkeyfile);

        // Only update if there is something to do
        if ( map.size() > 1) {
            ci.parameters(map);
            ci.execute();
        }
    }

    public void validateNode(final Node node) throws
            CommandValidationException {

        String value = null;

        value = node.getNodeDir();
        if (!StringUtils.ok(nodedir) && StringUtils.ok(value)) {
            // If no nodedir was passed, but the config has a value, then
            // consider that an error (14887)
            throw new CommandValidationException(
                Strings.get("attribute.mismatch", name,
                    "nodedir", nodedir, value));
        }
        validatePathSimple("nodedir", nodedir, value);

        value = node.getNodeHost();
        validateHostname("nodehost", nodehost, value);

        value = node.getInstallDir();
        validatePathSimple("installdir", installdir, value);

        SshConnector sshc = node.getSshConnector();

        if (sshc == null) {
            return;
        }

        value = sshc.getSshPort();
        validateString("sshport", sshport, value, false);

        value = sshc.getSshHost();
        validateHostname("sshnodehost", sshnodehost, value);

        SshAuth ssha = sshc.getSshAuth();

        if (ssha == null) {
            return;
        }

        value = ssha.getUserName();
        validateString("sshuser", sshuser, value, false);

        value = ssha.getKeyfile();
        validatePath("sshkeyfile", sshkeyfile, value);
    }

    private void validatePath(String propname, String value, String configValue)
            throws CommandValidationException {

        if (!StringUtils.ok(value) || !StringUtils.ok(configValue)) {
            // If no value was passed via the CLI then we don't check it since
            // the caller doesn't want it validated.
            // If no value exists in the config, then we don't check it since
            // we will update it.
            return;
        }

        String canonicalValueFile = FileUtils.safeGetCanonicalPath(new File(value));
        String canonicalConfigValueFile = FileUtils.safeGetCanonicalPath(new File(configValue));
        if (canonicalConfigValueFile == null || canonicalValueFile== null) {
            throw new CommandValidationException(
                Strings.get("attribute.null", name,
                           propname, canonicalValueFile, canonicalConfigValueFile));
        }

        if ( !canonicalValueFile.equals(canonicalConfigValueFile) ) {
            throw new CommandValidationException(
                Strings.get("attribute.mismatch", name,
                           propname, canonicalValueFile, canonicalConfigValueFile));
        }
        // Don't update an attribute that is considered a match
        excludeFromUpdate.add(propname);

    }

    private void validatePathSimple(String propname, String value, String configValue)
            throws CommandValidationException {

        //16288 normalize paths to use '/'
        if (value != null) {
            value = FileUtils.makeForwardSlashes(value);
        }
        if (configValue != null) {
           configValue = FileUtils.makeForwardSlashes(configValue);
        }

        //ignore trailing / (16131).  Avoid using File API since we don't
        //know about remote node's filesystem
        if (value != null && value.endsWith("/")) {
            value = value.substring(0, value.length()-1);
        }
        if (configValue != null && configValue.endsWith("/")) {
            configValue = configValue.substring(0, configValue.length()-1);
        }

        //Try to normalize if one of the path values is relative to the gf install dir (16206)
        if (value != null && configValue != null) {
            File valFile = new File(value);
            File configValFile = new File(configValue);
            if (valFile.isAbsolute() && !configValFile.isAbsolute() && value.endsWith(configValue)) {
                value = configValue;
            } else if (!valFile.isAbsolute() && configValFile.isAbsolute() && configValue.endsWith(value)) {
                configValue = value;
            }
        }

        // Compares paths by just doing a string comparison. Some of the paths
        // we are comparing are valid on remote systems, so we can't do any
        // path processing
        if (OS.isWindows()) {
            validateString(propname, value, configValue, true);
        } else {
            validateString(propname, value, configValue, false);
        }
    }

    private void validateHostname(String propname,
            String value, String configValue)
            throws CommandValidationException {

        try {
            // First do a simple case insensitve string comparison. If that
            // matches then it's good enough for us.
            validateString(propname, value, configValue, true);
            return;
        } catch (CommandValidationException e) {
            // Strings don't match, but we could have a case of
            // "sidewinder" and "sidewinder.us.oracle.com". NetUtils
            // isEqual() handles this check.
            if (! NetUtils.isEqual(value, configValue)) {
                // If they both refer to the localhost then consider them
                // them same.
                if ( ! (NetUtils.isThisHostLocal(value) &&
                        NetUtils.isThisHostLocal(configValue)) ) {
                    throw new CommandValidationException(
                        Strings.get("attribute.mismatch", name,
                            propname, value, configValue));
                }
            }
        }
        // Don't update an attribute that is considered a match
        excludeFromUpdate.add(propname);
    }

    private void validateString(String propname,
            String value, String configValue, boolean ignorecase)
            throws CommandValidationException {

        if (!StringUtils.ok(value) || !StringUtils.ok(configValue)) {
            // If no value was passed via the CLI then we don't check it since
            // the caller doesn't want it validated.
            // If no value exists in the config, then we don't check it since
            // we will update it.
            return;
        }

        boolean match = false;
        if (ignorecase) {
            match = value.equalsIgnoreCase(configValue);
        } else {
            match = value.equals(configValue);
        }

        if ( !match ) {
            throw new CommandValidationException(
                Strings.get("attribute.mismatch", name,
                            propname, value, configValue));
        }
        // Don't update an attribute that is considered a match
        excludeFromUpdate.add(propname);
    }
}

