/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SecureAdmin;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.RealmsManager;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.beans.PropertyVetoException;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
 * Create File User Command Usage: create-file-user [--terse=false] [--echo=false] [--interactive=true] [--host localhost]
 * [--port 4848|4849] [--secure | -s] [--user admin_user] [--userpassword admin_passwd] [--passwordfile file_name] [--groups
 * user_groups[:user_groups]*] [--authrealmname authrealm_name] [--target target(Default server)] username
 *
 * @author Nandini Ektare
 */

@Service(name = "create-file-user")
@PerLookup
@I18n("create.file.user")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
@RestEndpoints({
    @RestEndpoint(configBean = AuthRealm.class, opType = RestEndpoint.OpType.POST, path = "create-user", description = "Create", params = {
        @RestParam(name = "authrealmname", value = "$parent") }) })
public class CreateFileUser implements /*UndoableCommand*/ AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateFileUser.class);

    @Param(name = "groups", optional = true, separator = ':')
    private List<String> groups = new ArrayList<String>(0); //by default, an empty list is better than a null

    //TODO: this is still a String, need to convert to char[]
    @Param(name = "userpassword", password = true)
    private String userpassword;

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

    @Inject
    private ServerEnvironment se;

    @Inject
    private AdminService adminService;

    private SecureAdmin secureAdmin = null;

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
                localStrings.getLocalString("create.file.user.filerealmnotfound", "File realm {0} does not exist", authRealmName));
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
            report.setMessage(localStrings.getLocalString("create.file.user.realmnotsupported",
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
        final String kf = keyFile;
        if (keyFile == null) {
            report.setMessage(localStrings.getLocalString("create.file.user.keyfilenotfound",
                "There is no physical file associated with this file realm {0} ", authRealmName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        boolean exists = (new File(kf)).exists();
        if (!exists) {
            report.setMessage(localStrings.getLocalString("file.realm.keyfilenonexistent",
                "The specified physical file {0} associated with the file realm {1} does not exist.", new Object[] { kf, authRealmName }));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        // Now get all inputs ready. userid and groups are straightforward but
        // password is tricky. It is stored in the file passwordfile passed
        // through the CLI options. It is stored under the name
        // AS_ADMIN_USERPASSWORD. Fetch it from there.
        final String password = userpassword; // fetchPassword(report);
        if (password == null) {
            report.setMessage(localStrings.getLocalString("create.file.user.keyfilenotreadable",
                "Password for user {0} " + "has to be specified in --userpassword option or supplied "
                    + "through AS_ADMIN_USERPASSWORD property in the file specified " + "in --passwordfile option",
                userName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        //Issue 17525 Fix - Check for null passwords for admin-realm if secureadmin is enabled
        secureAdmin = domain.getSecureAdmin();
        if ((SecureAdmin.isEnabled(secureAdmin)) && (authRealmName.equals(adminService.getAuthRealmName()))) {
            if (password.isEmpty()) {
                report.setMessage(localStrings.getLocalString("null_empty_password", "The admin user password is null or empty"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
        // now adding user
        try {
            //even though create-file-user is not an update to the security-service
            //do we need to make it transactional by referncing the securityservice
            //hypothetically ?.
            ConfigSupport.apply(new SingleConfigCode<SecurityService>() {

                @Override
                public Object run(SecurityService param) throws PropertyVetoException, TransactionFailure {
                    try {
                        realmsManager.createRealms(config);
                        //If the (shared) keyfile is updated by an external process, load the users first
                        refreshRealm(config.getName(), authRealmName);
                        final FileRealm fr = (FileRealm) realmsManager.getFromLoadedRealms(config.getName(), authRealmName);
                        CreateFileUser.handleAdminGroup(authRealmName, groups);
                        String[] groups1 = groups.toArray(new String[groups.size()]);
                        try {
                            fr.addUser(userName, password.toCharArray(), groups1);
                        } catch (BadRealmException br) {
                            //Check if the server environment is  DAS. If is not  DAS (user creation was
                            //successful on DAS), then the error is caused in the instances because of shared keyfile between
                            // DAS and cluster instances - ignore the exception for instances.

                            if (se != null && se.isDas()) {
                                throw new BadRealmException(br);
                            }
                        }
                        fr.persist();
                        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                    } catch (Exception e) {
                        String localalizedErrorMsg = (e.getLocalizedMessage() == null) ? "" : e.getLocalizedMessage();
                        report.setMessage(localStrings.getLocalString("create.file.user.useraddfailed",
                            "Adding User {0} to the file realm {1} failed", userName, authRealmName) + "  " + localalizedErrorMsg);
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setFailureCause(e);
                    }
                    return null;
                }
            }, securityService);

        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("create.file.user.useraddfailed", "Adding User {0} to the file realm {1} failed",
                userName, authRealmName) + "  " + e.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    /* private String fetchPassword(ActionReport report) {
       String password = null;
       if (userpassword != null && passwordFile != null)
           return password;
       if (userpassword != null)
           password = userpassword;
       if (passwordFile != null) {
           File passwdFile = new File(passwordFile);
           InputStream is = null;
           try {
               is = new BufferedInputStream(new FileInputStream(passwdFile));
               Properties prop = new Properties();
               prop.load(is);
               for (Enumeration e=prop.propertyNames(); e.hasMoreElements();) {
                   String entry = (String)e.nextElement();
                   if (entry.equals("AS_ADMIN_USERPASSWORD")) {
                       password = prop.getProperty(entry);
                       break;
                   }
               }
           } catch(Exception e) {
               report.setFailureCause(e);
           } finally {
               try {
                   if (is != null)
                       is.close();
               } catch(final Exception ignore){}
           }
       }
       return password;
    } */

    public static void refreshRealm(String configName, String realmName) {
        if (realmName != null && realmName.length() > 0) {
            try {
                Realm realm = Realm.getInstance(configName, realmName);

                if (realm != null) {
                    realm.refresh(configName);
                }
            } catch (com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException nre) {
                //            _logger.fine("Realm: "+realmName+" is not configured");
            } catch (com.sun.enterprise.security.auth.realm.exceptions.BadRealmException bre) {
                //            _logger.fine("Realm: "+realmName+" is not configured");
            }
        }
    }

    static void handleAdminGroup(String lr, List<String> lg) {
        String fr = "admin-realm"; //this should be a constant defined at a central place -- the name of realm for admin
        String fg = "asadmin"; //this should be a constant defined at a central place -- fixed name of admin group
        if (fr.equals(lr) && lg != null) {
            lg.clear(); //basically, we are ignoring the group specified on command line when it's admin realm
            lg.add(fg);
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
