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

package com.sun.s1peqe.webservices.ejb.taxcal.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.naming.*;
import javax.xml.rpc.Stub;


public class TaxCalClient {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter();
    private static String stateTaxEndpoint = null;
    private static String fedTaxEndpoint = null;
    private static String testSuite = "TaxCalEjbbasedWS";
    private static String testCase = null;
    private static TaxCalEjbService taxCalService = null;

    public static void main(String[] args) {

        boolean stateTaxIntendedResult=true;
        boolean fedTaxIntendedResult=true;

        if(args[0] == null || args[1] == null){
            System.out.println("TaxCal client: Argument missing. Please provide target" +
                    "endpoint address as argument");
            System.exit(1);
        } else {
            stateTaxEndpoint = args[0];
            fedTaxEndpoint = args[1];
            if (args.length>2) {
                stateTaxIntendedResult = (new Boolean(args[2])).booleanValue();
                fedTaxIntendedResult = (new Boolean(args[3])).booleanValue();
            }
        }

        stat.addDescription("This is to test ejb based webservice");
        try {
            TaxCalClient client = new TaxCalClient();
            Context ic = new InitialContext();
            taxCalService = (TaxCalEjbService)
            ic.lookup("java:comp/env/service/TaxCalEjbService");

            client.callStateTaxService(stateTaxIntendedResult);
            client.callFedTaxService(fedTaxIntendedResult);
        }catch(Exception e){
            e.printStackTrace();
        }

        stat.printSummary(testSuite);
    }

    public void callStateTaxService(boolean intendedResult) {
        double income = 85000.00;
        double deductions = 5000.00;

        //String targetEndpointAddress =
        //        "http://localhost:1024/taxcalculator";

        try {

            StateTaxIF taxCalIFPort = taxCalService.getStateTaxIFPort();

            ((Stub)taxCalIFPort)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                    stateTaxEndpoint);

            double stateTax = taxCalIFPort.getStateTax(income, deductions);
            System.out.println("State tax from ejb based TaxCalService :" + stateTax);

            if(stateTax == 24000.00 && intendedResult)
                stat.addStatus(testSuite + " StateTaxPort", stat.PASS);
            else {
                System.out.println("Call succeeded while it should have failed");
                stat.addStatus(testSuite + " StateTaxPort", stat.FAIL);
            }

        } catch (Exception ex) {
            if (intendedResult) {
                System.out.println("TaxCalEjbWebservice client failed");
                stat.addStatus(testSuite + " StateTaxPort", stat.FAIL);
                ex.printStackTrace();
            } else {
                System.out.println("Expected Exception caught :");
                System.out.println(ex.getMessage());
                ex.printStackTrace();
                stat.addStatus(testSuite + "StateTaxPort", stat.PASS);
            }
        }
    }

    public void callFedTaxService(boolean intendedResult) {
        double income = 97000.00;
        double deductions = 7000.00;
        try {

            FedTaxIF taxCalIFPort = taxCalService.getFedTaxIFPort();
            ((Stub)taxCalIFPort)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                    fedTaxEndpoint);

            double fedTax = taxCalIFPort.getFedTax(income, deductions);
            System.out.println("Fed tax from Ejb based TaxCalService :" + fedTax);

            if(fedTax == 18000.00 && intendedResult)
                stat.addStatus(testSuite + " FedTaxPort", stat.PASS);
            else {
                System.out.println("Call succeeded while it should have failed");
                stat.addStatus(testSuite + " FedTaxPort", stat.FAIL);
            }
        } catch (Exception ex) {
            if (intendedResult) {
                System.out.println("TaxCalEjbWebService client failed");
                stat.addStatus(testSuite + " FedTaxPort", stat.FAIL);
                ex.printStackTrace();
            } else {
                System.out.println("Expected Exception caught :");
                System.out.println(ex.getMessage());
                stat.addStatus(testSuite + "FedTaxPort", stat.PASS);
            }
        }
    }
}

