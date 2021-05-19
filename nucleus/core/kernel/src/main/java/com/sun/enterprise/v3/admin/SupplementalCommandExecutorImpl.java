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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.inject.Inject;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.common.util.admin.CommandModelImpl;
import org.glassfish.common.util.admin.MapInjectionResolver;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.*;
import org.jvnet.hk2.config.InjectionManager;
import org.jvnet.hk2.config.InjectionResolver;

/**
 * An executor that executes Supplemental commands means for current command
 *
 * @author Vijay Ramachandran
 */
@Service(name="SupplementalCommandExecutorImpl")
public class SupplementalCommandExecutorImpl implements SupplementalCommandExecutor {

    @Inject
    private ServiceLocator habitat;

    @Inject
    private ServerEnvironment serverEnv;

    @Inject
    private ServerContext sc;

    private static final Logger logger = KernelLoggerInfo.getLogger();

    private static final LocalStringManagerImpl strings =
                        new LocalStringManagerImpl(SupplementalCommandExecutor.class);

    private Map<String, List<ServiceHandle<?>>> supplementalCommandsMap = null;

    public Collection<SupplementalCommand> listSuplementalCommands(String commandName) {
        List<ServiceHandle<?>> supplementalList = getSupplementalCommandsList().get(commandName);
        if (supplementalList == null) {
            return Collections.emptyList();
        }

        Collection<SupplementalCommand> result = new ArrayList<SupplementalCommand>(supplementalList.size());
        for (ServiceHandle<?> handle : supplementalList) {
            AdminCommand cmdObject = (AdminCommand) handle.getService();
            SupplementalCommand aCmd = new SupplementalCommandImpl(cmdObject);
            if( (serverEnv.isDas() && aCmd.whereToRun().contains(RuntimeType.DAS)) ||
                (serverEnv.isInstance() && aCmd.whereToRun().contains(RuntimeType.INSTANCE)) ) {
                result.add(aCmd);
            }
        }
        return result;
    }

    @Override
    public ActionReport.ExitCode execute(Collection<SupplementalCommand> suplementals, Supplemental.Timing time,
                             AdminCommandContext context, ParameterMap parameters,
                             MultiMap<String, File> optionFileMap) {
        //TODO : Use the executor service to parallelize this
        ActionReport.ExitCode finalResult = ActionReport.ExitCode.SUCCESS;
        if (suplementals == null) {
            return finalResult;
        }
        for (SupplementalCommand aCmd : suplementals) {
            if ((time.equals(Supplemental.Timing.Before) && aCmd.toBeExecutedBefore()) ||
                (time.equals(Supplemental.Timing.After) && aCmd.toBeExecutedAfter())   ||
                (time.equals(Supplemental.Timing.AfterReplication) && aCmd.toBeExecutedAfterReplication())) {
                ActionReport.ExitCode result = FailurePolicy.applyFailurePolicy(aCmd.onFailure(),
                        inject(aCmd, getInjector(aCmd.getCommand(), parameters, optionFileMap, context),
                                context.getActionReport()));
                if(!result.equals(ActionReport.ExitCode.SUCCESS)) {
                    if(finalResult.equals(ActionReport.ExitCode.SUCCESS))
                        finalResult = result;
                    continue;
                }
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(strings.getLocalString("dynamicreconfiguration.diagnostics.supplementalexec",
                            "Executing supplemental command " + aCmd.getClass().getCanonicalName()));
                }
                aCmd.execute(context);
                if(context.getActionReport().hasFailures()) {
                    result = FailurePolicy.applyFailurePolicy(aCmd.onFailure(), ActionReport.ExitCode.FAILURE);
                } else if(context.getActionReport().hasWarnings()) {
                    result = FailurePolicy.applyFailurePolicy(aCmd.onFailure(), ActionReport.ExitCode.WARNING);
                }
                if(!result.equals(ActionReport.ExitCode.SUCCESS)) {
                    if(finalResult.equals(ActionReport.ExitCode.SUCCESS))
                        finalResult = result;
                }
            }
        }
        return finalResult;
    }

    private static String getOne(String key, Map<String, List<String>> metadata) {
            if (key == null || metadata == null) return null;
            List<String> found = metadata.get(key);
            if (found == null) return null;

            if (found.isEmpty()) return null;

            return found.get(0);
    }

    /**
     * Get list of all supplemental commands, map it to various commands and cache this list
     */
    private synchronized Map<String, List<ServiceHandle<?>>> getSupplementalCommandsList() {

        if (supplementalCommandsMap != null) return supplementalCommandsMap;

        supplementalCommandsMap = new ConcurrentHashMap<String, List<ServiceHandle<?>>>();
        List<ServiceHandle<Supplemental>> supplementals = habitat.getAllServiceHandles(Supplemental.class);
        for (ServiceHandle<Supplemental> handle : supplementals) {
            ActiveDescriptor<Supplemental> inh = handle.getActiveDescriptor();
            String commandName = getOne("target", inh.getMetadata());
            if(supplementalCommandsMap.containsKey(commandName)) {
                supplementalCommandsMap.get(commandName).add(handle);
            } else {
                ArrayList<ServiceHandle<?>> inhList =
                        new ArrayList<ServiceHandle<?>>();
                inhList.add(handle);
                supplementalCommandsMap.put(commandName, inhList);
            }
        }
        return supplementalCommandsMap;
    }

    private InjectionResolver<Param> getInjector(AdminCommand command, ParameterMap parameters, MultiMap<String, File> map, AdminCommandContext context) {
        CommandModel model = command instanceof CommandModelProvider ?
            ((CommandModelProvider)command).getModel() :
            new CommandModelImpl(command.getClass());
        MapInjectionResolver injector = new MapInjectionResolver(model, parameters, map);
        injector.setContext(context);
        return injector;
    }

    private ActionReport.ExitCode inject(SupplementalCommand cmd,
            InjectionResolver<Param> injector, ActionReport subActionReport) {
        ActionReport.ExitCode result = ActionReport.ExitCode.SUCCESS;
        try {
            new InjectionManager().inject(cmd.getCommand(), injector);
        } catch (Exception e) {
            result = ActionReport.ExitCode.FAILURE;
            subActionReport.setActionExitCode(result);
            subActionReport.setMessage(e.getMessage());
            subActionReport.setFailureCause(e);
        }
        return result;
    }

    public class SupplementalCommandImpl implements SupplementalCommand  {

        private AdminCommand command;
        private Supplemental.Timing timing;
        private FailurePolicy failurePolicy;
        private List<RuntimeType> whereToRun = new ArrayList<RuntimeType>(2);
        private ProgressStatus progressStatus;
        private Progress progressAnnotation;

        private SupplementalCommandImpl(AdminCommand cmd) {
            command = cmd;
            Supplemental supAnn = cmd.getClass().getAnnotation(Supplemental.class);
            timing = supAnn.on();
            failurePolicy = supAnn.ifFailure();
            ExecuteOn onAnn = cmd.getClass().getAnnotation(ExecuteOn.class);
            progressAnnotation = cmd.getClass().getAnnotation(Progress.class);
            if (onAnn == null) {
                whereToRun.add(RuntimeType.DAS);
                whereToRun.add(RuntimeType.INSTANCE);
            } else {
                if(onAnn.value().length == 0) {
                    whereToRun.add(RuntimeType.DAS);
                    whereToRun.add(RuntimeType.INSTANCE);
                } else {
                    whereToRun.addAll(Arrays.asList(onAnn.value()));
                }
            }
        }

        @Override
        public void execute(AdminCommandContext ctxt) {
            Thread thread = Thread.currentThread();
            ClassLoader origCL = thread.getContextClassLoader();
            ClassLoader ccl = sc.getCommonClassLoader();
            if (progressStatus != null) {
                ctxt = new AdminCommandContextForInstance(ctxt, progressStatus);
            }
            if (origCL != ccl) {
                try {
                    thread.setContextClassLoader(ccl);
                    if (command instanceof AdminCommandSecurity.Preauthorization) {
                        ((AdminCommandSecurity.Preauthorization) command).preAuthorization(ctxt);
                    }
                    command.execute(ctxt);
                } finally {
                    thread.setContextClassLoader(origCL);
                }
            } else {
                if (command instanceof AdminCommandSecurity.Preauthorization) {
                    ((AdminCommandSecurity.Preauthorization) command).preAuthorization(ctxt);
                }
                command.execute(ctxt);
            }
        }

        @Override
        public AdminCommand getCommand() {
            return this.command;
        }

        @Override
        public boolean toBeExecutedBefore() {
            return timing.equals(Supplemental.Timing.Before);
        }

        @Override
        public boolean toBeExecutedAfter() {
            return timing.equals(Supplemental.Timing.After);
        }

        @Override
        public boolean toBeExecutedAfterReplication() {
            return timing.equals(Supplemental.Timing.AfterReplication);
        }

        @Override
        public FailurePolicy onFailure() {
            return failurePolicy;
        }

        @Override
        public List<RuntimeType> whereToRun() {
            return whereToRun;
        }

        @Override
        public ProgressStatus getProgressStatus() {
            return progressStatus;
        }

        @Override
        public void setProgressStatus(ProgressStatus progressStatus) {
            this.progressStatus = progressStatus;
        }

        @Override
        public Progress getProgressAnnotation() {
            return progressAnnotation;
        }

    }

}
