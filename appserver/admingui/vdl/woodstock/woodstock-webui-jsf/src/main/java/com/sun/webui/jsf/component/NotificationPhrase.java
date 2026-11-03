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

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.context.FacesContext;

/**
 * The NotificationPhrase component is used to display a message in the
 * masthead.
 */
@Component(type = "com.sun.webui.jsf.NotificationPhrase",
        family = "com.sun.webui.jsf.NotificationPhrase",
        displayName = "Notification Phrase",
        tagName = "notificationPhrase",
        //CHECKSTYLE:OFF
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_notification_phrase",
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_notification_phrase_props")
        //CHECKSTYLE:ON
public final class NotificationPhrase extends ImageHyperlink {

    /**
     * Default constructor.
     */
    public NotificationPhrase() {
        super();
        setRendererType("com.sun.webui.jsf.NotificationPhrase");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.NotificationPhrase";
    }

    @Override
    public String getIcon() {
        String icon = super.getIcon();
        if (icon == null) {
            icon = ThemeImages.ALERT_INFO_MEDIUM;
        }
        return icon;
    }

    @Override
    public String getStyleClass() {
        String styleClass = super.getStyleClass();
        if (styleClass == null) {
            styleClass = ThemeStyles.MASTHEAD_PROGRESS_LINK;
        }
        Theme theme = ThemeUtilities.getTheme(
                FacesContext.getCurrentInstance());
        return theme.getStyleClass(styleClass);
    }

    @Override
    public int getBorder() {
        return 0;
    }

    @Override
    public String getAlign() {
        return "middle";
    }

    @Override
    public String getAlt() {
        String alt = super.getAlt();
        if (alt == null) {
            // Don't convert if null, since convertValueToString
            // returns "" for a null value.
            Object textObj = getText();
            if (textObj != null) {
                String text = ConversionUtilities.convertValueToString(this,
                        textObj);
                if (text != null && text.length() != 0) {
                    alt = text;
                }
            }
        }
        return alt;
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     * @return String
     */
    @Property(name = "onDblClick", isHidden = false, isAttribute = true)
    @Override
    public String getOnDblClick() {
        return super.getOnDblClick();
    }

    @Override
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
    }

    @Override
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[1];
        values[0] = super.saveState(context);
        return values;
    }
}
