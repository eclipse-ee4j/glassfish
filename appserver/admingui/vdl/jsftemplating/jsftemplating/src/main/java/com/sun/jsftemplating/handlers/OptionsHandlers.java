/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

/*
 * OptionsHandlers.java
 *
 * Created on June 8, 2006, 5:01 PM
 */
package com.sun.jsftemplating.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.util.Util;

import jakarta.faces.model.SelectItem;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 *
 * @author Jennifer Chou
 */
public class OptionsHandlers {

    /**
     * Creates a new instance of OptionsHandlers
     */
    public OptionsHandlers() {
    }

    /**
     * <p>
     * This handler returns the Lockhart version of Options of the drop-down of the given <code>labels</code> and
     * <code>values</code>. <code>labels</code> and <code>values</code> arrays must be equal in size and in matching
     * sequence.
     * </p>
     *
     * <p>
     * Input value: <code>labels</code> -- Type: <code>java.util.Collection</code>
     * </p>
     *
     * <p>
     * Input value: <code>values</code> -- Type: <code>java.util.Collection</code>
     * </p>
     *
     * <p>
     * Output value: <code>options</code> -- Type: <code>SelectItem[] (castable to Option[])</code>
     * </p>
     *
     * @param context The HandlerContext.
     */
    @Handler(id = "getSunOptions", input = { @HandlerInput(name = "labels", type = Collection.class, required = true),
            @HandlerInput(name = "values", type = Collection.class, required = true) }, output = {
                    @HandlerOutput(name = "options", type = SelectItem[].class) })
    public static void getSunOptions(HandlerContext context) throws Exception {
        Collection<String> labels = (Collection) context.getInputValue("labels");
        Collection<String> values = (Collection) context.getInputValue("values");
        if (labels.size() != values.size()) {
            throw new Exception("getSunOptions Handler input " + "incorrect: Input 'labels' and 'values' size must be equal. "
                    + "'labels' size: " + labels.size() + " 'values' size: " + values.size());
        }

        SelectItem[] options = (SelectItem[]) Array.newInstance(SUN_OPTION_CLASS, labels.size());
        String[] labelsArray = labels.toArray(new String[labels.size()]);
        String[] valuesArray = values.toArray(new String[values.size()]);
        for (int i = 0; i < labels.size(); i++) {
            SelectItem option = getSunOption(valuesArray[i], labelsArray[i]);
            options[i] = option;
        }
        context.setOutputValue("options", options);
    }

    /**
     * Creates a Woodstock Option instance.
     */
    private static SelectItem getSunOption(String value, String label) {
        try {
            return (SelectItem) SUN_OPTION_CONSTRUCTOR.newInstance(value, label);
        } catch (InstantiationException ex) {
            throw new RuntimeException("Unable to instantiate '" + SUN_OPTION_CLASS + "'!", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Unable to instantiate '" + SUN_OPTION_CLASS + "'!", ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException("Unable to instantiate '" + SUN_OPTION_CLASS + "'!", ex);
        }
    }

    /**
     * <p>
     * Method wich returns the constructor on the class with the given arguments. It will return null if any exceptions
     * occur, no exceptions will be thrown from this method.
     * </p>
     */
    private static Constructor noExceptionFindConstructor(Class cls, Class args[]) {
        Constructor constructor = null;
        try {
            constructor = cls.getConstructor(args);
        } catch (Exception ex) {
            // Ignore...
        }
        return constructor;
    }

    private static final Class SUN_OPTION_CLASS = Util.noExceptionLoadClass("com.sun.webui.jsf.model.Option");
    private static final Constructor SUN_OPTION_CONSTRUCTOR = noExceptionFindConstructor(SUN_OPTION_CLASS,
            new Class[] { Object.class, String.class });
}
