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

package com.sun.cb.messages;

import java.util.*;

public class CBMessages_en extends ListResourceBundle {
  public Object[][] getContents() {
    return contents;
  }

  static final Object[][] contents = {

  {"ServerError", "Your request cannot be completed.  The server got the following error: "},
  {"TitleServerError", "Server Error"},
  {"TitleOrderForm", "Order Form"},
  {"TitleCheckoutForm", "Checkout Form"},
  {"TitleCheckoutAck", "Confirmation"},
  {"OrderInstructions", "Enter the amount of coffee and click Update to update the totals.<br>Click Checkout to proceed with your order. "},
  {"OrderForm", "OrderForm"},
  {"Price", "Price"},
  {"Quantity", "Quantity"},
  {"Total", "Total"},
  {"Update", "Update"},
  {"Checkout", "Checkout"},
  {"CheckoutInstructions", "To complete your order, fill in the form and click Submit."},
  {"YourOrder", "Your order totals "},
  {"CheckoutForm", "Checkout Form"},
  {"FirstName", "First Name"},
  {"FirstNameError", "Please enter your first name."},
  {"LastName", "Last Name"},
  {"LastNameError", "Please enter your last name."},
  {"EMail", " E-Mail"},
  {"EMailError", "Please enter a valid e-mail address."},
  {"PhoneNumber", "Phone Number"},
  {"AreaCodeError", "Please enter your area code."},
  {"PhoneNumberError", "Please enter your phone number."},
  {"Street", "Street"},
  {"StreetError", "Please enter your street."},
  {"City", "City"},
  {"CityError", "Please enter your city."},
  {"State", "State"},
  {"StateError", "Please enter your state."},
  {"Zip", "Zip"},
  {"ZipError", "Please enter a valid zip code."},
  {"CCOption", "Credit Card"},
  {"CCNumber", "Credit Card Number"},
  {"CCNumberError", "Please enter your credit card number."},
  {"Submit", "Submit"},
  {"Reset", "Reset"},
  {"ItemPrice", "Price"},
  {"OrderConfirmed", "Your order has been confirmed."},
  {"ShipDate", "Ship Date"},
  {"Items", "Items"},
  {"Coffee", "Coffee"},
  {"Pounds", "Pounds"},
  {"ContinueShopping", "Continue Shopping"}
  };
}

