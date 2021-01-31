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

package org.glassfish.api.deployment.archive;

import org.jvnet.hk2.annotations.Contract;

/**
 * ArchiveType is an extension over ModuleType defined in jsr88 API. It is analogous to type of an archive or a module
 * or deployment unit, whichever way you prefer to call them. Adding a new archive type (or ArchiveType) is a very
 * expensive operation. For example, there has been no new archive types introduced in Java EE since RAR type. Adding a
 * new archive type involves writing an ArchiveHandler which involves writing logic to create class loaders. Now, that's
 * not same as adding adding a technology type like jersey or jpa.
 * <p/>
 * This is only a contract. Actual types are made available as services by appropriate containers.
 * <p/>
 * GlassFish deployment framework uses the word container to refer to services. Containers are actually defined in Java
 * EE platform spec. ArchiveType maps to the way containers are defined in the Java EE platform spec.
 *
 * @author Sanjeeb Sahoo
 */
@Contract
@jakarta.inject.Singleton
public abstract class ArchiveType {
    /**
     * File extension for this type of archive. Empty string is used if there is no extension specified.
     */
    private String extension;

    /**
     * String value of this type
     */
    private String value;

    /**
     * Return the file extension string for this module type.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Ensure a correct value is passed as that's what is returned by {@link #toString()} which is sometimes used during
     * comparison as some old APIs like {@link ArchiveHandler#getArchiveType()} use String.
     *
     * @param value value of this archive type as reported in {@link #toString()}
     * @param extension file extension for this type of archive
     */
    protected ArchiveType(String value, String extension) {
        this.extension = extension;
        this.value = value;
    }

    /**
     * Same as calling #ArchiveType(String, String) with an empty string as extension.
     */
    protected ArchiveType(String value) {
        this(value, "");
    }

    /**
     * @return the string equivalent of this type.
     */
    @Override
    public final String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArchiveType) {
            return toString().equals(obj.toString());
        }
        return false;
    }
}
