/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.webui.jsf.component.WizardStep;

/**
 * Defines the interface of an object used to represent a WizardStep for use in
 * a WizardStepList, and for rendering a step list in the Steps pane of a
 * rendered Wizard component.
 */
public interface WizardStepListItem {

    /**
     * Returns {@code true} if this step is a sub-step.
     *
     * @return {@code true} if this step is a sub-step, {@code false} otherwise
     */
    boolean isSubstep();

    /**
     * Returns {@code true} if this step is a branch step.
     *
     * @return {@code boolean}
     */
    boolean isBranch();

    /**
     * Returns the text that should be displayed describing this branch step.
     *
     * @return String
     */
    String getPlaceholderText();

    /**
     * Returns the number of this step in the list of steps.
     *
     * @return String
     */
    String getStepNumberString();

    /**
     * Returns {@code true} if this step is the current step.
     *
     * @return {@code true} if this step is the current step, {@code false}
     * otherwise
     */
    boolean isCurrentStep();

    /**
     * If {@code currentStep} is {@code true} this WizardStepListItem represents
     * the current step in the step list. If false, it is not the current step.
     *
     * @param currentStep {@code true} if this WizardStepListItem is the current
     * step.
     */
    void setCurrentStep(boolean currentStep);

    /**
     * Returns {@code true} if this step can be navigated to out of sequence.
     *
     * @return {@code true} if this step can be navigated to out of sequence,
     * {@code false} otherwise
     */
    boolean canGotoStep();

    /**
     * If {@code canGotoStep} is true, this step can be navigated to out of
     * sequence.
     *
     * @param canGotoStep If {@code true} this step can be reached out of
     * sequence.
     */
    void setCanGotoStep(boolean canGotoStep);

    /**
     * Return the WizardStep instance that this WizardStepListItem represents.
     *
     * @return WizardStep
     */
    WizardStep getStep();

    /**
     * Set the WizardStep instance this WizardStepListItem represents.
     *
     * @param step The WizardStep instance this WizardStepListItem represents.
     */
    void setStep(WizardStep step);
}
