/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 *
 * @author anilam
 */

package org.glassfish.admingui.util;

import com.sun.webui.jsf.model.Option;

import jakarta.faces.model.SelectItem;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

/**
 *
 * @author anilam
 */
/**
 * TODO:  Class.forName can cause problems under OSGi
 *        SUN_OPTION_CLASS = Class.forName("com.sun.webui.jsf.model.Option");
 * for the time being, we have to ensure that this class stays in the 'core' which is part of the war file so any plugin
 * modul can access it.
 *
 */
public class SunOptionUtil {

    public static SelectItem[] getOptions(String[] values) {
        if (values == null) {
            SelectItem[] options = (SelectItem[]) Array.newInstance(SUN_OPTION_CLASS, 0);
            return options;
        }
        SelectItem[] options =
                (SelectItem[]) Array.newInstance(SUN_OPTION_CLASS, values.length);
        for (int i = 0; i < values.length; i++) {
            SelectItem option = getSunOption(values[i], values[i]);
            options[i] = option;
        }
        return options;
    }

    public static Option[] getOptionsArray(String[] values) {
        Option[] options =
                (Option[]) Array.newInstance(SUN_OPTION_CLASS, values.length);
        for (int i = 0; i < values.length; i++) {
            Option option = getOption(values[i], values[i]);
            options[i] = option;
        }
        return options;
    }

    public static Option getOption(String value, String label) {
        try {
            return (Option) SUN_OPTION_CONSTRUCTOR.newInstance(value, label);
        } catch (Exception ex) {
            return null;
        }
    }

    public static SelectItem[] getOptions(String[] values, String[] labels) {
        SelectItem[] options =
                (SelectItem[]) Array.newInstance(SUN_OPTION_CLASS, values.length);
        for (int i = 0; i < values.length; i++) {
            SelectItem option = getSunOption(values[i], labels[i]);
            options[i] = option;
        }
        return options;
    }

    public static SelectItem[] getModOptions(String[] values) {
        int size = (values == null) ? 1 : values.length + 1;
        SelectItem[] options =
                (SelectItem[]) Array.newInstance(SUN_OPTION_CLASS, size);
        options[0] = getSunOption("", "");
        for (int i = 0; i < size-1; i++) {
            SelectItem option = getSunOption(values[i], values[i]);
            options[i + 1] = option;
        }
        return options;
    }

    public static SelectItem getSunOption(String value, String label) {
        try {
            return (SelectItem) SUN_OPTION_CONSTRUCTOR.newInstance(value, label);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Class SUN_OPTION_CLASS = null;
    private static Constructor SUN_OPTION_CONSTRUCTOR = null;


    static {
        try {
            // TODO: Class.forName can cause problems under OSGi
            SUN_OPTION_CLASS = Class.forName("com.sun.webui.jsf.model.Option");
            SUN_OPTION_CONSTRUCTOR = SUN_OPTION_CLASS.getConstructor(new Class[]{Object.class, String.class});
        } catch (Exception ex) {
            // Ignore exception here, NPE will be thrown when attempting to
            // use SUN_OPTION_CONSTRUCTOR.
        }
    }
}
