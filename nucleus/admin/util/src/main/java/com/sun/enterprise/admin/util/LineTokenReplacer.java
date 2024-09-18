/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 */
public final class LineTokenReplacer {

    private final TokenValue[] tokenArray;

    /**
     * Creates a new instance of TokenReplacer
     */
    public LineTokenReplacer(TokenValueSet tokens) {
        final Object[] tmp = tokens.toArray();
        final int length = tmp.length;
        this.tokenArray = new TokenValue[length];
        System.arraycopy(tmp, 0, tokenArray, 0, length);
    }

    /**
     * Get a Reader that substitutes the tokens in the content that it returns.
     *
     * @param in the content in which tokens are to be substituted
     * @return a Reader that returns the substituted content
     */
    public Reader getReader(final Reader in) {
        return new Reader() {

            BufferedReader reader = new BufferedReader(in);
            String line;
            final String eol = System.getProperty("line.separator");

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                if (line == null || line.isEmpty()) {
                    line = reader.readLine();
                    if (line == null) {
                        return -1;
                    }
                    line = replaceLine(line) + eol;
                }
                int copySize = len - off;
                if (copySize > line.length()) {
                    copySize = line.length();
                }
                line.getChars(0, copySize, cbuf, off);
                line = line.substring(copySize);
                return copySize;
            }

            @Override
            public void close() throws IOException {
                reader.close();
            }
        };

    }

    /**
     * Converts inputFile to outputFile with the following rules:
     * <ul>
     * <li>Both files are encoded with UTF-8
     * <li>Line endings are replaced by {@link System#lineSeparator()}
     * <li>Tokens from constructor are resolved
     * </ul>
     * @param inputFile
     * @param outputFile
     * @throws IllegalStateException if conversion fails for any reason
     */
    public void replace(File inputFile, File outputFile) throws IllegalStateException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile, UTF_8));
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8))) {
            String lineContents;
            while ((lineContents = reader.readLine()) != null) {
                String modifiedLine = replaceLine(lineContents);
                writer.write(modifiedLine);
                writer.newLine();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void replace(String inputFileName, String outputFileName) {
        this.replace(new File(inputFileName), new File(outputFileName));
    }

    private String replaceLine(String lineWithTokens) {
        String tokenFreeString = lineWithTokens;

        for (TokenValue aPair : tokenArray) {
            //System.out.println("To replace: " + aPair.delimitedToken);
            //System.out.println("Value replace: " + aPair.value);
            tokenFreeString = tokenFreeString.replace(aPair.delimitedToken, aPair.value);
        }
        return tokenFreeString;
    }
}
