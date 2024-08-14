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
 * DatabaseOutputStream.java
 *
 * Created on Jan 14, 2003
 */


package com.sun.jdo.spi.persistence.generator.database;

import com.sun.jdo.spi.persistence.utility.logging.Logger;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/*
 * Represents a database connection as an output stream.
 *
 * @author Jie Leng
 */
public class DatabaseOutputStream extends OutputStream {
     /** The logger */
    private static final Logger logger =
            LogHelperDatabaseGenerator.getLogger();

    /** Connection to the database. */
    // XXX FIXME S/b final; make it so if we can get rid of setConnection.
    private Connection conn_ = null;

    // XXX FIXME Assert conn != null and directly set the value of conn;
    // remove setConnection (below)
    public DatabaseOutputStream(Connection conn) {
        super();
        setConnection(conn);
    }

    // XXX FIXME I think this is not needed.
    public DatabaseOutputStream() {
        super();
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        try {
            // XXX test is not necessary once we assert not null in constructor
            if (conn_ != null) {
                conn_.commit();
                // Close the connection
                conn_.close();
            }

        } catch (SQLException e) {
        if (logger.isLoggable(Logger.FINE))
            logger.fine("Exception in cleanup", e); // NOI18N
        }
    }

    /**
     * Commits the database connection.
     */
    public void flush() {
        try {
            // XXX test is not necessary once we assert not null in constructor
            if (conn_ != null) {
                conn_.commit();
            }
        } catch (SQLException e) {
            if (logger.isLoggable(Logger.FINE))
               logger.fine("Exception in cleanup", e); // NOI18N
        }
    }

    /**
     * This method is not supported in DatabaseOutputStream because it
     * doesn't make sense to write a single int to a database stream.  So
     * always throws UnsupportedOperationException.
     * @throws UnsupportedOperationException
     */
    public void write(int b) {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes the given statement in the database.
     * @param stmt SQL to be executed
     * @throws SQLException Thrown if there is a problem preparing stmt as a
     * statement, or in executing it.
     */
    public void write(String stmt) throws SQLException {
    // Check if stmt is empty (null), and abort if so.
        if (stmt == null || stmt.trim().length() == 0) {
            return;
        }

        PreparedStatement pstmt = conn_.prepareStatement(stmt);
        pstmt.execute();
    }

    // XXX FIXME Is this really necessary?  Delete if possible.
    public void setConnection(Connection conn) {
        conn_ = conn;
    }
}
