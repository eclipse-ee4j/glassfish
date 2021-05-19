/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.cb;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.math.BigDecimal;

public class Dispatcher extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    ResourceBundle messages = (ResourceBundle)session.getAttribute("messages");
    if (messages == null) {
        Locale locale=request.getLocale();
        messages = ResourceBundle.getBundle("com.sun.cb.messages.CBMessages", locale);
        session.setAttribute("messages", messages);
    }

    ServletContext context = getServletContext();
    RetailPriceList rpl = (RetailPriceList)context.getAttribute("retailPriceList");
    if (rpl == null) {
      try {
          rpl = new RetailPriceList();
          context.setAttribute("retailPriceList", rpl);
        } catch (Exception ex) {
          context.log("Couldn't create price list: " + ex.getMessage());
        }
    }
    ShoppingCart cart = (ShoppingCart)session.getAttribute("cart");
    if (cart == null) {
        cart = new ShoppingCart(rpl);
        session.setAttribute("cart", cart);
    }


    String selectedScreen = request.getServletPath();
    if (selectedScreen.equals("/checkoutForm")) {
      CheckoutFormBean checkoutFormBean = new CheckoutFormBean(cart, rpl, messages);

      request.setAttribute("checkoutFormBean", checkoutFormBean);
      try {
        checkoutFormBean.setFirstName(request.getParameter("firstName"));
        checkoutFormBean.setLastName(request.getParameter("lastName"));
        checkoutFormBean.setEmail(request.getParameter("email"));
        checkoutFormBean.setAreaCode(request.getParameter("areaCode"));
        checkoutFormBean.setPhoneNumber(request.getParameter("phoneNumber"));
        checkoutFormBean.setStreet(request.getParameter("street"));
        checkoutFormBean.setCity(request.getParameter("city"));
        checkoutFormBean.setState(request.getParameter("state"));
        checkoutFormBean.setZip(request.getParameter("zip"));
        checkoutFormBean.setCCNumber(request.getParameter("CCNumber"));
        checkoutFormBean.setCCOption(Integer.parseInt(request.getParameter("CCOption")));
      } catch (NumberFormatException e) {
        // not possible
      }
    }
    try {
        request.getRequestDispatcher("/template/template.jsp").forward(request, response);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession();
    ResourceBundle messages = (ResourceBundle)session.getAttribute("messages");
    String selectedScreen = request.getServletPath();
    ServletContext context = getServletContext();

    RetailPriceList rpl = (RetailPriceList)context.getAttribute("retailPriceList");
    if (rpl == null) {
      try {
          rpl = new RetailPriceList();
          context.setAttribute("retailPriceList", rpl);
        } catch (Exception ex) {
          context.log("Couldn't create price list: " + ex.getMessage());
        }
    }
    ShoppingCart cart = (ShoppingCart)session.getAttribute("cart");
    if (cart == null ) {
        cart = new ShoppingCart(rpl);
        session.setAttribute("cart", cart);
    }

    if (selectedScreen.equals("/orderForm")) {
      cart.clear();
      for(Iterator i = rpl.getItems().iterator(); i.hasNext(); ) {
        RetailPriceItem item = (RetailPriceItem) i.next();
        String coffeeName = item.getCoffeeName();
        BigDecimal pounds = new BigDecimal(request.getParameter(coffeeName + "_pounds"));
        BigDecimal price = item.getRetailPricePerPound().multiply(pounds).setScale(2, BigDecimal.ROUND_HALF_UP);
        ShoppingCartItem sci = new ShoppingCartItem(item, pounds, price);
        cart.add(sci);
      }

    } else if (selectedScreen.equals("/checkoutAck")) {
      CheckoutFormBean checkoutFormBean = new CheckoutFormBean(cart, rpl, messages);

      request.setAttribute("checkoutFormBean", checkoutFormBean);
      try {
        checkoutFormBean.setFirstName(request.getParameter("firstName"));
        checkoutFormBean.setLastName(request.getParameter("lastName"));
        checkoutFormBean.setEmail(request.getParameter("email"));
        checkoutFormBean.setAreaCode(request.getParameter("areaCode"));
        checkoutFormBean.setPhoneNumber(request.getParameter("phoneNumber"));
        checkoutFormBean.setStreet(request.getParameter("street"));
        checkoutFormBean.setCity(request.getParameter("city"));
        checkoutFormBean.setState(request.getParameter("state"));
        checkoutFormBean.setZip(request.getParameter("zip"));
        checkoutFormBean.setCCNumber(request.getParameter("CCNumber"));
        checkoutFormBean.setCCOption(Integer.parseInt(request.getParameter("CCOption")));
      } catch (NumberFormatException e) {
        // not possible
      }
      if (!checkoutFormBean.validate()) {
        try {
            request.getRequestDispatcher("/checkoutForm.jsp").forward(request, response);
        } catch(Exception e) {
            e.printStackTrace();
        }
      }
    }

    try {
        request.getRequestDispatcher("/template/template.jsp").forward(request, response);
    } catch(Exception e) {
    }
  }
}






