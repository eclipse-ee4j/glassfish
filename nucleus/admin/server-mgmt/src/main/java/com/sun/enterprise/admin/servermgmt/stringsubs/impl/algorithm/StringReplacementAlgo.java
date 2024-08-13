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

package com.sun.enterprise.admin.servermgmt.stringsubs.impl.algorithm;

import com.sun.enterprise.admin.servermgmt.stringsubs.StringSubstitutionException;
import com.sun.enterprise.admin.servermgmt.stringsubs.Substitutable;
import com.sun.enterprise.admin.servermgmt.stringsubs.SubstitutionAlgorithm;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * Perform's String substitution by replacing the matching target sequence with the specified literal replacement
 * sequence.
 */
public class StringReplacementAlgo implements SubstitutionAlgorithm {

    private Map<String, String> _substitutionMap;

    /**
     * Construct {@link StringReplacementAlgo} for the given substitutable key/value pair.
     *
     * @param substitutionMap Map of substitutable key/value pairs.
     */
    public StringReplacementAlgo(Map<String, String> substitutionMap) {
        if (substitutionMap == null || substitutionMap.isEmpty()) {
            throw new IllegalArgumentException("Can not construct algorithm for null or empty map.");
        }
        _substitutionMap = substitutionMap;
    }

    @Override
    public void substitute(Substitutable resolver) throws StringSubstitutionException {
        Reader reader = resolver.getReader();
        Writer writer = resolver.getWriter();
        try {
            String inputLine = null;
            char[] cbuffer = new char[8192];
            int count = 0;
            while ((count = reader.read(cbuffer)) > 0) {
                inputLine = new String(cbuffer, 0, count);
                Iterator<Map.Entry<String, String>> entryIterator = _substitutionMap.entrySet().iterator();
                while (entryIterator.hasNext()) {
                    Map.Entry<String, String> entry = entryIterator.next();
                    inputLine = inputLine.replace(entry.getKey(), entry.getValue());
                }
                writer.write(inputLine);
            }
            writer.flush();
        } catch (IOException e) {
            throw new StringSubstitutionException("Error occurred while performing the String substitution", e);
        }
    }
}
