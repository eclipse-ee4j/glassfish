/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl;

import com.sun.enterprise.admin.servermgmt.stringsubs.AttributePreprocessor;

import java.util.Map;

/**
 * Implementation of {@link AttributePreprocessor}
 */
public class AttributePreprocessorImpl implements AttributePreprocessor {
    private Map<String, String> _lookUpMap = null;
    private static final String DELIMITER = "$";

    AttributePreprocessorImpl() {
    }

    public AttributePreprocessorImpl(Map<String, String> lookUpMap) {
        _lookUpMap = lookUpMap;
    }

    @Override
    public String substituteBefore(String beforeValue) {
        return beforeValue;
    }

    @Override
    public String substituteAfter(String afterValue) {
        return substitute(afterValue, DELIMITER, DELIMITER);
    }

    @Override
    public String substitutePath(String path) {
        return substitute(path, DELIMITER, DELIMITER);
    }

    private String substitute(final String var, final String startDelim, final String endDelim) {
        if (var == null || startDelim == null || endDelim == null) {
            return var;
        }
        int firstIndex = var.indexOf(startDelim);
        int secondIndex = var.indexOf(endDelim, firstIndex + startDelim.length());
        if (firstIndex == -1 || secondIndex == -1) {
            return var;
        }
        StringBuilder stringStart = new StringBuilder(var.substring(0, firstIndex));
        String sub = _lookUpMap.get(var.substring(firstIndex + startDelim.length(), secondIndex));
        String stringEnd = var.substring(secondIndex + endDelim.length(), var.length());
        if (sub != null) {
            stringStart.append(sub);
        }
        stringStart.append((stringEnd.indexOf(startDelim) == -1) ? stringEnd : substitute(stringEnd, startDelim, endDelim));
        return stringStart.toString();
    }
}
