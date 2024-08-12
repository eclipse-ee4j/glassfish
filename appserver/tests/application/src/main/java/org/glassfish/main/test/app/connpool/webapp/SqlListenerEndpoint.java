/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.glassfish.main.test.app.connpool.webapp;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.ws.rs.core.MediaType;
import java.util.Optional;
import org.glassfish.api.jdbc.SQLTraceRecord;
import org.glassfish.main.test.app.connpool.lib.LastTraceSQLTraceListener;

@Stateless
@Path("/")
public class SqlListenerEndpoint {

    @PersistenceContext
    EntityManager entityManager;

    @GET
    @Path("validate-trace-listener")
    @Produces({MediaType.TEXT_PLAIN})
    public String validateTraceListener() {
        callSomeQuery();
        assertTraceListenerCalled();
        return "OK";
    }

    private void callSomeQuery() {
        CriteriaQuery cq = entityManager.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Employee.class));
        entityManager.createQuery(cq).getResultList();
    }

    private void assertTraceListenerCalled() {
        SQLTraceRecord sqlTr = LastTraceSQLTraceListener.lastTraceRecord;
        assertValueSet("applicationName", sqlTr.getApplicationName());
        assertValueSet("className", sqlTr.getClassName());
        assertValueSet("methodName", sqlTr.getMethodName());
        assertValueSet("poolName", sqlTr.getPoolName());
        assertValueSet("sqlQuery", sqlTr.getSqlQuery());
        assertValueSet("threadName", sqlTr.getThreadName());
        assertTrue("threadID > 0", sqlTr.getThreadID() > 0);
        assertTrue("timeStamp > 0", sqlTr.getTimeStamp() > 0);

        StackTraceElement caller = LastTraceSQLTraceListener.lastCallingApplicationMethod;
        assertTrue("application class should be " + SqlListenerEndpoint.class, caller.getClassName().equals(SqlListenerEndpoint.class.getName()));
        assertTrue("application method should be callSomeQuery", caller.getMethodName().equals("callSomeQuery"));
    }

    private void assertValueSet(String valueDescription, Optional<String> applicationName) {
        if (applicationName.isPresent()) {
            assertValueSet(valueDescription, applicationName.get());
        } else {
            throw new AssertionError(valueDescription + " has no value");
        }
    }

    private void assertValueSet(String valueDescription, String applicationName) {
        if (applicationName == null) {
            throw new AssertionError(valueDescription + " is null");
        }
        if (applicationName.isBlank()) {
            throw new AssertionError(valueDescription + " is blank");
        }
    }

    private void assertTrue(String valueDescription, boolean condition) {
        if (!condition) {
            throw new AssertionError(valueDescription + " is false");
        }
    }

}
