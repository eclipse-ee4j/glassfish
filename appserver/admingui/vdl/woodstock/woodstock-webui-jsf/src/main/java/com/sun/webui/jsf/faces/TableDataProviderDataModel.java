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

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TableDataProvider;

import jakarta.faces.model.DataModel;
import jakarta.faces.model.DataModelEvent;
import jakarta.faces.model.DataModelListener;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * {@code DataModel} implementation that wraps a specified
 * {@link TableDataProvider} with the standard JavaServer Faces API.
 * Note that setting the {@code rowIndex} property of this
 * {@code DataModel} does <strong>NOT</strong> cause the cursor of
 * the wrapped {@link TableDataProvider} to be repositioned.
 */
public final class TableDataProviderDataModel extends DataModel {

    /**
     * The set of {@link FieldKey}s for the currently wrapped
     * {@link TableDataProvider}.
     */
    private FieldKey[] fieldKeys = null;

    /**
     * The row index to which this {@code DataModel} is positioned.
     */
    private int rowIndex = -1;

    /**
     * The {@link TableDataProvider} that we are wrapping.
     */
    private TableDataProvider tdp = null;

    /**
     * Construct an uninitialized {@link TableDataProviderDataModel}.
     */
    public TableDataProviderDataModel() {
        this(null);
    }

    /**
     * Construct an {@link TableDataProviderDataModel} that wraps the
     * specified {@link TableDataProvider}.
     * @param newTdp new table data provider
     */
    public TableDataProviderDataModel(final TableDataProvider newTdp) {
        setTableDataProvider(newTdp);
    }

    /**
     * Return the {@link TableDataProvider} we are wrapping.
     * @return TableDataProvider
     */
    public TableDataProvider getTableDataProvider() {
        return this.tdp;
    }

    /**
     * Set the {@link TableDataProvider} we are wrapping.
     *
     * @param newTdp The {@link TableDataProvider} to be wrapped
     */
    public void setTableDataProvider(final TableDataProvider newTdp) {
        this.tdp = newTdp;
        if (newTdp == null) {
            this.fieldKeys = null;
            this.rowIndex = -1;
        } else {
            this.fieldKeys = newTdp.getFieldKeys();
        }
    }

    /**
     * Return {@code true} if the wrapped {@link TableDataProvider}
     * has an available row at the currently specified {@code rowIndex}.
     * @return @code true} if the wrapped {@link TableDataProvider}
     * has an available row, {@code false} otherwise
     */
    @Override
    public boolean isRowAvailable() {
        if (getTableDataProvider() == null) {
            return false;
        }
        return getTableDataProvider().isRowAvailable(getRowKey());
    }

    /**
     * Return the number of rows available in the wrapped
     * {@link TableDataProvider}, or {@code -1} if unknown.
     * @return count
     */
    @Override
    public int getRowCount() {
        if (getTableDataProvider() == null) {
            return -1;
        }
        return getTableDataProvider().getRowCount();
    }

    /**
     * Return a {@code Map} representing the data elements in the
     * current row, keyed by the canonical identifier for each element.
     * Any call to {@code get()} or {@code put()} operations on
     * this {@code Map} will be delegated to corresponding
     * {@code getValue()} and {@code setValue()} calls on the
     * wrapped {@link TableDataProvider}.  Operations that attempt to add,
     * delete, or replace keys will be rejected.
     * @return Object
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getRowData() {
        if (getTableDataProvider() == null) {
            return null;
        }
        if (!getTableDataProvider().isRowAvailable(getRowKey())) {
            throw new IllegalArgumentException("" + getRowIndex());
        }
        Map map = new TableDataProviderMap();
        for (FieldKey fieldKey : fieldKeys) {
            map.put(fieldKey.getFieldId(), tdp.getValue(fieldKey,
                    getRowKey()));
        }
        return map;
    }

    /**
     * Return the currently selected {@code rowIndex}, or -1 for
     * no current position.
     * @return int
     */
    @Override
    public int getRowIndex() {
        return this.rowIndex;
    }

    /**
     * Get the row key.
     * @return RowKey
     */
    public RowKey getRowKey() {
        int i = getRowIndex();
        RowKey[] rks = tdp.getRowKeys(i + 1, null);
        if (rks.length > i) {
            return rks[i];
        }
        return null;
    }

    /**
     * Set the currently selected {@code rowIndex}.  The cursor
     * position of the wrapped {@link TableDataProvider} is <strong>NOT</strong>
     * updated.
     *
     * @param newRowIndex The new selected row index, or -1 for no selection
     */
    @Override
    public void setRowIndex(final int newRowIndex) {
        if (newRowIndex < -1) {
            throw new IllegalArgumentException("" + newRowIndex);
        }
        int oldIndex = this.rowIndex;
        this.rowIndex = newRowIndex;
        if (getTableDataProvider() == null) {
            return;
        }
        DataModelListener[] listeners = getDataModelListeners();
        if ((oldIndex != newRowIndex) && (listeners != null)) {
            Object rowData = null;
            if (isRowAvailable()) {
                rowData = getRowData();
            }
            DataModelEvent event =
                    new DataModelEvent(this, newRowIndex, rowData);
            for (DataModelListener listener : listeners) {
                listener.rowSelected(event);
            }
        }
    }

    /**
     * Return the wrapped {@link TableDataProvider} instance, if any.
     * @return Object
     */
    @Override
    public Object getWrappedData() {
        return getTableDataProvider();
    }

    /**
     * Set the wrapped {@link TableDataProvider} instance (if any).
     *
     * @param data New {@link TableDataProvider} instance, or {@code null}
     *  to disassociate from any instance
     */
    @Override
    public void setWrappedData(final Object data) {
        setTableDataProvider((TableDataProvider) data);
    }

    /**
     * Private implementation of {@code Map} that delegates
     * {@code get()} and {@code put()} operations to
     * {@code getValue()} and {@code setValue()} calls on the
     * underlying {@link TableDataProvider}.
     */
    private final class TableDataProviderMap extends AbstractMap {

        /**
         * Row index.
         */
        private final int rowIndex;

        /**
         * Row key.
         */
        private final RowKey rowKey;

        /**
         * Field keys.
         */
        private final FieldKey[] fieldKeys =
                TableDataProviderDataModel.this.fieldKeys;

        /**
         * Table data provider.
         */
        private final TableDataProvider tdp =
                TableDataProviderDataModel.this.tdp;

        /**
         * Create a new instance.
         */
        TableDataProviderMap() {
            this.rowIndex = TableDataProviderDataModel.this.getRowIndex();
            this.rowKey = TableDataProviderDataModel.this.getRowKey();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(final Object value) {
            Iterator keys = keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                Object contained = get(key);
                if (value == null) {
                    if (contained == null) {
                        return true;
                    }
                } else {
                    if (value.equals(contained)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Set entrySet() {
            return new TableDataProviderEntries(this);
        }

        @Override
        public Object get(final Object key) {
            int columnIndex = index(key);
            if (columnIndex < 0) {
                return null;
            }
            return tdp.getValue(fieldKeys[columnIndex], rowKey);
        }

        @Override
        public Set keySet() {
            return new TableDataProviderKeys(this);
        }

        @Override
        public Object put(final Object key, final Object value) {
            int columnIndex = index(key);
            if (columnIndex < 0) {
                return null;
            }
            Object previous = tdp.getValue(fieldKeys[columnIndex], rowKey);
            tdp.setValue(fieldKeys[columnIndex], rowKey, value);
            return previous;
        }

        @Override
        public void putAll(final Map map) {
            Iterator keys = map.keySet().iterator();
            while (keys.hasNext()) {
                Object key = keys.next();
                put(key, map.get(key));
            }
        }

        @Override
        public Object remove(final Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection values() {
            return new TableDataProviderValues(this);
        }

        /**
         * Get the index for the given key.
         * @param key mapped key
         * @return int
         */
        private int index(final Object key) {
            int index = -1;
            for (int i = 0; i < fieldKeys.length; i++) {
                if (key.equals(fieldKeys[i].getFieldId())) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        /**
         * Get the real key.
         * @param key mapped key
         * @return Object
         */
        Object realKey(final Object key) {
            return super.get(key);
        }

        /**
         * Get the real keys.
         * @return Iterator
         */
        Iterator realKeys() {
            return super.keySet().iterator();
        }
    }

    /**
     * Private implementation of {@code Set} for implementing the
     * {@code entrySet()} behavior of {@code TableDataProviderMap}.
     */
    private final class TableDataProviderEntries extends AbstractSet {

        /**
         * Underlying map.
         */
        private TableDataProviderMap map = null;

        /**
         * Create a new instance.
         * @param newMap table data provider map
         */
        TableDataProviderEntries(final TableDataProviderMap newMap) {
            this.map = newMap;
        }

        @Override
        public boolean add(final Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(final Object o) {
            if (o == null) {
                throw new NullPointerException();
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            Object k = e.getKey();
            Object v = e.getValue();
            if (!map.containsKey(k)) {
                return false;
            }
            if (v == null) {
                return map.get(k) == null;
            } else {
                return v.equals(map.get(k));
            }
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Iterator iterator() {
            return new TableDataProviderIterator(map);
        }

        @Override
        public boolean remove(final Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return map.size();
        }
    }

    /**
     * Private implementation of {@code Iterator} for the
     * {@code Set} returned by {@code entrySet()}.
     */
    private final class TableDataProviderIterator implements Iterator {

        /**
         * Underlying map.
         */
        private TableDataProviderMap map = null;

        /**
         * Map keys.
         */
        private Iterator keys = null;

        /**
         * Create a new instance.
         * @param newMap table data provider map
         */
        TableDataProviderIterator(final TableDataProviderMap newMap) {
            this.map = newMap;
            this.keys = newMap.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return keys.hasNext();
        }

        @Override
        public Object next() {
            Object key = keys.next();
            return (new TableDataProviderEntry(map, key));
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Private implementation of {@code Map.Entry} that implements
     * the behavior for a single entry from the {@code Set} that is
     * returned by {@code entrySet()}.
     */
    private final class TableDataProviderEntry implements Map.Entry {

        /**
         * Value.
         */
        private TableDataProviderMap map = null;

        /**
         * Key.
         */
        private Object key = null;

        /**
         * Create a new instance.
         * @param newMap value
         * @param newKey key
         */
        TableDataProviderEntry(final TableDataProviderMap newMap,
                final Object newKey) {

            this.map = newMap;
            this.key = newKey;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            if (key == null) {
                if (e.getKey() != null) {
                    return false;
                }
            } else {
                if (!key.equals(e.getKey())) {
                    return false;
                }
            }
            Object v = map.get(key);
            if (v == null) {
                if (e.getValue() != null) {
                    return false;
                }
            } else {
                if (!v.equals(e.getValue())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Object getKey() {
            return this.key;
        }

        @Override
        public Object getValue() {
            return map.get(key);
        }

        @Override
        public int hashCode() {
            Object value = map.get(key);
            if (key == null) {
                return 0;
            }
            if (value == null) {
                return key.hashCode();
            }
            return key.hashCode() ^ value.hashCode();
        }

        @Override
        public Object setValue(final Object value) {
            Object previous = map.get(key);
            map.put(key, value);
            return previous;
        }
    }

    /**
     * Private implementation of {@code Set} that implements the
     * {@code keySet()} behavior.
     */
    private final class TableDataProviderKeys extends AbstractSet {

        /**
         * Underlying map.
         */
        private TableDataProviderMap map = null;

        /**
         * Create a new instance.
         * @param newMap table data provider map
         */
        TableDataProviderKeys(final TableDataProviderMap newMap) {
            this.map = newMap;
        }

        @Override
        public boolean add(final Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(final Object o) {
            return map.containsKey(o);
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Iterator iterator() {
            return new TableDataProviderKeysIterator(map);
        }

        @Override
        public boolean remove(final Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return map.size();
        }
    }

    /**
     * Private implementation of {@code Iterator} that implements the
     * {@code iterator()} method returned by {@code keySet()}.
     */
    private final class TableDataProviderKeysIterator implements Iterator {

        /**
         * Underlying map.
         */
        private TableDataProviderMap map = null;

        /**
         * Keys.
         */
        private Iterator keys = null;

        /**
         * Create a new instance.
         * @param newMap table data provider map
         */
        TableDataProviderKeysIterator(
                final TableDataProviderMap newMap) {

            this.map = newMap;
            this.keys = newMap.realKeys();
        }

        @Override
        public boolean hasNext() {
            return keys.hasNext();
        }

        @Override
        public Object next() {
            return keys.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Private implementation of {@code Collection} that implements
     * the {@code values()} method.
     */
    private final class TableDataProviderValues extends AbstractCollection {

        /**
         * Underlying map.
         */
        private TableDataProviderMap map = null;

        /**
         * Create a new instance.
         * @param newMap table data provider map
         */
        TableDataProviderValues(final TableDataProviderMap newMap) {
            this.map = newMap;
        }

        @Override
        public boolean add(final Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(final Object value) {
            return map.containsValue(value);
        }

        @Override
        public Iterator iterator() {
            return new TableDataProviderValuesIterator(map);
        }

        @Override
        public boolean remove(final Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(final Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return map.size();
        }
    }

    /**
     * Private implementation of {@code Iterator} that implements the
     * behavior for the {@code Iterator} returned by
     * {@code values().iterator()}.
     */
    private final class TableDataProviderValuesIterator implements Iterator {

        /**
         * Underlying map.
         */
        private TableDataProviderMap map = null;

        /**
         * Keys.
         */
        private Iterator keys = null;

        /**
         * Create a new instance.
         * @param newMap table data provider map
         */
        TableDataProviderValuesIterator(
                final TableDataProviderMap newMap) {

            this.map = newMap;
            this.keys = newMap.keySet().iterator();
        }
        @Override
        public boolean hasNext() {
            return keys.hasNext();
        }

        @Override
        public Object next() {
            return map.get(keys.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
