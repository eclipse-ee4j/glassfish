/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package examples.sfsb;

import java.io.Serializable;

/**
 * This class reflects the results of a buy/sell transaction.
 */
public final class TradeResult implements Serializable {

  public static final int SELL = 0;
  public static final int BUY  = 1;

  private int    numberTraded;    // Number of shares really bought or sold
  private int    action;          // Whether shares were bought or sold
  private double price;           // Price shares were bought or sold at

  public TradeResult(){
  }

  /**
   * Returns the number of shares and sales price for a
   * buy or sell transaction in a TradeResult object.
   *
   * @param numberTraded      int Number of shares traded
   * @param price             double Price shares sold at
   * @param action            int Action taken
   * @return                  TradeResult
   */
  public TradeResult(int numberTraded, double price, int action) {
    this.numberTraded = numberTraded;
    this.price        = price;
    this.action       = action;
  }

  public int getNumberTraded() {
    return numberTraded;
  }

  public void setNumberTraded( int numberTraded ){
    this.numberTraded = numberTraded;
  }

  public int getActionTaken() {
    return action;
  }

  public void setActionTaken( int action ){
    this.action = action;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice( double price ){
    this.price = price;
  }

  public String toString() {
    String result = numberTraded + " shares";
    if(action == SELL) {
      result += "sold";
    } else {
      result += "bought";
    }
    result += " at a price of " + price;
    return result;
  }
}
