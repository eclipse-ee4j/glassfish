/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation.
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
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.io.File;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
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
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * Delete File User Command Usage: delete-file-user [--terse=false] [--echo=false] [--interactive=true] [--host localhost]
 * [--port 4848|4849] [--secure | -s] [--user admin_user] [--passwordfile file_name] [--authrealmname authrealm_name] [--target
 * target(Default server)] username
 *
 * @author Nandini Ektare
 */

@Service(name = "delete-file-user")
@PerLookup
@I18n("delete.file.user")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
@RestEndpoints({
    @RestEndpoint(configBean = AuthRealm.class, opType = RestEndpoint.OpType.DELETE, path = "delete-user", description = "Delete", params = {
        @RestParam(name = "authrealmname", value = "$parent") }) })
public class DeleteFileUser implements /*UndoableCommand*/ AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteFileUser.class);

    @Param(name = "authrealmname", optional = true)
    private String authRealmName;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;

    @Param(name = "username", primary = true)
    private String userName;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Domain domain;
    @Inject
    private RealmsManager realmsManager;

    @AccessRequired.To("update")
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
                localStrings.getLocalString("delete.file.user.filerealmnotfound", "File realm {0} does not exist", authRealmName));
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
            report.setMessage(localStrings.getLocalString("delete.file.user.realmnotsupported",
                "Configured file realm {0} is not supported.", fileRealmClassName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // ensure we have the file associated with the authrealm
        String keyFile = null;
        for (Property fileProp : fileAuthRealm.getProperty()) {
            if (fileProp.getName().equals("file"))
                keyFile = fileProp.getValue();
        }
        final String kFile = keyFile;
        if (keyFile == null) {
            report.setMessage(localStrings.getLocalString("delete.file.user.keyfilenotfound",
                "There is no physical file associated with this file realm {0} ", authRealmName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        boolean exists = (new File(kFile)).exists();
        if (!exists) {
            report.setMessage(localStrings.getLocalString("file.realm.keyfilenonexistent",
                "The specified physical file {0} associated with the file realm {1} does not exist.",
                new Object[] { kFile, authRealmName }));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        //even though delete-file-user is not an update to the security-service
        //do we need to make it transactional by referncing the securityservice
        //hypothetically ?.
        try {
            ConfigSupport.apply(new SingleConfigCode<SecurityService>() {
                @Override
                public Object run(SecurityService param) throws PropertyVetoException, TransactionFailure {
                    try {
                        realmsManager.createRealms(config);
                        final FileRealm fr = (FileRealm) realmsManager.getFromLoadedRealms(config.getName(), authRealmName);
                        fr.removeUser(userName);
                        fr.persist();
                        CreateFileUser.refreshRealm(config.getName(), authRealmName);
                        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                    } catch (NoSuchUserException e) {
                        report.setMessage(localStrings.getLocalString("delete.file.user.usernotfound",
                            "There is no such existing user {0} in the file realm {1}.", userName, authRealmName) + "  "
                            + e.getLocalizedMessage());
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setFailureCause(e);
                    } catch (BadRealmException e) {
                        report.setMessage(localStrings.getLocalString("delete.file.user.realmcorrupted",
                            "Configured file realm {0} is corrupted.", authRealmName) + "  " + e.getLocalizedMessage());
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setFailureCause(e);
                    } catch (Exception e) {
                        e.printStackTrace();
                        report.setMessage(localStrings.getLocalString("delete.file.user.userdeletefailed",
                            "Removing User {0} from file realm {1} failed", userName, authRealmName) + "  " + e.getLocalizedMessage());
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setFailureCause(e);
                    }
                    return null;
                }
            }, securityService);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("delete.file.user.userdeletefailed",
                "Removing User {0} from file realm {1} failed", userName, authRealmName) + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
    //    @Override
    //    public ActionReport prepare(ParameterMap parameters) {
    //        //TODO: is there a way to check if in a Cluster some
    //        //instances are down
    ////        com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
    ////        if (cluster!=null) {
    ////            List<Server> servers = cluster.getInstances();
    ////        }
    //        final ActionReport report = new ActionReport();
    //    }
    //
    //    @Override
    //    public void undo(ParameterMap parameters) {
    //        throw new UnsupportedOperationException("Not supported yet.");
    //    }
}
