/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.orb.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;


@Service(name="delete-iiop-listener")
@PerLookup
@I18n("delete.iiop.listener")
@ExecuteOn(value={RuntimeType.DAS,RuntimeType.INSTANCE})
@TargetType(value={CommandTarget.CLUSTER,CommandTarget.CONFIG,
    CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE } )
public class DeleteIiopListener implements AdminCommand {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(DeleteIiopListener.class);

    @Param(name="listener_id", primary=true)
    String listener_id;

    @Param( name="target", optional=true,
        defaultValue=SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target ;

    @Inject
    ServiceLocator services ;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final Target targetUtil = services.getService(Target.class ) ;
        final Config config = targetUtil.getConfig(target) ;
        ActionReport report = context.getActionReport();
        IiopService iiopService = config.getExtensionByType(IiopService.class);

        if(!isIIOPListenerExists(iiopService)) {
            report.setMessage(localStrings.getLocalString("delete.iiop.listener" +
                ".notexists", "IIOP Listener {0} does not exist.", listener_id));
            report.setActionExitCode(ExitCode.FAILURE);
            return;
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<IiopService>() {
                @Override
                public Object run(IiopService param) throws PropertyVetoException,
                        TransactionFailure {
                    List<IiopListener> listenerList = param.getIiopListener();
                    for (IiopListener listener : listenerList) {
                        String currListenerId = listener.getId();
                        if (currListenerId != null && currListenerId.equals
                                (listener_id)) {
                            listenerList.remove(listener);
                            break;
                        }
                    }
                    return listenerList;
                }
            }, iiopService);
            report.setActionExitCode(ExitCode.SUCCESS);
        } catch(TransactionFailure e) {
            String actual = e.getMessage();
            report.setMessage(localStrings.getLocalString(
                "delete.iiop.listener.fail", "failed", listener_id, actual));
            report.setActionExitCode(ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private boolean isIIOPListenerExists(IiopService iiopService) {
        for (IiopListener listener : iiopService.getIiopListener()) {
            String currListenerId = listener.getId();
            if (currListenerId != null && currListenerId.equals(listener_id)) {
                return true;
            }
        }
        return false;
    }
}
