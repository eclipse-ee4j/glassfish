/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package endpoint;

import java.math.BigInteger;
import java.util.*;
import jakarta.jws.*;
import jakarta.ejb.*;
import jakarta.persistence.*;

@Stateless
@WebService
public class CustomerManager {
    @PersistenceContext EntityManager em;

    @WebMethod
    public Customer createCustomer( BigInteger code, String name) {
        System.out.println("createCustomer " + code + " " + name);
        Customer customer = new Customer( code, name );
        em.persist( customer );
        return customer;
    }

    @WebMethod
    public Collection getCustomerList() {
        System.out.println("getCustomerList");
        String ejbQL = "SELECT c FROM Customer c";
        return em.createQuery( ejbQL ).getResultList();
    }

    @WebMethod
    public int removeCustomer(String id) {
        System.out.println("removeCustomer");
        String ejbQL = "DELETE FROM Customer c WHERE c.name = \""+id+"\"";
        int ret = em.createQuery( ejbQL ).executeUpdate();
         return ret;
    }
}
