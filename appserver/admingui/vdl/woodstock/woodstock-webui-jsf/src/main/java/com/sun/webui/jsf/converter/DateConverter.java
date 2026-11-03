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
package com.sun.webui.jsf.converter;

import com.sun.webui.jsf.component.DateManager;
import com.sun.webui.jsf.util.ThemeUtilities;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Data converter.
 */
public final class DateConverter implements Converter, Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 1580936705838582740L;

    /**
     * Invalid date id.
     */
    private static final String INVALID_DATE_ID = "DateConverter.invalidDate";

    /**
     * Create a new instance.
     */
    public DateConverter() {
    }

    @Override
    public String getAsString(final FacesContext context,
            final UIComponent component, final Object o)
            throws ConverterException {

        try {
            return getDateManager(component).getDateFormat().format((Date) o);
        } catch (Exception ex) {
            throw new ConverterException(ex);
        }
    }

    @Override
    public Object getAsObject(final FacesContext context,
            final UIComponent component, final String s)
            throws ConverterException {

        if (s.length() == 0) {
            return null;
        }
        // Generate errors for dates that don't strictly follow format 6347646
        DateFormat df = getDateManager(component).getDateFormat();
        // Save old state in case there is other code that relies on it
        boolean saveLenient = df.isLenient();
        df.setLenient(false);
        try {
            Date date = df.parse(s);
            return date;
        } catch (ParseException ex) {
            FacesMessage facesMessage = null;
            try {
                String message = ThemeUtilities.getTheme(context)
                        .getMessage(INVALID_DATE_ID);
                MessageFormat mf = new MessageFormat(message,
                        context.getViewRoot().getLocale());
                String example = getDateManager(component).getDateFormat()
                        .format(new Date());
                Object[] params = {s, example};
                facesMessage = new FacesMessage(mf.format(params));
            } catch (Exception e) {
                throw new ConverterException(ex);
            }
            throw new ConverterException(facesMessage);
        } finally {
            // Restore original state
            df.setLenient(saveLenient);
        }
    }

    /**
     * Get the data manager.
     * @param component UI component
     * @return DateManager
     */
    private static DateManager getDateManager(final UIComponent component) {
        DateManager dateManager = null;
        if (component instanceof DateManager) {
            dateManager = (DateManager) component;
        } else if (component.getParent() instanceof DateManager) {
            dateManager = (DateManager) (component.getParent());
        }
        if (dateManager == null) {
            throw new RuntimeException(
                    "The DateConverter can only be used with components"
                    + " which implement DateManager");
        }
        return dateManager;
    }
}
