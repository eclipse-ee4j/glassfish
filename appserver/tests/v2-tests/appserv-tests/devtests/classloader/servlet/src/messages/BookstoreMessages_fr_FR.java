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


public class BookstoreMessages_fr_FR extends ListResourceBundle {
    static final Object[][] contents =
    {
        {
            "ServerError",
            "Votre commande ne peut pas se completer. Le serveur a retourne l'erreur suivante: "
        },
        { "TitleServerError", "Erreur de serveur" },
        { "TitleShoppingCart", "Panier" },
        { "TitleReceipt", "Re" + "\u00e7" + "u" },
        { "TitleBookCatalog", "Catalogue" },
        { "TitleCashier", "Cassier" },
        { "TitleBookDescription", "Description du livre" },
        { "Visitor", "Vous " + "\u00ea" + "tes visiteur numero " },
        { "What", "Ce que nous lisons" },
        {
            "Talk",
            " parle de comment les elements du web peuvent transformer la mani" +
            "\u00e8" + "re dont vous d" + "\u00e9" +
            "veloppez des applications d'internet. Ce livre est obligatoire pour tout d" +
            "\u00e9" + "veloppeur de web!"
        },
        { "Start", "Panier" },
        { "Critics", "Commentaires:  " },
        { "Price", "Prix: " },
        { "CartRemoved", "Vous venez d'enlever " },
        { "CartCleared", "Vous avez vide votre panier" },
        { "CartContents", "Votre panier contient " },
        { "CartItem", " article" },
        { "CartItem", " articles" },
        { "CartAdded1", "Vous avez ajoute " },
        { "CartAdded2", "a votre panier." },
        { "CartCheck", "Verifier votre panier" },
        { "CartAdd", "Ajouter au panier" },
        { "By", "par" },
        { "Buy", "Achetez vos livres" },
        { "Choose", "Choisissez:" },
        { "ItemQuantity", "Quantite" },
        { "ItemTitle", "Titre" },
        { "ItemPrice", "Prix" },
        { "RemoveItem", "Supprimer" },
        { "Subtotal", "Total partiel:" },
        { "ContinueShopping", "Continuer" },
        { "Checkout", "Caisse et Sortie" },
        { "ClearCart", "Vider le panier" },
        { "CartEmpty", "Votre panier est vide." },
        { "Amount", "Le montant de votre achat is:" },
        {
            "Purchase",
            "Pour acheter les articles dans votre panier, veuillez nous donner l'information suivante:"
        },
        { "Name", "Nom:" },
        { "CCNumber", "Numero de votre Carte de Credit:" },
        { "Submit", "Soumettre l'information" },
        { "Catalog", "Retourner au catalogue" },
        { "ThankYou", "Merci de votre achat " },
        { "ThankYouParam", "Merci, {0} pour acheter les livres ici " },
        {
            "OrderError",
            "Votre commande ne peut pas se faire a cause de manque d'inventaire"
        },
        { "With", "Avec" },
        { "Shipping", "Envoi:" },
        { "QuickShip", "Envoi Express" },
        { "NormalShip", "Envoi Normal" },
        { "SaverShip", "Envoi Economique" },
        { "ShipDate", "Votre commande sera envoyee le " },
        { "ShipDateLC", "Votre commande sera envoyee le " },
        { "ConfirmAdd", "Vous avez ajoute \"{0}\" a votre panier" },
        {
            "ConfirmRemove",
            "Vous avez supprim" + "\00e9" + " \"{0}\" de votre panier"
        },
        {
            "CartItemCount",
            "Votre panier contient " +
            "{0,choice,0#aucun article|1#un article|1< {0} articles}"
        },
        { "Newsletters", "Abonnement gratuit a notre bulletin :" },
        { "ThanksMsg", "Merci. Cliquez Soumettre pour acheter les livres." },
        {
            "DukeFanClub",
            "Je veux joindre le Duke Fan Club gratuitement avec mon achat de plus de 100" +
            "\u20a0"
        },
        { "UpdateQuantities", "Mettre a jour" },
        {
            "QuantitiesUpdated",
            "Vous venez de mettre a jour le nombre de livres dans votre panier "
        },
        { "Quantities", "Copies de livres dans le panier" },
        { "ChooseLocale", "Choisissez Votre Language dans la Carte" },
        { "English", "anglais" },
        { "German", "allemand" },
        { "Spanish", "espagnol" },
        { "French", "fran" + "\u00e7" + "ais" },
        { "CustomerInfo", "\u0039" + "crivez votre information dans la forme." },
        {
            "BookCatalog",
            "Ajoutez les livres du catalogue" + "\u00c1" + "votre caddie."
        },
        {
            "ShoppingCart",
            "Cette page " + "\u00e9" + "num" + "\u00e8" +
            "re les livres dans votre caddie."
        }
    };

    public Object[][] getContents() {
        return contents;
    }
}
