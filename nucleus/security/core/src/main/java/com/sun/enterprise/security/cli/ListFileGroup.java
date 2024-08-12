/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.cli;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.auth.realm.RealmsManager;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Enumeration;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

/**
 * List File GroupsCommand Usage: list-file-groups [--terse={true|false}][ --echo={true|false} ] [ --interactive={true|false} ]
 * [--host host] [--port port] [--secure| -s ] [--user admin_user] [--passwordfile filename] [--help] [--name username]
 * [--authrealmname auth_realm_name] [ target]
 *
 * @author Nandini Ektare
 */

@Service(name = "list-file-groups")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.file.group")
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CLUSTERED_INSTANCE,
    CommandTarget.CONFIG })
@RestEndpoints({
    @RestEndpoint(configBean = SecurityService.class, opType = RestEndpoint.OpType.GET, path = "list-file-groups", description = "list-file-groups") })
public class ListFileGroup implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListFileGroup.class);

    @Param(name = "authrealmname", optional = true)
    private String authRealmName;

    @Param(name = "name", optional = true)
    private String fileUserName;

    @Param(name = "target", primary = true, optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Configs configs;

    @Inject
    private Domain domain;

    @Inject
    private RealmsManager realmsManager;

    @AccessRequired.To("read")
    private AuthRealm fileAuthRealm;

    private SecurityService securityService;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.chooseConfig(domain, target, context.getActionReport());
        if (config == null) {
            return false;
        }
        securityService = config.getSecurityService();
        fileAuthRealm = CLIUtil.findRealm(securityService, authRealmName);
        if (fileAuthRealm == null) {
            final ActionReport report = context.getActionReport();
            report.setMessage(
                localStrings.getLocalString("list.file.group.filerealmnotfound", "File realm {0} does not exist", authRealmName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        /*
         * The realm might have been defaulted, so capture the actual name.
         */
        authRealmName = fileAuthRealm.getName();
        return true;
    }

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and the values the
     * parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        try {
            // Get all users of this file realm. If a username has
            // been passed in through the --name CLI option use that
            FileRealm fr = getFileRealm(securityService, fileAuthRealm, report);

            if (fr == null) {
                // the getFileRealm method would have filled
                // in the right cause of this situation
                return;
            }

            Enumeration groups = null;
            if (fileUserName != null) {
                fr.getUser(fileUserName);
                groups = fr.getGroupNames(fileUserName);
            } else {
                groups = fr.getGroupNames();
            }

            report.getTopMessagePart().setMessage(localStrings.getLocalString("list.file.group.success", "list-file-groups successful"));
            report.getTopMessagePart().setChildrenType("file-group");
            while (groups.hasMoreElements()) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage((String) groups.nextElement());
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (BadRealmException e) {
            report.setMessage(
                localStrings.getLocalString("list.file.group.realmcorrupted", "Configured file realm {0} is corrupted.", authRealmName)
                    + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        } catch (NoSuchUserException e) {
            report
                .setMessage(localStrings.getLocalString("list.file.group.usernotfound", "Specified file user {0} not found.", fileUserName)
                    + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private FileRealm getFileRealm(final SecurityService securityService, AuthRealm fileAuthRealm, ActionReport report) {
        // Get FileRealm class name, match it with what is expected.
        String fileRealmClassName = fileAuthRealm.getClassname();

        // Report error if provided impl is not the one expected
        if (fileRealmClassName != null && !fileRealmClassName.equals("com.sun.enterprise.security.auth.realm.file.FileRealm")) {
            report.setMessage(localStrings.getLocalString("list.file.user.realmnotsupported", "Configured file realm {0} is not supported.",
                fileRealmClassName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return null;
        }

        // ensure we have the file associated with the authrealm
        String keyFile = null;
        for (Property fileProp : fileAuthRealm.getProperty()) {
            if (fileProp.getName().equals("file"))
                keyFile = fileProp.getValue();
        }
        if (keyFile == null) {
            report.setMessage(localStrings.getLocalString("list.file.user.keyfilenotfound",
                "There is no physical file associated with this file realm {0} ", authRealmName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return null;
        }

        // We have the right impl so let's try to remove one
        FileRealm fr = null;
        try {
            realmsManager.createRealms(config);
            fr = (FileRealm) realmsManager.getFromLoadedRealms(config.getName(), authRealmName);
            if (fr == null) {
                throw new NoSuchRealmException(authRealmName);
            }
        } catch (NoSuchRealmException e) {
            report.setMessage(localStrings.getLocalString("list.file.user.realmnotsupported", "Configured file realm {0} is not supported.",
                authRealmName) + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
        return fr;
    }
}
