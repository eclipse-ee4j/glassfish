/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import javax.naming.*;
import jakarta.jms.*;
import com.sun.ejte.ccl.reporter.*;
import test.*;
import org.glassfish.test.jms.injection.ejb.*;

/*
 * Unit test for resource injection into servlet filter.
 */
public class WsTest {

    private static final String TEST_NAME = "jms-injection-ws(TransactionScoped)";
    private static final String EXPECTED_RESPONSE = "JSP Hello World!";

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private static String transactionScope = "around TransactionScoped";

    public WsTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for resource injection into webservice "
                            + "filter"+"(TransactionScoped)");
        WsTest wsTest = new WsTest(args);
        try {
            wsTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary();
    }

    public void doTest() throws Exception {
        String text = "WebService Hello World!";
        NewWebService_Service service = new NewWebService_Service_Impl();
        NewWebService_PortType stub = service.getNewWebServicePort();
        if(stub.hello(text).indexOf(transactionScope) == -1)
            throw new Exception("NOT in transactionScope scope!");

        Context ctx = new InitialContext();
        MessageReceiverRemote beanRemote = (MessageReceiverRemote) ctx.lookup(MessageReceiverRemote.RemoteJNDIName);
        boolean received = beanRemote.checkMessage(text);
        if (!received)
            throw new Exception("JMS Message Not Received!");
    }
}
