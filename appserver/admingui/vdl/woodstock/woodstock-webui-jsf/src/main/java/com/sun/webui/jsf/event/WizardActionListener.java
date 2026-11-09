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
import com.sun.webui.jsf.util.LogUtil;

import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

/**
 * The WizardActionListener is an internal listener specified on the navigation
 * and tab components of the {@link Wizard Wizard} component.
 */
public final class WizardActionListener implements ActionListener, StateHolder {

    /**
     * Wizard id.
     */
    private String wizardId;

    /**
     * Event id.
     */
    private int event;

    /**
     * Transient flag.
     */
    private boolean isTransient;

    /**
     * Create a new instance of {@code WizardActionListener} identifying
     * the wizard and event this listener is registered for.This class is used
     * internally to set the internal event state of the wizard.
     *
     * @param newWizardId wizard id
     * @param newEvent event id
     */
    public WizardActionListener(final String newWizardId, final int newEvent) {
        super();
        this.wizardId = newWizardId;
        this.event = newEvent;
    }

    /**
     * Default constructor for restoring and saving state.
     */
    public WizardActionListener() {
        super();
    }

    /**
     * This method locates the Wizard listening for this event and sets the
     * event state, for eventual broadcast to
     * WizardListeners.
     *
     * @param actionEvent The ActionEvent generated
     */
    @Override
    public void processAction(final ActionEvent actionEvent)
            throws AbortProcessingException {

        Wizard wizard = Wizard.getWizard((UIComponent) actionEvent.getSource(),
                wizardId);
        if (wizard == null) {
            // Log  and throw
            if (LogUtil.infoEnabled()) {
                LogUtil.info(WizardActionListener.class, "WEBUI0006",
                        new String[]{wizardId});
            }
            throw new AbortProcessingException(
                    "Wizard parent not found processing "
                    + event);
        }
        wizard.broadcastEvent((UIComponent) actionEvent.getSource(), event);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[3];
        values[0] = event;
        values[1] = wizardId;
        values[2] = isTransient;
        return values;
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        this.event = ((Integer) values[0]);
        this.wizardId = (String) values[1];
        this.isTransient = ((Boolean) values[2]);
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public void setTransient(final boolean newTransientValue) {
        // Don't honor this.
    }
}
