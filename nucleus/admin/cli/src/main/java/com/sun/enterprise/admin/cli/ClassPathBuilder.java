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

package com.sun.enterprise.admin.cli;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Builds up a classpath.
 *
 * @author Kohsuke Kawaguchi
 */
public class ClassPathBuilder implements Iterable<File> {
    private final List<File> elements = new ArrayList<File>();

    public Iterator<File> iterator() {
        return elements.iterator();
    }

    /**
     * Adds a single jar file or a class file directory.
     */
    public ClassPathBuilder add(File f) {
        elements.add(f);
        return this;
    }

    /**
     * Allows one to write {@code add(f,"lib","a.jar")} instead of <tt>add(new File(new File(f,"lib"),"a.jar")</tt>
     */
    public ClassPathBuilder add(File f, String... pathFragments) {
        for (String p : pathFragments)
            f = new File(f, p);
        return add(f);
    }

    /**
     * Adds all the files in the given directory that match the given filter.
     */
    public ClassPathBuilder addAll(File dir, FileFilter filter) {
        File[] files = dir.listFiles(filter);
        if (files != null)
            addAll(files);
        return this;
    }

    public ClassPathBuilder addAll(File... files) {
        for (File f : files)
            add(f);
        return this;
    }

    /**
     * Formats the path in a single-argument format suitable after the "-cp" JVM option.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (File f : elements) {
            if (buf.length() > 0)
                buf.append(File.pathSeparatorChar);
            // this method is normally used to create an argument for another process,
            // so better resolve relative path to absolute path.
            buf.append(f.getAbsolutePath());
        }
        return buf.toString();
    }
}
