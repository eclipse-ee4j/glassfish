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

package google;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.xml.rpc.Stub;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String googleKey;

    public static void main (String[] args) {
        stat.addDescription("webservices-google");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-googleID");
    }

    public void doTest(String[] args) {
        String word = (args.length == 0) ?
            "spellng" : args[0];
        String targetEndpointAddress = (args.length == 2) ?
            args[1] : "http://api.google.com/search/beta2";

            try {
            Context ic = new InitialContext();


            String googleKey = (String) ic.lookup("java:comp/env/googlekey");
            GoogleSearchService googleSearchService =
                (GoogleSearchService) ic.lookup("java:comp/env/service/GoogleSearch");
            GoogleSearchPort googlePort =
                googleSearchService.getGoogleSearchPort();

            ((Stub)googlePort)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                                            targetEndpointAddress);
            System.out.println("Contacting google for a spelling suggestion at " + targetEndpointAddress);
            String spellingSuggestion =
                googlePort.doSpellingSuggestion(googleKey, word);
            System.out.println("Gave google the word '" + word + "' ... " +
                               " and the suggested spelling is '" +
                               spellingSuggestion + "'");

            stat.addStatus("googleclient main", stat.PASS);

            } catch (Exception ex) {
            System.out.println("google client test failed");
            ex.printStackTrace();
            stat.addStatus("googleclient main" , stat.FAIL);
            //System.exit(15);
        }
    }
}
