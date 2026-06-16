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

package com.sun.jsftemplating.layout.descriptors;

import com.sun.jsftemplating.el.PermissionChecker;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This class defines a LayoutIf {@link LayoutElement}. The LayoutIf provides the functionality necessary to
 * conditionally display a portion of the layout tree. The condition is a boolean equation and may use "$...{...}" type
 * expressions to substitute in values.
 * </p>
 *
 * <p>
 * Depending on its environment, this {@link LayoutElement} can represent an {@link com.sun.jsftemplating.component.If}
 * <code>UIComponent</code> or simply exist as a {@link LayoutElement}. When its {@link #encode} method is called, the
 * if functionality will act as a {@link LayoutElement}. When the
 * {@link LayoutComponent#getChild(FacesContext, UIComponent)} method is called, it will create an
 * {@link com.sun.jsftemplating.component.If} <code>UIComponent</code>.
 * </p>
 *
 * @see com.sun.jsftemplating.el.VariableResolver
 * @see com.sun.jsftemplating.el.PermissionChecker
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutIf extends LayoutComponent {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public LayoutIf(LayoutElement parent, String condition) {
        this(parent, condition, LayoutDefinitionManager.getGlobalComponentType(null, "if"));
    }

    /**
     * <p>
     * This constructor may be used by subclasses which wish to provide an alternate {@link ComponentType}. The
     * {@link ComponentType} is used to instantiate an {@link com.sun.jsftemplating.component.If} <code>UIComponent</code>
     * (or whatever the given {@link ComponentType} specifies). This occurs when this {@link LayoutElement} is nested inside
     * a {@link LayoutComponent}. It must create a <code>UIComponent</code> in order to ensure it is executed because during
     * rendering there is no other way to get control to perform the functionality provided by this {@link LayoutElement}.
     * </p>
     */
    protected LayoutIf(LayoutElement parent, String condition, ComponentType type) {
        super(parent, (String) null, type);
        addOption("condition", condition);
        if (condition.equals("$property{condition}")) {
            _doubleEval = true;
        }
    }
// FIXME: getHandlers() may need to be overriden to prevent beforeEncode/afterEncode from being called multiple times in some cases.  I may also need to explicitly invoke these Handlers in some cases (in the Component??); See LayoutForEach for example of what may need to be done...

    /**
     * This method returns true if the condition of this LayoutIf is met, false otherwise. This provides the functionality
     * for conditionally displaying a portion of the layout tree.
     *
     * @param ctx The FacesContext
     * @param comp The UIComponent
     *
     * @return true if children are to be rendered, false otherwise.
     */
    @Override
    public boolean encodeThis(FacesContext ctx, UIComponent comp) {
        PermissionChecker checker = new PermissionChecker(this, comp,
                _doubleEval ? (String) getEvaluatedOption(ctx, "condition", comp) : (String) getOption("condition"));
        return checker.hasPermission();
    }

    /**
     * <p>
     * This flag is set to true when the condition equals "$property{condition}". This is a special case where the value to
     * be evaluated is not $property{condition}, but rather the value of this expression. This requires double evaluation to
     * correct interpret the expression. For now this is a hack for this case only. In the future we may want to support an
     * $eval{} or something more general syntax for doing this declaratively.
     * </p>
     *
     * See LayoutForEach also.
     */
    private boolean _doubleEval = false;
}
