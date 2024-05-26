/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.main.test.setup;

import java.io.File;

/**
 *
 * @author Ondro Mihalyi
 */
public interface ResourceAware {

    /**
     * Returns path relative to the current directory (project root) to a resource associated with the current test.
     * Tne resources reside in the {@code src/main/resources} directory, in the same package as the current test.
     * @param relativeResourcePath Name or path of the resource relative to the package of the current test in the {@code src/main/resources} directory
     * @return Path to the resource relative to the current (project root) directory
     */
    default File getTestResource(String relativeResourcePath) {
        return getTestResourceForClass(this.getClass().getPackageName(), relativeResourcePath);
    }

    /**
     * Same as {@link #getTestResource(java.lang.String)} but gets the test class from the current thread's stack and not from the {@code this} object. Suitable from within a static method of a test class.
     * @param relativeResourcePathName or path of the resource relative to the package of the current test in the {@code src/main/resources} directory
     * @return Path to the resource relative to the current (project root) directory
     */
    static File getTestResourceStatic(String relativeResourcePath) {
        // stacktrace[0] -> Thread.getStackTrace()
        // stacktrace[1] -> this method
        // stacktrace[2] -> caller
        final String currentClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        final String packageName = currentClassName.substring(0, currentClassName.lastIndexOf('.'));
        return getTestResourceForClass(packageName, relativeResourcePath);
    }

    static private File getTestResourceForClass(String packageName, String relativeResourcePath) {
        final String unixFilePath = "src/main/resources/" + packageName.replace('.', '/') + "/" + relativeResourcePath;
        return new File(unixFilePath.replace('/', File.separatorChar));
    }

}
