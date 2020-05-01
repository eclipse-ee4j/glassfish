/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import jakarta.mail.MailSessionDefinition;
import jakarta.mail.MailSessionDefinitions;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;


@MailSessionDefinitions(
        value = {
                @MailSessionDefinition(description = "Mail Session Description 1",
                        name = "java:global/env/HelloEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 1",
                        name = "java:comp/env/HelloEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 1",
                        name = "java:global/mail/HelloEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),

                @MailSessionDefinition(description = "Mail Session Description 2",
                        name = "java:comp/mail/HelloEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 3",
                        name = "java:app/mail/HelloEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 4",
                        name = "java:module/mail/HelloEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 4",
                        name = "java:module/env/HelloEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 4",
                        name = "java:global/env/HelloStatefulEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                ),
                @MailSessionDefinition(description = "Mail Session Description 4",
                        name = "java:app/env/HelloStatefulEJB_MailSession",
                        storeProtocol = "IMAP",
                        transportProtocol = "SMTP",
                        host = "localhost",
                        user = "naman",
                        password = "naman",
                        from = "naman.mehta@oracle.com",
                        properties = {"property1=10;property2=20"}
                )
        }
)

@Stateless
public class HelloEJB implements Hello {

    public void hello() {

        boolean global = lookupMailSession("java:global/env/HelloEJB_MailSession", true);

        boolean comp = lookupMailSession("java:comp/env/HelloEJB_MailSession", true);

        boolean moduleHelloEjb = lookupMailSession("java:module/env/HelloEJB_MailSession", true);

        boolean globalHelloStatefulEJB = lookupMailSession("java:global/env/HelloStatefulEJB_MailSession", true);

        boolean compHelloStatefulEJB = lookupMailSession("java:comp/env/HelloStatefulEJB_MailSession", false);

        boolean appHelloStatefulEjb = lookupMailSession("java:app/env/HelloStatefulEJB_MailSession", true);

        boolean globalServlet = lookupMailSession("java:global/env/Servlet_MailSession", true);

        boolean compServlet = lookupMailSession("java:comp/env/Servlet_MailSession", false);

        boolean appServlet = lookupMailSession("java:app/env/Servlet_MailSession", true);

        boolean moduleServlet = lookupMailSession("java:module/env/Servlet_MailSession", false);

        boolean globalServlet_MS_MailSession = lookupMailSession("java:global/env/Servlet_MS_MailSession", true);

        boolean compServlet_MS_MailSession = lookupMailSession("java:comp/env/Servlet_MS_MailSession", false);

        boolean globalHelloStateful_MS_MailSession = lookupMailSession("java:global/env/HelloStatefulEJB_MS_MailSession", true);

        boolean compHelloStateful_MS_MailSession = lookupMailSession("java:comp/env/HelloStatefulEJB_MS_MailSession", false);

        boolean globalHello_MS_MailSession = lookupMailSession("java:global/env/HelloEJB_MS_MailSession", true);

        boolean compHello_MS_MailSession = lookupMailSession("java:comp/env/HelloEJB_MS_MailSession", false);

        if (global && comp && globalHelloStatefulEJB && !compHelloStatefulEJB && globalServlet
                && !compServlet && appServlet && globalServlet_MS_MailSession && !compServlet_MS_MailSession
                && globalHelloStateful_MS_MailSession && !compHelloStateful_MS_MailSession &&
                globalHello_MS_MailSession && compHello_MS_MailSession && appHelloStatefulEjb &&
                moduleHelloEjb && !moduleServlet) {
            System.out.println("HelloEJB successful mail-session lookup !");
        } else {
            System.out.println("HelloEJB mail-session lookup failure");
            throw new RuntimeException("HelloEJB failure");
        }


        System.out.println("In HelloEJB::hello()");
    }

    private boolean lookupMailSession(String mailSessionName, boolean expectSuccess) {
        try {
            InitialContext ic = new InitialContext();
            Object ds = ic.lookup(mailSessionName);
            return true;
        } catch (Exception e) {
            if (expectSuccess) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
