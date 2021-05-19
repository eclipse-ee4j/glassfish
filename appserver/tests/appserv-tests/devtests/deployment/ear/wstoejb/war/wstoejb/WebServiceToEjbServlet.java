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

package wstoejb;

import java.rmi.*;
import javax.rmi.*;
import javax.naming.*;


 /**
  *  Simple servlet implementation of the SEI
  *
  * @author Jerome Dochez
  */
 public class WebServiceToEjbServlet implements WebServiceToEjbSEI {

         /**
          *  Implementation of the SEI's methods
          */
         public String payload(String requestInfo) throws RemoteException {
                 try {
                         if (bean==null) {
                                 bean = createEJB();
                         }
                         return(getMsg(requestInfo) + "; and dont bother this dummy servlet also");
                 } catch(Exception e) {
                         throw new RemoteException(e.getMessage());
                 }
         }

        /**
         * Creates the ejb object from it's home interface
         */
        private StatefulSessionBean createEJB() throws Exception {
                // connect to the EJB
                Context ctxt = new InitialContext();
                java.lang.Object objref = ctxt.lookup("java:comp/env/MyEjbReference");
                StatefulSessionBeanHome homeIntf = (StatefulSessionBeanHome) PortableRemoteObject.narrow(objref, StatefulSessionBeanHome.class);
                return homeIntf.create();
        }

        public String getMsg(String info) throws Exception {
                if (bean == null) {
                        return "could not talk to the EJB : java:comp/env/MyEjbReference";
                } else {
                        return bean.payLoad(info);
                }
        }

         private StatefulSessionBean bean;
 }
