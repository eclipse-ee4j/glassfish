/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.launcher;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * The one and only type of Exception that will be thrown out of this package. I18N is wired in. If a String message is
 * found in the resource bundle, it will use that String. If not, it will use the String itself.
 *
 * @author bnevins
 */
public class GFLauncherException extends Exception {

    private static final long serialVersionUID = 2048446361062717571L;
    private final static LocalStringsImpl strings = new LocalStringsImpl(GFLauncherException.class);

    /**
     *
     * @param msg The message is either pointing at a I18N key in the resource bundle or will be treated as a plain string.
     */
    public GFLauncherException(String msg) {
        super(strings.get(msg));
    }

    /**
     *
     * @param msg The message is either pointing at a I18N key in the resource bundle or will be treated as a plain string
     * that will get formatted with objs.
     * @param objs Objects used for formatting the message.
     */
    public GFLauncherException(String msg, Object... objs) {
        super(strings.get(msg, objs));
    }

    /**
     *
     * @param msg The message is either pointing at a I18N key in the resource bundle or will be treated as a plain string.
     * @param t The causing Throwable.
     */
    public GFLauncherException(String msg, Throwable t) {
        super(strings.get(msg), t);
    }

    /**
     *
     * @param msg The message is either pointing at a I18N key in the resource bundle or will be treated as a plain string
     * that will get formatted with objs.
     * @param t The causing Throwable.
     * @param objs Objects used for formatting the message.
     */
    public GFLauncherException(String msg, Throwable t, Object... objs) {
        super(strings.get(msg, objs), t);
    }

    /**
     *
     * @param t The causing Throwable.
     */
    public GFLauncherException(Throwable t) {
        super(t);
    }

}
