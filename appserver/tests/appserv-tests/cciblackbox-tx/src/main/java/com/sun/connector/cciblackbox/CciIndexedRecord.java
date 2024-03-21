/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.cciblackbox;

/**
 * This implementation class represents an ordered collection of record elements
 *
 * @author Sheetal Vartak
 */

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

public class CciIndexedRecord implements jakarta.resource.cci.IndexedRecord {

  private String recordName;

  private String description;

  private Vector indexedRecord;

  public CciIndexedRecord() {
    indexedRecord = new Vector();
  }

  public CciIndexedRecord(String name) {
    indexedRecord = new Vector();
    recordName = name;
  }

  public String getRecordName() {
    return recordName;
  }

  public void setRecordName(String name) {
    recordName = name;
  }

  public String getRecordShortDescription() {
    return description;
  }

  public void setRecordShortDescription(String description) {
    this.description = description;
  }

  public boolean equals(Object other) {
    return this.equals(other);
  }

  public int hashCode() {
    String result = "" + recordName;
    return result.hashCode();
  }

  public Object clone() throws CloneNotSupportedException {
    return this.clone();
  }

  //java.util.List methods

  public void add(int index, Object element) {
    indexedRecord.add(index, element);
  }

  public boolean add(Object o) {
    return indexedRecord.add(o);
  }

  public boolean addAll(Collection c) {
    return indexedRecord.addAll(c);
  }

  public boolean addAll(int index, Collection c) {
    return indexedRecord.addAll(index, c);
  }

  public void addElement(Object o) {
    indexedRecord.addElement(o);
  }

  public int capacity() {
    return indexedRecord.capacity();
  }

  public void clear() {
    indexedRecord.clear();
  }

  public boolean contains(Object elem) {
    return indexedRecord.contains(elem);
  }

  public boolean containsAll(Collection c) {
    return indexedRecord.containsAll(c);
  }

  public Object get(int index) {
    return (Object) indexedRecord.get(index);
  }

  public int indexOf(Object elem) {
    return indexedRecord.indexOf(elem);
  }

  public int indexOf(Object elem, int index) {
    return indexedRecord.indexOf(elem, index);
  }

  public boolean isEmpty() {
    return indexedRecord.isEmpty();
  }

  public Iterator iterator() {
    return indexedRecord.iterator();
  }

  public ListIterator listIterator() {
    return indexedRecord.listIterator();
  }

  public ListIterator listIterator(int index) {
    return indexedRecord.listIterator(index);
  }

  public Object lastElement() {
    return indexedRecord.lastElement();
  }

  public int lastIndexOf(Object elem) {
    return indexedRecord.lastIndexOf(elem);
  }

  public int lastIndexOf(Object elem, int index) {
    return indexedRecord.lastIndexOf(elem, index);
  }

  public Object remove(int index) {
    return indexedRecord.remove(index);
  }

  public boolean remove(Object o) {
    return indexedRecord.remove(o);
  }

  public boolean removeAll(Collection c) {
    return indexedRecord.remove(c);
  }

  public boolean retainAll(Collection c) {
    return indexedRecord.retainAll(c);
  }

  public Object set(int index, Object element) {
    return indexedRecord.set(index, element);
  }

  public int size() {
    return indexedRecord.size();
  }

  public List subList(int fromIndex, int toIndex) {
    return indexedRecord.subList(fromIndex, toIndex);
  }

  public Object[] toArray() {
    return indexedRecord.toArray();
  }

  public Object[] toArray(Object[] a) {
    return indexedRecord.toArray(a);
  }

  public String toString() {
    return indexedRecord.toString();
  }

  public void trimToSize() {
    indexedRecord.trimToSize();
  }

}
