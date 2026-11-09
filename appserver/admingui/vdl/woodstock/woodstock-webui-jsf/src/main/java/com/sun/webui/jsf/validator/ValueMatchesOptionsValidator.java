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

import com.sun.webui.jsf.component.ListSelector;
import com.sun.webui.jsf.model.list.ListItem;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

/**
 * Use this validator to check the number of characters in a string when you
 * need to set the validation messages.
 */
public final class ValueMatchesOptionsValidator
        implements Validator, Serializable {

    /**
     * The converter id for this converter.
     */
    public static final String VALIDATOR_ID =
            "com.sun.webui.jsf.ValueMatchesOptions";

    /**
     * Error message used if the value is not in the option.
     */
    private String message;

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Creates a new instance of StringLengthValidator.
     */
    public ValueMatchesOptionsValidator() {
    }

    /**
     * Getter for property message.
     *
     * @return Value of property message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Setter for property message.
     *
     * @param newMessage New value of property message
     */
    public void setMessage(final String newMessage) {
        this.message = newMessage;
    }

    @Override
    public void validate(final FacesContext context,
            final UIComponent component, final Object value)
            throws ValidatorException {

        if (DEBUG) {
            log("validate()");
        }
        if ((context == null) || (component == null)) {
            String msg = "Context or component is null";
            if (DEBUG) {
                log("\t" + msg);
            }
            throw new NullPointerException(msg);
        }

        if (!(component instanceof ListSelector)) {
            String msg = this.getClass().getName()
                    + " can only be used with components which subclass "
                    + ListSelector.class.getName();
            if (DEBUG) {
                log("\t" + msg);
            }
            throw new RuntimeException(msg);
        }

        ListSelector list = (ListSelector) component;
        Object valuesAsArray;

        if (value instanceof List) {
            if (DEBUG) {
                log("\tValue is list");
            }
            valuesAsArray = ((List) value).toArray();
        } else if (value.getClass().isArray()) {
            if (DEBUG) {
                log("\tValue is array");
            }
            valuesAsArray = value;
        } else {
            if (DEBUG) {
                log("\tValue is object");
            }
            valuesAsArray = new Object[]{value};
        }

        int numValues = Array.getLength(valuesAsArray);
        if (numValues == 0) {
            if (DEBUG) {
                log("\tArray is empty - values are OK");
            }
            return;
        }

        Object currentValue;
        Iterator itemsIterator;
        ListItem listItem;
        Object listObject;
        boolean foundValue;
        boolean error = false;

        for (int counter = 0; counter < numValues; ++counter) {
            currentValue = Array.get(valuesAsArray, counter);
            itemsIterator = list.getListItems();
            foundValue = false;

            if (DEBUG) {
                log("\tChecking: " + String.valueOf(currentValue));
            }
            while (itemsIterator.hasNext()) {
                listObject = itemsIterator.next();
                if (!(listObject instanceof ListItem)) {
                    continue;
                }
                listItem = (ListItem) listObject;
                if (DEBUG) {
                    log("ListItem is " + listItem.getLabel());
                }
                if (currentValue.equals(listItem.getValueObject())) {
                    if (DEBUG) {
                        log("Found match");
                    }
                    foundValue = true;
                    break;
                }
            }
            if (!foundValue) {
                if (DEBUG) {
                    log("No match found");
                }
                error = true;
                break;
            }
        }

        if (error) {
            if (message == null) {
                message = ThemeUtilities.getTheme(context)
                        .getMessage("ListSelector.badValue");
            }
            throw new ValidatorException(new FacesMessage(message));
        }
    }

    /**
     * Log a message to the standard output.
     *
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(ValueMatchesOptionsValidator.class.getName() + "::" + msg);
    }
}
