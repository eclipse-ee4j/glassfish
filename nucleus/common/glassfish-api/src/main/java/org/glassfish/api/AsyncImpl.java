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

package org.glassfish.api;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandAspectBase;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.WrappedAdminCommand;
import org.jvnet.hk2.annotations.Service;

/**
 * Implementation for the @Async command capability. 
 *
 * @author tmueller
 */
@Service
public class AsyncImpl extends CommandAspectBase<Async> {
    
    private static final Logger logger = Logger.getLogger(AsyncImpl.class.getName());
    private static final ResourceBundle strings = 
            ResourceBundle.getBundle("org/glassfish/api/LocalStrings");
    
    @Override
    public WrappedAdminCommand createWrapper(final Async async, final CommandModel model, 
            final AdminCommand command, final ActionReport report) {
        return new WrappedAdminCommand(command) {

            @Override
            public void execute(final AdminCommandContext context) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            command.execute(context);
                        } catch (RuntimeException e) {
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                    }
                };
                t.setPriority(async.priority());
                t.start();

                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                report.setMessage(MessageFormat.format(strings.getString("command.launch"),
                        model.getCommandName()));
            }
        };
    }
}
