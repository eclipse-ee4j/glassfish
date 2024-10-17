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
import java.util.Map;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * Perform's string substitution by constructing the {@link RadixTree} of change-value pair.
 *
 * @see RadixTreeSubstitution
 */
public class RadixTreeSubstitutionAlgo implements SubstitutionAlgorithm {

    private RadixTree _tree;
    private static final LocalStringsImpl _strings = new LocalStringsImpl(RadixTreeSubstitutionAlgo.class);

    /**
     * Construct {@link RadixTreeSubstitutionAlgo} for the given substitutable key/value pair by constructing the radix tree
     * for the same.
     *
     * @param substitutionMap Map of substitutable key/value pairs.
     */
    public RadixTreeSubstitutionAlgo(Map<String, String> substitutionMap) {
        if (substitutionMap == null || substitutionMap.isEmpty()) {
            throw new IllegalArgumentException(_strings.get("noKeyValuePairForSubstitution"));
        }
        _tree = new RadixTree();
        for (Map.Entry<String, String> entry : substitutionMap.entrySet()) {
            _tree.insert(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void substitute(Substitutable substitutable) throws StringSubstitutionException {
        Reader reader = substitutable.getReader();
        Writer writer = substitutable.getWriter();
        RadixTreeSubstitution sub = new RadixTreeSubstitution(_tree);
        String output = null;
        char[] cbuffer = new char[8192];
        int count = 0;
        try {
            while ((count = reader.read(cbuffer)) > 0) {
                for (int i = 0; i < count; i++) {
                    output = sub.substitute(cbuffer[i]);
                    if (output != null) {
                        writer.write(output);
                    }
                }
            }
            output = sub.substitute(null);
            if (output != null) {
                writer.write(output);
            }
            writer.flush();
        } catch (IOException e) {
            throw new StringSubstitutionException(_strings.get("errorInStringSubstitution", substitutable.getName()), e);
        }
    }
}
