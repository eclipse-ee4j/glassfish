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

import com.sun.webui.jsf.component.Table;
import com.sun.webui.jsf.component.TableActions;
import com.sun.webui.jsf.component.TableRowGroup;
import com.sun.webui.jsf.component.TextField;
import com.sun.webui.jsf.util.ConversionUtilities;
import com.sun.webui.jsf.util.LogUtil;

import jakarta.faces.component.UIComponent;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

import java.util.Iterator;

/**
 * A listener for receiving pagination toggle events.
 * <p>
 * A class that is interested in receiving such events registers itself with the
 * source {@link Table} of interest, by calling addActionListener().
 * </p>
 * <p>
 * <pre>
 * java.util.logging.ConsoleHandler.level = FINE
 * com.sun.webui.jsf.event.TablePaginationActionListener.level = FINE
 * </pre>
 * </p>
 */
public final class TablePaginationActionListener implements ActionListener {

    /**
     * Invoked when the action described by the specified
     * {@link ActionEvent} occurs. The source parent is expected to be a
     * TablePagination object.
     *
     * @param event The {@link ActionEvent} that has occurred
     *
     * @exception AbortProcessingException Signal the JavaServer Faces
     *  implementation that no further processing on the current event
     *  should be performed
     */
    @Override
    public void processAction(final ActionEvent event)
            throws AbortProcessingException {

        if (event != null) {
            UIComponent source = (UIComponent) event.getSource();
            processTable(getTableAncestor(source), source.getId());
        } else {
            log("processAction",
                    "Cannot process action, ActionEvent source is null");
        }
    }

    /**
     * Helper method to process Table components.
     * @param component table
     * @param id id
     * @throws AbortProcessingException if an error occurs
     */
    private static void processTable(final Table component, final String id)
            throws AbortProcessingException {

        if (component == null) {
            log("processTable", "Cannot process Table action, Table is null");
            return;
        }

        // Iterate over every TableRowGroup child and set pagination for each.
        Iterator kids = component.getTableRowGroupChildren();
        while (kids.hasNext()) {
            TableRowGroup group = (TableRowGroup) kids.next();

            if (id.equals(TableActions.PAGINATION_FIRST_BUTTON_ID)) {
                setFirst(group);
            } else if (id.equals(TableActions.PAGINATION_LAST_BUTTON_ID)) {
                setLast(group);
            } else if (id.equals(TableActions.PAGINATION_NEXT_BUTTON_ID)) {
                setNext(group);
            } else if (id.equals(TableActions.PAGINATE_BUTTON_ID)) {
                setPaginated(group);
            } else if (id.equals(TableActions.PAGINATION_PREV_BUTTON_ID)) {
                setPrev(group);
            } else if (id.equals(TableActions.PAGINATION_SUBMIT_BUTTON_ID)) {
                try {
                    setPage(group, getPage(component));
                } catch (NumberFormatException e) {
                    log("processTable", "Cannot obtain page field value");
                    return;
                }
            }
        }
    }

    /**
     * Get the closest Table ancestor that encloses this component.
     *
     * @param component UIComponent for which to extract children.
     * @return Table
     */
    private static Table getTableAncestor(final UIComponent component) {
        if (component == null) {
            log("getTableAncestor",
                    "Cannot obtain Table ancestor, UIComponent is null");
            return null;
        } else if (component instanceof Table) {
            return (Table) component;
        } else {
            return getTableAncestor(component.getParent());
        }
    }

    /**
     * Get the value of the page field.
     * @param component table
     * @return int
     */
    private static int getPage(final Table component) {
        UIComponent actions = component
                .getFacet(Table.TABLE_ACTIONS_BOTTOM_ID);
        UIComponent field;
        if (actions != null) {
            field = (UIComponent) actions
                        .getFacet(TableActions.PAGINATION_PAGE_FIELD_ID);
        } else {
            field = null;
        }
        String value = null;
        if (field instanceof TextField) {
            value = ConversionUtilities.convertValueToString(field,
                    ((TextField) field).getValue());
        } else {
            log("getPage",
                    "Cannot obtain page text field value,"
                            + " not TextField instance");
        }
        if (value != null) {
            return Integer.parseInt(value);
        }
        return -1;
    }

    /**
     * Set current page.
     * @param component row group
     * @param page index
     */
    private static void setPage(final TableRowGroup component, final int page) {
        if (component == null) {
            log("setPage", "Cannot set page, TableRowGroup is null");
            return;
        }
        // Set the starting row for the current page.
        component.setPage(page);
    }

    /**
     * Set first row.
     * @param component row group
     */
    private static void setFirst(final TableRowGroup component) {
        if (component == null) {
            log("setFirst", "Canot set first row, TableRowGroup is null");
            return;
        }
        // Set the starting row for the first page.
        component.setPage(1);
    }

    /**
     * Set last row.
     * @param component row group
     */
    private static void setLast(final TableRowGroup component) {
        if (component == null) {
            log("setLast", "Cannot set last row, TableRowGroup is null");
            return;
        }
        // Get the row number of the last page to be displayed.
        component.setPage(component.getPages());
    }

    /**
     * Set next row.
     * @param component row group
     */
    private static void setNext(final TableRowGroup component) {
        if (component == null) {
            log("setNext", "Cannot set next row, TableRowGroup is null");
            return;
        }
        // Get the starting row index for the next page.
        component.setPage(component.getPage() + 1);
    }

    /**
     * Set paginated.
     * @param component row group
     */
    private static void setPaginated(final TableRowGroup component) {
        if (component == null) {
            log("setPaginated", "Cannot set paginated, TableRowGroup is null");
            return;
        }
        // Toggle between paginated and scroll mode.
        component.setPaginated(!component.isPaginated());
    }

    /**
     * Set previous row.
     * @param component row group
     */
    private static void setPrev(final TableRowGroup component) {
        if (component == null) {
            log("setPrev", "Cannot set previous row, TableRowGroup is null");
            return;
        }
        // Set the starting row index for the previous page.
        component.setPage(component.getPage() - 1);
    }

    /**
     * Log fine messages.
     * @param method method name
     * @param message message to log
     */
    private static void log(final String method, final String message) {
        // Get class.
        Class clazz = TablePaginationActionListener.class;
        if (LogUtil.fineEnabled(clazz)) {
            // Log method name and message.
            LogUtil.fine(clazz, clazz.getName() + "." + method + ": "
                    + message);
        }
    }
}
