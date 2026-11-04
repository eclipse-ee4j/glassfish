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
package com.sun.webui.jsf.event;

import com.sun.webui.jsf.component.Wizard;
import com.sun.webui.jsf.component.WizardStep;

import jakarta.faces.component.UIComponent;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;

/**
 * {@code WizardEvent} is the event class broadcast by the
 * {@link com.sun.webui.jsf.component.Wizard Wizard} to
 * {@link com.sun.webui.jsf.event.WizardEventListener WizardEventListeners}.
 */
public final class WizardEvent extends FacesEvent {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 6398514451074650783L;

    /**
     * The cancel button was clicked.
     */
    public static final int CANCEL = 0;

    /**
     * The close button was clicked.
     */
    public static final int CLOSE = 1;

    /**
     * The finish button was clicked.
     */
    public static final int FINISH = 2;

    /**
     * A step link was clicked.
     */
    public static final int GOTOSTEP = 3;

    /**
     * The help tab was clicked.
     */
    public static final int HELPTAB = 4;

    /**
     * The next button was clicked.
     */
    public static final int NEXT = 6;

    /**
     * The previous button was clicked.
     */
    public static final int PREVIOUS = 7;

    /**
     * The steps tab was clicked.
     */
    public static final int STEPSTAB = 8;

    /**
     * This event is broadcast by the Wizard in
     * {@link Wizard#encodeBegin(FacesContext) encodeBegin} to indicate that
     * this wizard session is being rendered the first time.
     */
    public static final int START = 10;

    /**
     * This event is broadcast by the Wizard in
     * {@link Wizard#encodeEnd(FacesContext) encodeEnd} to indicate that this
     * wizard session has completed.
     */
    public static final int COMPLETE = 11;

    /**
     * This is an internal Wizard event and is not broadcast to application
     * defined {@link WizardEventListener WizardEventListeners}.
     */
    public static final int NOEVENT = 12;

    /**
     * @deprecated
     */
    public static final int INVALID = 9;

    /**
     * @deprecated
     */
    public static final int DECODE = 13;

    /**
     * @deprecated
     */
    public static final int VALIDATE = 14;

    /**
     * @deprecated
     */
    public static final int UPDATE = 15;

    /**
     * @deprecated
     */
    public static final int INVOKE_APPLICATION = 16;

    /**
     * @deprecated
     */
    public static final int RENDER = 17;

    /**
     * @deprecated
     */
    public static final int STEP_ENTER = 18;

    /**
     * @deprecated
     */
    public static final int STEP_EXIT = 19;

    /**
     * @deprecated use {@link #getStep()}
     */
    private WizardStep step;

    /**
     * @deprecated use {@link #getEvent()}
     */
    private int navigationEvent;

    /**
     * @deprecated use {@link #getEventSource()}
     */
    private UIComponent navigationSource;

    /**
     * @deprecated use {@link #getGotoStepId()}
     */
    private Object data;

    /**
     * Construct a {@code WizardEvent} instance, specifying the {@code Wizard}
     * source.
     *
     * @param wizard wizard
     */
    public WizardEvent(final Wizard wizard) {
        super(wizard);
    }

    /**
     * @param wizard wizard
     * @param newNavigationSource UI component
     * @param newNavigationEVent event id
     * @param newData event payload
     * @deprecated replaced by {@link #WizardEvent(Wizard, UIComponent, int,
     * WizardStep, String)}
     */
    @SuppressWarnings("checkstyle:avoidinlineconditionals")
    public WizardEvent(final Wizard wizard,
            final UIComponent newNavigationSource, final int newNavigationEVent,
            final Object newData) {

        this(wizard, newNavigationSource, newNavigationEVent, null,
                newData instanceof String ? (String) newData : null);

        // deprecation support
        //
        if (!(newData instanceof String)) {
            this.data = newData;
        }
    }

    /**
     * Construct a {@code WizardEvent} with event data. Not all data will be
     * available with all events. In particular:
     * <ul>
     * <li>{@code gotoStepId} will only be available if the event is
     * {@code GOTOSTEP}.
     * </li>
     * <li>{@code eventSource} will be null for the {@code START} and
     * {@code COMPLETE} events.
     * </li>
     * </ul>
     *
     * @param newWizard the wizard instance broadcasting this event
     * @param newEventSource the component that generated this event
     * @param newEvent the WizardEvent constant identifying this event
     * @param newStep the WizardStep generating this event
     * @param gotoStepId the id of the step the wizard will display next
     */
    public WizardEvent(final Wizard newWizard,
            final UIComponent newEventSource, final int newEvent,
            final WizardStep newStep, final String gotoStepId) {

        // wizard -> source
        // Should this acually be the button ?
        super(newWizard);
        this.navigationSource = newEventSource;
        this.navigationEvent = newEvent;
        this.data = gotoStepId;
    }

    /**
     * Get the navigation event.
     *
     * @return int
     * @deprecated replaced by {@link #getEvent()}
     */
    public int getNavigationEvent() {
        return navigationEvent;
    }

    /**
     * Return the {@code WizardEvent} constant identifying this event.
     *
     * @return int
     */
    public int getEvent() {
        return navigationEvent;
    }

    /**
     * Set the navigation event.
     *
     * @param newNavigationEvent event id
     * @deprecated replaced by {@link #setEvent(int)}
     */
    public void setNavigationEvent(final int newNavigationEvent) {
        this.navigationEvent = newNavigationEvent;
    }

    /**
     * Set the event constant identifying this event.
     *
     * @param event event id
     */
    public void setEvent(final int event) {
        this.navigationEvent = event;
    }

    /**
     * Get the navigation event source.
     *
     * @return UIComponent
     * @deprecated replaced by {@link #getEventSource}
     */
    public UIComponent getNavigationEventSource() {
        return navigationSource;
    }

    /**
     * Return the wizard sub component that generated the original
     * {@code ActionEvent} that queued this event.
     *
     * @return UIComponent
     */
    public UIComponent getEventSource() {
        return navigationSource;
    }

    /**
     * Return the {@code Wizard} broadcasting this event.
     *
     * @return Wizard
     */
    public Wizard getWizard() {
        return (Wizard) source;
    }

    /**
     * Return the {@code WizardStep} that generated this event.
     *
     * @return WizardStep
     */
    public WizardStep getStep() {
        return (WizardStep) step;
    }

    /**
     * Set the {@code WizardStep} that generated this event.
     *
     * @param newStep wizard step
     */
    public void setStep(final WizardStep newStep) {
        this.step = newStep;
    }

    /**
     * Return the component id of the {@code WizardStep} that the wizard will be
     * proceeding to.
     *
     * @return String
     */
    public String getGotoStepId() {
        if (navigationEvent == GOTOSTEP) {
            return (String) data;
        }
        return null;
    }

    @Override
    public boolean isAppropriateListener(final FacesListener listener) {
        return (listener instanceof WizardEventListener);
    }

    @Override
    public void processListener(final FacesListener listener) {
    }
}
