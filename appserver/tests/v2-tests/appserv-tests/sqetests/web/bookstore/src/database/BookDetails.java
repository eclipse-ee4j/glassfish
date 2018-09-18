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

public class BookDetails implements Comparable {
    private String bookId = null;
    private String title = null;
    private String firstName = null;
    private String surname = null;
    private float price = 0.0F;
    private boolean onSale = false;
    private int year = 0;
    private String description = null;
    private int inventory = 0;

    public BookDetails() {
    }

    public BookDetails(String bookId, String surname, String firstName,
        String title, float price, boolean onSale, int year,
        String description, int inventory) {
        this.bookId = bookId;
        this.title = title;
        this.firstName = firstName;
        this.surname = surname;
        this.price = price;
        this.onSale = onSale;
        this.year = year;
        this.description = description;
        this.inventory = inventory;
    }

    public String getBookId() {
        return this.bookId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getSurname() {
        return this.surname;
    }

    public float getPrice() {
        return this.price;
    }

    public boolean getOnSale() {
        return this.onSale;
    }

    public int getYear() {
        return this.year;
    }

    public String getDescription() {
        return this.description;
    }

    public int getInventory() {
        return this.inventory;
    }

    public void setBookId(String id) {
        this.bookId = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setOnSale(boolean onSale) {
        this.onSale = onSale;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public int compareTo(Object o) {
        BookDetails n = (BookDetails) o;
        int lastCmp = title.compareTo(n.title);

        return (lastCmp);
    }
}
