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

import com.sun.data.provider.FilterCriteria;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.impl.CompareFilterCriteria;
import com.sun.data.provider.impl.TableRowDataProvider;
import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;

import jakarta.el.ValueExpression;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Map;

/**
 * An implementation of {@link FilterCriteria}
 * that compares the value of a {@link ValueExpression} with a predefined
 * <code>compareValue</code>.  A user may specify matches to include less than
 * (<), equal to (==), or greater than (>) the <code>compareValue</code> Object,
 * or any combination of the above.
 *
 * <p>Use the <code>requestMapKey</code> property
 *
 * @see TableDataProvider
 * @see TableDataFilter
 */
@Component(isTag = false)
public final class ValueBindingFilterCriteria extends FilterCriteria {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 7979125228842716689L;

    /**
     * Match equal to flag.
     */
    private boolean matchEqualTo = true;

    /**
     * Match less than flag.
     */
    private boolean matchLessThan = false;

    /**
     * Match greater than flag.
     */
    private boolean matchGreaterThan = false;

    /**
     * Value expression.
     */
    @Property(displayName = "Value Binding", isAttribute = false)
    private transient ValueExpression valueExpression;

    /**
     * Compare value.
     */
    @Property(displayName = "Compare Value")
    private Object compareValue;

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
     * This is a monitor lock for rowProvider.
     */
    private String rowProviderLock = "rowProviderLock";

    /**
     * Storage for the compare locale.
     */
    private Locale compareLocale;

    /**
     * Create a new instance with no value expression.
     */
    public ValueBindingFilterCriteria() {
    }

    /**
     * Create a new instance with the specified value expression.
     * @param newValueExpression ValueExpression
     */
    public ValueBindingFilterCriteria(
            final ValueExpression newValueExpression) {
        this.valueExpression = newValueExpression;
    }

    /**
     * Create a new instance with the no value expression and the specified
     * compare value.
     * @param newCompareValue The desired compare value
     */
    public ValueBindingFilterCriteria(final Object newCompareValue) {
        this.compareValue = newCompareValue;
    }

    /**
     * Create a new instance with the specified expression and compare value.
     * @param newValueExpression ValueExpression
     * @param newCompareValue The desired compare value
     */
    public ValueBindingFilterCriteria(final ValueExpression newValueExpression,
            final Object newCompareValue) {

        this.valueExpression = newValueExpression;
        this.compareValue = newCompareValue;
    }

    /**
     * Create a new instance with the specified value expression, compare value
     * and matching rules.
     * @param newValueExpression ValueExpression
     * @param newCompareValue Object
     * @param newMatchLessThan boolean
     * @param newMatchEqualTo boolean
     * @param newMatchGreaterThan boolean
     */
    public ValueBindingFilterCriteria(final ValueExpression newValueExpression,
            final Object newCompareValue, final boolean newMatchLessThan,
            final boolean newMatchEqualTo, final boolean newMatchGreaterThan) {

        this.valueExpression = newValueExpression;
        this.compareValue = newCompareValue;
        this.matchLessThan = newMatchLessThan;
        this.matchEqualTo = newMatchEqualTo;
        this.matchGreaterThan = newMatchGreaterThan;
    }

    @Override
    public String getDisplayName() {
        String name = super.getDisplayName();
        if (name != null && !"".equals(name)) {
            return name;
        }

        // if there's no display name, make one...
        Object val = getCompareValue();
        StringBuilder sb = new StringBuilder();
        if (isInclude()) {
            sb.append("Include [");
        } else {
            sb.append("Exclude [");
        }
        if (valueExpression != null) {
            sb.append(valueExpression.getExpressionString());
        } else {
            sb.append("<no value binding>");
        }
        sb.append("] ");
        boolean anyMatches = false;
        if (matchLessThan) {
            anyMatches = true;
            sb.append("is less than ");
        }
        if (matchEqualTo) {
            if (anyMatches) {
                sb.append("OR ");
            }
            anyMatches = true;
            sb.append("is equal to ");
        }
        if (matchGreaterThan) {
            if (anyMatches) {
                sb.append("OR ");
            }
            sb.append("is greater than ");
        }
        sb.append("[").append(val).append("]");
        return sb.toString();
    }

    /**
     * Set the value expression.
     * @param newValueExpression ValueExpression
     */
    public void setValueExpression(final ValueExpression newValueExpression) {
        this.valueExpression = newValueExpression;
    }

    /**
     * Get the value expression.
     * @return ValueExpression
     */
    public ValueExpression getValueExpression() {
        return valueExpression;
    }

    /**
     * Returns the request map variable key that will be used to store the
     * {@link TableRowDataProvider} for the current row being match tested.
     * This allows value expressions to refer to the "current" row during the
     * filter operation.
     *
     * @return String key to use for the {@link TableRowDataProvider}
     */
    public String getRequestMapKey() {
        return requestMapKey;
    }

    /**
     * Sets the request map variable key that will be used to store the
     * {@link TableRowDataProvider} for the current row being match tested. This
     * allows value expressions to refer to the "current" row during the filter
     * operation.
     *
     * @param newRequestMapKey String key to use for the
     * {@link TableRowDataProvider}
     */
    public void setRequestMapKey(final String newRequestMapKey) {
        this.requestMapKey = newRequestMapKey;
    }

    /**
     * Set the compare value.
     * @param newCompareValue Object
     */
    public void setCompareValue(final Object newCompareValue) {
        this.compareValue = newCompareValue;
    }

    /**
     * Get the compare value.
     * @return Object
     */
    public Object getCompareValue() {
        return compareValue;
    }

    /**
     * Set the compare locale.
     * @param newCompareLocale Locale
     */
    public void setCompareLocale(final Locale newCompareLocale) {
        this.compareLocale = newCompareLocale;
    }

    /**
     * Get the compare locale.
     * @return Locale
     */
    public Locale getCompareLocale() {
        return compareLocale;
    }

    /**
     *
     * @param newMatchEqualTo boolean
     */
    public void setMatchEqualTo(final boolean newMatchEqualTo) {
        this.matchEqualTo = newMatchEqualTo;
    }

    /**
     *
     * @return boolean
     */
    public boolean isMatchEqualTo() {
        return matchEqualTo;
    }

    /**
     *
     * @param newMatchLessThan boolean
     */
    public void setMatchLessThan(final boolean newMatchLessThan) {
        this.matchLessThan = newMatchLessThan;
    }

    /**
     *
     * @return boolean
     */
    public boolean isMatchLessThan() {
        return matchLessThan;
    }

    /**
     *
     * @param newMatchGreaterThan boolean
     */
    public void setMatchGreaterThan(final boolean newMatchGreaterThan) {
        this.matchGreaterThan = newMatchGreaterThan;
    }

    /**
     *
     * @return boolean
     */
    public boolean isMatchGreaterThan() {
        return matchGreaterThan;
    }

    /**
     * <p>This method tests a match by comparing the <code>compareValue</code>
     * and the data value stored under the {@link ValueExpression}.  The passed
     * TableDataProvider and RowKey parameters are ignored.  The
     * <code>matchLessThan</code>,  <code>matchEqualTo</code>, and
     * <code>matchGreaterThan</code> properties  are used to determine if a
     * match was found.  The <code>compareLocale</code> is used for String
     * comparisons.</p>
     *
     * {@inheritDoc}
     */
    @Override
    public boolean match(final TableDataProvider provider, final RowKey row) {

        if (valueExpression == null) {
            return true;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, Object> requestMap = facesContext.getExternalContext()
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

        int compare = CompareFilterCriteria.compare(value, compareValue,
                compareLocale);
        switch (compare) {
            case -1: // Less Than
                return matchLessThan;
            case 0: // Equal To
                return matchEqualTo;
            case 1: // Greater Than
                return matchGreaterThan;
            default:
                return false;
        }
    }

    /**
     * Serialize this instance to the specified output stream.
     * @param out ouput stream
     * @throws IOException if an IO error occurs
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {

        // Serialize simple objects first
        out.writeObject(compareValue);
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
        if (valueExpression != null) {
            out.writeObject(valueExpression.getExpressionString());
        }
    }

    /**
     * De-serialize from the specified input stream.
     * @param in input stream
     * @throws IOException if an IO error occurs
     * @throws ClassNotFoundException if a class is not found during
     * de-serialization
     */
    private void readObject(final ObjectInputStream in)
            throws IOException, ClassNotFoundException {

        // Deserialize simple objects first
        compareValue = in.readObject();
        requestMapKey = (String) in.readObject();
        rowProviderLock = (String) in.readObject();

        // Deserialize valueExpression specially
        String s = (String) in.readObject();
        if (s != null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            valueExpression = facesContext.getApplication()
                    .getExpressionFactory()
                    .createValueExpression(facesContext.getELContext(), s,
                            Object.class);
        } else {
            valueExpression = null;
        }
    }
}
