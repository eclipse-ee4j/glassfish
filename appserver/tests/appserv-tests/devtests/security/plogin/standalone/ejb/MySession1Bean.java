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

import jakarta.ejb.*;

/**
 * This is the bean class for the MySession1Bean enterprise bean.
 */
public class MySession1Bean implements SessionBean, MySession1RemoteBusiness {
    private SessionContext context;

    /**
     * @see jakarta.ejb.SessionBean#setSessionContext(jakarta.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext aContext) {
        context = aContext;
    }

    /**
     * @see jakarta.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {

    }

    /**
     * @see jakarta.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {

    }

    /**
     * @see jakarta.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {

    }
    // </editor-fold>

    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        // TODO implement ejbCreate if necessary, acquire resources
        // This method has access to the JNDI context so resource aquisition
        // spanning all methods can be performed here such as home interfaces
        // and data sources.
    }



    public String businessMethod(String name) {
        return "hello " + name;
    }

    public String businessMethod2(String name) {
        return "hey " + name;
    }

    public String businessMethod3(String name) {
        return "howdy "+name;
    }



}
