/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.connectors.admin.cli;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.connectors.config.AdminObjectResource;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Admin Object command
 *
 */
@TargetType(value={CommandTarget.DAS,CommandTarget.CONFIG, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@ExecuteOn(value={RuntimeType.ALL})
@Service(name="delete-admin-object")
@PerLookup
@I18n("delete.admin.ojbect")
public class DeleteAdminObject implements AdminCommand {
    private static final Logger LOG = System.getLogger(DeleteAdminObject.class.getName());
    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(DeleteAdminObject.class);

    @Param(optional = true, defaultValue = CommandTarget.TARGET_SERVER)
    private String target;

    @Param(name="jndi_name", primary=true)
    private String jndiName;

    @Inject
    private org.glassfish.resourcebase.resources.admin.cli.ResourceUtil resourceUtil;

    @Inject
    private Domain domain;

    @Inject
    private ServerEnvironment environment;


    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        LOG.log(Level.DEBUG, "execute(context={0}); jndiName={1}, target={2}", context, jndiName, target);
        final ActionReport report = context.getActionReport();
        if (jndiName == null) {
            report.setMessage(I18N.getLocalString("delete.admin.object.noJndiName",
                            "No JNDI name defined for administered object."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // ensure we already have this resource
        final SimpleJndiName simpleJndiName = new SimpleJndiName(jndiName);
        if (!isResourceExists(domain.getResources(), simpleJndiName)) {
            report.setMessage(I18N.getLocalString("delete.admin.object.notfound",
                    "An administered object named {0} does not exist.", jndiName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (environment.isDas()) {
            if (domain.getConfigNamed(target)!=null) {
                if (!resourceUtil.getTargetsReferringResourceRef(simpleJndiName).isEmpty()) {
                    report.setMessage(I18N.getLocalString("delete.admin.object.resource-ref.exist",
                            "admin-object [ {0} ] is referenced in an " +
                                    "instance/cluster target, Use delete-resource-ref on appropriate target",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            } else {
                if (!resourceUtil.isResourceRefInTarget(simpleJndiName, target)) {
                    report.setMessage(I18N.getLocalString("delete.admin.object.no.resource-ref",
                            "admin-object [ {0} ] is not referenced in target [ {1} ]",
                            jndiName, target));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;

                }

                if (resourceUtil.getTargetsReferringResourceRef(simpleJndiName).size() > 1) {
                    report.setMessage(I18N.getLocalString("delete.admin.object.multiple.resource-refs",
                            "admin-object [ {0} ] is referenced in multiple " +
                                    "instance/cluster targets, Use delete-resource-ref on appropriate target",
                            jndiName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
        }

        try {
            // delete resource-ref
            if (!CommandTarget.TARGET_DOMAIN.equals(target)) {
                resourceUtil.deleteResourceRef(simpleJndiName, target);
            }

            // delete admin-object-resource
            SingleConfigCode<Resources> configCode = param -> {
                Resource resource = domain.getResources().getResourceByName(AdminObjectResource.class, simpleJndiName);
                return param.getResources().remove(resource);
            };
            if (ConfigSupport.apply(configCode, domain.getResources()) == null) {
                report.setMessage(I18N.getLocalString("delete.admin.object.fail",
                                "Unable to delete administered object {0}", jndiName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        } catch(TransactionFailure tfe) {
            report.setMessage(I18N.getLocalString("delete.admin.object.fail",
                            "Unable to delete administered object {0}", jndiName)
                            + " " + tfe.getLocalizedMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }

        report.setMessage(I18N.getLocalString("delete.admin.object.success",
                "Administered object {0} deleted", jndiName));
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private boolean isResourceExists(Resources resources, SimpleJndiName simpleJndiName) {
        return resources.getResourceByName(AdminObjectResource.class, simpleJndiName) != null;
    }
}
