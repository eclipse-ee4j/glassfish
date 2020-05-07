/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.paas.externaldbservicetest.generatederbyvm;

import com.sun.enterprise.util.OS;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.virtualization.runtime.VirtualClusters;
import org.glassfish.virtualization.spi.AllocationConstraints;
import org.glassfish.virtualization.spi.AllocationPhase;
import org.glassfish.virtualization.spi.IAAS;
import org.glassfish.virtualization.spi.KeyValueType;
import org.glassfish.virtualization.spi.PhasedFuture;
import org.glassfish.virtualization.spi.SearchCriteria;
import org.glassfish.virtualization.spi.TemplateInstance;
import org.glassfish.virtualization.spi.TemplateRepository;
import org.glassfish.virtualization.spi.VirtualCluster;
import org.glassfish.virtualization.spi.VirtualMachine;
import org.glassfish.virtualization.util.ServiceType;
import org.glassfish.virtualization.util.SimpleSearchCriteria;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Optional;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Properties;

/**
 * @author Shalini M
 */
@Service(name = "create-derby-vm")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
public class CreateDerbyVM implements AdminCommand {

    @Param(name = "servicecharacteristics", optional = true, separator = ':')
    public Properties serviceCharacteristics;

    @Inject @Optional
    private IAAS iaas;

    @Inject @Optional
    private VirtualClusters virtualClusters;

    @Inject @Optional
    private TemplateRepository templateRepository;

    @Inject
    private CommandRunner commandRunner;

    @Param(name = "virtualcluster", optional = true, defaultValue = "db-external-service-test-cluster")
    private String virtualClusterName;

    private static final MessageFormat ASADMIN_COMMAND = new MessageFormat(
            "{0}" + File.separator + "lib" + File.separator + "nadmin" +
                    (OS.isWindows() ? ".bat" : "")); // {0} must be install root.

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            String templateId = findTemplate(serviceCharacteristics);

            TemplateInstance ti = templateRepository.byName(templateId);

            commandRunner.run("create-cluster", virtualClusterName);

            VirtualCluster vCluster = virtualClusters.byName(virtualClusterName);

            PhasedFuture<AllocationPhase, VirtualMachine> future =
                    iaas.allocate(new AllocationConstraints(ti, vCluster), null);

            VirtualMachine vm = future.get();

            runAsadminCommand("start-database", vm);
            report.setMessage("\n" + vm.getAddress().getHostAddress());

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private String findTemplate(Properties sc) {
        String templateId = null;
        if (sc != null && templateRepository != null) {
            // find the right template for the service characterstics specified.
            SearchCriteria searchCriteria = new SimpleSearchCriteria();
            searchCriteria.and(new ServiceType(sc.getProperty("service-type")));
            for (Object characteristic : sc.keySet()) {
                if (!"service-type".equalsIgnoreCase((String) characteristic)) {
                    searchCriteria.and(new KeyValueType(
                            (String) characteristic, sc.getProperty((String) characteristic)));
                }
            }
            Collection<TemplateInstance> matchingTemplates =
                    templateRepository.get(searchCriteria);
            if (!matchingTemplates.isEmpty()) {
                // TODO :: for now let us pick the first matching templates
                TemplateInstance matchingTemplate = matchingTemplates.iterator().next();
                templateId = matchingTemplate.getConfig().getName();
            } else {
                throw new RuntimeException("no template found");
            }
        }
        return templateId;
    }

    public void runAsadminCommand(String commandName, VirtualMachine virtualMachine) {
        String[] installDir = {virtualMachine.getProperty(VirtualMachine.PropertyName.INSTALL_DIR) +
                File.separator + "glassfish"};

        String[] args = {ASADMIN_COMMAND.format(installDir).toString(),
                commandName};
        try {
            String output = virtualMachine.executeOn(args);
            Object[] params = new Object[]{virtualMachine.getName(), output};
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
