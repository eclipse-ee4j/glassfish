/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package samples.ejb.stateless.simple.ejb;

import java.util.*;
import java.io.*;

/**
 * A simple stateless bean for the HelloWorld application. This bean implements one
 * business method as declared by the remote interface.
 */
public class GreeterEJB implements jakarta.ejb.SessionBean {

    private  jakarta.ejb.SessionContext m_ctx = null;

    /**
     * Sets the session context. Required by EJB spec.
     * @param ctx A SessionContext object.
     */
    public void setSessionContext(jakarta.ejb.SessionContext ctx) {
        m_ctx = ctx;
    }

    /**
     * Creates a bean. Required by EJB spec.
     * @exception throws CreateException.
     */
    public void ejbCreate() throws jakarta.ejb.EJBException, jakarta.ejb.CreateException {
        System.out.println("ejbCreate() on obj " + this);
    }

    /**
     * Removes the bean. Required by EJB spec.
     */
    public void ejbRemove() {
        System.out.println("ejbRemove() on obj " + this);
    }

    /**
     * Loads the state of the bean from secondary storage. Required by EJB spec.
     */
    public void ejbActivate() {
        System.out.println("ejbActivate() on obj " + this);
    }

    /**
     * Serializes the state of the bean to secondary storage. Required by EJB spec.
     */
    public void ejbPassivate() {
        System.out.println("ejbPassivate() on obj " + this);
    }

    /**
     * Required by EJB spec.
     */
    public void Greeter() {
    }


    /**
     * Returns a greeting, based on the time of the day.
     * @return returns a greeting as a string.
     */
    public String getGreeting() {
        String message = null;
        Calendar calendar = new GregorianCalendar();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        if(currentHour < 12) message = "morning";
        else {
          if( (currentHour >= 12) &&
            (calendar.get(Calendar.HOUR_OF_DAY) < 18)) message = "afternoon";
          else message = "evening";
        }
        return message;
    }
}
