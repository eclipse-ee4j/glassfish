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

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.RefContainer;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerResource;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Create Resource Ref Command
 *
 * @author Jennifer Chou, Jagadish Ramu
 *
 */
@TargetType(value = { CommandTarget.CONFIG, CommandTarget.DAS, CommandTarget.CLUSTER, CommandTarget.STANDALONE_INSTANCE })
@RestEndpoints({
        @RestEndpoint(configBean = Resources.class, opType = RestEndpoint.OpType.POST, path = "create-resource-ref", description = "create-resource-ref") })
@org.glassfish.api.admin.ExecuteOn(value = { RuntimeType.DAS, RuntimeType.INSTANCE })
@Service(name = "create-resource-ref")
@PerLookup
@I18n("create.resource.ref")
public class CreateResourceRef implements AdminCommand, AdminCommandSecurity.Preauthorization, AdminCommandSecurity.AccessCheckProvider {
    private static final Logger LOG = System.getLogger(CreateResourceRef.class.getName());

    @Param(optional = true, defaultValue = "true")
    private Boolean enabled;

    @Param(optional = true, defaultValue = CommandTarget.TARGET_SERVER)
    private String target;

    @Param(name = "reference_name", primary = true)
    private String refName;

    @Inject
    private Domain domain;

    @Inject
    private ServiceLocator locator;

    @Inject
    private ConfigBeansUtilities configBeansUtilities;

    private String commandName;

    private CommandTarget[] targets;

    private boolean isTargetValid;

    private RefContainer refContainer;

    @AccessRequired.To("refer")
    private Resource resourceOfInterest;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        resourceOfInterest = getResourceByIdentity(refName);
        if (resourceOfInterest == null) {
            report.setMessage(MessageFormat.format("Resource {0} does not exist", refName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }

        refContainer = chooseRefContainer(context); // also sets isTargetValid
        LOG.log(Level.DEBUG, "refContainer={0}, refContainer.resourceRefs={1}", refContainer,
            refContainer.getResourceRefNames());
        if (!isTargetValid) {
            report.setMessage(MessageFormat.format(
                "Resource {0} has invalid target {1} to create resource-ref. Valid targets are: {2}.", refName, target,
                targets));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }

        return refContainer != null;
    }

    @Override
    public Collection<AccessCheck<?>> getAccessChecks() {
        final Collection<AccessCheck<?>> accessChecks = new ArrayList<>();
        accessChecks.add(new AccessCheck<>(
                AccessRequired.Util.resourceNameFromConfigBeanType(refContainer, null /* collection name */, ResourceRef.class), "create"));
        return accessChecks;
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

        if (isResourceRefAlreadyPresent()) {
            report.setMessage(MessageFormat.format("Resource ref {0} already exists for target {1}", refName, target));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        try {
            createResourceRef();
            // create new ResourceRef for all instances of Cluster, if it's a cluster
            if (refContainer instanceof Cluster && isElegibleResource(refName)) {
                Target tgt = locator.getService(Target.class);
                List<Server> instances = tgt.getInstances(target);
                for (Server svr : instances) {
                    svr.createResourceRef(enabled.toString(), SimpleJndiName.of(refName));
                }
            }
            ActionReport.ExitCode ec = ActionReport.ExitCode.SUCCESS;
            report.setMessage(MessageFormat.format("Resource ref {0} created successfully.", refName));
            report.setActionExitCode(ec);
        } catch (TransactionFailure tfe) {
            report.setMessage(MessageFormat.format("Resource ref {0} creation failed", refName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(tfe);
        }
    }

    private boolean isElegibleResource(String refName) {
        SimpleJndiName jndiName = SimpleJndiName.of(refName);
        return isBindableResource(jndiName) || isServerResource(jndiName);
    }

    private boolean isResourceRefAlreadyPresent() {
        for (ResourceRef rr : refContainer.getResourceRef()) {
            if (rr.getRef().equals(refName)) {
                return true;
            }
        }
        return false;
    }

    private void createResourceRef() throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<RefContainer>() {

            @Override
            public Object run(RefContainer param) throws PropertyVetoException, TransactionFailure {

                ResourceRef newResourceRef = param.createChild(ResourceRef.class);
                newResourceRef.setEnabled(enabled.toString());
                newResourceRef.setRef(refName);
                param.getResourceRef().add(newResourceRef);
                return newResourceRef;
            }
        }, refContainer);
    }

    private RefContainer chooseRefContainer(final AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        Class<?>[] allInterfaces = resourceOfInterest.getClass().getInterfaces();
        for (Class<?> resourceInterface : allInterfaces) {
            ResourceConfigCreator resourceConfigCreator = resourceInterface.getAnnotation(ResourceConfigCreator.class);
            if (resourceConfigCreator != null) {
                commandName = resourceConfigCreator.commandName();
            }
        }

        if (commandName == null) {
            report.setMessage(MessageFormat.format("Resource ref {0} creation failed", refName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return null;
        }
        List<ServiceHandle<?>> serviceHandles = locator.getAllServiceHandles(new Filter() {
            @Override
            public boolean matches(Descriptor arg0) {
                String name = arg0.getName();
                if (name != null && name.equals(commandName)) {
                    return true;
                }
                return false;
            }
        });
        for (ServiceHandle<?> handle : serviceHandles) {
            ActiveDescriptor<?> descriptor = handle.getActiveDescriptor();
            if (descriptor.getName().equals(commandName)) {
                if (!descriptor.isReified()) {
                    locator.reifyDescriptor(descriptor);
                }
                AdminCommand service = locator.<AdminCommand>getService(descriptor.getImplementationClass());
                if (service != null) {
                    TargetType targetType = descriptor.getImplementationClass().getAnnotation(TargetType.class);
                    targets = targetType.value();
                    break;
                }
            }
        }

        isTargetValid = validateTarget(target, targets);
        if (!isTargetValid) {
            return null;
        }

        Config config = domain.getConfigs().getConfigByName(target);
        if (config != null) {
            return config;
        }
        Server server = configBeansUtilities.getServerNamed(target);
        if (server != null) {
            return server;
        }
        Cluster cluster = domain.getClusterNamed(target);
        return cluster;
    }

    private boolean isBindableResource(SimpleJndiName name) {
        return domain.getResources().getResourceByName(BindableResource.class, name) != null;
    }

    private boolean isServerResource(SimpleJndiName name) {
        return domain.getResources().getResourceByName(ServerResource.class, name) != null;
    }

    private Resource getResourceByIdentity(String id) {
        for (Resource resource : domain.getResources().getResources()) {
            if (resource.getIdentity().equals(id)) {
                return resource;
            }
        }
        return null;
    }

    private boolean validateTarget(String target, CommandTarget[] targets) {
        List<String> validTarget = new ArrayList<>();

        for (CommandTarget commandTarget : targets) {
            validTarget.add(commandTarget.name());
        }

        if (target.equals(CommandTarget.TARGET_DOMAIN)) {
            return validTarget.contains(CommandTarget.DOMAIN.name());
        } else if (target.equals(CommandTarget.TARGET_SERVER)) {
            return validTarget.contains(CommandTarget.DAS.name());
        } else if (domain.getConfigNamed(target) != null) {
            return validTarget.contains(CommandTarget.CONFIG.name());
        } else if (domain.getClusterNamed(target) != null) {
            return validTarget.contains(CommandTarget.CLUSTER.name());
        } else if (domain.getServerNamed(target) != null) {
            return validTarget.contains(CommandTarget.STANDALONE_INSTANCE.name());
        } else if (domain.getClusterForInstance(target) != null) {
            return validTarget.contains(CommandTarget.CLUSTERED_INSTANCE.name());
        } else if (domain.getNodeNamed(target) != null) {
            return validTarget.contains(CommandTarget.NODE.name());
        }

        return false;
    }
}
