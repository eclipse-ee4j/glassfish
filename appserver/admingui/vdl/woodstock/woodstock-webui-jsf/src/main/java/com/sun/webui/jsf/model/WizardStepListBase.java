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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Wizard step list base.
 */
public final class WizardStepListBase implements WizardStepList {

    /**
     * Dot character constant.
     */
    private static final String DOT = ".";

    /**
     * Left square bracket character constant.
     */
    private static final String SWBRACKET_OPEN = "[";

    /**
     * Right square bracket character constant.
     */
    private static final String SWBRACKET_CLOSE = "]";

    /**
     * Wizard model.
     */
    private final WizardModel wModel;

    /**
     * Current step.
     */
    private int currentStep;

    /**
     * Current step number as a {@code String}.
     */
    private String currentStepNumberString;

    /**
     * Create a new instance.
     * @param newWModel wizard model
     */
    public WizardStepListBase(final WizardModel newWModel) {
        this.wModel = newWModel;
        iterate();
    }

    @Override
    public String getCurrentStepNumberString() {
        return currentStepNumberString;
    }

    /**
     * Format the specified step number.
     * @param stepNumber step number to format
     * @return String
     */
    protected String formatStepNumber(final int stepNumber) {
        return String.valueOf(stepNumber);
    }

    /**
     * Format the specified step number and sub step number.
     * @param stepNumber step number to format
     * @param substep sub step number to format
     * @return String
     */
    protected String formatSubstepNumber(final int stepNumber,
            final int substep) {

        return String.valueOf(stepNumber).concat(DOT).
                concat(String.valueOf(substep));
    }

    /**
     * Format the specified branch.
     * @param placeholderText placeholder text
     * @return String
     */
    protected String formatBranch(final String placeholderText) {
        return SWBRACKET_OPEN.concat(placeholderText).concat(SWBRACKET_CLOSE);
    }

    /**
     * Set up the state of the list.
     */
    private void iterate() {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
    }

    @Override
    public Iterator iterator() {

        return new Iterator() {

            /**
             * Step number.
             */
            private int stepNumber;

            /**
             * Sub step number.
             */
            private int substep;

            /**
             * Step number as a {@code String}.
             */
            private String stepNumberString;

            /**
             * Placeholder text.
             */
            private String placeholderText;

            /**
             * isBranch flag.
             */
            private boolean isBranch;

            /**
             * Wizard step iterator.
             */
            private final Iterator wizardStepIterator =
                    wModel.getWizardStepIterator();

            @Override
            public boolean hasNext() {
                return wizardStepIterator.hasNext();
            }

            @Override
            public Object next() throws NoSuchElementException {

                if (isBranch) {
                    // Log too
                    throw new NoSuchElementException(
                            "No more steps after branch");
                }

                boolean isCurrentStep;
                try {
                    WizardStep step = (WizardStep) wizardStepIterator.next();
                    boolean isSubstep = wModel.isSubstep(step);
                    isBranch = wModel.isBranch(step);
                    placeholderText = null;
                    // A substep cannot be the first step.
                    if (isSubstep) {
                        ++substep;
                        stepNumberString =
                                formatSubstepNumber(stepNumber, substep);
                    } else if (isBranch) {
                        ++stepNumber;
                        substep = 0;
                        stepNumberString = null;
                    } else {
                        ++stepNumber;
                        substep = 0;
                        stepNumberString = formatStepNumber(stepNumber);
                    }
                    isCurrentStep = wModel.isCurrentStep(step);
                    if (isCurrentStep) {
                        currentStep = stepNumber;
                        currentStepNumberString = stepNumberString;
                    }
                    return new WizardStepListItemBase(step, stepNumberString,
                            isCurrentStep, isSubstep, isBranch, placeholderText,
                            wModel.canGotoStep(step));

                } catch (Exception e) {
                    NoSuchElementException nse = new NoSuchElementException();
                    nse.initCause(e);
                    throw nse;
                }
            }

            @Override
            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException();
            }
        };
    }
}
