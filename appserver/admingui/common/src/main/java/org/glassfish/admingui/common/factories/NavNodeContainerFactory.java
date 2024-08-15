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

package org.glassfish.admingui.common.factories;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * The NavigationFactory provides an abstraction layer for the Woodstock tree
 * component (currently), giving us the ability to change the tree implementation to
 * another component or set, or a different component type altogether.  The supported
 * attributes are:
 * <ul>
 * <li>id</li>
 * <li>label - The text label for the top of the tree</li>
 * <li>url - An optional URL</li>
 * <li>icon - The URL to an image for the tree's root icon</li>
 * <li>target</li>
 * </ul>
 */
@UIComponentFactory("gf:navNodeContainer")
public class NavNodeContainerFactory extends ComponentFactoryBase {

    /**
     * <p>
     * This is the factory method responsible for creating the
     * <code>UIComponent</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated
     *     with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     * @return The newly created <code>Tree</code>.
     */
    @Override
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Create the UIComponent
        UIComponent comp = createComponent(context, COMPONENT_TYPE, descriptor, parent);
        String compId = descriptor.getId(context, comp.getParent());
        if ((compId != null) && (!compId.equals(""))) {
            comp.setId(compId);
        }

        final Object url = descriptor.getOption("url");
        final Object icon = descriptor.getOption("icon");
        final Object label = descriptor.getOption("label");
        final Object target = descriptor.getOption("target");

        if (label != null) {
            setOption(context, comp, descriptor, "text",label);
        }
        if (target != null) {
            setOption(context, comp, descriptor, "target",target);
        }
        if (icon != null) {
            setOption(context, comp, descriptor, "imageURL", icon);
        }
        if (url != null) {
            setOption(context, comp, descriptor, "url", url);
        }
        setOption(context, comp, descriptor, "clientSide", Boolean.TRUE);

        // Return the component
        return comp;
    }

    /**
     * <p>
     * The <code>UIComponent</code> type that must be registered in the
     * <code>faces-config.xml</code> file mapping to the UIComponent class
     * to use for this <code>UIComponent</code>.
     * </p>
     */
    public static final String COMPONENT_TYPE = "com.sun.webui.jsf.Tree";
}
