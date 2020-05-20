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

package dispatcher;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import cart.ShoppingCart;
import database.*;
import exception.*;
import foo.Bar;

public class Dispatcher extends HttpServlet {
    public void init() {
       //to test --libraries
       (new Bar()).baz();
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        String bookId = null;
        BookDetails book = null;
        String clear = null;
        BookDBAO bookDBAO =
            (BookDBAO) getServletContext()
                           .getAttribute("bookDBAO");

        HttpSession session = request.getSession();
        String selectedScreen = request.getServletPath();
        ShoppingCart cart = (ShoppingCart) session.getAttribute("cart");

        if (cart == null) {
            cart = new ShoppingCart();

            session.setAttribute("cart", cart);
        }

        if (selectedScreen.equals("/bookcatalog")) {
            bookId = request.getParameter("Add");

            if (!bookId.equals("")) {
                try {
                    book = bookDBAO.getBookDetails(bookId);

                    if (book.getOnSale()) {
                        double sale = book.getPrice() * .85;
                        Float salePrice = new Float(sale);
                        book.setPrice(salePrice.floatValue());
                    }

                    cart.add(bookId, book);
                } catch (BookNotFoundException ex) {
                    // not possible
                }
            }
        } else if (selectedScreen.equals("/bookshowcart")) {
            bookId = request.getParameter("Remove");

            if (bookId != null) {
                cart.remove(bookId);
            }

            clear = request.getParameter("Clear");

            if ((clear != null) && clear.equals("clear")) {
                cart.clear();
            }
        } else if (selectedScreen.equals("/bookreceipt")) {
            // Update the inventory
            try {
                bookDBAO.buyBooks(cart);
            } catch (OrderException ex) {
                try {
                    request.getRequestDispatcher("/bookordererror.jsp")
                           .forward(request, response);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            request.getRequestDispatcher("/template/template.jsp")
                   .forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute("selectedScreen", request.getServletPath());

        try {
            request.getRequestDispatcher("/template/template.jsp")
                   .forward(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
