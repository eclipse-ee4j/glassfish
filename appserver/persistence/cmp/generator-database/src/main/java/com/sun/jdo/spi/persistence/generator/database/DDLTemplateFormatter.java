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
 * DDLTemplateFormatter.java
 *
 * Created on Jan 14, 2003
 */

package com.sun.jdo.spi.persistence.generator.database;

import java.text.MessageFormat;

/*
 * This class provides methods that format strings containing DDL.  The
 * resulting strings are dependent on a particular MappingPolicy.
 *
 * @author Jie Leng, Dave Bristor
 */
// XXX FIXME This will not work in the unlikely event that 2 apps are being
// deployed at once.  It has reset invoked in DDLGenerator.generateDDL, but
// if another generateDDL can happen simultaneously, we're in trouble.
class DDLTemplateFormatter {
    /** Formatter for the start of "create table" DDL. */
    private static MessageFormat createTableStart = null;

    /** Formatter for the start of "create index" DDL. */
    private static MessageFormat createIndex = null;

    /** Formatter for "add constraint" DDL. */
    private static MessageFormat alterTableAddConstraintStart = null;

    /** Formatter for "drop constraing" DDL. */
    private static MessageFormat alterTableDropConstraint = null;

    /** Formatter for DDL for adding a PK constraint. */
    private static MessageFormat primaryKeyConstraint = null;

    /** Formatter for DDL for adding an FK constraint. */
    private static MessageFormat foreignKeyConstraint = null;

    /** Formatter for "drop table" DDL. */
    private static MessageFormat dropTable = null;


    /**
     * Prevent instantiation.
     */
    private DDLTemplateFormatter() {
    }

    /**
     * Resets MessageFormats for code generation as per policy.
     * @param mappingPolicy Policy that determines formatters provided by
     * this class.
     */
    static void reset(MappingPolicy mappingPolicy) {
        createTableStart = new MessageFormat(
                mappingPolicy.getCreateTableStart());
    // Added for Symfoware support as indexes on primary keys are mandatory
        createIndex = new MessageFormat(
                mappingPolicy.getCreateIndex());

        alterTableAddConstraintStart = new MessageFormat(
                mappingPolicy.getAlterTableAddConstraintStart());

        alterTableDropConstraint = new MessageFormat(
                mappingPolicy.getAlterTableDropConstraint());

        primaryKeyConstraint = new MessageFormat(
                mappingPolicy.getPrimaryKeyConstraint());

        foreignKeyConstraint = new MessageFormat(
                mappingPolicy.getForeignKeyConstraint());

        dropTable = new MessageFormat(
                mappingPolicy.getDropTable());
    }


    /**
     * @returns A String formatted for the start of "create table" DDL.
     */
    static String formatCreateTable(Object o) {
        return createTableStart.format(o);
    }

    /**
     * @returns A String formatted for the start of "create index" DDL.
     */
    static String formatCreateIndex(Object o) {
        return createIndex.format(o);
    }

    /**
     * @returns A String formatted for "add constraint" DDL.
     */
    static String formatAlterTableAddConstraint(Object o) {
        return alterTableAddConstraintStart.format(o);
    }

    /**
     * @returns A String formatted for "drop constraint" DDL.
     */
    static String formatAlterTableDropConstraint(Object o) {
        return alterTableDropConstraint.format(o);
    }

    /**
     * @returns A String formatted for DDL for adding a PK constraint.
     */
    static String formatPKConstraint(Object o) {
        return primaryKeyConstraint.format(o);
    }

    /**
     * @returns A String formatted for DDL for adding an FK constraint.
     */
    static String formatFKConstraint(Object o) {
        return foreignKeyConstraint.format(o);
    }

    /**
     * @returns A String formatted for "drop table" DDL.
     */
    static String formatDropTable(Object o) {
        return dropTable.format(o);
    }
}
