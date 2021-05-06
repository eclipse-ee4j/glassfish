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

import javax.xml.datatype.*;
import java.util.*;

public class DateTime {


    public static void main(String[] args) {

        String xsdDateTime = args[0];

        System.out.println("Converting xsdDateTime " + xsdDateTime + " ...");

        try {
            DatatypeFactory factory = DatatypeFactory.newInstance();

            XMLGregorianCalendar xmlGreg = factory.newXMLGregorianCalendar(xsdDateTime);

            GregorianCalendar greg = xmlGreg.toGregorianCalendar();

            Date date = greg.getTime();

            System.out.println("Date = " + date);

            GregorianCalendar reverseCalendar = new GregorianCalendar();
            reverseCalendar.setTime(date);


            XMLGregorianCalendar reverseXmlGreg =
                factory.newXMLGregorianCalendar(reverseCalendar);

            String reverseXsdDateTime = reverseXmlGreg.toXMLFormat();

            System.out.println("Back to xsdDateTime = " + reverseXsdDateTime);

        } catch(Exception e) {
            e.printStackTrace();
        }


    }

}
