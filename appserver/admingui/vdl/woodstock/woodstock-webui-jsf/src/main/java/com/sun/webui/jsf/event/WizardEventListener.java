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

import jakarta.faces.component.StateHolder;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesListener;

/**
 * The WizardEventListener is an event listener defined on
 * {@link com.sun.webui.jsf.component.WizardStep WizardStep} and
 * {@link com.sun.webui.jsf.component.Wizard Wizard} components to receive
 * {@link WizardEvent WizardEvent} notifications.
 * <p>
 * Typically the {@link WizardEventListener WizardEventListener} instance is
 * defined on a {@link com.sun.webui.jsf.component.Wizard Wizard} using the
 * eventListener attribute on the &lt;ui:wizard&gt; or the &lt;wizardStep&gt;
 * tags.
 * <p>
 * The listener can expect to receive the following events.
 * </p>
 * <ul>
 * <li>{@link WizardEvent#CANCEL WizardEvent.CANCEL}</li>
 * <li>{@link WizardEvent#CLOSE WizardEvent.CLOSE}</li>
 * <li>{@link WizardEvent#FINISH WizardEvent.FINISH}</li>
 * <li>{@link WizardEvent#GOTOSTEP WizardEvent.GOTOSTEP}</li>
 * <li>{@link WizardEvent#HELPTAB WizardEvent.HELPTAB}</li>
 * <li>{@link WizardEvent#NEXT WizardEvent.NEXT}</li>
 * <li>{@link WizardEvent#PREVIOUS WizardEvent.PREVIOUS}</li>
 * <li>{@link WizardEvent#STEPSTAB WizardEvent.STEPSTAB}</li>
 * <li>{@link WizardEvent#START WizardEvent.START}</li>
 * <li>{@link WizardEvent#COMPLETE WizardEvent.COMPLETE}</li>
 * </ul>
 */
public interface WizardEventListener extends FacesListener, StateHolder {

    /**
     * Perform functionality suitable for the specified {@code event}.
     *
     * @param event The WizardEvent being broadcast
     * @return boolean
     * @throws AbortProcessingException if an error occurs
     */
    boolean handleEvent(WizardEvent event) throws AbortProcessingException;
}
