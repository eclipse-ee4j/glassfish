/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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

import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import java.text.MessageFormat;

/**
 * Use this validator to check the number of characters in a string when you
 * need to set the validation messages.
 */
public final class StringLengthValidator implements Validator, StateHolder {

    /**
     * The converter id for this converter.
     */
    public static final String VALIDATOR_ID = "com.sun.webui.jsf.StringLength";

    /**
     * The message to use in case the value is too short. May include {0} for
     * the minimum value.
     */
    private String tooShortMessage = null;

    /**
     * The message to use in case the value is too long. May include {0} for the
     * maximum value.
     */
    private String tooLongMessage;

    /**
     * Minimum length.
     */
    private int minLength = 0;

    /**
     * Maximum length.
     */
    private int maxLength = 0;

    /**
     * Is the minimum length set.
     */
    private boolean minimumSet = false;

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Create a new instance.
     */
    public StringLengthValidator() {
    }

    /**
     * Creates a new instance of StringLengthValidator.
     *
     * @param max The maximum number of characters allowed in the string
     */
    public StringLengthValidator(final int max) {
        maxLength = max;
    }

    /**
     * Creates a new instance of StringLengthValidator.
     *
     * @param max The maximum number of characters allowed in the string
     * @param min The minimum number of characters allowed in the string
     */
    public StringLengthValidator(final int max, final int min) {
        this(max);
        minLength = min;
        minimumSet = true;
    }

    @Override
    public void validate(final FacesContext context,
            final UIComponent component, final Object value)
            throws ValidatorException {

        if (DEBUG) {
            log("validate(" + String.valueOf(value) + ")");
        }

        if ((context == null) || (component == null)) {
            if (DEBUG) {
                log("\tContext or component is null");
            }
            throw new NullPointerException();
        }

        String string;
        if (value == null) {
            if (DEBUG) {
                log("\tValue is null!");
            }
            string = new String();
        } else {
            string = (String) value;

        }
        if (DEBUG) {
            log("\tValue is !" + string + "!");
        }
        if (string.length() > maxLength) {
            if (DEBUG) {
                log("\tString is longer than maxlength");
            }
            if (tooLongMessage == null) {
                Theme theme = ThemeUtilities.getTheme(context);
                tooLongMessage = theme.getMessage(
                        "StringLengthValidator.itemTooLong");
            }
            MessageFormat mf
                    = new MessageFormat(tooLongMessage,
                            context.getViewRoot().getLocale());
            Object[] params = {String.valueOf(maxLength)};
            FacesMessage msg = new FacesMessage(mf.format(params));
            throw new ValidatorException(msg);

        }
        if (minimumSet && string.length() < minLength) {
            if (DEBUG) {
                log("\tString is shorter than minlength");
            }
            if (tooShortMessage == null) {
                Theme theme = ThemeUtilities.getTheme(context);
                tooShortMessage = theme.getMessage(
                        "StringLengthValidator.itemTooLong");
            }
            MessageFormat mf
                    = new MessageFormat(tooShortMessage,
                            context.getViewRoot().getLocale());
            Object[] params = {String.valueOf(minLength)};
            FacesMessage msg = new FacesMessage(mf.format(params));
            throw new ValidatorException(msg);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[5];
        values[0] = maxLength;
        values[1] = minLength;
        if (minimumSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = tooLongMessage;
        values[4] = tooShortMessage;
        return (values);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        maxLength = ((Integer) values[0]);
        minLength = ((Integer) values[1]);
        minimumSet = ((Boolean) values[2]);
        if (values[3] != null) {
            tooLongMessage = values[3].toString();
        }
        if (values[4] != null) {
            tooShortMessage = values[4].toString();
        }
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public void setTransient(final boolean transientValue) {
    }

    /**
     * Get the message to be used if the string is longer than the maximum
     * number of characters.
     *
     * @return the message to be used if the string is longer than the maximum
     * number of characters
     */
    public String getTooLongMessage() {
        return this.tooLongMessage;
    }

    /**
     * Set the message to be used if the string is longer than the maximum
     * number of characters.
     *
     * @param newTooLongMessage the message to be used if the string is longer
     * than the maximum number of characters
     */
    public void setTooLongMessage(final String newTooLongMessage) {
        this.tooLongMessage = newTooLongMessage;
    }

    /**
     * Get the message to be used if the string is shorter than the minimum
     * number of characters.
     *
     * @return the message to be used if the string is shorter than the minimum
     * number of characters
     */
    public String getTooShortMessage() {
        return this.tooShortMessage;
    }

    /**
     * Set the message to be used if the string is shorter than the minimum
     * number of characters.
     *
     * @param newTooShortMessage the message to be used if the string is shorter
     * than the minimum number of characters
     */
    public void setTooShortMessage(final String newTooShortMessage) {
        this.tooShortMessage = newTooShortMessage;
    }

    /**
     * Log a message to the standard output.
     *
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(StringLengthValidator.class.getName() + "::" + msg);
    }
}
