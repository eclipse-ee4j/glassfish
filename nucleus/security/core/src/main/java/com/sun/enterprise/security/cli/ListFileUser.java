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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

/**
 * List File Users Command Usage: list-file-users [--terse=false] [--echo=false] [--interactive=true] [--host localhost] [--port
 * 4848|4849] [--secure | -s] [--user admin_user] [--passwordfile file_name] [--authrealmname authrealm_name] [target(Default
 * server)]
 *
 * @author Nandini Ektare
 */

@Service(name = "list-file-users")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.file.user")
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CLUSTERED_INSTANCE,
    CommandTarget.CONFIG })
@RestEndpoints({
    @RestEndpoint(configBean = AuthRealm.class, opType = RestEndpoint.OpType.GET, path = "list-users", description = "List Users", params = {
        @RestParam(name = "authrealmname", value = "$parent") }) })
public class ListFileUser implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListFileUser.class);

    @Param(name = "authrealmname", optional = true)
    private String authRealmName;

    @Param(name = "target", primary = true, optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

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
                localStrings.getLocalString("list.file.user.filerealmnotfound", "File realm {0} does not exist", authRealmName));
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

        // Get FileRealm class name, match it with what is expected.
        String fileRealmClassName = fileAuthRealm.getClassname();

        // Report error if provided impl is not the one expected
        if (fileRealmClassName != null && !fileRealmClassName.equals("com.sun.enterprise.security.auth.realm.file.FileRealm")) {
            report.setMessage(localStrings.getLocalString("list.file.user.realmnotsupported", "Configured file realm {0} is not supported.",
                fileRealmClassName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
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
            return;
        }

        boolean exists = (new File(keyFile)).exists();
        if (!exists) {
            report.setMessage(localStrings.getLocalString("file.realm.keyfilenonexistent",
                "The specified physical file {0} associated with the file realm {1} does not exist.",
                new Object[] { keyFile, authRealmName }));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        // We have the right impl so let's try to remove one
        FileRealm fr = null;
        try {
            realmsManager.createRealms(config);
            //account for updates to realms from outside this config sharing
            //same keyfile
            CreateFileUser.refreshRealm(config.getName(), authRealmName);
            fr = (FileRealm) realmsManager.getFromLoadedRealms(config.getName(), authRealmName);
            if (fr == null) {
                throw new NoSuchRealmException(authRealmName);
            }
        } catch (NoSuchRealmException e) {
            report.setMessage(localStrings.getLocalString("list.file.user.realmnotsupported", "Configured file realm {0} is not supported.",
                authRealmName) + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        try {
            Enumeration users = fr.getUserNames();
            List userList = new ArrayList();

            while (users.hasMoreElements()) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                String userName = (String) users.nextElement();
                part.setMessage(userName);
                Map userMap = new HashMap();
                userMap.put("name", userName);
                try {
                    userMap.put("groups", Collections.list(fr.getGroupNames(userName)));
                } catch (NoSuchUserException ex) {
                    // This should never be thrown since we just got the user name from the realm
                }
                userList.add(userMap);
            }
            Properties extraProperties = new Properties();
            extraProperties.put("users", userList);
            report.setExtraProperties(extraProperties);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (BadRealmException e) {
            report.setMessage(
                localStrings.getLocalString("list.file.user.realmcorrupted", "Configured file realm {0} is corrupted.", authRealmName)
                    + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
