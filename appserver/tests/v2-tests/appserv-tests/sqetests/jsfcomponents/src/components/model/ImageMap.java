/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package components.model;


import components.components.AreaSelectedEvent;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * ImageMap is the "backing file" class for the image map application.
 * It contains a method event handler that sets the locale from
 * information in the <code>AreaSelectedEvent</code> event.
 */
public class ImageMap {

    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The locales to be selected for each hotspot, keyed by the
     * alternate text for that area.</p>
     */
    private Map locales = null;

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Construct a new instance of this image map.</p>
     */
    public ImageMap() {
        locales = new HashMap();
        locales.put("NAmerica", Locale.ENGLISH);
        locales.put("SAmerica", new Locale("es", "es"));
        locales.put("Germany", Locale.GERMAN);
        locales.put("Finland", new Locale("fi", "fi"));
        locales.put("France", Locale.FRENCH);
    }


    /**
     * <p>Select a new Locale based on this event.</p>
     *
     * @param actionEvent The {@link AreaSelectedEvent} that has occurred
     */
    public void processAreaSelected(ActionEvent actionEvent) {
        AreaSelectedEvent event = (AreaSelectedEvent) actionEvent;
        String current = event.getMapComponent().getCurrent();
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale((Locale) locales.get(current));
    }


    /**
     * <p>Return an indication for navigation.  Application using this component,
     * can refer to this method via an <code>action</code> expression in their
     * page, and set up the "outcome" (success) in their navigation rule.
     */
    public String status() {
        return "success";
    }
}
