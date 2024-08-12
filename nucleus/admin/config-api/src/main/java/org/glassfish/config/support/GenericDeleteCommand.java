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

package org.glassfish.config.support;

import com.sun.enterprise.config.util.ConfigApiLoggerInfo;
import com.sun.enterprise.util.AnnotationUtil;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ManagedJob;
import org.glassfish.api.logging.LogHelper;
import org.glassfish.common.util.admin.GenericCommandModel;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.WriteableView;

/**
 * Implementation of the generic delete command
 *
 * @author Jerome Dochez
 */
@PerLookup
public class GenericDeleteCommand extends GenericCrudCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    @Inject
    CommandRunner runner;

    private ConfigBeanProxy parentBean;

    private ConfigBeanProxy tgt;

    private ConfigBean child;

    private String name;

    CommandModel model;
    Delete delete = null;

    @Override
    public CommandModel getModel() {
        return model;
    }

    @Override
    public void postConstruct() {

        super.postConstruct();
        delete = targetMethod.getAnnotation(Delete.class);
        resolverType = delete.resolver();
        try {
            // we pass false for "useAnnotations" as the @Param declarations on
            // the target type are not used for the Delete method parameters.
            model = new GenericCommandModel(targetType, false, delete.cluster(), delete.i18n(), new LocalStringManagerImpl(targetType),
                    habitat.<DomDocument>getService(DomDocument.class), commandName,
                    AnnotationUtil.presentTransitive(ManagedJob.class, delete.decorator()), delete.resolver(), delete.decorator());
            if (logger.isLoggable(level)) {
                for (String paramName : model.getParametersNames()) {
                    CommandModel.ParamModel param = model.getModelFor(paramName);
                    logger.log(Level.FINE, "I take {0} parameters", param.getName());
                }
            }
        } catch (Exception e) {
            String msg = localStrings.getLocalString(GenericCrudCommand.class, "GenericCreateCommand.command_model_exception",
                    "Exception while creating the command model for the generic command {0} : {1}", commandName, e.getMessage());
            LogHelper.log(logger, Level.SEVERE, ConfigApiLoggerInfo.GENERIC_CREATE_CMD_FAILED, e, new Object[] { commandName });
            throw new RuntimeException(msg, e);
        }

    }

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final Collection<AccessCheck> checks = new ArrayList<AccessCheck>();
        parentBean = habitat.getService((Class<? extends ConfigBeanProxy>) parentType);
        name = "";
        if (resolver instanceof TypeAndNameResolver) {
            name = ((TypeAndNameResolver) resolver).name();
        }
        checks.add(new AccessCheck(parentBean, targetType, name, "delete"));
        return checks;
    }

    @Override
    void prepareInjection(final AdminCommandContext ctx) {
        super.prepareInjection(ctx);
        tgt = resolver.resolve(ctx, targetType);

        if (tgt != null) {
            child = (ConfigBean) ConfigBean.unwrap(tgt);
        }
    }

    @Override
    public void execute(final AdminCommandContext context) {

        final ActionReport result = context.getActionReport();

        if (tgt == null) {

            String msg = localStrings.getLocalString(GenericDeleteCommand.class, "TypeAndNameResolver.target_object_not_found",
                    "Cannot find a {0} with a name {1}", targetType.getSimpleName(), name);
            logger.log(Level.SEVERE, ConfigApiLoggerInfo.TARGET_OBJ_NOT_FOUND,
                    new Object[] { resolver.getClass().toString(), parentType, targetType });
            result.failure(logger, msg);
            return;
        }

        try {
            ConfigBeanProxy parentProxy = child.parent().createProxy();
            ConfigSupport.apply(new SingleConfigCode<ConfigBeanProxy>() {
                @Override
                public Object run(ConfigBeanProxy parentProxy) throws PropertyVetoException, TransactionFailure {
                    ConfigSupport._deleteChild(child.parent(), (WriteableView) Proxy.getInvocationHandler(parentProxy), child);

                    DeletionDecorator<ConfigBeanProxy, ConfigBeanProxy> decorator = habitat.getService(delete.decorator());
                    if (decorator == null) {
                        String msg = localStrings.getLocalString(GenericCrudCommand.class,
                                "GenericCreateCommand.deletion_decorator_not_found",
                                "The DeletionDecorator {0} could not be found in the habitat,is it annotated with @Service ?",
                                delete.decorator().toString());
                        result.failure(logger, msg);
                        throw new TransactionFailure(msg);
                    } else {
                        // inject the decorator with any parameters from the initial CLI invocation
                        manager.inject(decorator, paramResolver);

                        // invoke the decorator
                        decorator.decorate(context, parentProxy, tgt);

                    }
                    return null;
                }
            }, parentProxy);

        } catch (TransactionFailure e) {
            String msg = localStrings.getLocalString(GenericCrudCommand.class, "GenericDeleteCommand.transaction_exception",
                    "Exception while deleting the configuration {0} :{1}", child.getImplementation(), e.getMessage());
            result.failure(logger, msg);
        }

    }

    @Override
    public Class getDecoratorClass() {
        if (delete != null) {
            return delete.decorator();
        } else {
            return null;
        }
    }
}
