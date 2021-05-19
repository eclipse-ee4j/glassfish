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

/*
 * JavaFileWriter.java
 *
 * Created on November 14, 2001, 5:11 PM
 */

package com.sun.jdo.spi.persistence.utility.generator;

import java.io.IOException;

/**
 * This interface can be used to describe a java source file.  The resulting
 * source definition can be viewed by calling {@link java.lang.Object#toString}
 * or saved to a file by calling {@link #save}.  The semantics of this
 * interface are as follows:
 * - Resource allocation is deferred to save - there is no need to worry about
 * resource cleanup if an IOException is thrown from other methods (addImport,
 * setPackage, addClass).
 * - Any implementation of this interface must handle the closing of resources
 * during save if an exception is thrown.
 *
 *<p>
 * Use this interface in conjunction with one or more {@link JavaClassWriter}
 * instances to describe the class(es) in a java file.
 *
 * @author raccah
 */
public interface JavaFileWriter
{
    /** Sets the package for this file.  Note that the package name format
     * must be package style (that is - it can contain . but not / or $).
     * @param packageName The name of the package for this source file.
     * @param comments The comments shown just above the package statement.
     * The comments are passed as an array so the line separators can be added
     * by the implementation.  Note that not all implementations will choose
     * to make use of this comment.
     * @throws IOException If the package cannot be set.
     */
    public void setPackage (String packageName, String[] comments)
        throws IOException;

    /** Adds an import statement for this source file.
     * @param importName Name of the class or package (including the *) to be
     * imported.  This string should not contain "import" or the ;
     * @param comments The comments shown just above the import statement.
     * The comments are passed as an array so the line separators can be added
     * by the implementation.  Note that not all implementations will choose
     * to make use of this comment.
     * @throws IOException If the import information cannot be added.
     */
    public void addImport (String importName, String[] comments)
        throws IOException;

    /** Adds a class to this source file.
     * @param classWriter The definition of the class.
     * @throws IOException If the class information cannot be added.
     */
    public void addClass (JavaClassWriter classWriter) throws IOException;

    /** Saves the file by writing out the source contents to whatever
     * file (or alternate representation) was specified (usually by the
     * constructor of the implementation class.
     * @throws IOException If the file cannot be saved.
     */
    public void save () throws IOException;
}
