/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.api.jdbc.objects;

import java.sql.Connection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
import static java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;
import static java.sql.Connection.TRANSACTION_REPEATABLE_READ;
import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

/**
 * Enum for {@link Connection} TRANSACTION* constants referred in descriptors etc.
 *
 * @author David Matejcek
 */
public enum TxIsolationLevel {

    /**
     * A constant indicating that dirty reads, non-repeatable reads and phantom reads can occur.
     * This level allows a row changed by one transaction to be read by another transaction before
     * any changes in that row have been committed (a "dirty read").
     * <p>
     * If any of the changes are rolled back, the second transaction will have retrieved
     * an invalid row.
     */
    READ_UNCOMMITTED("read-uncommitted", TRANSACTION_READ_UNCOMMITTED),

    /**
     * A constant indicating that dirty reads are prevented;
     * non-repeatable reads and phantom reads can occur.
     * <p>
     * This level only prohibits a transaction from reading a row with uncommitted changes in it.
     */
    READ_COMMITTED("read-committed", TRANSACTION_READ_COMMITTED),

    /**
     * A constant indicating that dirty reads and non-repeatable reads are prevented;
     * phantom reads can occur.
     * <p>
     * This level prohibits a transaction from reading a row with uncommitted changes in it, and it
     * also prohibits the situation where one transaction reads a row, a second transaction alters
     * the row, and the first transaction rereads the row, getting different values the second time
     * (a "non-repeatable read").
     */
    REPEATABLE_READ("repeatable-read", TRANSACTION_REPEATABLE_READ),

    /**
     * A constant indicating that dirty reads, non-repeatable reads and phantom reads are prevented.
     * <p>
     * This level includes the prohibitions in {@link #TRANSACTION_REPEATABLE_READ} and further
     * prohibits the situation where one transaction reads all rows that satisfy
     * a <code>WHERE</code> condition, a second transaction inserts a row that satisfies that
     * <code>WHERE</code> condition, and the first transaction rereads for the same condition,
     * retrieving the additional "phantom" row in the second read.
     */
    SERIALIZABLE("serializable", TRANSACTION_SERIALIZABLE),
    ;

    private String name;
    private int id;

    TxIsolationLevel(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /**
     * @return name used in descriptors etc.
     */
    public String getName() {
        return name;
    }

    /**
     * @return one of {@link Connection} TRANSACTION* constants
     */
    public int getId() {
        return id;
    }


    /**
     * @param id of the level
     * @return {@link TxIsolationLevel}, never null
     * @throws IllegalArgumentException if the id is not valid.
     */
    public static TxIsolationLevel byId(int id) throws IllegalArgumentException {
        return Stream.of(TxIsolationLevel.values()).filter(v -> v.getId() == id).findAny()
            .orElseThrow(() -> new IllegalArgumentException("Invalid transaction isolation id; the transaction "
                + "isolation level can be any of the following: " + Stream.of(TxIsolationLevel.values())
                    .map(t -> Integer.toString(t.getId())).collect(Collectors.joining(", "))));
    }


    /**
     * @param name of the level
     * @return {@link TxIsolationLevel}, never null
     * @throws IllegalArgumentException if the name is not valid.
     */
    public static TxIsolationLevel byName(String name) throws IllegalArgumentException {
        return Stream.of(TxIsolationLevel.values()).filter(v -> v.getName() == name).findAny()
            .orElseThrow(() -> new IllegalArgumentException("Invalid transaction isolation; the transaction"
                + " isolation level can be empty or any of the following: " + Stream.of(TxIsolationLevel.values())
                    .map(TxIsolationLevel::getName).collect(Collectors.joining(", "))));
    }


    /**
     * @return all defined names
     */
    public static String[] getAllNames() {
        return Stream.of(TxIsolationLevel.values()).map(TxIsolationLevel::getName).toArray(String[]::new);
    }
}
