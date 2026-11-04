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
 * For internal use only.
 */
public interface SelectorManager {

    /**
     * JSF standard method from UIComponent.
     * @param context The FacesContext for the request
     * @return The client id, also the JavaScript element id
     */
    String getClientId(FacesContext context);

    /**
     * JSF standard method from UIComponent.
     * @return true if the component is disabled
     */
    boolean isDisabled();

    /**
     * Get the JS {@code onchange} event handler.
     * @return A string representing the JS event handler
     */
    String getOnChange();

    /**
     * Get the tab index for the component.
     * @return the tab index
     */
    int getTabIndex();

    /**
     * Returns true if the component allows multiple selections.
     * @return true if the component allows multiple selections
     */
    boolean isMultiple();

    /**
     * Returns true if the component is read-only.
     * @return true if the component is read-only
     */
    boolean isReadOnly();

    /**
     * Get the style.
     * @return String
     */
    String getStyle();

    /**
     * Get the style class.
     * @return String
     */
    String getStyleClass();
}
