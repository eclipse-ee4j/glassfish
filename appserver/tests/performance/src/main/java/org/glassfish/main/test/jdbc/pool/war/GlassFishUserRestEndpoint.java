/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.jdbc.pool.war;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import java.util.List;

@Path("/user")
@RequestScoped
public class GlassFishUserRestEndpoint {

    @PersistenceContext(unitName = "UnitA")
    private EntityManager em;

    @PUT
    @Path("/create")
    @Transactional(TxType.REQUIRES_NEW)
    public void create(User user) {
        em.persist(user);
    }


    @GET
    @Path("/list")
    @Transactional(TxType.NOT_SUPPORTED)
    public List<User> list() {
        return em.createQuery("select u from User u", User.class).setMaxResults(100).getResultList();
    }


    @GET
    @Path("/count")
    @Transactional(TxType.NOT_SUPPORTED)
    public long count() {
        return em.createQuery("select count(u) from User u", Long.class).getSingleResult();
    }
}
