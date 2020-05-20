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

package ejb32.intrfaces;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateful;

/*
   StflEJB exposes remote interface St7. St5 isn't business interface.
*/
@Stateful
public class StflEJB implements St5, St7 {
    @EJB(lookup = "java:module/StlesEJB1!ejb32.intrfaces.St6")
    St6 st6;
    @EJB(lookup = "java:app/ejb32-intrfaces-ejb2/StlesEJB1!ejb32.intrfaces.StlesEJB1")
    StlesEJB1 stlesEJB1;
    @EJB(lookup = "java:global/ejb32-intrfacesApp/ejb32-intrfaces-ejb2/StlesEJB1!ejb32.intrfaces.St7")
    St7 st7;

    @Resource
    SessionContext ctx;

    @Override
    public String st5() throws Exception {
        return st6.st6() + "." + "StflEJB.st5";
    }

    // expectation: StlesEJB1.st6.StlesEJB1.st7.StflEJB.st7
    @Override
    public String st7() throws Exception {
        try {
            ctx.lookup("java:module/StlesEJB1!ejb32.intrfaces.St5");
        } catch (Exception e) {
            e.printStackTrace();
            // St5 isn't business interface of StlesEJB1
            return stlesEJB1.st6() + "." + st7.st7() + "." + "StflEJB.st7";
        }
        throw new IllegalStateException("Error occurred for StflEJB!");
    }
}
