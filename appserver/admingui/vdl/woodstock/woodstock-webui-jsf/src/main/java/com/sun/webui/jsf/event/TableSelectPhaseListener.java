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
package com.sun.webui.jsf.event;

import com.sun.data.provider.RowKey;
import com.sun.webui.jsf.util.LogUtil;

import jakarta.faces.FactoryFinder;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.faces.lifecycle.LifecycleFactory;

import java.util.HashMap;

/**
 * A utility class for radiobutton and checkbox components used to select rows
 * of a table.
 * <p>
 * Note: UI guidelines recommend that rows should be unselected when no longer
 * in view. For example, when a user selects rows of the table and navigates to
 * another page. Or, when a user applies a filter or sort that may hide
 * previously selected rows from view. If a user invokes an action to delete the
 * currently selected rows, they may inadvertently remove rows not displayed on
 * the current page. Using TableSelectPhaseListener ensures that invalid row
 * selections are not rendered by clearing selected state after the render
 * response phase. That said, there are cases when maintaining state across
 * table pages is necessary. In this scenario, use the keepSelected method to
 * prevent state from being cleared by this instance.
 * </p>
 * <p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.event.TableSelectPhaseListener.level = FINE
 * </pre>
 * </p>
 */
public final class TableSelectPhaseListener implements PhaseListener {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 6955269103244653901L;

    /**
     * Unselected object for primitive values.
     */
    private Object unselected = null;

    /**
     * Selected values map.
     */
    private final HashMap<String, Object> selected =
            new HashMap<String, Object>();

    /**
     *  Do not clear selected flag.
     */
    private boolean keepSelected = false;

    /**
     * Default constructor.
     */
    public TableSelectPhaseListener() {
        // Add phase listener.
        LifecycleFactory factory = (LifecycleFactory) FactoryFinder
                .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle = factory.getLifecycle(
                LifecycleFactory.DEFAULT_LIFECYCLE);
        lifecycle.addPhaseListener(this);
    }

    /**
     * Construct an instance with the given flag indicating that selected
     * objects should not be cleared after the render response phase.
     *
     * @param newKeepSelected If true, objects are not cleared.
     */
    public TableSelectPhaseListener(final boolean newKeepSelected) {
        this();
        keepSelected(newKeepSelected);
    }

    /**
     * Construct an instance with an unselected parameter.
     * <p>
     * The unselected parameter is only required if a primitive value is being
     * used for the selecteValue attribute of the checkbox or radio button. If
     * the selectedValue property is an Object value then unselected can be
     * null. If however it is a primitive type then it should be the MIN_VALUE
     * constant instance of the wrapper Object type. For example if the
     * application is assigning int values to selectedValue then unselected
     * should be new Integer(Integer.MIN_VALUE).
     * </p>
     *
     * @param newUnselected the object to return for an unselected checkbox.
     */
    public TableSelectPhaseListener(final Object newUnselected) {
        this();
        this.unselected = newUnselected;
    }

    /**
     * Called during the JSF life-cycle after the RENDER_RESPONSE phase.
     *
     * @param event The PhaseEvent object.
     */
    @Override
    public void afterPhase(final PhaseEvent event) {
        if (!keepSelected) {
            selected.clear();
        } else {
            log("afterPhase",
                    "Selected values not cleared, keepSelected is false");
        }
    }

    /**
     * Called during the JSF life-cycle before the RENDER_RESPONSE phase.
     *
     * @param event The PhaseEvent object.
     */
    @Override
    public void beforePhase(final PhaseEvent event) {
        // Not needed
    }

    /**
     * Get the phase id.
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    /**
     * Clear all selected objects.
     */
    public void clear() {
        selected.clear();
    }

    /**
     * Get the selected object from this instance.
     * <p>
     * Note: Call this method from the get method that that is bound to the
     * selected attribute.
     * </p>
     *
     * @param rowKey The current RowKey.
     * @return The selected object.
     */
    public Object getSelected(final RowKey rowKey) {
        if (rowKey != null) {
            return selected.get(rowKey.getRowId());
        }
        return unselected;
    }

    /**
     * Test if the flag indicating that selected objects should be cleared after
     * the render response phase.
     *
     * @return true if objects are not to be cleared.
     */
    public boolean isKeepSelected() {
        return keepSelected;
    }

    /**
     * Test if the object associated with the given RowKey is selected.
     *
     * @param rowKey The current RowKey.
     * @return A true or false value.
     */
    public boolean isSelected(final RowKey rowKey) {
        Object object = getSelected(rowKey);
        return (object != null && object != unselected);
    }

    /**
     * Set the flag indicating that selected objects should be cleared after the
     * render response phase.
     *
     * @param newKeepSelected Selected objects are kept true, cleared if false.
     */
    public void keepSelected(final boolean newKeepSelected) {
        this.keepSelected = newKeepSelected;
    }

    /**
     * Set the selected object for this instance.
     * <p>
     * Note: Call this method from the set method that that is bound to the
     * selected attribute.
     * </p>
     *
     * @param rowKey The current RowKey.
     * @param object The selected object.
     */
    public void setSelected(final RowKey rowKey, final Object object) {
        if (rowKey != null) {
            selected.put(rowKey.getRowId(), object);
        }
    }

    /**
     * Log fine messages.
     * @param method method name
     * @param message message to log
     */
    private static void log(final String method, final String message) {
        // Get class.
        Class clazz = TableSelectPhaseListener.class;
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": "
                    + message);
        }
    }
}
