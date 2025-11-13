/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import java.io.File;
import java.io.FilenameFilter;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * Class to retrieve all the matching files for a given input path. It also handles the processing of wild-card in the
 * given path.
 */
final class FileLister {
    private static final Logger LOG = System.getLogger(FileLister.class.getName());

    private static final LocalStringsImpl _strings = new LocalStringsImpl(FileLister.class);

    final static String ASTERISK = "*";

    /**
     * Recursively find all files represented by path with wild-card character
     *
     * @param pathPattern path with wild-card character ASTERISK
     * @return List<File> all files whose paths match the pattern
     */
    public List<File> getFiles(String pathPattern) {
        int asteriskIndex = pathPattern.indexOf(ASTERISK);
        // if input does not contain a wild-card character, return child files directly
        if (asteriskIndex < 0) {
            return getAllChildFiles(new File(pathPattern));
        }

        List<File> retrievedFiles = new LinkedList<File>();

        // try twice to handle '/' in windows
        // if the first try of parsing pathWithPattern fails, then replace all '/'
        // with '\' and try again
        int numTries = 0;
        while (numTries < 2) {
            // path substring before the wild-card character
            String head = pathPattern.substring(0, asteriskIndex);
            // path substring after the wild-card character
            String tail = (asteriskIndex < pathPattern.length() - 1 ? pathPattern.substring(asteriskIndex + 1) : "");
            // get parent file of the head, add "temp" to handle input like /path/to/parent/*
            File parent = (new File(head + "temp")).getParentFile();
            if (parent == null) {
                if (LOG.isLoggable(Level.DEBUG)) {
                    LOG.log(Level.DEBUG, _strings.get("parentFileNotSpecified"));
                }
                parent = (new File(head + "temp").getAbsoluteFile()).getParentFile();
            }

            String pattern = pathPattern;
            // index of separator after the wild-card character
            int nextSeparator = -1;
            if (tail.length() > 0) {
                nextSeparator = pathPattern.indexOf(File.separator, asteriskIndex + 1);
            }
            // for input like /temp/bar*/var, create a filter with /temp/bar*
            if (nextSeparator > asteriskIndex) {
                pattern = pathPattern.substring(0, nextSeparator);
            }
            WildCardFilenameFilter filter = new WildCardFilenameFilter(pattern);

            // get a filtered list of children
            String childFileNames[] = parent.list(filter);
            if (childFileNames != null) {
                for (String childName : childFileNames) {
                    String path = parent.getAbsolutePath() + File.separator + childName;
                    File file = new File(path);
                    // input ends with wild-card, e.g. /temp/*
                    if (nextSeparator < asteriskIndex) {
                        if (file.isFile()) { // case of /path/to/childfile
                            retrievedFiles.add(file);
                        }
                        //TODO : Currently wild card character search do not allow to look for the
                        // matching files in sub-directories for e.g
                        // |-Directory
                        // | |-testFile1.txt
                        // | |-testDirectory
                        // | | |-testFile2.txt
                        // wild card search pattern 'Directory/test*' will retrieve only one file
                        // testFile1.txt to retrieve other file uncomment the below code.
                        /*else
                        {
                        retrievedFiles.addAll(getAllChildFiles(file));
                        }*/
                    } else { // input does not end with wild-card, e.g. /temp/*/bar
                        if (file.isDirectory()) { // file has to be a directory here
                            if (nextSeparator == pathPattern.length() - 1) { // case of /path/to/child/
                                retrievedFiles.addAll(getAllChildFiles(file));
                            } else { // case of /path/to/child/blah
                                String newpattern = path + File.separator + pathPattern.substring(nextSeparator + 1);
                                // recursively handle /path/to/child/blah
                                retrievedFiles.addAll(getFiles(newpattern));
                            }
                        }
                        // do nothing if child is not a directory, which is impossible
                    }
                }
            }

            // if retval is empty, and if the system's default file separator is '\' (windows),
            // then replace all '/' with '\' and try parsing the input again
            if (!retrievedFiles.isEmpty()) {
                break;
            } else if (File.separator.equals("\\") && pathPattern.contains("/")) {
                pathPattern = pathPattern.replace("/", File.separator);
                LOG.log(Level.DEBUG, "detected \"/\" in pathWithPattern on Windows, replace with \"\\\"");
                numTries++;
            } else {
                break;
            }
        }
        return retrievedFiles;
    }

    /**
     * Gets the list of child files. If the given file is a directory then all the files under directory and sub-directories
     * will be retrieved recursively.
     *
     * @param rootfile
     * @return List<File>
     */
    public List<File> getAllChildFiles(File rootfile) {
        List<File> retFiles = new LinkedList<File>();
        if (!rootfile.exists()) {
            // No operation, return empty list
            LOG.log(Level.INFO, "Could not locate file or resource {0}", rootfile.getAbsolutePath());
        } else if (!rootfile.isDirectory()) {
            retFiles.add(rootfile);
        } else {
            File files[] = rootfile.listFiles();
            if (files != null) {
                for (File file : files) {
                    retFiles.addAll(getAllChildFiles(file));
                }
            }
        }
        return retFiles;
    }

    /**
     * Custom filename filter to deal with wild-card character
     */
    private static class WildCardFilenameFilter implements FilenameFilter {
        private String _pattern;
        private boolean _endsWithWc;

        public WildCardFilenameFilter(String pattern) {
            _pattern = pattern;
            _endsWithWc = _pattern.endsWith(ASTERISK);
        }

        @Override
        public boolean accept(File dir, String name) {
            String fullpath = dir + File.separator + name;
            StringTokenizer tokenizedPattern = new StringTokenizer(_pattern, ASTERISK);
            while (tokenizedPattern.hasMoreTokens()) {
                String subpattern = tokenizedPattern.nextToken();
                int start = fullpath.indexOf(subpattern);
                if (start < 0) {
                    return false;
                }
                fullpath = fullpath.substring(start + subpattern.length());
            }
            return fullpath.length() == 0 || _endsWithWc;
        }
    }
}
