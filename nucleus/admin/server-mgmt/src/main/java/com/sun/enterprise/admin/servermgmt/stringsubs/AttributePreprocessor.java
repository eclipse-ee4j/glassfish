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

package com.sun.enterprise.admin.servermgmt.stringsubs;

import com.sun.enterprise.admin.servermgmt.xml.stringsubs.ChangePair;
import com.sun.enterprise.admin.servermgmt.xml.stringsubs.FileEntry;

/**
 * An object to pre-process the input string. This input string can either b
 */
public interface AttributePreprocessor {

    /**
     * Process the before value of the change-pair element and retrieve its value.
     * <p>
     * Note: A change-pair element is a macro definition that specifies the string to be substituted ("before") and the
     * replacement ("after") value. <br/>
     * E.g. &lt;change-pair id="pair1" before="@JAVA_HOME" after="$JAVA_HOME$"/&gt;
     * </p>
     *
     * @param beforeValue The before value of change-pair.
     * @return Substituted String.
     * @see ChangePair#getBefore()
     */
    String substituteBefore(String beforeValue);

    /**
     * Process the after value of the change-pair element and retrieve its value.
     * <p>
     * Note: A change-pair element is a macro definition that specifies the string to be substituted ("before") and the
     * replacement ("after") value. <br/>
     * E.g. &lt;change-pair id="pair1" before="@JAVA_HOME" after="$JAVA_HOME$"/&gt;
     * </p>
     *
     * @param afterValue The after value of change-pair.
     * @return Substituted String.
     * @see ChangePair#getAfter()
     */
    String substituteAfter(String afterValue);

    /**
     * Process the file name/member entry path. The name value of file-entry can contain the substitutable variable for e.g.
     * <p>
     * &lt;file-entry name="$DOMAIN_DIRECTORY$/start.cmd"/&gt;
     * </p>
     * Path pointing to the domain directory. The value of these variable will be retrieved.
     * <p>
     * Note: A file-entry defines a text file or set of files where substitution has to be performed.<br/>
     * </p>
     *
     * @param path The file path.
     * @return Substituted String.
     * @see FileEntry#getName()
     */
    String substitutePath(String path);
}
