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

import exception.*;
import java.util.List;
import cart.*;


public class BookDB {
    private String bookId = "0";
    private BookDBAO database = null;

    public BookDB() {
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public void setDatabase(BookDBAO database) {
        this.database = database;
    }

    public BookDetails getBookDetails() throws BookNotFoundException {
        return (BookDetails) database.getBookDetails(bookId);
    }

    public List getBooks() throws BooksNotFoundException {
        return database.getBooks();
    }

    public void buyBooks(ShoppingCart cart) throws OrderException {
        database.buyBooks(cart);
    }
}
