/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.Collections.emptyList;

/**
 * Simple tracker objects to remember what operations were performed
 *
 * @author Jerome Dochez
 */
public abstract class ProgressTracker {

    Map<String, List<Object>> subjects = new HashMap<>();

    public synchronized <T> void add(String name, Class<T> type, T subject) {
        subjects.computeIfAbsent(name, e -> new ArrayList<Object>()).add(subject);
    }

    public <T> void addAll(Class<T> type, Iterable<T> subjects) {
        for (T subject : subjects) {
            add(type, subject);
        }
    }

    public <T> void add(Class<T> type, T subject) {
        add(type.getName(), type, subject);
    }

    public <T> List<T> get(Class<T> type) {
        return get(type.getName(), type);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> get(String name, Class<T> type) {
        if (!subjects.containsKey(name)) {
            return emptyList();
        }

        return (List<T>) subjects.get(name);
    }

    public abstract void actOn(Logger logger);
}
