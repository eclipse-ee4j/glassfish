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
package com.sun.webui.jsf.component;

import jakarta.faces.context.FacesContext;

/**
 * Most components whose renderers write more than one HTML element need to
 * implement this interface, which exists to allow for a distinction between the
 * component ID and the ID of the primary element that can receive user input or
 * focus. The latter is needed to allow the application to maintain focus, and
 * to set the {@code for}attribute on labels.
 */
public interface ComplexComponent {

    /**
     * Implement this method so that it returns the DOM ID of the HTML element
     * which should receive focus when the component receives focus, and to
     * which a component label should apply. Usually, this is the first element
     * that accepts input.
     *
     * @param context The FacesContext for the request
     * @return The client id, also the JavaScript element id
     * @deprecated
     * @see #getLabeledElementId
     * @see #getFocusElementId
     */
    String getPrimaryElementID(FacesContext context);

    /**
     * Returns the absolute ID of an HTML element suitable for use as the value
     * of an HTML LABEL element's {@code for} attribute. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is the target of a label, if that sub-component is a
     * {@code ComplexComponent}, then {@code getLabeledElementId} must called on
     * the sub-component and the value returned. The value returned by this
     * method call may or may not resolve to a component instance.
     *
     * @param context The FacesContext used for the request
     * @return An absolute id suitable for the value of an HTML LABEL element's
     * {@code for} attribute.
     */
    String getLabeledElementId(FacesContext context);

    /**
     * Returns the id of an HTML element suitable to receive the focus. If the
     * {@code ComplexComponent} has sub-components, and one of the
     * sub-components is to receive the focus, if that sub-component is a
     * {@code ComplexComponent}, then {@code getFocusElementId} must called on
     * the sub-component and the value returned. The value returned by this
     * method call may or may not resolve to a component instance.
     *
     * @param context The FacesContext used for the request
     * @return String
     */
    String getFocusElementId(FacesContext context);
}
