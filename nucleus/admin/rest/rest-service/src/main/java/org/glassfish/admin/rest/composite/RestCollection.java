/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.composite;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.glassfish.admin.rest.composite.metadata.RestModelMetadata;

/**
 *
 * @author jdlee
 */
public class RestCollection<T> {
    private List<T> models = new ArrayList();
    private List<RestModelMetadata> metadata = new ArrayList<RestModelMetadata>();

    public void put(String id, T model) {
        models.add(model);
        metadata.add(new RestModelMetadata(id));
    }

    public T get(String id) {
        return get(new RestModelMetadata(id));
    }

    public T remove(String id) {
        return remove(new RestModelMetadata(id));
    }

    public boolean containsKey(String id) {
        return containsKey(new RestModelMetadata(id));
    }

    public int size() {
        return models.size();
    }

    public boolean isEmpty() {
        return models.isEmpty();
    }

    public boolean containsKey(Object key) {
        checkClass(RestModelMetadata.class, key.getClass());
        RestModelMetadata desired = (RestModelMetadata) key;
        boolean found = false;

        for (RestModelMetadata md : metadata) {
            if (md.equals(desired)) {
                found = true;
                break;
            }
        }

        return found;
    }

    public boolean containsValue(Object value) {
        checkClass(RestModel.class, value.getClass());
        RestModel desired = (RestModel) value;
        boolean found = false;

        for (T rm : models) {
            if (rm.equals(desired)) {
                found = true;
                break;
            }
        }

        return found;
    }

    public T get(Object key) {
        checkClass(RestModelMetadata.class, key.getClass());
        RestModelMetadata desired = (RestModelMetadata) key;
        T result = null;

        for (int index = 0, total = metadata.size(); index < total; index++) {
            if (metadata.get(index).equals(desired)) {
                result = models.get(index);
                break;
            }
        }

        return result;
    }

    public T put(RestModelMetadata key, T value) {
        models.add(value);
        metadata.add(key);

        return value;
    }

    public T remove(Object key) {
        checkClass(RestModelMetadata.class, key.getClass());
        RestModelMetadata desired = (RestModelMetadata) key;
        T result = null;

        for (int index = 0, total = metadata.size(); index < total; index++) {
            if (metadata.get(index).equals(desired)) {
                result = models.get(index);
                models.remove(index);
                metadata.remove(index);
                break;
            }
        }

        return result;
    }

    public void putAll(Map<? extends RestModelMetadata, ? extends T> m) {
        for (Map.Entry<? extends RestModelMetadata, ? extends T> entry : m.entrySet()) {
            metadata.add(entry.getKey());
            models.add(entry.getValue());
        }
    }

    public void clear() {
        models.clear();
        metadata.clear();
    }

    public Set<RestModelMetadata> keySet() {
        return new TreeSet<RestModelMetadata>(metadata);
    }

    public Collection<T> values() {
        return new RestModelSet(models);
    }

    public Set<Entry<RestModelMetadata, T>> entrySet() {
        if (metadata.size() != models.size()) {
            throw new IllegalStateException("InternalError: keys and values out of sync");
        }
        ArrayList al = new ArrayList();
        for (int i = 0; i < metadata.size(); i++) {
            al.add(new RestCollectionEntry(metadata.get(i), models.get(i)));
        }
        return new TreeSet<Entry<RestModelMetadata, T>>(al);
    }

    protected void checkClass(Class<?> desired, Class<?> given) throws IllegalArgumentException {
        if (!desired.isAssignableFrom(given)) {
            throw new IllegalArgumentException("Expected " + desired.getName() + ". Found " + given.getName());
        }
    }

    private class RestCollectionEntry<T> implements Map.Entry<RestModelMetadata, T>, Comparable {
        private RestModelMetadata key;
        private T value;

        public RestCollectionEntry(RestModelMetadata key, T value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public RestModelMetadata getKey() {
            return key;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public T setValue(T newValue) {
            value = newValue;
            return newValue;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof RestCollectionEntry)) {
                throw new IllegalArgumentException("Huh? Not a MapEntry?");
            }
            Object otherKey = ((RestCollectionEntry) o).getKey();
            return ((Comparable) key).compareTo((Comparable) otherKey);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RestCollectionEntry<T> other = (RestCollectionEntry<T>) obj;
            if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
                return false;
            }
            if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
                return false;
            }
            return true;
        }

    }

    private static class RestModelSet<T> extends AbstractSet<T> {
        private List<T> items;

        private RestModelSet(List<T> items) {
            this.items = items;
        }

        @Override
        public Iterator<T> iterator() {
            return items.iterator();
        }

        @Override
        public int size() {
            return items.size();
        }

        @Override
        public boolean containsAll(Collection<?> objects) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /*
        @Override
        public boolean contains(Object o) {
            for (T item : items) {
                if (CompositeUtil.compare(item, o)) {
                    return true;
                }
            }
            return false;
        }
        */
    }
}
