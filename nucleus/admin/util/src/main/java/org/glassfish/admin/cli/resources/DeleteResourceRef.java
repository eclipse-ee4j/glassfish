/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.cli.resources;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.RefContainer;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Resource Ref Command
 *
 * @author Jennifer Chou, Jagadish Ramu
 */
@TargetType(value = { CommandTarget.CONFIG, CommandTarget.DAS, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean = Resources.class, opType = RestEndpoint.OpType.DELETE, path = "delete-resource-ref", description = "delete-resource-ref") })
@org.glassfish.api.admin.ExecuteOn(value = { RuntimeType.DAS, RuntimeType.INSTANCE })
@Service(name = "delete-resource-ref")
@PerLookup
@I18n("delete.resource.ref")
public class DeleteResourceRef implements AdminCommand, AdminCommandSecurity.Preauthorization {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(DeleteResourceRef.class);

    @Param(optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Param(name = "reference_name", primary = true)
    private String refName;

    //not needed, but mvn based test might not have initialized ConfigBeanUtilities
    @Inject
    private ConfigBeansUtilities configBeanUtilities;

    @Inject
    private ServiceLocator habitat;

    @Inject
    private Domain domain;

    @Inject
    private ConfigBeansUtilities configBeansUtilities;

    private RefContainer refContainer;

    @AccessRequired.To("delete")
    private ResourceRef resourceRef;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        refContainer = CLIUtil.chooseRefContainer(domain, target, configBeansUtilities);
        if (refContainer != null) {
            resourceRef = getResourceRef();
        }

        if (resourceRef == null) {
            setResourceRefDoNotExistMessage(context.getActionReport());
        }
        return resourceRef != null;
    }

    private ResourceRef getResourceRef() {
        for (ResourceRef rr : refContainer.getResourceRef()) {
            if (rr.getRef().equals(refName)) {
                return rr;
            }
        }
        return null;
    }

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the parameter names and the
     * values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            if (refName.equals("jdbc/__default")) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(
                        I18N.getLocalString("delete.resource.ref.jdbc.default", "Default JDBC resource ref cannot be deleted."));
                return;
            }
            if (refName.equals("jms/__defaultConnectionFactory")) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(I18N.getLocalString("delete.resource.ref.jms.default",
                        "Default JMS connection factory ref cannot be deleted."));
                return;
            }
            deleteResourceRef();
            if (refContainer instanceof Cluster) {

                // delete ResourceRef for all instances of Cluster
                Target tgt = habitat.getService(Target.class);
                List<Server> instances = tgt.getInstances(target);
                for (Server svr : instances) {
                    svr.deleteResourceRef(SimpleJndiName.of(refName));
                }
            }
        } catch (Exception e) {
            setFailureMessage(report, e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        report.setMessage(I18N.getLocalString("delete.resource.ref.success",
                "resource-ref {0} deleted successfully from target {1}.", refName, target));
    }

    private void deleteResourceRef() throws TransactionFailure {
        if (resourceRef != null) {
            ConfigSupport.apply(new SingleConfigCode<RefContainer>() {

                @Override
                public Object run(RefContainer param) {
                    return param.getResourceRef().remove(resourceRef);
                }
            }, refContainer);
        }
    }

    private void setResourceRefDoNotExistMessage(ActionReport report) {
        report.setMessage(I18N.getLocalString("delete.resource.ref.doesNotExist",
                "A resource ref named {0} does not exist for target {1}.", refName, target));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }

    private void setFailureMessage(ActionReport report, Exception e) {
        report.setMessage(I18N.getLocalString("delete.resource.ref.failed", "Resource ref {0} deletion failed", refName));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        report.setFailureCause(e);
    }
}
