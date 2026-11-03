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
import com.sun.webui.jsf.component.WizardBranch;
import com.sun.webui.jsf.component.WizardBranchSteps;
import com.sun.webui.jsf.component.WizardStep;
import com.sun.webui.jsf.component.WizardSubstepBranch;
import com.sun.webui.jsf.event.WizardEvent;

import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * {@code WizardModelBase} is the default {@link WizardModel WizardModel}
 * instance used by the {@link Wizard Wizard}. This class's behavior supports
 * wizard functionality as defined by "Web Application Guidelines - Version
 * 3.0". The support for a step list as defined by the guidelines is
 * encapsulated in {@link WizardStepListBase WizardStepListBase}. This class
 * maintains state, controls navigation, and expects the existence of
 * {@code finish} and {@code results} wizard steps. In addition this class can
 * control whether or not steps should participate in JSF life-cycle phases,
 * see null {@link WizardModelBase#decode(int, boolean) decode},
 * {@link WizardModelBase#validate(int, boolean) validate}, and
 * {@link WizardModelBase#update(int, boolean) update}.
 * <p>
 * {@code WizardModelBase} expects a hierarchy of null
 * {@link WizardStep WizardStep}, {@link WizardBranch WizardBranch},
 * {@link WizardBranchSteps WizardBranchSteps}, and
 * {@link WizardSubstepBranch WizardSubstepBranch} components. This hierarchy
 * can be specified as children of the {@link Wizard Wizard} component or as a
 * list returned by {@link Wizard#getSteps() Wizard.getSteps()}. If
 * {@link Wizard#getSteps() getSteps} does not return null, the steps
 * represented by that value takes precedence over the existence of
 * {@link Wizard Wizard} child components.
 * </p>
 * <p>
 * The {@link Wizard Wizard} component calls the {@link #initialize(Wizard)
 * initialize} method twice during the request life-cycle. Once during the
 * RESTORE_VIEW phase, and once during the RENDER_RESPONSE phase. This is
 * necessary so the model can re-evaluate the steps if changes have occurred
 * during request processing or in the INVOKE_APPLICATION phase, which occur
 * before the RENDER_RESPONSE phase. On the first display of a wizard, the
 * {@link #initialize(Wizard) initialize} method is only called once during the
 * RENDER_RESPONSE phase.<br>
 * Note that a call to initialize during RESTORE_VIEW phase is only relevant
 * when the application is configured in client side state saving mode.
 * </p>
 * <p>
 * When the wizard receives events due to a user clicking one of the navigation
 * components, it calls {@link WizardModelBase#handleEvent(WizardEvent)
 * handleEvent} with the corresponding event and data.
 * </p>
 * <p>
 * The wizard will call the {@link WizardModelBase#complete()
 * complete} method to indicate to the model that it considers the wizard
 * session to be over and will no longer reference it during this wizard
 * session, and that it is free to de-allocate any resources.
 * </p>
 */
public class WizardModelBase implements WizardModel {

    /**
     * The current step sequence of {@link WizardStep WizardStep} instances.
     */
    private ArrayList<WizardStep> wizardSteps;

    /**
     * The current model state.
     */
    private final WizardState wizardState;

    /**
     * The current step list of {@link WizardStepListItem WizardStepListItem}
     * instances.
     */
    private WizardStepList wizardStepList;

    /**
     * Construct a {@code WizardModelBase} instance. The {@code initialize}
     * method must be called with the {@code wizard} instance when the step
     * children are available. The {@code Wizard} instance will call this method
     * at the appropriate time.
     */
    public WizardModelBase() {
        super();
        wizardState = new WizardState();
    }

    /**
     * Initialize the current set of WizardSteps. This method is called by the
     * {@link Wizard Wizard} twice. Once during RETORE_VIEW phase, and once
     * during RENDER_RESPONSE phase. This sequence affords the model the
     * opportunity to modify its state due to changes that may have occurred
     * during request processing.
     * <br>
     * Note that a call during RESTORE_VIEW phase is only relevant when the
     * application is configured in client side state saving mode.
     *
     * @param wizard The Wizard component.
     */
    @Override
    public void initialize(final Wizard wizard) {
        wizardSteps = new ArrayList<WizardStep>();

        // Steps can be obtained from both "getSteps" or
        // as children of Wizard. This model uses one or the
        // other. If wizard.getSteps() does not return null
        // it uses that list, if it returns null it checks for
        // wizard children and uses that list.
        //
        List steps = (List) wizard.getSteps();
        if (steps == null) {
            steps = (List) wizard.getChildren();
        }
        ListIterator childIterator = steps.listIterator();
        buildStepList(childIterator, wizardSteps);
        wizardStepList = new WizardStepListBase(this);
    }

    // Call recursively to collect substep and branch children
    /**
     * This method builds the current step sequence based on the state of the
     * wizard. The current sequence is determined by the values returned from
     * calls to the {@code isTaken} method on {@link WizardBranch WizardBranch},
     * {@link WizardBranchSteps WizardBranchSteps} and
     * {@link WizardSubstepBranch WizardSubstepBranch} instances in the step
     * hierarchy.<br>
     * The current step sequence is constructed according to the "Web
     * Application Guidelines - Version 3.0".<br>
     * For example if a {@link WizardBranch WizardBranch} is encountered, the
     * step sequence will end with this step. According to the guidelines, a
     * branch indicates that proceeding steps cannot be determined until data is
     * collected before this branch step is reached. When the step hierarchy is
     * evaluated again and the branch isTaken method returns true, then the
     * steps for that particular branch are added to the sequence.
     * <p>
     * This method also determines if there is step help available.<br>
     * </p>
     *
     * @param childIterator contains all the steps of the wizard.
     * @param steps ArrayList to contain the steps that are in the current
     * sequence.
     */
    protected void buildStepList(final ListIterator childIterator,
            final ArrayList<WizardStep> steps) {

        Object step;
        while (childIterator.hasNext()) {
            step = childIterator.next();
            if (step instanceof WizardBranch) {
                WizardBranch branch = (WizardBranch) step;
                if (branch.isTaken()) {
                    buildStepList(branch.getChildren().listIterator(),
                            steps);
                } else {
                    // If the branch hasn't been taken then
                    // this is the last step until it is taken.
                    //
                    steps.add((WizardStep) step);
                    break;
                }
            } else if (step instanceof WizardBranchSteps) {
                WizardBranchSteps branchSteps = (WizardBranchSteps) step;
                if (branchSteps.isTaken()) {
                    buildStepList(branchSteps.getChildren().listIterator(),
                            steps);
                }
            } else if (step instanceof WizardSubstepBranch) {
                WizardSubstepBranch substep = (WizardSubstepBranch) step;
                if (substep.isTaken()) {
                    buildStepList(substep.getChildren().listIterator(),
                            steps);
                }
            } else if (step instanceof WizardStep) {
                // If even one WizardStep has help, show help
                //
                if (wizardState.getHasStepHelp() == null) {
                    if (((WizardStep) step).getHelp() != null) {
                        wizardState.setHasStepHelp(Boolean.TRUE);
                    }
                }
                steps.add((WizardStep) step);
            }
        }
    }

    /**
     * This implementation invokes {@code ArrayList.iterator}.
     * @return Iterator
     */
    @Override
    public Iterator getWizardStepIterator() {
        return wizardSteps.iterator();
    }

    /**
     * This implementation returns the encapsulated wizard step list model.
     * @return WizardStepList
     */
    @Override
    public WizardStepList getWizardStepList() {
        return wizardStepList;
    }

    /**
     * Return the first {@link WizardStep WizardStep} instance.
     */
    @Override
    public WizardStep getFirstStep() {
        return (WizardStep) wizardSteps.get(0);
    }

    /**
     * Return the last {@link WizardStep WizardStep} instance.The step that is
     * returned is dependent on the state of the wizard. It may only be the last
     * step at this point in time. At a later point in time, it may be a
     * different step, due to the existence of branch steps.
     *
     * @return WizardStep
     */
    public WizardStep getLastStep() {
        return (WizardStep) wizardSteps.get(wizardSteps.size() - 1);
    }

    /**
     * Return the index into an {@code ArrayList} of
     * {@link WizardStep WizardStep} instances, for the step with {@code id}.
     *
     * @param id The id of a {@link WizardStep WizardStep} instance.
     * @return index
     */
    protected int getStepIndex(final String id) {
        for (int i = 0; i < wizardSteps.size(); ++i) {
            WizardStep step = (WizardStep) wizardSteps.get(i);
            if (id.equals(step.getId())) {
                return i;
            }
        }
        // What else ?
        // Need to implement errors to component
        // alerts ? FacesMessage ?
        return wizardState.getCurrentStep();
    }

    /**
     * Return the {@link WizardStep WizardStep} instance following {@code step}.
     * If there are no more steps return null.
     *
     * @param step The step preceding the returned step.
     */
    @Override
    public WizardStep getNextStep(final WizardStep step) {
        WizardStep next = null;
        try {
            int i = wizardSteps.indexOf(step);
            if (i != -1) {
                next = (WizardStep) wizardSteps.get(++i);
            }
        } catch (Exception e) {
        }
        return next;
    }

    /**
     * Return the {@link WizardStep WizardStep} instance preceding {@code step}
     * If there is no previous step return null.
     *
     * @param step The step following the returned step.
     */
    @Override
    public WizardStep getPreviousStep(final WizardStep step) {
        WizardStep previous = null;
        try {
            int i = wizardSteps.indexOf(step);
            if (i != -1) {
                previous = (WizardStep) wizardSteps.get(--i);
            }
        } catch (Exception e) {
        }
        return previous;
    }

    /**
     * Return the current {@link WizardStep WizardStep} instance.
     */
    @Override
    public WizardStep getCurrentStep() {
        WizardStep step = null;
        try {
            step = (WizardStep) wizardSteps.get(wizardState.getCurrentStep());
        } catch (Exception e) {
        }
        return step;
    }

    /**
     * Return true if {@code step} is the current step, else false.
     *
     * @param step The step to test.
     */
    @Override
    public boolean isCurrentStep(final WizardStep step) {
        boolean result = false;
        try {
            int i = wizardSteps.indexOf(step);
            result = wizardState.getCurrentStep() == i;
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * Return true if {@code step} is the step the should contain a Finish
     * button. This is the step that performs the wizard task with the data
     * collected from previous steps.
     *
     * @param step The step to identify.
     */
    @Override
    public boolean isFinishStep(final WizardStep step) {
        if (step == null) {
            return false;
        }
        return step.isFinish();
    }

    /**
     * Return true if {@code step} is the results step, else false.
     * The Results step follows the Finish step and displays
     * only  a "Close" button. It displays results of the task
     * performed in the Finish step.
     *
     * @param step The step to identify.
     */
    @Override
    public boolean isResultsStep(final WizardStep step) {
        if (step == null) {
            return false;
        }
        return step.isResults();
    }

    /**
     * Return true if {@code step} is a branching step, else false.
     * A branching step acts as a step "placeholder" and informs
     * the user that the steps following this step are determined by
     * the data entered in this or previous steps. Text should be
     * provided from the {@link #getPlaceholderText(WizardStep)
     * getPlaceHolderText} method for this step, describing the branch.
     *
     * @param step The step to identify.
     */
    @Override
    public boolean isBranch(final WizardStep step) {
        return step instanceof WizardBranch;
    }

    /**
     * Return a description of this {@link WizardBranch WizardBranch} step.
     *
     * @param step A branching step. It must be a
     * {@link WizardBranch WizardBranch} instance.
     */
    @Override
    public String getPlaceholderText(final WizardStep step) {
        if (step == null) {
            return null;
        }
        return ((WizardBranch) step).getPlaceholderText();
    }

    /**
     * Return true if {@code step} is a substep step, else false.
     * A substep is a step or one of a series of substeps,
     * that occurs in every instance of this wizard.
     * Unlike the branch step, substep sequences are always the same
     * but may or may not be performed based on previous steps.
     *
     * @param step The step to check.
     */
    @Override
    public boolean isSubstep(final WizardStep step) {
        if (step == null) {
            return false;
        }
        return step.getParent() instanceof WizardSubstepBranch;
    }

    /**
     * Return true if the user can navigate to {@code step} out
     * of sequence, else false.
     * Typically this method is called to determine if a previous
     * step should be rendered such that the user can select it
     * and navigate back to that step. Its possible that some
     * wizards may also allow forward navigation.
     * <p><em>
     * Note this method only supports navigating to steps occuring
     * before the current step.
     * </em></p>
     *
     * @param step The step to check.
     */
    @Override
    public boolean canGotoStep(final WizardStep step) {
        try {
            int i = wizardSteps.indexOf(step);
            return wizardState.getCurrentStep() > i;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Return true if the previous button should be disabled for this step, else
     * false. Typically the first step of a sequence should return true, since
     * there usually isn't a step before the first step.
     *
     * @param step The step to check.
     */
    @Override
    public boolean isPreviousDisabled(final WizardStep step) {
        WizardStep first = getFirstStep();
        if (first == null) {
            return false;
        }
        return first.getId().equals(step.getId());
    }

    /**
     * Return true if the next button should be disabled for this step, else
     * false. This method always returns false;
     *
     * @param step The step to check.
     */
    @Override
    public boolean isNextDisabled(final WizardStep step) {
        return false;
    }

    /**
     * Return true if the finish button should be disabled for this step, else
     * false. This method always returns false;
     *
     * @param step The step to check.
     */
    @Override
    public boolean isFinishDisabled(final WizardStep step) {
        return false;
    }

    /**
     * Return true if the cancel button should be disabled for this step, else
     * false. This method always returns false;
     *
     * @param step The step to check.
     */
    @Override
    public boolean isCancelDisabled(final WizardStep step) {
        return false;
    }

    /**
     * Return true if the close button should be disabled for this step, else
     * false. This method always returns false;
     *
     * @param step The step to check.
     */
    @Override
    public boolean isCloseDisabled(final WizardStep step) {
        return false;
    }

    /**
     * Return true if the previous button should be rendered for this step, else
     * false. Typically this method returns true for all steps except for steps
     * that are results steps.
     *
     * @param step The step to check.
     */
    @Override
    public boolean hasPrevious(final WizardStep step) {
        if (step == null) {
            return false;
        }
        return !isResultsStep(step);
    }

    /**
     * Return true if the next button should be rendered for this step, else
     * false. Typically this method returns true for all steps except for steps
     * that are finish or results steps.
     *
     * @param step The step to check.
     */
    @Override
    public boolean hasNext(final WizardStep step) {
        if (step == null) {
            return false;
        }
        return !(isFinishStep(step) || isResultsStep(step));
    }

    /**
     * Return true if the cancel button should be rendered for this step, else
     * false. Typically this method returns true for all steps except for steps
     * that are results steps.
     *
     * @param step The step to check.
     */
    @Override
    public boolean hasCancel(final WizardStep step) {
        if (step == null) {
            return false;
        }
        return !isResultsStep(step);
    }

    /**
     * Return true if the close button should be rendered for this step, else
     * false. Typically this method returns true only for the results step.
     *
     * @param step The step to check.
     */
    @Override
    public boolean hasClose(final WizardStep step) {
        if (step == null) {
            return false;
        }
        return isResultsStep(step);
    }

    /**
     * Return true if the finish button should be rendered for this step, else
     * false. Typically this method returns true only for the finish step.
     *
     * @param step The step to check.
     */
    @Override
    public boolean hasFinish(final WizardStep step) {
        // For now we only support simple linear wizards with no
        // results page.
        if (step == null) {
            return false;
        }
        return isFinishStep(step);
    }

    /**
     * Return true if any of the steps have step help. If any of the steps have
     * step help, this method should return true, unless no step help should be
     * shown for the wizard. If the determination had not been made when this
     * method is called, since the step list must be built at least once, false
     * is returned.
     */
    @Override
    public boolean hasStepHelp() {
        if (wizardState.getHasStepHelp() == null) {
            return false;
        }
        return wizardState.getHasStepHelp();
    }

    /**
     * Return true if the wizard has completed and there are no more steps for
     * the user to complete, else false. Typically this informs the wizard that
     * there is nothing more to render. This may cause a popup wizard to be
     * dismissed or an inline wizard to navigate to some other page.
     */
    @Override
    public boolean isComplete() {
        return wizardState.isComplete();
    }

    /**
     * Called to inform the model that this instance will no longer be
     * referenced.
     */
    @Override
    public void complete() {
        wizardSteps = null;
        wizardState.reset();
    }

    /**
     * Returns false if prematureRender is true, else true if the step should
     * participate in the APPLY_REQUEST_VALUES phase.
     *
     * @param event The event that precipitated this call.
     * @param prematureRender true if rendering is occurring before
     * RENDER_RESPONSE phase was normally expected.
     */
    @Override
    public boolean decode(final int event, final boolean prematureRender) {
        if (prematureRender) {
            return false;
        }
        return wizardState.decode(event);
    }

    /**
     * Return true if the current step should participate in the
     * PROCESS_VALIDATIONS phase. Returns true if event is WizardEvent.NEXT or
     * WizardEvent.FINISH and prematureRender is false.
     *
     * @param event The event that precipitated this call.
     * @param prematureRender Is true if rendering is occurring before
     * RENDER_RESPONSE phase was normally expected.
     */
    @Override
    public boolean validate(final int event, final boolean prematureRender) {
        if (prematureRender) {
            return false;
        }
        return wizardState.validate(event);
    }

    /**
     * Return true if the current step should participate in the
     * UPDATE_MODEL_VALUES phase. Returns true if event is WizardEvent.NEXT or
     * WizardEvent.FINISH and prematureRender is false.
     *
     * @param event The event that precipitated this call.
     * @param prematureRender true if rendering is occurring before
     * RENDER_RESPONSE phase was normally expected.
     */
    @Override
    public boolean update(final int event, final boolean prematureRender) {
        if (prematureRender) {
            return false;
        }
        return wizardState.update(event);
    }

    /**
     * Handle the following {@link WizardEvent WizardEvent} events and adjust
     * the state accordingly.
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
     */
    @Override
    public boolean handleEvent(final WizardEvent event) {

        boolean returnValue = true;
        switch (event.getNavigationEvent()) {
            case WizardEvent.NEXT:
                wizardState.nextStep();
                break;
            case WizardEvent.PREVIOUS:
                wizardState.previousStep();
                break;
            case WizardEvent.FINISH:
                int i = wizardState.getCurrentStep() + 1;
                if (i == wizardSteps.size()) {
                    wizardState.close();
                } else {
                    wizardState.finishStep();
                }
                break;
            case WizardEvent.GOTOSTEP:
                String gotoStepId = event.getGotoStepId();
                if (gotoStepId != null) {
                    int index = getStepIndex(gotoStepId);
                    wizardState.gotoStep(index);
                }
                break;
            case WizardEvent.CANCEL:
                wizardState.cancel();
                break;
            case WizardEvent.CLOSE:
                wizardState.close();
                break;
            case WizardEvent.HELPTAB:
                break;
            case WizardEvent.STEPSTAB:
                break;
            case WizardEvent.INVALID:
                break;
            case WizardEvent.NOEVENT:
                break;
            default:
                break;
        }
        return returnValue;
    }

    /**
     * This class maintains the current step and most recent navigation event
     * received from the Wizard component. Is stores a simple index into the
     * wizardSteps ArrayList
     *
     * It defines the state that controls the decode, validate and update
     * methods of the WizardModel.
     */
    private static final class WizardState {

        /**
         * Start state.
         */
        static final int START = -1;

        /**
         * Next state.
         */
        static final int NEXT = 0;

        /**
         * Previous state.
         */
        static final int PREVIOUS = 1;

        /**
         * Cancel state.
         */
        static final int CANCEL = 2;

        /**
         * Finish state.
         */
        static final int FINISH = 3;

        /**
         * Close state.
         */
        static final int CLOSE = 4;

        /**
         * Goto state.
         */
        static final int GOTOSTEP = 7;

        /**
         * Current state.
         */
        private int state;

        /**
         * Current step.
         */
        private int currentStep;

        /**
         * hasStepHelp flag.
         */
        private Boolean hasStepHelp;

        /**
         * Create a new instance.
         */
        WizardState() {
            super();
            this.state = START;
            this.currentStep = 0;
        }

        /**
         * Get the current state.
         *
         * @return int
         */
        int getState() {
            return state;
        }

        /**
         * Set the current state.
         *
         * @param newState state
         */
        void setState(final int newState) {
            this.state = newState;
        }

        /**
         * Get the hasStepHelp flag value.
         *
         * @return Boolean
         */
        Boolean getHasStepHelp() {
            return hasStepHelp;
        }

        /**
         * Set the hasStepHelp flag value.
         *
         * @param newHasStepHelp new hasStepHelp flag value
         */
        void setHasStepHelp(final Boolean newHasStepHelp) {
            this.hasStepHelp = newHasStepHelp;
        }

        /**
         * Test if the state is 'close' or 'cancel'.
         *
         * @return boolean
         */
        boolean isComplete() {
            return state == CLOSE || state == CANCEL;
        }

        /**
         * Decode the event.
         *
         * @param event event id
         * @return boolean
         */
        boolean decode(final int event) {
            return true;
        }

        /**
         * Validate the specified event.
         *
         * @param event event id
         * @return boolean
         */
        boolean validate(final int event) {
            return event == WizardEvent.FINISH
                    || event == WizardEvent.NEXT
                    || event == WizardEvent.NOEVENT;
        }

        /**
         * Its not clear if another state is needed for updating on the previous
         * click. We want the state but not the commit.
         *
         * @param event event id
         * @return boolean
         */
        boolean update(final int event) {
            return event == WizardEvent.FINISH
                    || event == WizardEvent.NEXT
                    || event == WizardEvent.NOEVENT;
        }

        /**
         * Advance to the next step.
         */
        void nextStep() {
            state = NEXT;
            ++currentStep;
        }

        /**
         * Finish the current step.
         */
        void finishStep() {
            state = FINISH;
            ++currentStep;
        }

        /**
         * Go back to the previous step.
         */
        void previousStep() {
            state = PREVIOUS;
            --currentStep;
        }

        /**
         * Go to the specified step.
         *
         * @param step step
         */
        void gotoStep(final int step) {
            state = GOTOSTEP;
            currentStep = step;
        }

        /**
         * Get the current step.
         *
         * @return int
         */
        int getCurrentStep() {
            return currentStep;
        }

        /**
         * Set the current step.
         *
         * @param newCurrentStep step
         */
        void setCurrentStep(final int newCurrentStep) {
            this.currentStep = newCurrentStep;
        }

        /**
         * Set the state to 'start' and currentStep to '0'.
         */
        void reset() {
            state = START;
            currentStep = 0;
        }

        /**
         * Set the state to 'cancel'.
         */
        void cancel() {
            this.state = CANCEL;
        }

        /**
         * Set the state to 'close'.
         */
        void close() {
            this.state = CLOSE;
        }
    };

    /**
     * {@code StateHolder} method called to save the state the model's state.
     * The saved state consists of the values of {@code state},
     * {@code currentStep}, and {@code hasStepHelp} of the internal
     * {@code WizardState} instance.
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] state = new Object[3];
        int i = 0;
        state[i++] = wizardState.getState();
        state[i++] = wizardState.getCurrentStep();
        state[i++] = wizardState.getHasStepHelp();
        return state;
    }

    /**
     * {@code StateHolder} method called to restore the model's current state
     * based on the values in {@code state}. The restored state consists of
     * values for {@code state},
     * {@code currentStep}, and {@code hasStepHelp} of the internal
     * {@code WizardState} instance.
     */
    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] stateArray = (Object[]) state;
        int i = 0;
        wizardState.setState(((Integer) stateArray[i++]));
        wizardState.setCurrentStep(((Integer) stateArray[i++]));
        wizardState.setHasStepHelp((Boolean) stateArray[i++]);
    }

    /**
     * {@code StateHolder} method to set the persistent state of this class.This
     * call does not modify this class.
     *
     * @param transientFlag flag
     */
    @Override
    public void setTransient(final boolean transientFlag) {
        // ignore this
    }

    /**
     * {@code StateHolder} method indicating the this class is persistent. This
     * method returns {@code false}
     */
    @Override
    public boolean isTransient() {
        return false;
    }
}
