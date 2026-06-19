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
import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.layout.LayoutDefinitionManager;
import com.sun.jsftemplating.layout.descriptors.ComponentType;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutStaticText;
import com.sun.jsftemplating.layout.event.CreateChildEvent;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * This factory is responsible for instantiating a <code>TableRowGroup
 *    UIComponent</code> which supports a dynamic # of columns.
 * </p>
 *
 * <p>
 * The {@link ComponentType} id for this factory is: "sun:dynamicColumnRowGroup".
 * </p>
 *
 * <p>
 * This factory fires a "createChild" event for each column that it creates. This allows the children of the column to
 * be created in the event allowing for the page developer to customize this (otherwise only "StaticText" children will
 * be created in the columns). In the "createChild" event handler, you may return a <code>UIComponent</code> which will
 * be used as the <code>UIComponent</code> for that column. You also have access to the column <code>UICompoent</code>
 * and may add children directly to it if you wish. The "data" value set in the <code>CreateChildEvent</code> object is
 * the value passed in for the column.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("sun:dynamicColumnRowGroup")
public class DynamicColumnTableRowGroupFactory extends TableRowGroupFactory {

    /**
     * <p>
     * This is the factory method responsible for creating the <code>UIComponent</code>.
     * </p>
     *
     * @param ctx The <code>FacesContext</code>
     * @param desc The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     *
     * @return The newly created <code>TableRowGroup</code>.
     */
    @Override
    public UIComponent create(FacesContext ctx, LayoutComponent desc, UIComponent parent) {
        // Create the UIComponent
        UIComponent comp = super.create(ctx, desc, parent);
        String id = comp.getId();

        // Dynamically create the child columns...
        // First find all the column-specific properties
        ArrayList<String> colAttKeys = new ArrayList<>();
        for (String key : desc.getOptions().keySet()) {
            if (key.startsWith("column")) {
                colAttKeys.add(key);
            }
        }

        // Make sure we have some...
        if (colAttKeys.size() == 0) {
            // Nothing to do... hopefully there are table column children,
            // otherwise nothing will happen
            return comp;
        }

        // Now determine the # of columns
        List<Object> values = getColumnPropertyValues(ctx, desc, colAttKeys.get(0), parent);
        int size = values.size();

        // Create the LC's for the columns
        List<LayoutComponent> columns = new ArrayList<>(size);
        ComponentType colType = LayoutDefinitionManager.getGlobalComponentType(ctx, COLUMN_TYPE);
        for (int idx = 0; idx < size; idx++) {
            LayoutComponent column = new LayoutComponent(desc, id + COLUMN_SEPERATOR + idx, colType);
            columns.add(column);
        }

        // Loop through the properties to set on the columns
        for (String key : colAttKeys) {
            if (key.equals(COLUMN_VALUE_KEY)) {
                // Skip the value as it is added as a child.
                continue;
            }
            values = getColumnPropertyValues(ctx, desc, key, parent);

            // Loop through the columns
            int idx = 0;
            for (Object entry : values) {
                // Set the value of each property on each column
                columns.get(idx++).addOption(getColumnKey(key), entry);
            }
        }

        // Create the columns...
        UIComponent columnComp = null;
        String value = null;
        Object eventVal = null;
        values = getColumnPropertyValues(ctx, desc, COLUMN_VALUE_KEY, parent);
        int idx = 0;
        ComponentUtil compUtil = ComponentUtil.getInstance(ctx);
        for (LayoutComponent columnDesc : columns) {
            columnComp = compUtil.createChildComponent(ctx, columnDesc, comp);

            value = "" + values.get(idx++);
            eventVal = desc.dispatchHandlers(ctx, CreateChildEvent.EVENT_TYPE, new CreateChildEvent(columnComp, value));

            if (eventVal != null && eventVal instanceof UIComponent) {
                // Add the child created during the event.
                columnComp.getChildren().add((UIComponent) eventVal);
            } else {
                // Create the column (value) child
                // The child of each TableColumn will be a LayoutStaticText for now...
                compUtil.createChildComponent(ctx, new LayoutStaticText(columnDesc, columnDesc.getUnevaluatedId() + CHILD_SUFFIX, value), columnComp);
            }
        }

        // Return the component
        return comp;
    }

    /**
     * <p>
     * This method obtains a <code>List</code> from the given property name. If the value is not a <code>List</code>, it
     * will throw an exception.
     * </p>
     */
    private List<Object> getColumnPropertyValues(FacesContext ctx, LayoutComponent parentDesc, String propertyName, UIComponent parent) {
        Object val = parentDesc.getEvaluatedOption(ctx, propertyName, parent);
        if (val == null) {
            throw new IllegalArgumentException(
                    "DynamicColumnTableRowGroupFactory requires a valid '" + propertyName + "' attribute, however one was not supplied!");
        }
        if (val instanceof String) {
            // Only try to do this if we have a String (not for Lists)
            if (ComponentUtil.getInstance(ctx).isValueReference((String) val)) {
// FIXME: resolve $x{y} values: Object newBinding = VariableResolver.resolveVariables(ctx, elt, parent, binding);??
// FIXME: I believe I have a util method for this (resolveValue?)
                ValueExpression ve = ctx.getApplication().getExpressionFactory().createValueExpression(ctx.getELContext(), (String) val, Object.class);
                val = ve.getValue(ctx.getELContext());
            }
        }
        if (!(val instanceof List)) {
            throw new IllegalArgumentException("DynamicColumnTableRowGroupFactory requires all 'column*' " + "properties to resolve to an instance of a List. '"
                    + propertyName + "' is a '" + val.getClass().getName() + "'!");
        }

        // Return the result
        return (List<Object>) val;
    }

    /**
     * <p>
     * This method removes the "column" prefix and lower-cases the first letter of the property name.
     * </p>
     */
    private String getColumnKey(String key) {
        return key.substring(COLUMN_LEN, COLUMN_LEN + 1).toLowerCase() + key.substring(COLUMN_LEN + 1);
    }

    /**
     * <p>
     * This method is overriden to prevent setting properties that don't apply to this component (but may apply to their
     * children or to the behavior of this factory).
     * </p>
     */
    @Override
    protected void setOption(FacesContext ctx, UIComponent comp, LayoutComponent desc, String key, Object value) {
        if (key.startsWith("column")) {
            return;
        }
        super.setOption(ctx, comp, desc, key, value);
    }

    /**
     * <p>
     * The component type of the column component to create.
     * </p>
     */
    public static final String COLUMN_TYPE = "sun:tableColumn";
    public static final String COLUMN_VALUE_KEY = "columnValue";
    public static final String COLUMN_SEPERATOR = "col";
    public static final String CHILD_SUFFIX = "child";
    public static final int COLUMN_LEN = "column".length();
}
