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

package com.sun.enterprise.admin.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 */
public final class LineTokenReplacer {

    private final TokenValue[] tokenArray;
    private final String charsetName;

    public LineTokenReplacer(TokenValueSet tokens) {
        this(tokens, null);
    }

    /**
     * Creates a new instance of TokenReplacer
     */
    public LineTokenReplacer(TokenValueSet tokens, String charset) {
        final Object[] tmp = tokens.toArray();
        final int length = tmp.length;
        this.tokenArray = new TokenValue[length];
        System.arraycopy(tmp, 0, tokenArray, 0, length);
        this.charsetName = charset;
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
            String line = null;
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

    public void replace(File inputFile, File outputFile) {
        //Edge-cases
        BufferedReader reader = null;
        BufferedWriter writer = null;
        // @todo Java SE 7 - use try with resources
        try {
            reader = new BufferedReader(new FileReader(inputFile));
            try {
                if (charsetName != null) {
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    Charset charset = Charset.forName(charsetName);
                    writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset));
                } else {
                    writer = new BufferedWriter(new FileWriter(outputFile));
                }
                String lineContents;
                while ((lineContents = reader.readLine()) != null) {
                    String modifiedLine = replaceLine(lineContents);
                    writer.write(modifiedLine);
                    writer.newLine();
                }
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public void replace(String inputFileName, String outputFileName) {
        this.replace(new File(inputFileName), new File(outputFileName));
    }

    private String replaceLine(String lineWithTokens) {
        String tokenFreeString = lineWithTokens;

        for (int i = 0; i < tokenArray.length; i++) {
            TokenValue aPair = tokenArray[i];
            //System.out.println("To replace: " + aPair.delimitedToken);
            //System.out.println("Value replace: " + aPair.value);
            tokenFreeString = tokenFreeString.replace(aPair.delimitedToken, aPair.value);
        }
        return (tokenFreeString);
    }
}
