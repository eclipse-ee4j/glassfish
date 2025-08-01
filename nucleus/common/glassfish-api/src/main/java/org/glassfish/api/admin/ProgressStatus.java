/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.api.admin;

/**
 * API for providing information about work progress in {@link AdminCommand} implementations.
 *
 * <p>
 * Progress is represented by two attributes:
 * <ul>
 * <li>{@code steps}</li> - an integer value which represents units of progress (a.k.a. steps) taken during command
 * execution to show progress. Should not be greater than * {@code totalStepCount} (if defined).
 * <li>{@code message}</li> - A progress message to be sent to the client.
 * <ul>
 * <p>
 * Because {@code AdminCommand} can execute other {@code AdminCommand} and command execution on a cluster triggers the
 * replication process the {@code ProgressStatus} can have child ProgressStatus objects. Any {@code ProgressStatus} can
 * be used independently without knowledge of the parent's {@code ProgressStatus}.<br/>
 * Any child ProgressStatus objects can be <i>named</i> by its creator. The {@code name} is mainly used in connection
 * with the {@code message} attribute to define its context.
 *
 * <p>
 * <b>Usage:</b>
 * <p>
 * First inject a {@code ProgressStatus} into an {@code AdminCommand} implementation.
 * <p>
 * <blockquote>
 *
 * <pre>
 * &#064;Progress
 * private ProgressStatus ps;
 * </pre>
 *
 * </blockquote>
 *
 * Optionally pass the total step count or name to provide context of the progress (if the name is not provided it
 * defaults to the command's name).
 * <p>
 * <blockquote>
 *
 * <pre>
 *     &#064;Progress(totalStepCount=10, name="deploy")
 * </pre>
 *
 * </blockquote>
 * <p>
 *
 * Total step count can also be set via this method. This overrides the count if it was previously set (possibly via the
 * &#064;Progress annotation).
 * <p>
 * <blockquote>
 *
 * <pre>
 * setTotalStepCount(int totalStepCount);
 * </pre>
 *
 * </blockquote>
 * <p>
 * The primary use of {@code tatalStepCount} is as the denominator for computing the completion percentage as reported
 * in the commands progress report:
 * <p>
 * <blockquote>
 *
 * <pre>
 *    percent complete = current step count / total step count * 100
 * </pre>
 *
 * </blockquote>
 * <p>
 * The total step count can be changed after being set but may result in the completion percentage jumping (forward or
 * backwards).
 * <p>
 * If the total step count is not set then a completion percentage will not be available.
 *
 * <p>
 * The {@code progress()} methods are used to move progress forward (or backwards). The current step count will be
 * incremented by the number of steps indicated.
 * <p>
 * <blockquote>
 *
 * <pre>
 * progress(String message, int steps);
 *
 * progress(String message);
 *
 * progress(int steps);
 * </pre>
 *
 * </blockquote>
 * <p>
 * The version of progress that only takes a message will cause a new message to be displayed but will not change the
 * completion percentage.
 * <p>
 *
 * <p>
 * It is possible to set the current step count to a specific value, for example when an error occurs and the command
 * must repeat a set of operations.
 * <p>
 * <blockquote>
 *
 * <pre>
 * setCurrentStepCount(int stepCount);
 * </pre>
 *
 * </blockquote>
 * <p>
 * This will likely result in the overall completion percentage jumping when the next progress() message is generated.
 *
 * <p>
 * Any ProgressStatus can be completed using methods
 * <p>
 * <blockquote>
 *
 * <pre>
 * complete(String message);
 *
 * complete();
 * </pre>
 *
 * </blockquote>
 * <p>
 * Indicates the command is complete and no further progress status will be delivered. Subsequent invocations of
 * progress() will be ignored. This method also invokes {@code complete()} on all child ProgressStatus objects.
 * <p>
 *
 * <p>
 * It is also possible to create a child ProgressStatus with independent logic used to determine progress. The parent
 * {@code ProgressStatus} is not visible to the child ProgressStatus object and the child can progress somewhat
 * independently from the parent's progress. But generally the parent should not complete its progress before the
 * children have completed.
 * <p>
 * <blockquote>
 *
 * <pre>
 * createChild(String name, int allocatedSteps);
 *
 * createChild(int allocatedSteps);
 * </pre>
 *
 * </blockquote>
 * <p>
 * The name allocated to the child is used in the progress status output to define context of the {@code message}.
 * <p>
 * <blockquote>
 *
 * <pre>
 * .
 *     80%: [parent name:[child name:[...]] message
 * </pre>
 *
 * </blockquote>
 * <p>
 * The allocatedSteps parameter represents the subset of steps from the parent's allocation that will be given to the
 * child to complete.
 *
 * @see Progress
 * @author mmares
 */
public interface ProgressStatus extends ProgressEvent {

    /**
     * Number of steps necessary to complete the operation. Value is used to determine percentage of work completed. This
     * method can be used to override the totalStepCount if it was established via the {@link Progress}
     * annotation. The total step count is used as the denominator for computing the completion percentage as reported in
     * the command's progress output: {@code percent complete = current step count / total step count * 100} Note the above
     * formula is a bit more complex when child ProgressStatus objects are in use.
     * <p>
     * The total step count can be changed after being set but may result in the completion percentage jumping (forward or
     * backwards).
     * <p>
     * If the total step count is not set then a completion percentage will not be available.
     * <p>
     * It can be also set during injection using {@code totalStepCount} parameter in {@link Progress}
     * annotation.
     *
     * @param totalStepCount non-negative value defines denominator for the percentage computation
     */
    void setTotalStepCount(int totalStepCount);

    /**
     * Total step count. Used for computing the completion percentage.
     *
     * @return total step count. Negative for undefined value.
     */
    int getTotalStepCount();

    /**
     * Remaining count of steps to complete this progress.<br/>
     * {@code totalStepCount - currentStepCount - allocated step count}
     *
     * @return negative value for undefined totalStepCount. 0 value for completed progress.
     */
    int getRemainingStepCount();

    /**
     * Indicates progress occurred. The steps taken will be used to reduce the remaining step count. If the number of steps
     * taken exceeds the total step count the current step count will be normalized to the total step count. This avoids the
     * completion percentage ever exceeding 100%. The message will be sent to the client along with the completion
     * percentage if it can be computed.
     *
     * @param steps the number of steps taken. Negative steps will reduce the completion percentage. Never to non-negative
     * value.
     * @param message to be displayed by the client.
     * @param spinner {@code true} starts showing the spinner. It will be active until next progress.
     */
    void progress(int steps, String message, boolean spinner);

    /**
     * Indicates progress occurred. The steps taken will be used to reduce the remaining step count. If the number of steps
     * taken exceeds the total step count the current step count will be normalized to the total step count. This avoids the
     * completion percentage ever exceeding 100%. The message will be sent to the client along with the completion
     * percentage if it can be computed.
     *
     * @param steps the number of steps taken. Negative steps will reduce the completion percentage. Never to non-negative
     * value.
     * @param message to be displayed by the client.
     */
    void progress(int steps, String message);

    /**
     * Indicate progress occurred. The existing (prior) progress message, if available will be reused. If the number of
     * steps taken exceeds the total step count the current step count will be normalized to the total step count. This
     * avoids the completion percentage ever exceeding 100%.
     *
     * @param steps the number of steps taken. Negative steps will reduce the completion percentage. Never to non-negative
     * value.
     */
    void progress(int steps);

    /**
     * Indicate progress occurred. The completion percentage (if computable) will be displayed.
     *
     * @param message to be displayed by the client.
     */
    void progress(String message);

    /**
     * This allows the current step count to be changed to a specific value, for example when an error occurs and the
     * command must repeat a set of operations.<br/>
     * This will likely result in the overall completion percentage jumping when the next progress() message is generated.
     * If child ProgressStatus objects exist care must be taken when changing the step count value to account for steps
     * allocated to children. Generally the current step count should not be advanced beyond the number of steps allocated
     * to child ProgressStatus objects.
     *
     * @param stepCount new {@code stepCount} value. Negative is normalized to 0 greater than the total step count is
     * normalized to the total step count
     */
    void setCurrentStepCount(int stepCount);

    /**
     * Indicates the command is complete and no further progress status will be delivered. Subsequent invocations of
     * progress() will be ignored. This method also invokes {@code complete()} on all child ProgressStatus objects.
     *
     * If this method is not invoked prior to the command completing the CLI framework will implicitly invoke
     * {@code complete()} for the ProgressStatus associated with the command.
     *
     * @param message to be displayed to the user.
     */
    void complete(String message);

    /**
     * Indicates the command is complete and no further progress status will be delivered. Subsequent invocations of
     * progress() will be ignored. This method also invokes {@code complete()} on all child ProgressStatus objects.
     *
     * If this method is not invoked prior to the command completing the CLI framework will implicitly invoke
     * {@code complete()} for the ProgressStatus associated with the command.
     */
    void complete();

    /**
     * Returns true if the {@code ProgressStatus} has been marked as complete via the {@code complete()} method.
     *
     * @return if this progress was completed
     */
    boolean isComplete();

    /**
     * Create a child ProgressStatus object which can be used independently by a command's subroutine or sub-commands. <br/>
     * The name allocated to the child is used in the progress status output:<br/>
     * <p>
     * <blockquote>
     *
     * <pre>
     *     80%: [parent name:[child name: message]]
     * </pre>
     *
     * </blockquote>
     * <p>
     * The allocatedSteps parameter represents the subset of steps from the parent's allocation that will be given to the
     * child to complete. When the child has completed all its steps it will have progressed the parent's allocated steps.
     * <p>
     * <u>Example:</u> Suppose the parent sets its TotalStepCount to 100 and allocates 25 steps to a child. The child sets
     * its TotalStepCount to 100. Then for every 4 steps the child progresses it will move the parent's progress 1 step
     * given the parent only allocated a total of 25 steps to the child but the child has a total step count of
     * {@code 100: 100/25 = 4} child steps are equivalent to 1 parent step.
     *
     * Note: care must be taken when allocating steps to children. The number of steps allocated to all children of the
     * parent must not exceed the parent's total step count. Doing so may results in erroneous completion percentages.
     *
     * @param name to be associated with the child ProgressStatus. This name appears in the progress sent to the client. If
     * the name is an empty string a name for this child will not be included in the message.
     * @param allocatedSteps the number of progress steps the parent is allocating to the child.
     * @return ProgressStatus of the child
     */
    ProgressStatus createChild(String name, int allocatedSteps);

    /**
     * Create a child ProgressStatus object which can be used independently by a command's subroutine or sub-commands. <br/>
     * This version does not take a name and therefor a child name will not be use in any messages generated from this
     * ProgressStatus object.
     * <p>
     * <blockquote>
     *
     * <pre>
     *     80%: [parent name: message]
     * </pre>
     *
     * </blockquote>
     * <p>
     *
     * @param allocatedSteps the number of progress steps the parent is allocating to the child.
     * @return ProgressStatus of the child
     */
    ProgressStatus createChild(int allocatedSteps);

    /**
     * @return Id is unique for any ProgressStatuses. It is mainly used for remote communication.
     */
    String getId();

}
