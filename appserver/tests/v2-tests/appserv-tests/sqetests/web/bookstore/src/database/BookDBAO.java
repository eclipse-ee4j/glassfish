/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package database;

import java.sql.*;
import javax.sql.*;
import javax.naming.*;
import java.util.*;
import exception.*;
import cart.*;


public class BookDBAO {
    private ArrayList books;
    Connection con;

    public BookDBAO() throws Exception {
        try {
            InitialContext ic = new InitialContext();
            Context envCtx = (Context) ic.lookup("java:comp/env");
            DataSource ds = (DataSource) envCtx.lookup("jdbc/BookDB");
            con = ds.getConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Couldn't open connection to database: " +
                ex.getMessage());
        }
    }

    public void remove() {
        try {
            con.close();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public List getBooks() throws BooksNotFoundException {
        books = new ArrayList();

        try {
            String selectStatement = "select * " + "from books";
            PreparedStatement prepStmt = con.prepareStatement(selectStatement);
            ResultSet rs = prepStmt.executeQuery();

            while (rs.next()) {
                BookDetails bd =
                    new BookDetails(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4), rs.getFloat(5),
                        rs.getBoolean(6), rs.getInt(7), rs.getString(8),
                        rs.getInt(9));

                if (rs.getInt(9) > 0) {
                    books.add(bd);
                }
            }

            prepStmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BooksNotFoundException(ex.getMessage());
        }

        Collections.sort(books);

        return books;
    }

    public BookDetails getBookDetails(String bookId)
        throws BookNotFoundException {
        try {
            String selectStatement = "select * " + "from books where id = ? ";
            PreparedStatement prepStmt = con.prepareStatement(selectStatement);
            prepStmt.setString(1, bookId);

            ResultSet rs = prepStmt.executeQuery();

            if (rs.next()) {
                BookDetails bd =
                    new BookDetails(rs.getString(1), rs.getString(2),
                        rs.getString(3), rs.getString(4), rs.getFloat(5),
                        rs.getBoolean(6), rs.getInt(7), rs.getString(8),
                        rs.getInt(9));
                prepStmt.close();

                return bd;
            } else {
                prepStmt.close();
                System.err.println("Couldn't find book: " + bookId);
                throw new BookNotFoundException("Couldn't find book: " +
                    bookId);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new BookNotFoundException("Couldn't find book: " + bookId +
                " " + ex.getMessage());
        }
    }

    public void buyBooks(ShoppingCart cart) throws OrderException {
        Collection items = cart.getItems();
        Iterator i = items.iterator();

        try {
            con.setAutoCommit(false);

            while (i.hasNext()) {
                ShoppingCartItem sci = (ShoppingCartItem) i.next();
                BookDetails bd = (BookDetails) sci.getItem();
                String id = bd.getBookId();
                int quantity = sci.getQuantity();
                buyBook(id, quantity);
            }

            con.commit();
            con.setAutoCommit(true);
        } catch (Exception ex) {
            try {
                con.rollback();
                throw new OrderException("Transaction failed: " +
                    ex.getMessage());
            } catch (SQLException sqx) {
                throw new OrderException("Rollback failed: " +
                    sqx.getMessage());
            }
        }
    }

    public void buyBook(String bookId, int quantity) throws OrderException {
        try {
            String selectStatement = "select * " + "from books where id = ? ";
            PreparedStatement prepStmt = con.prepareStatement(selectStatement);
            prepStmt.setString(1, bookId);

            ResultSet rs = prepStmt.executeQuery();

            if (rs.next()) {
                int inventory = rs.getInt(9);
                prepStmt.close();

                if ((inventory - quantity) >= 0) {
                    String updateStatement =
                        "update books set inventory = inventory - ? where id = ?";
                    prepStmt = con.prepareStatement(updateStatement);
                    prepStmt.setInt(1, quantity);
                    prepStmt.setString(2, bookId);
                    prepStmt.executeUpdate();
                    prepStmt.close();
                } else {
                    throw new OrderException("Not enough of " + bookId +
                        " in stock to complete order.");
                }
            }
        } catch (Exception ex) {
            throw new OrderException("Couldn't purchase book: " + bookId +
                ex.getMessage());
        }
    }
}
