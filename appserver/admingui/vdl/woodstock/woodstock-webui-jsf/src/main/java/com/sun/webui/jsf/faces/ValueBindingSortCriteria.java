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

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * The ValueBindingSortCriteria class is an implementation of SortCriteria that
 * simply retrieves the sort value from the {@link ValueBinding}.
 */
@Component(isTag = false)
public final class ValueBindingSortCriteria extends SortCriteria {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 3213170928687846906L;

    /**
     * Value binding.
     */
    @Property(displayName = "Value Binding", isAttribute = false)
    private transient ValueExpression valueExpression;

    /**
     * Request map key.
     */
    @Property(displayName = "Request Map Key")
    private String requestMapKey = "currentRow";

    /**
     * Row provider.
     */
    private transient TableRowDataProvider rowProvider;

    /**
     * Lock.
     */
    private String rowProviderLock = "rowProviderLock";

    /**
     * Constructs a ValueBindingSortCriteria with no associated
     * {@link ValueExpression}.
     */
    public ValueBindingSortCriteria() {
    }

    /**
     * Constructs a ValueBindingSortCriteria with the specified
     * {@link ValueExpression}.
     *
     * @param newValueExpression The desired ValueExpression
     */
    public ValueBindingSortCriteria(final ValueExpression newValueExpression) {
        this.valueExpression = newValueExpression;
    }

    /**
     * Constructs a ValueBindingSortCriteria with the specified
     * {@link ValueExpression} and ascending state.
     *
     * @param newValueExpression The desired ValueExpression
     * @param newAscending The desired boolean state for the ascending property
     */
    public ValueBindingSortCriteria(final ValueExpression newValueExpression,
            final boolean newAscending) {

        this.valueExpression = newValueExpression;
        this.setAscending(newAscending);
    }

    /**
     * Returns the ValueExpression to use for this sort criteria.
     *
     * @return The currently set ValueExpression for this sort criteria
     */
    public ValueExpression getValueExpression() {
        return valueExpression;
    }

    /**
     * Sets the ValueExpression for this sort criteria.
     *
     * @param newValueExpression The desired ValueExpression for this sort
     * criteria
     */
    public void setValueExpression(final ValueExpression newValueExpression) {
        this.valueExpression = newValueExpression;
    }

    /**
     * Returns the request map variable key that will be used to store the
     * {@link TableRowDataProvider} for the current row being sorted. This
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
     * If no display name is set, this returns the {@link ValueExpression}'s
     * display name.
     *
     * {@inheritDoc}
     */
    @Override
    public String getDisplayName() {
        String name = super.getDisplayName();
        if ((name == null || "".equals(name)) && valueExpression != null) {
            return valueExpression.getExpressionString();
        }
        return name;
    }

    /**
     * Returns the ValueExpression's value expresssion string.
     *
     * {@inheritDoc}
     */
    @Override
    public String getCriteriaKey() {
        if (valueExpression != null) {
            return valueExpression.getExpressionString();
        }
        return "";
    }

    /**
     * Returns the value from the {@link ValueExpression} ignoring the
     * arguments.
     *
     * {@inheritDoc}
     */
    @Override
    public Object getSortValue(final TableDataProvider provider,
            final RowKey row) {

        if (valueExpression == null) {
            return null;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, Object> requestMap = facesContext
                .getExternalContext()
                .getRequestMap();
        Object value;
        //FIXME synchronization on a non-final field
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

            value = valueExpression.getValue(facesContext.getELContext());
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

    /**
     * Serialize this instance to the given output stream.
     * @param out output stream
     * @throws IOException if an IO error occurs
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {

        // Serialize simple objects first
        out.writeObject(requestMapKey);
        out.writeObject(rowProviderLock);

        // Serialize valueExpression specially
        if (valueExpression != null) {
            out.writeObject(valueExpression.getExpressionString());
        } else {
            out.writeObject((String) null);
        }

        // NOTE - rowProvider is reconstituted on demand,
        // so we don't need to serialize it
    }

    /**
     * De-serialize from input stream.
     * @param in input stream
     * @throws IOException if an IO error occurs
     * @throws ClassNotFoundException if a class is not found during
     * de-serialization
     */
    private void readObject(final ObjectInputStream in)
            throws IOException, ClassNotFoundException {

        // Deserialize simple objects first
        requestMapKey = (String) in.readObject();
        rowProviderLock = (String) in.readObject();

        // Deserialize valueExpression specially
        String s = (String) in.readObject();
        if (s != null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext elContext = FacesContext.getCurrentInstance()
                    .getELContext();
            valueExpression = facesContext.getApplication()
                            .getExpressionFactory()
                            .createValueExpression(elContext, s, Object.class);
        } else {
            valueExpression = null;
        }
    }
}
