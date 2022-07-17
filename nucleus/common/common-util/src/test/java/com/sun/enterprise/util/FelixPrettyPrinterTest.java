/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author David Matejcek
 */
public class FelixPrettyPrinterTest {

    @Test
    public void test1() {
        String src = "org.osgi.framework.BundleException:"
            + " Unable to resolve org.glassfish.main.webservices.connector [207](R 207.0):"
            + " missing requirement [org.glassfish.main.webservices.connector [207](R 207.0)] osgi.wiring.package;"
            + " (&(osgi.wiring.package=jakarta.xml.ws)(version>=3.0.0)(!(version>=4.0.0))) [caused by:"
            + " Unable to resolve org.glassfish.metro.webservices-api-osgi [236](R 236.0):"
            + " missing requirement [org.glassfish.metro.webservices-api-osgi [236](R 236.0)] osgi.wiring.package;"
            + " (&(osgi.wiring.package=jakarta.xml.bind)(version>=3.0.0)(!(version>=4.0.0)))]"
            + " Unresolved requirements: [[org.glassfish.main.webservices.connector [207](R 207.0)] osgi.wiring.package;"
            + " (&(osgi.wiring.package=jakarta.xml.ws)(version>=3.0.0)(!(version>=4.0.0)))]";
        String message = FelixPrettyPrinter.prettyPrintExceptionMessage(src);
        assertThat(message,
            stringContainsInOrder("Unable to resolve\n", "org.glassfish.main.webservices.connector [207]\n",
                "missing requirement\n", "&(package = jakarta.xml.ws) (version >= 3.0.0) (!(version >= 4.0.0))\n",
                "caused by:\n", "Unable to resolve\n", "org.glassfish.metro.webservices-api-osgi [236]\n",
                "missing requirement\n", "&(package = jakarta.xml.bind) (version >= 3.0.0) (!(version >= 4.0.0)))]\n"));

        assertAll(
            () -> assertThat(FelixPrettyPrinter.findBundleIds(message), contains(207, 236)),
            () -> assertThat(FelixPrettyPrinter.findBundleIds(src), contains(207, 236))
        );
    }

    @Test
    public void test2() {
        String src = FelixPrettyPrinter.prettyPrintExceptionMessage("  Unable to resolve"
            + " org.apache.felix.scr [304](R 304.0):"
            + " missing requirement [org.apache.felix.scr [304](R 304.0)] osgi.wiring.package;"
            + " (&(osgi.wiring.package=org.osgi.framework)(version>=1.10.0)(!(version>=2.0.0)))"
            + " Unresolved requirements: [[org.apache.felix.scr [304](R304.0)] osgi.wiring.package;"
            + " (&(osgi.wiring.package=org.osgi.framework)(version>=1.10.0)(!(version>=2.0.0)))]\n"
            + "at org.apache.felix.framework.Felix.resolveBundleRevision(Felix.java:4398) ");
        String message = FelixPrettyPrinter.prettyPrintExceptionMessage(src);
        assertThat(message,
            stringContainsInOrder("Unable to resolve\n", "org.apache.felix.scr [304]\n", "missing requirement\n"));

        List<Integer> ids = FelixPrettyPrinter.findBundleIds(message);
        assertThat(ids, contains(304));
    }
}
