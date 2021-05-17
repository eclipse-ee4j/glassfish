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

package messages;

import java.util.*;


public class BookstoreMessages_en_US extends ListResourceBundle {
    static final Object[][] contents =
    {
        {
            "ServerError",
            "Your request cannot be completed.  The server got the following error: "
        },
        { "TitleServerError", "Server Error" },
        { "TitleShoppingCart", "Shopping Cart" },
        { "TitleReceipt", "Receipt" },
        { "TitleBookCatalog", "Book Catalog" },
        { "TitleCashier", "Cashier" },
        { "TitleBookDescription", "Book Description" },
        { "Visitor", "You are visitor number " },

        { "What", "What We\'re Reading" },
        {
            "Talk",
            " talks about how web components can transform the way you develop applications for the web. This is a must read for any self respecting web developer!"
        },
        { "Start", "Start Shopping" },
        { "Critics", "Here's what the critics say: " },
        { "Price", "Our Price: " },
        { "CartRemoved", "You just removed " },
        { "CartCleared", "You just cleared your shopping cart!" },
        { "CartContents", "Your shopping cart contains " },
        { "CartItem", " item" },
        { "CartItems", " items" },
        { "CartAdded1", "You added " },
        { "CartAdded2", " to your shopping cart." },
        { "CartCheck", "Check Shopping Cart" },
        { "CartAdd", "Add to Cart" },
        { "By", "by" },
        { "Buy", "Buy Your Books" },
        { "Choose", "Please choose from our selections:" },
        { "ItemQuantity", "Quantity" },
        { "ItemTitle", "Title" },
        { "ItemPrice", "Price" },
        { "RemoveItem", "Remove Item" },
        { "Subtotal", "Subtotal:" },
        { "ContinueShopping", "Continue Shopping" },
        { "Checkout", "Check Out" },
        { "ClearCart", "Clear Cart" },
        { "CartEmpty", "Your cart is empty." },
        { "Amount", "Your total purchase amount is:" },
        {
            "Purchase",
            "To purchase the items in your shopping cart, please provide us with the following information:"
        },
        { "Name", "Name:" },
        { "CCNumber", "Credit Card Number:" },
        { "Submit", "Submit Information" },
        { "Catalog", "Back to the Catalog" },
        { "ThankYou", "Thank you for purchasing your books from us " },
        { "ThankYouParam", "Thank you, {0} for purchasing your books from us " },
        {
            "OrderError",
            "Your order could not be completed due to insufficient inventory."
        },
        { "With", "With" },

        { "Shipping", "Shipping:" },
        { "QuickShip", "Quick Shipping" },
        { "NormalShip", "Normal Shipping" },
        { "SaverShip", "Saver Shipping" },
        { "ShipDate", "Your order will be shipped on " },
        { "ShipDateLC", "your order will be shipped on " },

        { "ConfirmAdd", "You just added \"{0}\" to your shopping cart" },
        { "ConfirmRemove", "You just removed \"{0}\" from your shopping cart" },
        {
            "CartItemCount",
            "Your shopping cart contains " +
            "{0,choice,0#no items|1#one item|1< {0} items}"
        },
        { "Newsletters", "FREE Newsletter Subscriptions:" },
        { "ThanksMsg", "Thank you.  Please click Submit to purchase your books." },
        {
            "DukeFanClub",
            "I'd like to join the Duke Fan Club, free with my purchase of over $100"
        },
        { "UpdateQuantities", "Update Quantities" },
        {
            "QuantitiesUpdated",
            "You just updated the quantity of each book in your shopping cart"
        },
        { "Quantities", "Copies of book in cart" },
        { "ChooseLocale", "Choose Your Preferred Locale From the Map" },
        { "English", "English" },
        { "German", "German" },
        { "Spanish", "Spanish" },
        { "French", "French" },
        { "CustomerInfo", "Enter your information into the form." },
        { "BookCatalog", "Add books from the catalog to your shopping cart." },
        { "ShoppingCart", "This page lists the books in your shopping cart." }
    };

    public Object[][] getContents() {
        return contents;
    }
}
