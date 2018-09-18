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

/*
 * $Id: CustomerBean.java,v 1.3 2004/11/14 07:33:17 tcfujii Exp $
 */

package demo.model;


import java.io.Serializable;


/**
 * <p>JavaBean represented the data for an individual customer.</p>
 */

public class CustomerBean implements Serializable {


    public CustomerBean() {
        this(null, null, null, 0.0);
    }


    public CustomerBean(String accountId, String name,
                        String symbol, double totalSales) {
        this.accountId = accountId;
        this.name = name;
        this.symbol = symbol;
        this.totalSales = totalSales;
    }


    private String accountId = null;


    public String getAccountId() {
        return (this.accountId);
    }


    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }


    private String name = null;


    public String getName() {
        return (this.name);
    }


    public void setName(String name) {
        this.name = name;
    }


    private String symbol = null;


    public String getSymbol() {
        return (this.symbol);
    }


    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    private double totalSales = 0.0;


    public double getTotalSales() {
        return (this.totalSales);
    }


    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer("CustomerBean[accountId=");
        sb.append(accountId);
        sb.append(",name=");
        sb.append(name);
        sb.append(",symbol=");
        sb.append(symbol);
        sb.append(",totalSales=");
        sb.append(totalSales);
        sb.append("]");
        return (sb.toString());
    }


}
