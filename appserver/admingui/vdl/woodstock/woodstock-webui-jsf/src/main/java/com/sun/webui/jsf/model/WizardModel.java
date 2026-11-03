/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.model;

import com.sun.webui.jsf.component.Wizard;
import com.sun.webui.jsf.component.WizardStep;
import com.sun.webui.jsf.event.WizardEvent;

import jakarta.faces.component.StateHolder;

import java.util.Iterator;

/**
 * Defines an interface for control a sequence of WizardStep components through
 * a Wizard component. A Wizard component delegates to a WizardModel instance
 * for control and navigation through a set WizardStep instances.
 */
public interface WizardModel extends StateHolder {

    /**
     * This method is called when the wizard instance has completed assembling
     * any child components.
     *
     * @param wizard The Wizard instance owning this model instance.
     */
    void initialize(Wizard wizard);

    // It's not clear how the model should be allowed to abort
    // the wizard at this point. It could throw an exception
    // that is caught by the wizard broadcasting this event
    // fires a cancel or fatal event, allowing the model
    // to "clean" up. Then using a message in the exception
    // the wizard propagates that messsage to a log or the user, how ?
    //
    /**
     * Handle the following {@code WizardEvent} events.
     * <ul>
     * <li>WizardEvent.CANCEL</li>
     * <li>WizardEvent.CLOSE</li>
     * <li>WizardEvent.FINISH</li>
     * <li>WizardEvent.GOTOSTEP</li>
     * <li>WizardEvent.HELPTAB</li>
     * <li>WizardEvent.NEXT</li>
     * <li>WizardEvent.PREVIOUS</li>
     * <li>WizardEvent.STEPSTAB</li>
     * </ul>
     *
     * @param event wizard event
     * @return boolean
     */
    boolean handleEvent(WizardEvent event);

    /**
     * Based on the current state of the Wizard, return an Iterator of the
     * current sequence of WizardSteps.
     *
     * @return Iterator
     */
    Iterator getWizardStepIterator();

    /**
     * Return a WizardStepList of WizardStepListItem instances.
     *
     * @return WizardStepList
     */
    WizardStepList getWizardStepList();

    /**
     * Return the first WizardStep instance of the sequence.
     *
     * @return WizardStep
     */
    WizardStep getFirstStep();

    /**
     * Return the WizardStep instance preceding the specified step. If there is
     * no previous step return null.
     *
     * @param step The step following the returned WizardStep.
     * @return WizardStep
     */
    WizardStep getPreviousStep(WizardStep step);

    /**
     * Return the WizardStep instance following the specified step.
     *
     * @param step The step preceding the returned WizardStep.
     * @return WizardStep
     */
    WizardStep getNextStep(WizardStep step);

    /**
     * Return the step currently being preformed.
     *
     * @return WizardStep
     */
    WizardStep getCurrentStep();

    /**
     * Return {@code true} if any of the steps have step help. If any of the
     * steps have step help, this method should return true, unless no step help
     * should be shown for the wizard. This method may only be called once.
     *
     * @return {@code true} if has step help, {@code false} otherwise
     */
    boolean hasStepHelp();

    /**
     * Return {@code true} if step is the current step, else {@code false}.
     *
     * @param step The step to check.
     * @return {@code true} if is current step, {@code false} otherwise
     */
    boolean isCurrentStep(WizardStep step);

    /**
     * Return {@code true} if step is the finish step, else {@code false}. The
     * finish step has a button called "Finish" instead of "Next". The finish
     * step performs the task based on the data collected in previous steps.
     *
     * @param step The step to check.
     * @return {@code true} if the given step is finished, {@code false}
     * otherwise
     */
    boolean isFinishStep(WizardStep step);

    /**
     * Return {@code true} if step is the results step, else {@code false}. The
     * Results step follows the Finish step and displays only a "Close" button.
     * It displayed results of the task performed in the Finish step.
     *
     * @param step The step to check.
     * @return {@code true} if the given step is the results step, {@code false}
     * otherwise
     */
    boolean isResultsStep(WizardStep step);

    /**
     * Return {@code true} if step is a branching step, else {@code false}. A
     * branching step acts as a step "placeholder" and informs the user that the
     * steps following this step are determined by the data entered in this or
     * previous steps. Text should be provided from the
     * {@code getPlaceholderText} for this step, describing the branch.
     *
     * @param step The step to check.
     * @return {@code true} if the given step is a branch step, {@code false}
     * otherwise
     */
    boolean isBranch(WizardStep step);

    /**
     * Return a description of this branch step.
     *
     * @param step A branching step.
     * @return String
     */
    String getPlaceholderText(WizardStep step);

    /**
     * Return {@code true} if step is a sub-step step, else {@code false}. A
     * sub-step is a step or one of a series of sub-steps, that is the same in
     * every instance of this wizard. Unlike the branch step, sub-step sequences
     * are always the same but may or may not be performed based on previous
     * steps.
     *
     * @param step The step to check.
     * @return {@code true} if the given step is a sub step, {@code false}
     * otherwise
     */
    boolean isSubstep(WizardStep step);

    /**
     * Return {@code true} if the user can navigate to this step out of
     * sequence, else {@code false}.Typically this method is called to determine
     * if a previous step should be rendered such that the user can select it
     * and navigate back to that step. Its possible that some wizards may also
     * allow forward navigation.
     *
     * @param step The step to check.
     * @return boolean
     */
    boolean canGotoStep(WizardStep step);

    /**
     * Return {@code true} if the previous button should be disabled for this
     * step, else {@code false}. Typically the first step of a sequence should
     * return true, since there usually isn't a step before the first step.
     *
     * @param step The step to check.
     * @return boolean
     */
    boolean isPreviousDisabled(WizardStep step);

    /**
     * Return {@code true} if the next button should be disabled for this step,
     * else {@code false}.
     *
     * @param step The step to check.
     * @return {@code true} if the next button should be disabled for this step,
     * {@code false} otherwise
     */
    boolean isNextDisabled(WizardStep step);

    /**
     * Return {@code true} if the finish button should be disabled for this
     * step, else {@code false}.
     *
     * @param step The step to check.
     * @return {@code true} if the finish button should be disabled for this
     * step, else {@code false}
     */
    boolean isFinishDisabled(WizardStep step);

    /**
     * Return {@code true} if the cancel button should be disabled for this
     * step, else {@code false}.
     *
     * @param step The step to check.
     * @return {@code true} if the cancel button should be disabled for this
     * step, else {@code false}
     */
    boolean isCancelDisabled(WizardStep step);

    /**
     * Return {@code true} if the close button should be disabled for this step,
     * else false.
     *
     * @param step The step to check.
     * @return {@code true} if the close button should be disabled for this
     * step, else false
     */
    boolean isCloseDisabled(WizardStep step);

    /**
     * Return {@code true} if the previous button should be rendered for this
     * step, else {@code false}. Typically this method returns {@code true} for
     * all steps except for steps that are results steps.
     *
     * @param step The step to check.
     * @return {@code true} if the previous button should be rendered for this
     * step, else {@code false}
     */
    boolean hasPrevious(WizardStep step);

    /**
     * Return {@code true} if the next button should be rendered for this step,
     * else {@code false}. Typically this method returns {@code true} for all
     * steps except for steps that are finish or results steps.
     *
     * @param step The step to check.
     * @return {@code true} if the next button should be rendered for this step,
     * else {@code false}
     */
    boolean hasNext(WizardStep step);

    /**
     * Return {@code true} if the cancel button should be rendered for this
     * step, else {@code false}. Typically this method returns {@code true} for
     * all steps except for steps that are results steps.
     *
     * @param step The step to check.
     * @return {@code true} if the cancel button should be rendered for this
     * step, else {@code false}
     */
    boolean hasCancel(WizardStep step);

    /**
     * Return {@code true} if the close button should be rendered for this step,
     * else {@code false}. Typically this method returns {@code true} only for
     * the results step.
     *
     * @param step The step to check.
     * @return {@code true} if the close button should be rendered for this
     * step, else {@code false}
     */
    boolean hasClose(WizardStep step);

    /**
     * Return {@code true} if the finish button should be rendered for this
     * step, else {@code false}.Typically this method returns {@code true} only
     * for the finish step.
     *
     * @param step The step to check.
     * @return {@code true} if the finish button should be rendered for this
     * step, {@code false} otherwise
     */
    boolean hasFinish(WizardStep step);

    /**
     * Return {@code true} if {@code processValidators()} should be called for
     * the current step. The {@code event} argument is the event that
     * precipitated this validate call, one of:
     * <ul>
     * <li>WizardEvent.CANCEL</li>
     * <li>WizardEvent.CLOSE</li>
     * <li>WizardEvent.FINISH</li>
     * <li>WizardEvent.GOTOSTEP</li>
     * <li>WizardEvent.HELPTAB</li>
     * <li>WizardEvent.NEXT</li>
     * <li>WizardEvent.PREVIOUS</li>
     * <li>WizardEvent.STEPSTAB</li>
     * <li>WizardEvent.NOEVENT</li>
     * </ul>
     *
     * @param event The event that precipitated this call.
     * @param prematureRender Is {@code true} if rendering is occurring before
     * RENDER_RESPONSE phase.
     * @return {@code true} if {@code processValidators()} should be called for
     * the current step, {@code false} otherwise
     */
    boolean validate(int event, boolean prematureRender);

    /**
     * Return {@code true} if {@code processUpdates()} should be called for the
     * current step. The {@code event} argument is the event that precipitated
     * this update call, one of:
     * <ul>
     * <li>WizardEvent.CANCEL</li>
     * <li>WizardEvent.CLOSE</li>
     * <li>WizardEvent.FINISH</li>
     * <li>WizardEvent.GOTOSTEP</li>
     * <li>WizardEvent.HELPTAB</li>
     * <li>WizardEvent.NEXT</li>
     * <li>WizardEvent.PREVIOUS</li>
     * <li>WizardEvent.STEPSTAB</li>
     * <li>WizardEvent.NOEVENT</li>
     * </ul>
     *
     * @param event The event that precipitated this call.
     * @param prematureRender Is {@code true} if rendering is occurring before
     * RENDER_RESPONSE phase.
     * @return {@code true} is if {@code processUpdates()} should be called for
     * the current step, {@code false} otherwise
     */
    boolean update(int event, boolean prematureRender);

    /**
     * Return {@code true} if {@code processDecodes()} should be called for the
     * current step. The {@code event} argument is the event that precipitated
     * this decode call, one of:
     * <ul>
     * <li>WizardEvent.CANCEL</li>
     * <li>WizardEvent.CLOSE</li>
     * <li>WizardEvent.FINISH</li>
     * <li>WizardEvent.GOTOSTEP</li>
     * <li>WizardEvent.HELPTAB</li>
     * <li>WizardEvent.NEXT</li>
     * <li>WizardEvent.PREVIOUS</li>
     * <li>WizardEvent.STEPSTAB</li>
     * <li>WizardEvent.NOEVENT</li>
     * </ul>
     *
     * @param event The event that precipitated this call.
     * @param prematureRender Is {@code true} if rendering is occurring before
     * RENDER_RESPONSE phase.
     * @return {@code true} if {@code processDecodes()} should be called for the
     * current step, {@code false} otherwise
     */
    boolean decode(int event, boolean prematureRender);

    /**
     * Return {@code true} if the wizard has completed and there are no more
     * steps for the user to complete, else {@code false}. Typically this
     * informs the wizard that there is nothing more to render. This may cause a
     * popup wizard to be dismissed or an inline wizard to navigate to some
     * other page.
     *
     * @return {@code true} if the wizard has completed and there are no more
     * steps for the user to complete, {@code false} otherwise
     */
    boolean isComplete();

    /**
     * Called to inform the model that no more references will be made to this
     * model instance.
     */
    void complete();
}
