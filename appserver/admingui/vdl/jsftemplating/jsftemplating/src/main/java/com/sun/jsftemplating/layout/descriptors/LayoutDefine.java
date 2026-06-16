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

package com.sun.jsftemplating.layout.descriptors;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.IOException;

/**
 * <p>
 * This {@link LayoutElement} provides a means to identify a portion of the LayoutDefinition tree by name (id). This is
 * used by {@link LayoutInsert} to include portions of the tree defined elsewhere at the location of the
 * {@link LayoutInsert}.
 * </p>
 *
 * @author Jason Lee
 */
public class LayoutDefine extends LayoutElementBase {
    private static final long serialVersionUID = 1L;

    /**
     * @param parent
     * @param id
     */
    public LayoutDefine(LayoutElement parent, String id) {
        super(parent, id);
    }

    @Override
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
        return true;
    }
}
