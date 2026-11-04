/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.util;

import java.io.File;
import java.io.FileFilter;
import java.util.StringTokenizer;

/**
 * A utility class that checks if a file or folder should be displayed based on
 * the filter entered by the user.
 */
public final class FilterUtil implements FileFilter {

    /**
     * Filter string.
     */
    private String filterString = null;

    /**
     * Extension pattern.
     */
    private String extPattern = null;

    /**
     * Name pattern.
     */
    private String namePattern = null;

    /**
     * Create a new instance.
     */
    public FilterUtil() {
    }

    /**
     * Create a new instance.
     * @param filterStr filter string
     */
    public FilterUtil(final String filterStr) {
        if (filterStr != null) {
            this.filterString = filterStr;
            this.filterString = filterStr.trim();
            int index = filterString.indexOf('.');
            if (index == -1) {
                namePattern = filterString;
            } else if (index == 0) {
                if (filterString.length() > 1) {
                    extPattern = filterString.substring(1);
                }
            } else if (index == (filterString.length() - 1)) {
                namePattern = filterString.substring(0, index - 1);
            } else {
                namePattern = filterString.substring(0, index);
                extPattern = filterString.substring(index + 1);
            }
        }
    }

    @Override
    public boolean accept(final File f) {

        if (f.isDirectory()
                || filterString == null
                || filterString.length() == 0
                || filterString.equals("*")) {
            return true;
        }

        String fileName = f.getName();
        if (filterString.equals(fileName)) {
            return true;
        }

        // Now we should get both name and extension
        // and then match name against name pattern and
        // extension against ext pattern. Only if both
        // match return true.
        String extension = getExtension(fileName);
        String name = getName(fileName);

        // if pattern does not have a dot there is no need to
        // check for filename and the extension separately.
        if (filterString.indexOf('.') == -1) {
            return check(fileName, filterString);
        }

        // if filename does not have an extension and there
        // exists an extension pattern return false
        if (filterString.indexOf('.') != -1) {
            if (fileName.indexOf('.') == -1) {
                return false;
            }
        }

        // check for name and extension match separately and
        // return true if both are true
        boolean nameFlag = check(name, namePattern);
        boolean extFlag = check(extension, extPattern);
        return (nameFlag && extFlag);
    }

    /**
     * This function checks to see if the value matches a pattern. If the value
     * is null and the pattern is * this is also considered a match. The result
     * of the comparison is returned as a {@code boolean} value.
     * @param value value to check
     * @param pattern pattern to use
     * @return {@code boolean}
     */
    private static boolean check(final String value, final String pattern) {
        boolean flag = false;
        if ((value == null) && (pattern == null)) {
            flag = true;
        } else if ((value == null) && (pattern != null)) {
            if (pattern.equals("*")) {
                flag = true;
            }
        } else if ((value != null) && (pattern != null)) {
            if (pattern.equals("*")) {
                flag = true;
            } else if (pattern.equals(value)) {
                flag = true;
            } else {
                // match patterns of type *foo*bar*
                StringTokenizer st = new StringTokenizer(pattern, "*");
                if (st.countTokens() == 1) {
                    if (pattern.endsWith("*") && pattern.startsWith("*")) {
                        flag = checkPattern(st, value);
                    } else if (pattern.endsWith("*")) {
                        String prefix = pattern.substring(0,
                                pattern.length() - 1);
                        flag = value.startsWith(prefix);
                    } else if (pattern.startsWith("*")) {
                        String suffix = pattern.substring(1, pattern.length());
                        flag = value.endsWith(suffix);
                    }
                } else {
                    flag = checkPattern(st, value);
                }
            }
        }
        return flag;
    }

    /**
     * Get the extension of a file.
     * @param token tokenizer to use
     * @param name file name
     * @return {@code true} if the filename has an extension, {@code false}
     *  otherwise
     */
    private static boolean checkPattern(final StringTokenizer token,
            final String name) {

        int position = 0;
        boolean flag = true;
        while (token.hasMoreTokens()) {
            String subset = token.nextToken();
            int index = name.indexOf(subset, position);
            if (index == -1) {
                flag = false;
                break;
            } else {
                position = index + subset.length();
            }
        }
        return flag;
    }

    /**
     * Get the extension of a file.
     * @param name file name
     * @return String
     */
    private static String getExtension(final String name) {
        String ext = null;
        if (name != null) {
            int i = name.indexOf('.');
            if (i == -1) {
                return null;
            } else if (i == 0) {  // its a .file
                ext = name.substring(1);
            } else if (i < name.length() - 1) {
                // this change makes filter case sensitive
                // ext = name.substring(i+1).toLowerCase();
                ext = name.substring(i + 1);
            }
        }
        return ext;
    }

    /**
     * Get the name part of a file.
     * @param name file name
     * @return String
     */
    private static String getName(final String name) {
        if (name == null) {
            return null;
        }
        String fileName = null;
        int length = name.length();
        int i = name.indexOf('.');

        if (i == -1) {
            // entire thing is the file name
            fileName = name;
        } else if (i == 0) {
            // its a .file
            fileName = null;
        } else if (i <= length - 1) {
            fileName = name.substring(0, i);
        }
        return fileName;
    }
}
