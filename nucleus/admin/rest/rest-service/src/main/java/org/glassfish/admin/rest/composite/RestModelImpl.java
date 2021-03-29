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

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * All RestModel implementations will extend this base class, which will provide the functionality for dirty field field
 * checking. It is an implementation detail, and should not be accessed directly.
 *
 * @author jdlee
 */
public class RestModelImpl {
    private boolean allFieldsSet = false;

    private Set<String> setFields = new TreeSet<String>();

    public boolean isSet(String fieldName) {
        return allFieldsSet || setFields.contains(fieldName.toLowerCase(Locale.US));
    }

    public void allFieldsSet() {
        this.allFieldsSet = true;
    }

    public void fieldSet(String fieldName) {
        setFields.add(fieldName.toLowerCase(Locale.US));
    }

    // TBD - remove once the conversion to the new REST style guide is completed
    private boolean trimmed = false;

    public void trimmed() {
        this.trimmed = true;
    }

    public boolean isTrimmed() {
        return trimmed;
    }
}
