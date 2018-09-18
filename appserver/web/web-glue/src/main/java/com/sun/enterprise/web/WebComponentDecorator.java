/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web;

import org.jvnet.hk2.annotations.Contract;

/**
 * Used to decorate a web component before it is put to service.
 * e.g., WebBeans module may like to perform additional injection
 * in a servlet or a filter.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Contract
public interface WebComponentDecorator<T>
{
    /**
     * Decoare a web component.
     * @param webComponent web component to be decorated.
     * @param wm web module which owns this web component.
     */
    void decorate(T webComponent, WebModule wm);
}
