/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package slsbnicmt;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.persistence.*;
import jakarta.transaction.*;

@Stateless
public class AnnotatedEJB {
    @PersistenceContext
    private EntityManager em;

    private String name = "foo";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean persistEntity(){
        boolean pass = false;
        try {
      JpaBean jpaBean = new JpaBean();
      jpaBean.setName("JpaBean");
          System.out.println("Persisting ....");
      em.persist(jpaBean);
          pass = true;
    } catch (Throwable e) {
           e.printStackTrace();
    }
        return pass;
    }

    public boolean removeEntity(){
        boolean pass = false;
        try {
      Query query = em.createQuery("SELECT j FROM JpaBean j WHERE j.name='JpaBean'");
      JpaBean jpaBean = (JpaBean) query.getSingleResult();
      System.out.println("Loaded " + jpaBean);
      em.remove(jpaBean);
          pass = true;
    } catch (Throwable e) {
           e.printStackTrace();
    }
        return pass;
    }

    public boolean verifyRemove(){
        boolean pass = false;
        try {
      Query query = em.createQuery("SELECT count(j) FROM JpaBean j WHERE j.name='JpaBean'");
      int count = ((Number) query.getSingleResult()).intValue();
      if (count == 0) {
            pass = true;
      }
    } catch (Throwable e) {
           e.printStackTrace();
    }
        return pass;
    }

    public String toString() {
        return "AnnotatedEJB[name=" + name + "]";
    }
}
