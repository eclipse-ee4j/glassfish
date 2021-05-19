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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import javax.xml.registry.*;
import javax.xml.registry.infomodel.*;

public class RetailPriceList implements Serializable {

    private ArrayList retailPriceItems;
    private ArrayList distributors;

    public RetailPriceList() {
      String RegistryURL = URLHelper.getQueryURL();
      String RPCDistributor = "JAXRPCCoffeeDistributor";
      retailPriceItems = new ArrayList();
      distributors = new ArrayList();

      JAXRQueryByName jq = new JAXRQueryByName();
      Connection connection =  jq.makeConnection(RegistryURL, RegistryURL);
      Collection orgs = jq.executeQuery(RPCDistributor);
      Iterator orgIter = orgs.iterator();
      // Display organization information
      try {
        while (orgIter.hasNext()) {
          Organization org = (Organization) orgIter.next();
          System.out.println("Org name: " + jq.getName(org));
          System.out.println("Org description: " + jq.getDescription(org));
          System.out.println("Org key id: " + jq.getKey(org));

          // Display service and binding information
          Collection services = org.getServices();
          Iterator svcIter = services.iterator();
          while (svcIter.hasNext()) {
            Service svc = (Service) svcIter.next();
            System.out.println(" Service name: " + jq.getName(svc));
            System.out.println(" Service description: " + jq.getDescription(svc));
            Collection serviceBindings = svc.getServiceBindings();
            Iterator sbIter = serviceBindings.iterator();
            while (sbIter.hasNext()) {
              ServiceBinding sb = (ServiceBinding) sbIter.next();
              String distributor = sb.getAccessURI();
              System.out.println("  Binding Description: " + jq.getDescription(sb));
              System.out.println("  Access URI: " + distributor);

              // Get price list from service at distributor URI
              PriceListBean priceList = PriceFetcher.getPriceList(distributor);

              PriceItemBean[] items = priceList.getPriceItems();
              retailPriceItems = new ArrayList();
              distributors = new ArrayList();
              BigDecimal price = new BigDecimal("0.00");
              for (int i = 0; i < items.length; i++) {
                price = items[i].getPricePerPound().multiply(new BigDecimal("1.35")).setScale(2, BigDecimal.ROUND_HALF_UP);
                RetailPriceItem pi = new RetailPriceItem(items[i].getCoffeeName(), items[i].getPricePerPound(), price , distributor);
                retailPriceItems.add(pi);
              }
              distributors.add(distributor);
            }
          }
          // Print spacer between organizations
          System.out.println(" --- ");
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally  {
        // At end, close connection to registry
        if (connection != null) {
          try {
                  connection.close();
          } catch (JAXRException je) {}
        }
      }
      String SAAJPriceListURL = URLHelper.getSaajURL() + "/getPriceList";
      String SAAJOrderURL = URLHelper.getSaajURL() + "/orderCoffee";
      PriceListRequest plr = new PriceListRequest(SAAJPriceListURL);
      PriceListBean priceList = plr.getPriceList();;
      PriceItemBean[] priceItems = priceList.getPriceItems();
      for (int i = 0; i < priceItems.length; i++ ) {
        PriceItemBean pib = priceItems[i];
        BigDecimal price = pib.getPricePerPound().multiply(new BigDecimal("1.35")).setScale(2, BigDecimal.ROUND_HALF_UP);
        RetailPriceItem rpi = new RetailPriceItem(pib.getCoffeeName(), pib.getPricePerPound(), price, SAAJOrderURL);
        retailPriceItems.add(rpi);
      }
      distributors.add(SAAJOrderURL);
                 }

    public ArrayList getItems() {
        return retailPriceItems;
    }

    public ArrayList getDistributors() {
        return distributors;
    }

    public void setItems(ArrayList priceItems) {
        this.retailPriceItems = priceItems;
    }

    public void setDistributors(ArrayList distributors) {
        this.distributors = distributors;
    }
}
