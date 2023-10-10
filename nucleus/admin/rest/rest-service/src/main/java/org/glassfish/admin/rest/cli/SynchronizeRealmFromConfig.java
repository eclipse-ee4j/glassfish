/*
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

package org.glassfish.admin.rest.cli;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.security.auth.realm.Realm;
import com.sun.enterprise.security.auth.realm.RealmsManager;
import com.sun.enterprise.security.auth.realm.exceptions.BadRealmException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchRealmException;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoint.OpType;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.config.types.Property;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * returns the list of targets
 *
 * @author ludovic Champenois
 */
@Service(name = "__synchronize-realm-from-config")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG,
        CommandTarget.CLUSTERED_INSTANCE })
@RestEndpoints({ @RestEndpoint(configBean = Config.class, opType = OpType.POST, path = "synchronize-realm-from-config", params = {
        @RestParam(name = "target", value = "$parent") }) })
public class SynchronizeRealmFromConfig implements AdminCommand {

    @Inject
    com.sun.enterprise.config.serverbeans.Domain domain;
    //TODO: for consistency with other commands dealing with realms
    //uncomment this below.
    //@Param(name="authrealmname")
    @Param
    String realmName;
    @Param(name = "target", primary = true, optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    private String target;
    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;
    @Inject
    private Configs configs;
    @Inject
    RealmsManager realmsManager;
    private static final LocalStringManagerImpl _localStrings = new LocalStringManagerImpl(SupportsUserManagementCommand.class);

    @Override
    public void execute(AdminCommandContext context) {
        Config realConfig = null;

        try {
            realConfig = configs.getConfigByName(target);
        } catch (Exception ex) {
        }
        if (realConfig == null) {
            Server targetServer = domain.getServerNamed(target);
            if (targetServer != null) {
                realConfig = domain.getConfigNamed(targetServer.getConfigRef());
            }
            com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
            if (cluster != null) {
                realConfig = domain.getConfigNamed(cluster.getConfigRef());
            }
        }

        ActionReport report = context.getActionReport();
        try {
            //TODO: can i use realConfig.equals(config) instead
            if (realConfig.getName().equals(config.getName())) {
                this.setRestartRequired(report);
                return;
            }
            //this is not an active config so try and update the backend
            //directly
            Realm r = realmsManager.getFromLoadedRealms(realConfig.getName(), realmName);
            if (r == null) {
                //realm is not loaded yet
                report.setMessage(_localStrings.getLocalString("REALM_SYNCH_SUCCESSFUL",
                        "Synchronization of Realm {0} from Configuration Successful.", new Object[] { realmName }));
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                return;
            }
            //now we really need to update the realm in the backend from the config.
            realmsManager.removeFromLoadedRealms(realConfig.getName(), realmName);
            boolean done = this.instantiateRealm(realConfig, realmName);
            if (done) {
                report.setMessage(_localStrings.getLocalString("REALM_SYNCH_SUCCESSFUL",
                        "Synchronization of Realm {0} from Configuration Successful.", new Object[] { realmName }));
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                return;
            }
        } catch (BadRealmException ex) {
            //throw new RuntimeException(ex);
            report.setFailureCause(ex);
            report.setActionExitCode(ExitCode.FAILURE);
        } catch (NoSuchRealmException ex) {
            //throw new RuntimeException(ex);
            report.setFailureCause(ex);
            report.setActionExitCode(ExitCode.FAILURE);
        } catch (Exception ex) {
            report.setFailureCause(ex);
            report.setActionExitCode(ExitCode.FAILURE);
        }

    }

    private void setRestartRequired(ActionReport report) {
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport.MessagePart mp = report.getTopMessagePart();

        Properties extraProperties = new Properties();
        Map<String, Object> entity = new HashMap<String, Object>();
        mp.setMessage(_localStrings.getLocalString("RESTART_REQUIRED",
                "Restart required for configuration updates to active server realm: {0}.", new Object[] { realmName }));
        entity.put("restartRequired", "true");
        extraProperties.put("entity", entity);
        ((ActionReport) report).setExtraProperties(extraProperties);
    }

    private boolean instantiateRealm(Config cfg, String realmName) throws BadRealmException, NoSuchRealmException {
        List<AuthRealm> authRealmConfigs = cfg.getSecurityService().getAuthRealm();
        for (AuthRealm authRealm : authRealmConfigs) {
            if (realmName.equals(authRealm.getName())) {
                List<Property> propConfigs = authRealm.getProperty();
                Properties props = new Properties();
                for (Property p : propConfigs) {
                    String value = p.getValue();
                    props.setProperty(p.getName(), value);
                }
                Realm.instantiate(authRealm.getName(), authRealm.getClassname(), props, cfg.getName());
                return true;
            }
        }
        throw new NoSuchRealmException(_localStrings.getLocalString("NO_SUCH_REALM", "No Such Realm: {0}", new Object[] { realmName }));
    }
}
