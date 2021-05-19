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

package com.sun.jdo.api.persistence.enhancer.util;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;


/**
 * Basic support for enhancer implementation.
 */
// @olsen: added class
public class Support extends Assertion {

    // ^olsen: hack
    static public final Timer timer = new Timer();

    /**
     * I18N message handler
     */
    static private ResourceBundle MESSAGES;

    static {
        try {
            MESSAGES = I18NHelper.loadBundle("com.sun.jdo.api.persistence.enhancer.Bundle", // NOI18N
                Support.class.getClassLoader());
        } catch (java.util.MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key) {
        return I18NHelper.getMessage(MESSAGES, key);
    }


    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key, String arg) {
        return I18NHelper.getMessage(MESSAGES, key, arg);
    }


    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key, String arg1, String arg2) {
        return I18NHelper.getMessage(MESSAGES, key, arg1, arg2);
    }


    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key, String arg1, String arg2, String arg3) {
        return I18NHelper.getMessage(MESSAGES, key, arg1, arg2, arg3);
    }


    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key, int arg1, String arg2) {
        return I18NHelper.getMessage(MESSAGES, key, new Object[] {Integer.valueOf(arg1), arg2});
    }


    /**
     * Returns the I18N message.
     */
    static protected final String getI18N(String key, Object[] args) {
        return I18NHelper.getMessage(MESSAGES, key, args);
    }
}
