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
 * Wizard step list item base.
 */
public final class WizardStepListItemBase implements WizardStepListItem {

    /**
     * Step number as a {@code String}.
     */
    private final String stepNumberString;

    /**
     * Sub step flag.
     */
    private final boolean substep;

    /**
     * Branch flag.
     */
    private final boolean branch;

    /**
     * Placeholder text.
     */
    private final String placeholderText;

    /**
     * Current step.
     */
    private boolean currentStep;

    /**
     * Can go to step flag.
     */
    private boolean canGotoStep;

    /**
     * Step.
     */
    private WizardStep step;

    /**
     * Create a new instance.
     * @param newStep step
     * @param newStepNumberString step number string
     * @param newCurrentStep current step
     * @param newSubstep sub-step
     * @param newBranch branch flag
     * @param newPlaceholderText place holder text
     * @param newCanGotoStep canGotoStep flag
     */
    WizardStepListItemBase(final WizardStep newStep,
            final String newStepNumberString, final boolean newCurrentStep,
            final boolean newSubstep, final boolean newBranch,
            final String newPlaceholderText, final boolean newCanGotoStep) {

        this.step = newStep;
        this.stepNumberString = newStepNumberString;
        this.currentStep = newCurrentStep;
        this.substep = newSubstep;
        this.branch = newBranch;
        this.placeholderText = newPlaceholderText;
        this.canGotoStep = newCanGotoStep;
    }

    @Override
    public boolean isSubstep() {
        return substep;
    }

    @Override
    public boolean isBranch() {
        return branch;
    }

    @Override
    public String getPlaceholderText() {
        return placeholderText;
    }

    @Override
    public boolean isCurrentStep() {
        return currentStep;
    }

    @Override
    public void setCurrentStep(final boolean newCurrentStep) {
        this.currentStep = newCurrentStep;
    }

    @Override
    public boolean canGotoStep() {
        return canGotoStep;
    }

    @Override
    public void setCanGotoStep(final boolean newCanGotoStep) {
        this.canGotoStep = newCanGotoStep;
    }

    @Override
    public WizardStep getStep() {
        return step;
    }

    @Override
    public void setStep(final WizardStep newStep) {
        this.step = newStep;
    }

    @Override
    public String getStepNumberString() {
        return stepNumberString;
    }
}
