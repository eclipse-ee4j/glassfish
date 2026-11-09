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

import java.util.Locale;
import java.util.Map;

/**
 * <p>The ValueExpressionFilterCriteria is an implementation of
 * {@link FilterCriteria} that compares the value of a {@link String}
 * (created with the specified value expression) with a predefined
 * <code>compareValue</code>.  A user may specify matches to include less than
 * (&lt;), equal to (==), or greater than (>) the <code>compareValue</code> Object,
 * or any combination of the above.</p>
 *
 * @see TableDataProvider
 */
@Component(isTag = false)
public final class ValueExpressionFilterCriteria extends FilterCriteria {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 8984072154367845774L;

    /**
     * Storage for the compare locale.
     */
    private Locale compareLocale;

    /**
     * Is the value less than.
     */
    private boolean matchLessThan;

    /**
     * Is the value greater than.
     */
    private boolean matchGreaterThan;

    /**
     * Is the value equal to.
     */
    private boolean matchEqualTo = true;

    /**
     * Value expression.
     */
    @Property(displayName = "Value Expression")
    private String valueExpression;

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
    private final String rowProviderLock = "rowProviderLock";

    /**
     * Create a new instance with no value expression.
     */
    public ValueExpressionFilterCriteria() {
    }

    /**
     * Create a new instance with the specified value expression.
     * @param newValueExpression String
     */
    public ValueExpressionFilterCriteria(final String newValueExpression) {
        this.valueExpression = newValueExpression;
    }

    /**
     * Create a new instance with no value expression and the given compare
     * value.
     * @param newCompareValue The desired compare value
     */
    public ValueExpressionFilterCriteria(final Object newCompareValue) {
        this.compareValue = newCompareValue;
    }

    /**
     * Create a new instance with the specified value expression and compare
     * value.
     * @param newValueExpression String
     * @param newCompareValue The desired compare value
     */
    public ValueExpressionFilterCriteria(final String newValueExpression,
            final Object newCompareValue) {

        this.valueExpression = newValueExpression;
        this.compareValue = newCompareValue;
    }

    /**
     * Create a new instance with the specified value expression and compare
     * value and matching rules.
     * @param newValueExpression String
     * @param newCompareValue Object
     * @param newMatchLessThan boolean
     * @param newMatchEqualTo boolean
     * @param newMatchGreaterThan boolean
     */
    public ValueExpressionFilterCriteria(final String newValueExpression,
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
            sb.append(valueExpression);
        } else {
            sb.append("<no value expression>");
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
        sb.append("[")
                .append(val)
                .append("]");
        return sb.toString();
    }

    /**
     * Set the value expression.
     * @param newValueExpression String
     */
    public void setValueExpression(final String newValueExpression) {
        this.valueExpression = newValueExpression;
    }

    /**
     * Get the value expression.
     * @return String
     */
    public String getValueExpression() {
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
     * Set the match equal to flag value.
     * @param newMatchEqualTo boolean
     */
    public void setMatchEqualTo(final boolean newMatchEqualTo) {
        this.matchEqualTo = newMatchEqualTo;
    }

    /**
     * Test if is match equal to is {@code true}.
     * @return boolean
     */
    public boolean isMatchEqualTo() {
        return matchEqualTo;
    }

    /**
     * Set the match less than flag value.
     * @param newMatchLessThan boolean
     */
    public void setMatchLessThan(final boolean newMatchLessThan) {
        this.matchLessThan = newMatchLessThan;
    }

    /**
     * Test the match less than flag.
     * @return boolean
     */
    public boolean isMatchLessThan() {
        return matchLessThan;
    }

    /**
     * Set the match greater than flag value.
     * @param newMatchGreaterThan boolean
     */
    public void setMatchGreaterThan(final boolean newMatchGreaterThan) {
        this.matchGreaterThan = newMatchGreaterThan;
    }

    /**
     * Test the match greater than value.
     * @return boolean
     */
    public boolean isMatchGreaterThan() {
        return matchGreaterThan;
    }

    /**
     * <p>This method tests a match by comparing the <code>compareValue</code>
     * and the data value stored under the {@link String}.  The passed
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
        if (valueExpression == null || "".equals(valueExpression)) {
            return true;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueExpression valueBinding = facesContext
                .getApplication()
                .getExpressionFactory()
                .createValueExpression(
                facesContext.getELContext(), valueExpression, Object.class);

        if (valueBinding == null) {
            return true;
        }

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

            value = valueBinding.getValue(facesContext.getELContext());

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
}
