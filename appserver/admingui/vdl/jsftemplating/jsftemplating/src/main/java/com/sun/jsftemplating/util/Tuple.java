/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jason CTR Lee
 */
public class Tuple {
    private List<Object> elements = new ArrayList<>();

    public Tuple(Object... elements) {
        for (Object element : elements) {
            this.elements.add(element);
        }
    }

    public Object getElement(int index) {
        return elements.get(index);
    }

    public List<Object> getElements() {
        return Collections.unmodifiableList(elements);
    }
}
