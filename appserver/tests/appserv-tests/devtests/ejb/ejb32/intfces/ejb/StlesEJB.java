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
import jakarta.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;

/*
    StlesEJB exposes remote interface St3 and St4. St5 isn't business interface
 */
@Remote({St3.class, St4.class})
@Stateless
public class StlesEJB implements St3, St4, St5 {
    @EJB(lookup = "java:module/SingletonBean1!ejb32.intrfaces.St6")
    St6 st6;

    @EJB(lookup = "java:app/ejb32-intrfaces-ejb2/StflEJB2!ejb32.intrfaces.St4")
    St4 st4_stflEJB2;
    @EJB(lookup = "java:module/StflEJB2!ejb32.intrfaces.St6")
    St6 st6_stflEJB2;

    @Resource
    SessionContext ctx;

    // expectation: SingletonBean1.st6.StlesEJB.st3
    public String st3() throws Exception {
        try {
            ctx.lookup("java:module/SingletonBean1!ejb32.intrfaces.St5");
        } catch (Exception e) {
            e.printStackTrace();
            return st6.st6() + "." + "StlesEJB.st3";
        }
        throw new IllegalStateException("Error occurred for SingletonBean1!");
    }

    // expectation: StflEJB2.st4.StflEJB2.st6.StlesEJB.st4
    public String st4() throws Exception {
        try {
            ctx.lookup("java:module/StflEJB2!ejb32.intrfaces.St5");
        } catch (Exception e) {
            e.printStackTrace();
            return st4_stflEJB2.st4() + "." + st6_stflEJB2.st6() + "." + "StlesEJB.st4";
        }
        throw new IllegalStateException("Error occurred for StflEJB2!");
    }


    @Override
    public String st5() throws Exception {
        return "StlesEJB.st5";
    }
}
