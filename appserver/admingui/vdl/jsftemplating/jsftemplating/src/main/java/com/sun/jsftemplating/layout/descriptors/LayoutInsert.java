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

import com.sun.jsftemplating.layout.event.EncodeEvent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * <p>
 * This class represents a <code>ui:insert</code>.
 * </p>
 *
 * @author Jason Lee
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutInsert extends LayoutElementBase {
    private static final long serialVersionUID = 1L;
    private String name;

    /**
     * @param parent
     * @param id
     */
    public LayoutInsert(LayoutElement parent, String id) {
        super(parent, id);
    }

    /**
     * <p>
     * Returns the name of the {@link LayoutDefine} to look for when including content for this <code>LayoutInsert</code>.
     * This value may be <code>null</code> to indicate that it should use its body content.
     * </p>
     */
    public String getName() {
        return name;
    }

    /**
     * <p>
     * Sets the name of the {@link LayoutDefine} to look for when including content for this <code>LayoutInsert</code>. This
     * value may be <code>null</code> to indicate that it should use its body content.
     * </p>
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * <p>
     * This method is override to enable searching of its content via the name of this insert (if supplied), or the
     * rendering of its body if not supplied, or not found. If this is encountered outside the context of a composition, it
     * will render its body content also.
     * </p>
     *
     * @see LayoutElementBase#encodeThis(jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent)
     */
    @Override
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
        Stack<LayoutElement> stack = LayoutComposition.getCompositionStack(context);
        if (stack.empty()) {
            // Render whatever is inside the insert
            return true;
        }

        // Get assoicated UIComposition
        String name = getName();
        if (name == null) {
            encodeChildren(context, component, stack.get(0));
        } else {
            // First resolve any EL in the insertName
            name = "" + resolveValue(context, component, name);

            // Search for specific LayoutDefine
            LayoutElement def = LayoutInsert.findLayoutDefine(context, component, stack, name);
            if (def == null) {
                // Render whatever is inside the insert
                return true;
            } else {
                // Found ui:define, render it
                encodeChildren(context, component, def);
            }
        }
        return false; // Already rendered it
    }

    /**
     * <p>
     * Encode the appropriate children...
     * </p>
     */
    private void encodeChildren(FacesContext context, UIComponent component, LayoutElement parentElt) throws IOException {
        // Fire an encode event
        dispatchHandlers(context, ENCODE, new EncodeEvent(component));

        // Iterate over children
        LayoutElement childElt = null;
        Iterator<LayoutElement> it = parentElt.getChildLayoutElements().iterator();
        while (it.hasNext()) {
            childElt = it.next();
            childElt.encode(context, component);
        }
    }

    /**
     * <p>
     * This method searches the given the entire <code>stack</code> for a {@link LayoutDefine} with the given
     * <code>name</code>.
     * </p>
     */
    public static LayoutDefine findLayoutDefine(FacesContext context, UIComponent parent, List<LayoutElement> eltList, String name) {
        Iterator<LayoutElement> stackIt = eltList.iterator();
        LayoutDefine define = null;
        while (stackIt.hasNext()) {
            define = findLayoutDefine(context, parent, stackIt.next(), name);
            if (define != null) {
                return define;
            }
        }

        // Not found!
        return null;
    }

    /**
     * <p>
     * This method searches the given {@link LayoutElement} for a {@link LayoutDefine} with the given <code>name</code>.
     * </p>
     */
    private static LayoutDefine findLayoutDefine(FacesContext context, UIComponent parent, LayoutElement elt, String name) {
        Iterator<LayoutElement> it = elt.getChildLayoutElements().iterator();
        LayoutElement def = null;
        while (it.hasNext()) {
            def = it.next();
            if (def instanceof LayoutDefine && def.getId(context, parent).equals(name)) {
                // We found what we're looking for...
                return (LayoutDefine) def;
            }
        }

        // We still haven't found it, search the child LayoutElements
        it = elt.getChildLayoutElements().iterator();
        while (it.hasNext()) {
            def = findLayoutDefine(context, parent, it.next(), name);
            if (def != null) {
                return (LayoutDefine) def;
            }
        }

        // Not found!
        return null;
    }
}
