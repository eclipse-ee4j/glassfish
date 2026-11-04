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

package com.sun.webui.jsf.validator;

import com.sun.webui.jsf.component.DateManager;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import java.text.MessageFormat;
import java.util.Date;

/**
 * Use this validator to check the number of characters in a string when
 * you need to set the validation messages.
 */
public final class DateInRangeValidator implements Validator {

    /**
     * The converter id for this converter.
     */
    public static final String VALIDATOR_ID = "com.sun.webui.jsf.DateInRange";

    /**
     * Creates a new instance of StringLengthValidator.
     */
    public DateInRangeValidator() {
    }

    @Override
    public void validate(final FacesContext context,
            final UIComponent component, final Object value)
            throws ValidatorException {

        if ((context == null) || (component == null)) {
            throw new NullPointerException();
        }
        if (!(value instanceof Date)) {
            return;
        }

        DateManager dateManager = null;
        if (component instanceof DateManager) {
            dateManager = (DateManager) component;
        } else if (component.getParent() instanceof DateManager) {
            dateManager = (DateManager) (component.getParent());
        }
        if (dateManager == null) {
            return;
        }

        Date date = (Date) value;
        Date minDate = dateManager.getFirstAvailableDate();
        if (minDate != null && date.before(minDate)) {
            FacesMessage msg = getFacesMessage(component, context, minDate,
                    "DateInRangeValidator.after");
            throw new ValidatorException(msg);
        }
        Date maxDate = dateManager.getLastAvailableDate();
        if (maxDate != null && maxDate.before(date)) {
            FacesMessage msg = getFacesMessage(component, context, maxDate,
                    "DateInRangeValidator.before");
            throw new ValidatorException(msg);
        }
    }

    /**
     * Get the faces message.
     * @param component UI component
     * @param context faces context
     * @param date date
     * @param key message key
     * @return FacesMessage
     */
    private static FacesMessage getFacesMessage(final UIComponent component,
            final FacesContext context, final Date date, final String key) {

        String message = ThemeUtilities.getTheme(context).getMessage(key);
        String arg = ConversionUtilities.convertValueToString(component, date);
        MessageFormat mf = new MessageFormat(message,
                context.getViewRoot().getLocale());
        Object[] params = {arg};
        return new FacesMessage(mf.format(params));
    }
}
