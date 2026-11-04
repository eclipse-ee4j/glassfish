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

package com.sun.webui.jsf.faces;

import com.sun.data.provider.RowKey;
import com.sun.data.provider.SortCriteria;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.TableRowDataProvider;
import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

import java.util.Map;

/**
 * The ValueExpressionSortCriteria class is an implementation of SortCriteria
 * that simply retrieves the sort value from a {@link ValueExpression} which is
 * created using the specified value expression.
 */
@Component(isTag = false)
public final class ValueExpressionSortCriteria extends SortCriteria {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -1400195724479592404L;

    /**
     * The value expression.
     */
    @Property(displayName = "Value Expression")
    private String valueExpression;

    /**
     * The request map key.
     */
    @Property(displayName = "Request Map Key")
    private String requestMapKey = "currentRow";

    /**
     * Row provider.
     */
    private transient TableRowDataProvider rowProvider;

    /**
     * This is a monitor lock for rowProvider.
     */
    private final String rowProviderLock = "rowProviderLock";

    /**
     * Constructs a ValueExpressionSortCriteria with no value expression.
     */
    public ValueExpressionSortCriteria() {
    }

    /**
     * Constructs a ValueExpressionSortCriteria with the specified value
     * expression.
     *
     * @param newValueExpression The desired value expression
     */
    public ValueExpressionSortCriteria(final String newValueExpression) {
        this.valueExpression = newValueExpression;
    }

    /**
     * Constructs a ValueExpressionSortCriteria with the specified value
     * expression and ascending state.
     *
     * @param newValueExpression The desired value expression
     * @param newAscending The desired boolean state for the ascending property
     */
    public ValueExpressionSortCriteria(final String newValueExpression,
            final boolean newAscending) {

        this.valueExpression = newValueExpression;
        this.setAscending(newAscending);
    }

    /**
     * Returns the value expression to use for this sort criteria.
     *
     * @return The currently set value expression for this sort criteria
     */
    public String getValueExpression() {
        return valueExpression;
    }

    /**
     * Sets the value expression for this sort criteria.
     *
     * @param newValueExpression The desired value expression for this sort
     * criteria
     */
    public void setValueExpression(final String newValueExpression) {
        this.valueExpression = newValueExpression;
    }

    /**
     * Returns the request map variable key that will be used to store the
     * {@link TableRowDataProvider} for the current row being sorted.  This
     * allows value expressions to refer to the "current" row during the sort
     * operation.
     *
     * @return String key to use for the {@link TableRowDataProvider}
     */
    public String getRequestMapKey() {
        return requestMapKey;
    }

    /**
     * Sets the request map variable key that will be used to store the
     * {@link TableRowDataProvider} for the current row being sorted. This
     * allows value expressions to refer to the "current" row during the sort
     * operation.
     *
     * @param newRequestMapKey String key to use for the
     * {@link TableRowDataProvider}
     */
    public void setRequestMapKey(final String newRequestMapKey) {
        this.requestMapKey = newRequestMapKey;
    }

    /**
     * If no display name is set, this returns the value expression.
     *
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        String name = super.getDisplayName();
        if ((name == null || "".equals(name))
                && valueExpression != null
                && !"".equals(valueExpression)) {
            return valueExpression;
        }
        return name;
    }

    @Override
    public String getCriteriaKey() {
        if (valueExpression != null) {
            return valueExpression;
        }
        return "";
    }

    @Override
    public Object getSortValue(final TableDataProvider provider,
            final RowKey row) {

        if (valueExpression == null || "".equals(valueExpression)) {
            return null;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueExpression valueBinding = facesContext
                .getApplication()
                .getExpressionFactory()
                .createValueExpression(facesContext.getELContext(),
                        valueExpression, Object.class);

        if (valueBinding == null) {
            return null;
        }

        Map<String, Object> requestMap = facesContext
                .getExternalContext()
                .getRequestMap();
        Object value;

        synchronized (rowProviderLock) {

            Object storedRequestMapValue = null;
            if (requestMapKey != null && !"".equals(requestMapKey)) {
                storedRequestMapValue = requestMap.get(requestMapKey);
                if (rowProvider == null) {
                    rowProvider = new TableRowDataProvider();
                }
                rowProvider.setTableDataProvider(provider);
                rowProvider.setTableRow(row);
                requestMap.put(requestMapKey, rowProvider);
            }

            value = valueBinding.getValue(facesContext.getELContext());
            if (requestMapKey != null && !"".equals(requestMapKey)) {
                if (rowProvider != null) {
                    rowProvider.setTableDataProvider(null);
                    rowProvider.setTableRow(null);
                }
                requestMap.put(requestMapKey, storedRequestMapValue);
            }
        }
        return value;
    }
}
