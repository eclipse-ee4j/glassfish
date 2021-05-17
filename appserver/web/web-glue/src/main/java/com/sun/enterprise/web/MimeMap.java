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

package com.sun.enterprise.web;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class representing the parsed mime mapping file of a mime element.
 */
public class MimeMap implements Serializable {

    private static final String MIME_TYPE = "type=";
    private static final String MIME_EXTS = "exts=";

    private String id;
    private HashMap<String, String> mimeMappings;

    /**
     * Constructor.
     *
     * @param id The mime id of the mime element which this MimeMap represents
     */
    MimeMap(String id) {
        this.id = id;
    }

    /**
     * Gets the mime id of the mime element which this MimeMap represents.
     */
    String getID() {
        return this.id;
    }

    /**
     * Parses the mime mappings from the given file.
     *
     * @param file The mime file
     */
    void load(String file) throws IOException {

        BufferedReader in = new BufferedReader(new FileReader(file));

        try {
            while (true) {
                // Get next line
                String line = in.readLine();
                if (line == null)
                    return;

                int len = line.length();
                if (len > 0) {
                    // Ignore comments
                    char firstChar = line.charAt(0);
                    if ((firstChar != '#') && (firstChar != '!')) {

                        // Find start of key
                        int keyStart = 0;
                        while (keyStart < len
                                && Character.isWhitespace(line.charAt(keyStart))) {
                            keyStart++;
                        }

                        // Blank lines are ignored
                        if (keyStart == len) {
                            continue;
                        }

                        int keyEnd = keyStart;
                        while (keyEnd<len
                                && !Character.isWhitespace(line.charAt(keyEnd))) {
                            keyEnd++;
                        }

                        // Find start of value
                        int valueStart = keyEnd;
                        while (valueStart<len
                                && Character.isWhitespace(line.charAt(valueStart))) {
                            valueStart++;
                        }
                        if (valueStart == len) {
                            // Ignore this MIME mapping
                            continue;
                        }
                        int valueEnd = valueStart;
                        while (valueEnd<len
                                && !Character.isWhitespace(line.charAt(valueEnd))) {
                            valueEnd++;
                        }

                        String key = line.substring(keyStart, keyEnd);
                        String value = line.substring(valueStart, valueEnd);

                        addMappings(key, value);
                    }
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Returns an iterator over the mime extensions that were parsed
     *
     * @return Iterator over the mime extensions that were parsed, or null if
     * the mime file was empty
     */
    Iterator<String> getExtensions() {
        Iterator<String> ret = null;
        if (mimeMappings != null) {
            ret = mimeMappings.keySet().iterator();
        }
        return ret;
    }

    /**
     * Gets the mime type corresponding to the given extension
     *
     * @param extension The mime extension
     *
     * @return The mime type corresponding to the given extension, or null if
     * the given extension does not map to any mime type
     */
    String getType(String extension) {
        String ret = null;
        if (mimeMappings != null) {
            ret = mimeMappings.get(extension);
        }
        return ret;
    }

    private void addMappings(String type, String exts) {
        // Remove "type=" prefix
        int index = type.indexOf(MIME_TYPE);
        if (index == -1) {
            // ignore
            return;
        }
        type = type.substring(index + MIME_TYPE.length());

        // Remove "exts=" prefix
        index = exts.indexOf(MIME_EXTS);
        if (index == -1) {
            // ignore
            return;
        }

        if (mimeMappings == null) {
            mimeMappings = new HashMap<String, String>();
    }

        exts = exts.substring(index + MIME_EXTS.length());
        index = exts.indexOf(',');
        String ext = null;
        if (index != -1) {
            // e.g., exts=aif,aiff,aifc
            int fromIndex = 0;
            while (index != -1) {
                ext = exts.substring(fromIndex, index).trim();
                if (ext.length() > 0) {
                    mimeMappings.put(ext, type);
                }
                fromIndex = index+1;
                index = exts.indexOf(',', fromIndex);
            }
            ext = exts.substring(fromIndex);
        } else {
            // e.g., exts=gif
            ext = exts;
        }

        if (ext != null) {
            ext = ext.trim();
            if (ext.length() > 0) {
                mimeMappings.put(ext, type);
            }
        }
    }
}
