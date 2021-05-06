/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package googleejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.ejb.EJBException;
import javax.naming.*;
import javax.xml.rpc.handler.MessageContext;
import java.security.Principal;

public class GoogleEJB implements SessionBean {
    private SessionContext sc;

    public GoogleEJB(){}

    public void ejbCreate() throws RemoteException {
        System.out.println("In GoogleEJB::ejbCreate !!");
    }

    public byte[] doGetCachedPage(java.lang.String key, java.lang.String url)
    {
        return null;
    }

    public String doSpellingSuggestion(java.lang.String key,
                                       java.lang.String phrase)
    {

        try {
            Principal p = sc.getCallerPrincipal();
            if( p != null ) {
                System.out.println("getCallerPrincipal() was successful");
            } else {
                throw new EJBException("getCallerPrincipal() returned null");
            }
        } catch(Exception e) {
            EJBException ejbEx = new EJBException("getCallerPrincipal exception");
            ejbEx.initCause(e);
            throw ejbEx;
        }

        MessageContext msgContext = sc.getMessageContext();
        System.out.println("msgContext = " + msgContext);

        System.out.println("GoogleEJB.doSpellingSuggestion() called with " +
                           phrase);

        String returnValue = phrase + "spelling suggestion";

        System.out.println("GoogleEJB returning " + returnValue);

        return returnValue;
    }

    public GoogleSearchResult doGoogleSearch(java.lang.String key, java.lang.String q, int start, int maxResults, boolean filter, java.lang.String restrict, boolean safeSearch, java.lang.String lr, java.lang.String ie, java.lang.String oe) {
        return null;
    }

    public void setSessionContext(SessionContext sc) {

        this.sc = sc;
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

}
