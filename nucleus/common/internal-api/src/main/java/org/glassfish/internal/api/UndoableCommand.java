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

package org.glassfish.internal.api;

import com.sun.enterprise.config.serverbeans.Server;

import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.IfFailure;
import org.glassfish.api.admin.ParameterMap;

/**
 * Interface that augment the AdminCommand responsibilities by adding the ability
 * to undo a previously successful execution of an administrative command.
 *
 * <p>The ability to rollback is not meant to be used as an exception handling
 * mechanism while in the {@link AdminCommand#execute(AdminCommandContext)} invocation.
 * The ability to rollback is meant for undoing a successful command execution that
 * need to be roll-backed for reasons outside of the knowledge of the command
 * implementation.
 *
 * <p>Roll-backing can be very useful in clustering mode where actions can be performed
 * successfully on some instances but fail on others necessitating to rollback the
 * entire set of instances to a state previous to the action execution.
 *
 * <p>The implementations of this interface must retain any pertinent information necessary
 * to undo the command within its instance context. Therefore all UndoableCommand implementations
 * must have a {@link org.glassfish.hk2.api.PerLookup} scope otherwise the system will flag it
 * as an error and will refuse to execute the command.
 *
 * <p>An undo-able command has a slightly more complicated set of phases execution as compared
 * to the AdminCommand.
 *
 * <p>During the first phase, called the prepare phase, the framework will call
 * this command prepare method as well as all supplemented commands prepare methods (if such
 * supplemented commands implement the UndoableCommand interface). If the prepare phase is
 * not successful, the command execution stops here and the command feedback is returned to the
 * initiator.
 *
 * <p>Once the prepare phase has succeeded, the normal {@link AdminCommand#execute(AdminCommandContext)}
 * method is invoked (and any supplemented methods).
 *
 * <p>If the framework is electing that successful commands execution need to be rolled back, it will
 * call the {@link #undo(AdminCommandContext, ParameterMap, List<Server>)} method on the same instance that was used for the
 * {@link #execute(AdminCommandContext)} invocation, as well as any supplemented commands that implement
 * this interface.
 *
 * @author Jerome Dochez
 */
public interface UndoableCommand extends AdminCommand {

    /**
     * Checks whether the command execution has a chance of success before the execution is
     * attempted. This could be useful in clustering environment where you must check certain
     * pre-conditions before attempting to run an administrative change.
     *
     * <p>For instance, the change-admin-password should probably not be attempted if all the
     * servers instances are on-line and can be notified of the change.
     *
     * <p>No changes to the configuration should be made within the implementation of the
     * prepare method since {@link #undo(AdminCommandContext, ParameterMap, List<Server>)} will not be called if the command
     * execution stops at the prepare phase.
     *
     * <p>Note that if, as part of prepare, remote instances have to be contacted, then it is the responsibility of
     * the command implementation to invoke remote instaces for such verification.
     *
     * The framework will call prepare() method in DAS before execution of the main command
     * and the execution of the main command will happen only if the prepare() call returns ActionReport.ExitCode.SUCCESS
     * on DAS.
     *
     * @param context the command's context
     * @param parameters parameters to the commands.
     */
    @IfFailure(FailurePolicy.Error)
    public ActionReport.ExitCode prepare(AdminCommandContext context, ParameterMap parameters);

    /**
     * Undo a previously successful execution of the command implementation. The context
     * for undoing the administrative operation should be obtained from either the
     * parameters passed to the command execution or the command instance context.
     * The list of servers indicates to the command implementation on which servers the command succeeded
     * The command implementation is guaranteed that the  {@link #undo(AdminCommandContext, ParameterMap, List<Server>)}
     * is called on DAS if the main command execution failed on one instance but it is the responsiblity of the command
     * implementation to invoke remote instances to do the actual undo if required
     *
     * @param context the command's context
     * @param parameters parameters passed to the command.
     * @param instances instances on which the command succeeded
     */
    public void undo(AdminCommandContext context, ParameterMap parameters, List<Server> instances);
}
