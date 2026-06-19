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

package com.sun.jsftemplating.component;

import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * This interface defines additional methods in addition to those defined by UIComponent that are needed to work with a
 * TemplateRenderer.
 * </p>
 *
 * <p>
 * JSF did not define an interface for UIComponent, so I cannot extend an interface here. This means that casting is
 * needed to use UIComponent features from a TemplateComponent.
 * </p>
 *
 * <p>
 * If you need to have a <code>NamingContainer</code>, do not forget to implement that interface in addition to this
 * interface.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public interface TemplateComponent extends ChildManager {

    /**
     * This method will find the request child UIComponent by id. If it is not found, it will attempt to create it if it can
     * find a LayoutElement describing it.
     *
     * @param context The FacesContext
     * @param id The UIComponent id to search for
     *
     * @return The requested UIComponent
     */
    UIComponent getChild(FacesContext context, String id);

    /**
     * This method returns the LayoutDefinition associated with this component.
     *
     * @param context The FacesContext
     *
     * @return LayoutDefinition associated with this component.
     */
    LayoutDefinition getLayoutDefinition(FacesContext context);

    /**
     * This method returns the LayoutDefinitionKey for this component.
     *
     * @return key The key to use in the LayoutDefinitionManager
     */
    String getLayoutDefinitionKey();

    /**
     * This method sets the LayoutDefinition key for this component.
     *
     * @param key The key to use in the LayoutDefinitionManager
     */
    void setLayoutDefinitionKey(String key);

    /**
     * <p>
     * This method returns the value of the requested field. It should first check the value of <code>field</code> passed
     * in, it should return that value if set. Next, it should check to see if there is a <code>ValueExpression</code>
     * matching <code>attributeName</code> and return that value, if it exists. If neither of the first 2 cases yielded a
     * result, <code>defaultValue</code> is returned.
     * </p>
     *
     * @param field The field which may contain the value.
     * @param attributeName The <code>ValueExpression</code> name.
     * @param defaultValue The default value.
     *
     * @return The value of the property.
     */
    <V> V getPropertyValue(V field, String attributeName, V defaultValue);
}
