/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util.test;

import com.sun.enterprise.admin.util.LineTokenReplacer;
import com.sun.enterprise.admin.util.TokenValue;
import com.sun.enterprise.admin.util.TokenValueSet;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.StringTokenizer;

/**
 *
 * @author kedar
 */
public class TokenReplacementTester {

    /** Creates a new instance of TokenReplacementTester */
    private final LineTokenReplacer replacer;

    public TokenReplacementTester(String tokensFileName, String fromFile, String toFile) {
        final TokenValueSet tokens = getTokensFromFile(tokensFileName);
        replacer = new LineTokenReplacer(tokens);
        replacer.replace(fromFile, toFile);
    }

    private TokenValueSet getTokensFromFile(String fileName) {
        final TokenValueSet tokens = new TokenValueSet();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line = null;
            while ((line = reader.readLine()) != null) {
                final TokenValue tv = getTokenValue(line);
                tokens.add(tv);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                }
            }
        }
        return tokens;
    }

    private TokenValue getTokenValue(String line) {
        final String delim = "=";
        final StringTokenizer parser = new StringTokenizer(line, delim);
        final String[] output = new String[2];
        int i = 0;
        while (parser.hasMoreTokens()) {
            output[i++] = parser.nextToken();
        }
        final String DELIM = "%%%";
        TokenValue tv = new TokenValue(output[0], output[1], DELIM);
        return (tv);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int length = args.length;
        if (length < 2) {
            usage();
            System.exit(1);
        }
        final String tokensFile = args[0];
        final String fromFile = args[1];
        final String toFile = fromFile + ".out";
        new TokenReplacementTester(tokensFile, fromFile, toFile);
    }

    private static void usage() {
        System.out.println("java TokenReplacementTester <tokens-file> <template-file>");
    }
}
