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

package com.sun.jsftemplating.component.dataprovider;

import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import com.sun.data.provider.RowKey;
import com.sun.data.provider.TransactionalDataListener;
import com.sun.data.provider.impl.IndexRowKey;
import com.sun.data.provider.impl.ObjectFieldKeySupport;
import com.sun.data.provider.impl.ObjectListDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 * This implementation allows for multiple <code>List</code> objects to be used to represent table rows. If mulitiple
 * <code>List</code>s are used, they must be parallel lists (i.e. have the same # and order of information).
 * </p>
 */
public class MultipleListDataProvider extends ObjectListDataProvider {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Default Constructor.
     * </p>
     */
    public MultipleListDataProvider() {
        setIncludeFields(false);
    }

    /**
     * <p>
     * Constructor that creates initializes the DataProvider with a single <code>List</code>. Fields are <b>not</b> included
     * by default.
     * </p>
     *
     * @param list Primary list containing row information.
     */
    public MultipleListDataProvider(List<List<Object>> list) {
        this(list, false);
    }

    /**
     * <p>
     * Constructor that creates initializes the DataProvider with a single <code>List</code>. Fields are included if
     * <code>includeFields</code> is true.
     * </p>
     *
     * @param lists <code>List&lt;List&lt;Object&gt;&gt; to be
     *                wrapped.</code>
     * @param includeFields Desired include fields property setting
     */
    public MultipleListDataProvider(List<List<Object>> lists, boolean includeFields) {
        // Set the lists
        setLists(lists);

        // Do not include fields
        setIncludeFields(false);
    }

    /**
     * <p>
     * Constructor for an empty <code>DataProvider</code> with a known type. This constructor is only useful when there is a
     * single <code>List</code>. Fields are <b>not</b> included.
     * </p>
     *
     * @param objectTypes Desired object type Classes.
     */
    public MultipleListDataProvider(Class[] objectTypes) {
        this(objectTypes, false);
    }

    /**
     * <p>
     * Constructor for an empty <code>DataProvider</code> with a known type. This constructor is only useful when there is a
     * single <code>List</code>. Fields are included if <code>includeFields</code> is true.
     * </p>
     *
     * @param objTypes Desired object type of Classes
     * @param includeFields Desired include fields property setting
     */
    public MultipleListDataProvider(Class[] objTypes, boolean includeFields) {
        setObjectTypes(objTypes);
        setIncludeFields(includeFields);
    }

// FIXME: Provide apis to manage multiple lists:
// FIXME:   addList(String, List)
// FIXME:   removeList(String)??

    /**
     * <p>
     * This method returns an <b>array</b> of <code>Object</code> <code>(Object [])</code> for the given <code>row</code>.
     * Each array element cooresponds to an <code>Object</code> in one of the <code>List</code>s represented by this
     * instance of <code>MultipleListDataProvider</code>.
     * </p>
     *
     * @throws IndexOutOfBoundsException If <code>row</code> is invalid.
     */
    @Override
    public Object getObject(RowKey row) {
        if (!isRowAvailable(row)) {
            throw new IndexOutOfBoundsException("" + row);
        }
        int rowIdx = getRowIndex(row);
        List<List<Object>> lists = getLists();
        Object[] result = new Object[lists.size()];
        int idx = 0;
        for (List list : lists) {
            result[idx++] = list.get(rowIdx);
        }
        return result;
    }

    /**
     * <p>
     * This method returns an array of array of<code>Object</code>s <code>(Object [][])</code>. The first element of the
     * array is the row, the second cooresponds to the <code>List</code>: <code>Object[row #][list #]</code>. If there is
     * only one <code>List</code> represented by this instance of <code>MultipleListDataProvider</code>, then the list #
     * will be 0; if there are 2 <code>List</code>s, then the list # will be 0 or 1, etc.
     * </p>
     */
    @Override
    public Object[] getObjects() {
        // Get the array demensions
        List<List<Object>> lists = getLists();
        int numLists = lists.size();
        if (numLists == 0) {
            // This isn't likely, but just in case...
            return new Object[0][0];
        }
        int numRows = lists.get(0).size();
        Object[][] result = new Object[numRows][numLists];

        // Fill the array
        int listNum = 0;
        for (int rowNum = 0; rowNum < numRows; rowNum++) {
            listNum = 0;
            for (List list : lists) {
                result[rowNum][listNum++] = list.get(rowNum);
            }
        }

        // Return the result (Object[rows][lists])
        return result;
    }

    /**
     * <p>
     * This sets the <code>Object<code> type contained in the
     *        <code>List</code> that this <code>MulitpleObjectDataProvider</code> represents. This method should only be
     * used when there is one <code>List</code> held by this <code>MultipleListDataProvider</code>. In cases where you have
     * more than one list (and likely you do because you are using this <code>DataProvider</code>), you should use
     * {@link #setObjectTypes(Class [])}.
     * </p>
     */
    @Override
    public void setObjectType(Class objectType) {
        setObjectTypes(new Class[] { objectType });
    }

    /**
     * <p>
     * This method sets the Object types for each <code>List</code> that is represented by this instance of this class. By
     * doing this, you allow the FieldKeys to be generated. You only need to specify this information if you have no rows.
     * If there is some data, that data will be used to determine the type.
     * </p>
     *
     * @param objectTypes The <code>Class</code> types of the row data.
     */
    public void setObjectTypes(Class[] objectTypes) {
        _types = objectTypes;
    }

    /**
     * <p>
     * Not supported. This method does not appear to have great value.
     * </p>
     */
    @Override
    public void removeObject(Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Not yet supported.
     * </p>
     *
     * @param row The <code>RowKey</code> of the row to check.
     */
    @Override
    public boolean isRemoved(RowKey row) {
        return _deletes.contains(row);
    }

    /**
     * <p>
     * Replace the object at the specified row.
     * </p>
     *
     * @param row The desired row to set the contained object
     * @param object The new object to set at the specified row
     */
    @Override
    public void setObject(RowKey row, Object object) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support the setObject(RowKey, Object) method. "
                + "Instead use setObjects(RowKey, Object []).");
    }

    /**
     * <p>
     * This method allows the given <code>row</code> to be replaced with the given <code>objects</code>.
     * </p>
     *
     * @param row The row to replace.
     * @param objects The array of <code>Objects</code> to use.
     */
    public void setObjects(RowKey row, Object[] objects) {
        Object[] previous = (Object[]) getObject(row);
        int rowNum = getRowIndex(row);
        int cnt = 0;
        for (List list : getLists()) {
            list.set(rowNum, objects[cnt++]);
        }
        fireValueChanged(null, row, previous, objects);
        if (getCursorRow() == row) {
            fireValueChanged(null, previous, objects);
        }
    }

    /**
     * <p>
     * Not yet supported...
     * </p>
     */
    @Override
    public void addObject(Object object) {
// FIXME: Add api to add the Object*s* (see addObject(object), must have 1 for each List
        throw new UnsupportedOperationException();
    }

    // ---------------------------------------------------- DataProvider Methods

    /**
     *
     */
    @Override
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {
        return getSupport().getFieldKey(fieldId);
    }

    /**
     *
     */
    @Override
    public FieldKey[] getFieldKeys() throws DataProviderException {
        return getSupport().getFieldKeys();
    }

    /**
     *
     */
    @Override
    public Class getType(FieldKey fieldKey) throws DataProviderException {
        return getSupport().getType(fieldKey);
    }

    /**
     *
     */
    @Override
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {
        return getSupport().isReadOnly(fieldKey);
    }

// FIXME: The following method is very difficult to implement correctly b/c
// FIXME: the super class uses its own private "getSupport()" method which will
// FIXME: return the wrong "support" class.  However, the superclass is also
// FIXME: managing the "updates" so we must call it... Ask Creator group to
// FIXME: properly expose "support" via an interface and get/setSupport()
// FIXME: methods.  Or ask them to provide access to the updates/deletes/etc.
// FIXME: properties.
//
// FIXME: OK... it appears that they ObjectListDataProvider does not utilize the FieldKey methods in its superclass and instead maintains a private list via its support class.  This means I cannot do anything to make this method work with updates!!!
    /**
     *
     */
    @Override
    public Object getValue(FieldKey fieldKey, RowKey rowKey) throws DataProviderException {
        Object val = null;
        try {
            // This will try to get the value, but is unlikely to succeed...
            val = super.getValue(fieldKey, rowKey);
        } catch (Exception ex) {
            // Now check the "right" support class...
            if (getSupport().getFieldKey(fieldKey.getFieldId()) == null) {
                throw new IllegalArgumentException("" + fieldKey);
            }

            // Make sure it's a valid row key...
            if (!isRowAvailable(rowKey)) {
                throw new IndexOutOfBoundsException("" + rowKey);
            }
            int index = getRowIndex(rowKey);
            if (index < getRowCount()) {
                val = getSupport().getValue(fieldKey, getListForFieldKey(fieldKey).get(index));
            } else {
// FIXME: Need to manually implement appends, updates, and deletes!! :(
//        return getSupport().getValue(fieldKey, appends.get(index - getRowCount()));
            }
        }
        return val;
    }

    /**
     *
     */
    @Override
    public void setValue(FieldKey fieldKey, RowKey rowKey, Object value) throws DataProviderException {
        if (getSupport().getFieldKey(fieldKey.getFieldId()) == null) {
            throw new IllegalArgumentException("" + fieldKey);
        }
        if (getSupport().isReadOnly(fieldKey)) {
            throw new IllegalStateException("" + fieldKey);
        }
        if (!isRowAvailable(rowKey)) {
            throw new IndexOutOfBoundsException("" + rowKey);
        }

        // Retrieve the previous value and determine if it has changed
        Object previous = getValue(fieldKey, rowKey);
        if (previous == null && value == null || previous != null && value != null && previous.equals(value)) {
            return; // No change
        }

        // Verify type compatibility of the proposed new value
        if (!getSupport().isAssignable(fieldKey, value)) {
            throw new IllegalArgumentException(fieldKey + " = " + value); // NOI18N
        }

// FIXME: Need to explicitly support updates (can't get via polymorphism due to bad design)! :(
        /*
         * // Record a pending change for this row and field Map fieldUpdates = (Map) updates.get(rowKey); if (fieldUpdates ==
         * null) { fieldUpdates = new HashMap(); updates.put(rowKey, fieldUpdates); } fieldUpdates.put(fieldKey, value);
         */
        // Remove the following set line and defer until commit() once updates/deletes/etc. is worked out
        int index = getRowIndex(rowKey);
        getSupport().setValue(fieldKey, getListForFieldKey(fieldKey).get(index), value);

        fireValueChanged(fieldKey, rowKey, previous, value);
        fireValueChanged(fieldKey, previous, value);
    }

    /**
     * <p>
     * Construct new instances for each <code>List</code> represented by this class and appended them to each
     * <code>List</code>.
     */
    @Override
    public RowKey appendRow() throws DataProviderException {
// FIXME: Support this!
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * This method returns <code>true</code> if rows may be appended.
     * </p>
     */
    @Override
    public boolean canAppendRow() throws DataProviderException {
        return isUserResizable() && getObjectTypes() != null;
    }

    /**
     * <p>
     * This method is not supported.
     * </p>
     *
     * @param object Object to be appended.
     */
    @Override
    public RowKey appendRow(Object object) throws DataProviderException {
// FIXME: Support this!
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Remove the object at the specified row from the list.
     * </p>
     *
     * {@inheritDoc}
     */
    @Override
    public void removeRow(RowKey rowKey) throws DataProviderException {
        // Verify we can actually remove this row
        if (!canRemoveRow(rowKey)) {
// FIXME: I18N
            throw new IllegalStateException("This ObjectListDataProvider is not resizable.");
        }
        if (!isRowAvailable(rowKey)) {
// FIXME: I18N
            throw new IllegalArgumentException("Cannot delete row for row key " + rowKey);
        }

        // Record the fact that we are going to delete this row
        _deletes.add(rowKey);

        // Fire appropriate events regarding this deletion
        fireRowRemoved(rowKey);
        if (getCursorRow() == rowKey) {
            fireValueChanged(null, getObject(rowKey), null);
        }
    }

    /**
     * <p>
     * This method returns the # of rows represented by this <code>MultipleListDataProvider</code>.
     * </p>
     */
    @Override
    public int getRowCount() throws DataProviderException {
        List<List<Object>> lists = getLists();
        int count = 0;
        if (lists != null && lists.size() != 0) {
            count = lists.get(0).size();
        }
        return count;
    }

    /**
     * <p>
     * Return <code>true</code> if the specified <code>RowKey</code> represents a row in the original list, or a row that
     * has been appended.
     * </p>
     *
     * @param row <code>RowKey</code> to test for availability.
     */
    @Override
    public boolean isRowAvailable(RowKey row) throws DataProviderException {
        int idx = getRowIndex(row);
        if (idx < 0) {
            return false;
        }
        if (idx < getRowCount()) {
            return true;
        }
        return false;
    }

    // --------------------------------------- TransactionalDataProvider Methods
    /**
     * <p>
     * Cause any cached updates to existing field values, as well as inserted and deleted rows, to be flowed through to the
     * underlying <code>List</code>s wrapped by this <code>DataProvider</code>.
     * </p>
     */
    @Override
    public void commitChanges() throws DataProviderException {
        // FIXME: Do updates here... (see super())

        // Commit pending deletes
        // Iterate backwards so that we correctly modify List
        RowKey deletes[] = _deletes.toArray(new RowKey[_deletes.size()]);
        int rowIdx = -1;
        for (int idx = deletes.length - 1; idx >= 0; idx--) {
            rowIdx = getRowIndex(deletes[idx]);
            for (List list : getLists()) {
                list.remove(rowIdx);
            }
        }
        _deletes.clear();

        // FIXME: Deal w/ appends (see super())

        // Notify interested listeners that we have committed
        fireChangesCommitted();
    }

    /**
     * <p>
     * Fire a <code>changesCommitted</code> method to all registered listeners.
     * </p>
     */
    protected void fireChangesCommitted() {
// FIXME: This method overrides a method by the same name in the superclass that is private!  The super() should make this protected.
        TransactionalDataListener listeners[] = getTransactionalDataListeners();
        for (TransactionalDataListener listener : listeners) {
            listener.changesCommitted(this);
        }
    }

    /**
     * <p>
     * Fire a <code>changesReverted</code> method to all registered listeners.
     * </p>
     */
    private void fireChangesReverted() {
// FIXME: This method overrides a method by the same name in the superclass that is private!  The super() should make this protected.
        TransactionalDataListener listeners[] = getTransactionalDataListeners();
        for (TransactionalDataListener listener : listeners) {
            listener.changesReverted(this);
        }
    }

    /**
     *
     */
    @Override
    public void revertChanges() throws DataProviderException {
        // FIXME: Do updates here... (see super())
        // updates.clear();

        _deletes.clear();

        // FIXME: Deal w/ appends
        // appends.clear();

        // Notify interested listeners that we are reverting
        fireChangesReverted();
    }

    /**
     * <p>
     * Accessor for the <code>List</code> of <code>List</code>s.
     * </p>
     */
    public List<List<Object>> getLists() {
        return _lists;
    }

    /**
     * <p>
     * Setter for the <code>List</code> of <code>List</code>s.
     * </p>
     */
    public void setLists(List<List<Object>> lists) {
        _lists = lists;
    }

    /**
     * <p>
     * This method returns
     */
    public List getListForFieldKey(FieldKey key) {
        if (key != null && !(key instanceof IndexFieldKey)) {
            key = support.getFieldKey(key.getFieldId());
        }
        if (key == null) {
            throw new IllegalArgumentException("Invalid FieldKey: " + key);
        }
        return getLists().get(((IndexFieldKey) key).getIndex());
    }

    /**
     * <p>
     * The cached support object for field key manipulation. Must be transient because its content is not Serializable.
     * </p>
     */
    private transient MultipleObjectFieldKeySupport support = null;

    /**
     * <p>
     * Return the {@link ObjectFieldKeySupport} instance for the object class we are wrapping.
     * </p>
     */
    private MultipleObjectFieldKeySupport getSupport() {
        if (support == null) {
            // Try to get first element of the list to help find FieldKeys
            Object[] objs = (Object[]) getObject(getRowKey(0));
            if (objs != null && objs.length > 0) {
                support = new MultipleObjectFieldKeySupport(objs, isIncludeFields());
            }
//        else {
// FIXME: Add ability to use other meta information (i.e. _types and/or special class to describe info (i.e. for Maps)) to do this.
//        }
        }
        return support;
    }

    /**
     * <p>
     * This method converts the given <code>rowKey</code> to an <code>int</code>.
     * </p>
     */
    protected int getRowIndex(RowKey rowKey) {
// FIXME: This method overrides a private method by the same name... that method shouldn't be private!
        if (rowKey instanceof IndexRowKey) {
            return ((IndexRowKey) rowKey).getIndex();
        }
        return -1;
    }

    /**
     * <p>
     * This method converts the given <code>int</code> to a <code>RowKey</code>.
     * </p>
     */
    protected RowKey getRowKey(int index) {
        return new IndexRowKey(index);
    }

    ////////////////////////////////////////////////////////////////////////
    // Unsupported Methods
    ////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * This method is not supported.
     * </p>
     */
    @Override
    public Class getObjectType() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support the getObjectType() method because it "
                + "must return multiple types.  Please use \"Class [] " + "getObjectTypes()\" instead.");
    }

    /**
     * <p>
     * This method returns an array of <code>Class []</code> representing the <code>Object</code> types represented by this
     * instance of this class.
     * </p>
     */
    public Class[] getObjectTypes() {
        return _types;
    }

    /**
     * <p>
     * Storage for our List of Lists.
     * </p>
     */
    private List<List<Object>> _lists = new ArrayList<>();

    /**
     * <p>
     * Type information for the LIsts.
     * </p>
     */
    private Class[] _types = null;

    /**
     * <p>
     * Set of {@link RowKey}s marked to be deleted. An <code>Iterator</code> over this set will return the corresponding
     * {@link RowKey}s in ascending order.
     * </p>
     */
    protected Set<RowKey> _deletes = new TreeSet<>();
}
