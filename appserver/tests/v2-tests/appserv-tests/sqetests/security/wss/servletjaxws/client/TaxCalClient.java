/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.sqe.security.wss.annotations.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.appserv.sqe.security.taxws.Tax;
import com.sun.appserv.sqe.security.taxws.TaxService;
import jakarta.xml.ws.WebServiceRef;
import javax.xml.rpc.Stub;

/**
 * This is AppClient program that access StateTaxEJB and FedTax EJB Webservices.
 * It expects StateTax webservice endpoint and FedTax endpoint URLs.
 * This client is accessed in the ant run target after configuring the webservices
 * message security at system level or applevel.
 *
 * @version 1.1  05 Aug 2005
 * @author Jagadesh Munta
 */

public class TaxCalClient {
    /*
     * Tests the getStateTax and getFedTax with expected values. If value
     * matched,then test PASSED else FAILED.
     */
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter();
    // J2EE simple reporter for logging the test status.
    private static String taxEndpoint = null;
    private static String testSuite = "sec-wss-annotate-servletendpoint";
    private static String testCase = null;
    @WebServiceRef(wsdlLocation="http://localhost:8080/wss-tax-web/wss/TaxService?wsdl")
    static TaxService service;

    public static void main (String[] args) {

        if(args.length<1){
            System.out.println("TaxCal client: Argument missing."+
                " Please provide target" +
                "endpoint address as argument");
            System.exit(1);
        } else {
            taxEndpoint = args[0];
        }

        stat.addDescription("Security-WSS-ejb webservice");
        try {
            TaxCalClient client = new TaxCalClient();
            client.callTaxService();
        }catch(Exception e){
            e.printStackTrace();
        }

        stat.printSummary(testSuite);
    }


    public void callTaxService() {
        double income = 97000.00;
        double deductions = 7000.00;
        double expectedTax = 18000.00;
        String testStatus = "fail";

        try {
            if (service!=null) {
                Tax port = service.getTaxPort();
                //((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                //        taxEndpoint);

                double fedTax = port.getFedTax(income, deductions);
                System.out.println("Fed tax from annotations based TaxCalService endpoint:" +
                    fedTax);

                if(fedTax == expectedTax) {
                    testStatus = stat.PASS;
                } else {
                    testStatus = stat.FAIL;
                }
            }else {
                System.out.println("Error: Not able to get the service and is null!");
                testStatus = stat.FAIL;
            }

        } catch (Exception ex) {
            System.out.println("TaxCal client failed");
            ex.printStackTrace();
            testStatus = stat.FAIL;
        } finally {
            stat.addStatus(testSuite+"-getFedTax" , testStatus);
        }
    }
}

