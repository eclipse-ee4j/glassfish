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

package com.sun.jsftemplating.component.factory.sun;

import com.sun.jsftemplating.annotation.UIComponentFactory;

// FIXME: Document

/**
 * <p>
 * This factory is responsible for instantiating an <code>Listbox
 *    UIComponent</code>.
 * </p>
 *
 * <p>
 * The {@link com.sun.jsftemplating.layout.descriptors.ComponentType} id for this factory is: "sun:listbox".
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("sun:listbox")
public class ListboxFactory extends DropDownFactory {

    /**
     * <p>
     * This method overrides the parent class to return a different ComponentType.
     * </p>
     */
    @Override
    protected String getComponentType() {
        return COMPONENT_TYPE;
    }

    /**
     * <p>
     * The <code>UIComponent</code> type that must be registered in the <code>faces-config.xml</code> file mapping to the
     * UIComponent class to use for this <code>UIComponent</code>.
     * </p>
     */
    public static final String COMPONENT_TYPE = "com.sun.webui.jsf.Listbox";
}
