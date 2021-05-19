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


public class BookstoreMessages_de_DE extends ListResourceBundle {
    static final Object[][] contents =
    {
        {
            "ServerError",
            "Aufgrund eines Server Fehlers kann ihre Anfrage nicht erfuellt werden. Server Fehler: "
        },
        { "TitleServerError", "Server Fehler" },
        { "TitleShoppingCart", "Einkaufswagen" },
        { "TitleReceipt", "Quittung" },
        { "TitleBookCatalog", "Buchkatalog" },
        { "TitleCashier", "Kasse" },
        { "TitleBookDescription", "Buchbeschreibung" },
        { "Visitor", "Sie sind Besucher Nummer " },

        { "What", "Was wir gerade lesen" },
        {
            "Talk",
            " handelt davon, wie Sie mit Hilfe von Webkomponenten ganz neuartige Webapplikationen entwickeln koennen. Ein Muss fuer jeden Webentwickler, der etwas auf sich haelt!"
        },
        { "Start", "Einkauf Beginnen" },
        { "Critics", "Das meinen die Kritiker: " },
        { "Price", "Unser Preis: " },
        { "CartRemoved", "Sie haben gerade entfernt " },
        { "CartCleared", "Sie haben gerade Ihren Einkaufswagen geleert!" },
        { "CartContents", "Ihr Einkaufswagen enthaelt " },
        { "CartItem", " Artikel" },
        { "CartItems", " Artikel" },
        { "CartAdded1", "Sie haben hinzugefuegt " },
        { "CartAdded2", " zu Ihrem Einkaufswagen." },
        { "CartCheck", "Einkaufswagen Pruefen" },
        { "CartAdd", "Zum Einkaufswagen Zufuegen" },
        { "By", "von" },
        { "Buy", "Buecher Kaufen" },
        { "Choose", "Bitte waehlen Sie aus unserer Auswahl:" },
        { "ItemQuantity", "Anzahl" },
        { "ItemTitle", "Titel" },
        { "ItemPrice", "Preis" },
        { "RemoveItem", "Artikel Entfernen" },
        { "Subtotal", "Zwischensumme:" },
        { "ContinueShopping", "Einkauf Fortsetzen" },
        { "Checkout", "Bezahlen" },
        { "ClearCart", "Einkaufswagen Leeren" },
        { "CartEmpty", "Ihr Einkaufswagen ist leer." },
        { "Amount", "Gesamtpreis:" },
        {
            "Purchase",
            "Damit Sie die Artikel in Ihrem Einkaufswagen kaufen koennen, geben Sie uns bitte die folgenden Informationen:"
        },
        { "Name", "Name:" },
        { "CCNumber", "Kreditkartennummer:" },
        { "Submit", "Information Abschicken" },
        { "Catalog", "Zurueck zum Katalog" },
        { "ThankYou", "Vielen Dank fuer Ihren Buechereinkauf bei uns " },
        { "ThankYouParam", "Vielen Dank, {0} fuer Ihren Buechereinkauf bei uns " },
        {
            "OrderError",
            "Ihre Bestellung konnte aufgrund unzureichenden Inventars nicht ausgefuehrt werden."
        },
        { "With", "Mit" },

        { "Shipping", "Versand:" },
        { "QuickShip", "Schnellversand" },
        { "NormalShip", "Normalversand" },
        { "SaverShip", "Spar-Option Versand" },
        { "ShipDate", "Ihre Sendung wird geliefert am " },
        { "ShipDateLC", "ihre sendung wird geliefert am " },

        { "ConfirmAdd", "Sie haben gerade \"{0}\" in Ihren Einkaufswagen gelegt" },
        {
            "ConfirmRemove",
            "Sie haben gerade \"{0}\" von Ihrem Einkaufswagen entfernt"
        },
        {
            "CartItemCount",
            "Ihr Einkaufswagen enthaelt " +
            "{0,choice,0#Ihr Einkaufswagen enthaelt keine Artikel |1#Ihr Einkaufswagen enthaelt einen Artikel|1< Ihr Einkaufswagen enthaelt {0} Artikel }"
        },
        { "Newsletters", "Kostenloses Newsletter-Abo:" },
        {
            "ThanksMsg",
            "Vielen Dank. Bitte klicken Sie auf Submit, um ihre Buecher zu kaufen."
        },
        {
            "DukeFanClub",
            "Ich moechte dem Duke Fanclub beitreten, kostenlos mit meinem Einkauf ueber 100" +
            "\u20a0"
        },
        { "UpdateQuantities", "Anzahl neu berechnen " },
        {
            "QuantitiesUpdated",
            "Sie haben gerade die Anzahl fuer jedes Buch in Ihrem Einkaufswagen neu berechnet "
        },
        { "Quantities", "Buchexemplare in Ihrem Einkaufswagen" },
        {
            "ChooseLocale",
            "W\u00e4hlen Sie Ihren bevorzugten Standort aus der Karte."
        },
        { "English", "Englisch" },
        { "German", "Deutch" },
        { "Spanish", "Spanisch" },
        { "French", "Franzoesisch" },
        { "CustomerInfo", "Tragen Sie Ihre Informationen in die Form ein." },
        {
            "BookCatalog",
            "F" + "\u00fc" + "gen Sie die B" + "\u00fc" +
            "cher vom Katalog Ihrer Einkaufenkarre hinzu."
        },
        {
            "ShoppingCart",
            "Diese Seite verzeichnet die B" + "\u00fc" +
            "cher in Ihrer Einkaufenkarre."
        }
    };

    public Object[][] getContents() {
        return contents;
    }
}
