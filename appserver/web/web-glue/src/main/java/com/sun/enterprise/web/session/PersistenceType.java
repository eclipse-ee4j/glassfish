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

package com.sun.enterprise.web.session;

/**
 * Represents each of the persistence mechanisms supported by the session
 * managers.
 */
public final class PersistenceType {

    // ------------------------------------------------------- Static Variables

    /**
     * Memory based persistence for sessions (i.e. none);
     */
    public static final PersistenceType MEMORY =
        new PersistenceType("memory");

    /**
     * File based persistence for sessions.
     */
    public static final PersistenceType FILE =
        new PersistenceType("file");

    /**
     * Cookie-based persistence
     */
    public static final PersistenceType COOKIE =
        new PersistenceType("cookie");

    /**
     * Custom/user implemented session manager.
     */
    public static final PersistenceType CUSTOM =
        new PersistenceType("custom");

    /**
     * old iWS 6.0 style session manager.
     */
    public static final PersistenceType S1WS60 =
        new PersistenceType("s1ws60");

    /**
     * old iWS 6.0 style
     * MMapSessionManager.
     */
    public static final PersistenceType MMAP =
        new PersistenceType("mmap");

    /**
     * JDBC based persistence for sessions.
     */
    public static final PersistenceType JDBC =
        new PersistenceType("jdbc");

    /**
     * HADB based persistence for sessions.
     */
    public static final PersistenceType HA =
        new PersistenceType("ha");

    /**
     * SJSWS replicated persistence for sessions.
     */
    public static final PersistenceType REPLICATED =
        new PersistenceType("replicated");

    /**
     * Coherence Web
     */
    public static final PersistenceType COHERENCE_WEB =
        new PersistenceType("coherence-web");

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor that sets its type to the specified string.
     */
    private PersistenceType(String type) {
        _type = type;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The persistence type specifier.
     */
    private String _type = null;

    // ------------------------------------------------------------- Properties

    /**
     * Returns a string describing the persistence mechanism that the
     * object represents.
     */
    public String getType() {
        return _type;
    }

    // --------------------------------------------------------- Static Methods

    /**
     * Parse the specified string and return the corresponding instance
     * of this class that represents the persistence type specified
     * in the string.
     */
    public static PersistenceType parseType(String type) {
        // Default persistence type is MEMORY
        return parseType(type, MEMORY);
    }

    /**
     * Parse the specified string and return the corresponding instance
     * of this class that represents the persistence type specified
     * in the string.  Default back into passed-in parameter
     */
    public static PersistenceType parseType(String type, PersistenceType defaultType) {
        // Default persistence type is defaultType
        PersistenceType pType = defaultType;
        if (type != null) {
            if (type.equalsIgnoreCase(MEMORY.getType()))
                pType = MEMORY;
            else if (type.equalsIgnoreCase(FILE.getType()))
                pType = FILE;
            else if (type.equalsIgnoreCase(COOKIE.getType()))
                pType = COOKIE;
            else if (type.equalsIgnoreCase(CUSTOM.getType()))
                pType = CUSTOM;
            else if (type.equalsIgnoreCase(S1WS60.getType()))
                pType = S1WS60;
            else if (type.equalsIgnoreCase(MMAP.getType()))
                pType = MMAP;
            else if (type.equalsIgnoreCase(JDBC.getType()))
                pType = JDBC;
            else if (type.equalsIgnoreCase(HA.getType()))
                pType = HA;
            else if (type.equalsIgnoreCase(REPLICATED.getType()))
                pType = REPLICATED;
            else if (type.equalsIgnoreCase(COHERENCE_WEB.getType()))
                pType = COHERENCE_WEB;
        }
        return pType;
    }

}

