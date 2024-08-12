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

package org.glassfish.api.admin;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.hk2.api.Metadata;

/**
 * Annotation to define a supplemental command
 *
 * A supplemental command runs when a main command implementation is ran, it can be used to attach behaviours to
 * existing commands without modifying the original implementation.
 *
 * <p>
 * A supplemental command can be very useful to configure extra or external components of an installation. For instance,
 * a load-balancer module can be installed when a load-balancer is added to a glassfish installation. Such module can
 * contain supplemental commands to supplement commands like "create-instance" in order to update the load-balancer
 * specific information.
 *
 * <p>
 * An implementation must use the value() attribute of the @Supplemental annotation to express the supplemented command.
 * Its value is the name of the command as defined by the supplemented command @Service annotation.
 *
 * <p>
 * Example : take a command implementation
 *
 * <pre>
 * <code>
 * &#64Service(name="randomCommand")
 * public MyRandomCommand implements AdminCommand {
 * ...
 * }
 * </code>
 * </pre>
 * <p>
 * a supplemental command may be defined as follows :
 *
 * <pre>
 * <code>
 * &#64Service(name="mySupCommand")
 * &#64Supplemental("randomCommand")
 * public MySupplementalCommand implements AdminCommand {
 * ...
 * }
 * </code>
 * </pre>
 * <p>
 * Another implementation that does not use the same parameter names as the supplemented command will need to use
 * a @Bridge annotation
 *
 * <pre>
 * <code>
 * &#64Service(name="otherSupCommand")
 * &#64Supplemental(value="randomCommand" bridge=MyParameterMapper.class)
 * public OtherSupplementedCommand implements AdminCommand {
 * ...
 * }
 * </code>
 * </pre>
 * <p>
 * There can be several supplemental commands for a command implementation.
 *
 * <p>
 * A supplemental command can be executed in "isolation" (not part of the supplemented command execution) and should not
 * make any assumption that it is called as part of its supplemented command execution. If a command should not be
 * invokable in isolation, it must not define a name() attribute on the @Service annotation :
 *
 * <pre>
 * <code>
 * &#64Service
 * &#64Supplemental("randomCommand")
 * public MySupplementalCommand implements AdminCommand {
 *  // can only be invoked as a supplemental command
 * }
 * </code>
 * </pre>
 * <p>
 * If a supplemental command is annotated with @Rollback, the annotation will be ignored when the supplemental command
 * is executed in isolation.
 *
 * <p>
 * If a supplemental command is annotated with @Rollback, it is still subject to the supplemented command
 * {@link org.glassfish.api.admin.ExecuteOn#ifFailure()} value to decide whether or not roll-backing should happen in
 * case of failure.
 *
 * <p>
 * When associating a supplemental command to a command X, it's always a good idea to associate a roll-backing
 * supplemental command to the rollbacking command of X. For instance, if an "add-lb-config" supplemental command is
 * attached to the "create-instance" command, a "delete-lb-config" supplemental command should be attached to the
 * "delete-instance" command.
 *
 * @author Jerome Dochez
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Qualifier
public @interface Supplemental {

    /**
     * enumeration of when a supplemental command can be invoked with regards to the supplemented command execution.
     */
    public enum Timing {
        Before, After, AfterReplication
    }

    /**
     * Name of the supplemented command as it can be looked up in the habitat. * Therefore the supplemented command must
     * have the {@link AdminCommand} type and the provided registration name.
     *
     * @return habitat registration name for the command
     */
    @Metadata("target")
    public String value();

    public Class<? extends ParameterBridge> bridge() default ParameterBridge.NoMapper.class;

    /**
     * Supplemental commands can be run before or after the supplemented command. Returns when this supplemental command is
     * expecting its execution.
     *
     * @return Before if it should be run before the supplemented method or After if it should run after the supplemented
     * method.
     */
    public Timing on() default Timing.After;

    /**
     * Indicates to the framework what type of action should be taken if the execution of this command was to return a
     * failure exit code. The action will apply on the supplemented command as well as all supplemental commands.
     *
     * <p>
     * If rollback is expected, the failure of this supplemental command will cause the rollbacking of all the already
     * executed supplemented commands as well as the main supplemented command.
     *
     * @return the action the framework is expected to invoke when this supplemental command execution failed.
     */
    public FailurePolicy ifFailure() default FailurePolicy.Error;

}
