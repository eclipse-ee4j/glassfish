/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.DottedNames;
import org.glassfish.admin.amxtest.AMXTestBase;

import javax.management.Attribute;

/**
 */
public final class DottedNamesTest
        extends AMXTestBase {
    public DottedNamesTest() {
    }

    private void
    checkAttribute(final Attribute attr) {
        assert (attr != null);

        final Object value = attr.getValue();
        if (value instanceof Attribute) {
            warning("Is value of " + attr.getName() + " really another Attribute? => " +
                    toString(value));
        }
    }

    private void
    checkResultsFromWildGet(
            final Object[] results) {
        for (int i = 0; i < results.length; ++i) {
            final Object result = results[i];

            if (result == null) {
                warning("null result from dottedNameGet( \"*\" )");
            } else if (!(result instanceof Attribute)) {
                warning("non-Attribute result from dottedNameGet( \"*\" ): " + result);
            } else {
                // it's an Attribute
                final Attribute attr = (Attribute) result;
                checkAttribute((Attribute) result);
            }
        }
    }

    private void
    checkResultsFromGet(
            final String[] names,
            final Object[] results) {
        for (int i = 0; i < results.length; ++i) {
            final Object result = results[i];

            if (result == null) {
                warning("Dotted name has null result: " + names[i]);
            } else if (!(result instanceof Attribute)) {
                warning("Dotted name " + names[i] + " could not be obtained: " + result);
            }
        }
    }

    private String[]
    getAllNames(final DottedNames dottedNames) {
        final Attribute[] attrs = (Attribute[]) dottedNames.dottedNameGet("*");
        final String[] names = new String[attrs.length];
        for (int i = 0; i < names.length; ++i) {
            names[i] = attrs[i].getName();
        }

        return (names);
    }


    public void
    testGetAllConfigDottedNames() {
        final long start = now();
        final DottedNames dottedNames = getDomainRoot().getDottedNames();

        final String[] names = getAllNames(dottedNames);

        final Object[] results = dottedNames.dottedNameGet(names);

        checkResultsFromGet(names, results);
        printElapsed("testGetAllConfigDottedNames", start);
    }

/*    public void
    testGetAllMonitoringDottedNames() {
        if (checkNotOffline("testMonitoringRefresh")) {
            final MonitoringDottedNames dottedNames = getDomainRoot().getMonitoringDottedNames();
            final long start = now();
            final String[] names = getAllNames(dottedNames);

            final Object[] results = dottedNames.dottedNameGet(names);

            checkResultsFromGet(names, results);
            printElapsed("testGetAllMonitoringDottedNames", start);
        }
    }*/

    public void
    testWildGetAllDottedNames() {
        final long start = now();
        final DottedNames dottedNames = getDomainRoot().getDottedNames();

        final Attribute[] results = (Attribute[]) dottedNames.dottedNameGet("*");
        checkResultsFromWildGet(results);
        printElapsed("testWildGetAllConfigDottedNames", start);
    }
/*


    public void
    testWildGetAllMonitoringDottedNames() {
        if (checkNotOffline("testMonitoringRefresh")) {
            final long start = now();
            final MonitoringDottedNames dottedNames = getDomainRoot().getMonitoringDottedNames();
            final Attribute[] results = (Attribute[]) dottedNames.dottedNameGet("*");
            checkResultsFromWildGet(results);
            printElapsed("testWildGetAllMonitoringDottedNames", start);
        }
    }
*/

    /**
     Test that we can set (change) a dotted name.
     */
    public void
    testDottedNameSet() {
        final long start = now();

        final DottedNames dottedNames = getDomainRoot().getDottedNames();

        final String target = "domain.locale";
        final Object result = dottedNames.dottedNameGet(target);

        final Attribute localeAttr = (Attribute) dottedNames.dottedNameGet(target);
        checkAttribute(localeAttr);

        final String locale = (String) localeAttr.getValue();

        // set to a new value
        Object[] results = dottedNames.dottedNameSet(new String[]{target + "=dummy_locale"});
        assert (results.length == 1);
        checkAttribute((Attribute) results[0]);

        // change back to previous value
        final String restoreString = target + "=" + (locale == null ? "" : locale);
        results = dottedNames.dottedNameSet(new String[]{restoreString});

        final Attribute finalAttr = (Attribute) dottedNames.dottedNameGet(target);
        assert (
                (finalAttr.getValue() == null && localeAttr.getValue() == null) ||
                        finalAttr.getValue().equals(localeAttr.getValue()));
        printElapsed("testConfigDottedNameSet", start);
    }

    private int
    testList(
            final DottedNames dottedNames,
            final String dottedName) {
        final Object[] results = dottedNames.dottedNameList(new String[]{dottedName});

        //trace( dottedName + ": " + toString( results ) );
        for (int i = 0; i < results.length; ++i) {
            testList(dottedNames, (String) results[i]);
        }

        return (results.length);
    }

        public void
    testRecursiveDottedNameList() {
        final long start = now();
        final DottedNames dottedNames = getDomainRoot().getDottedNames();

        final int numFound = testList(dottedNames, "domain");
        assert (numFound >= 4);    // should be at least 4.
        printElapsed("testRecursiveConfigDottedNameList", start);
    }

    /*

    public void
    testRecursiveMonitoringDottedNameList() {
        if (checkNotOffline("testRecursiveMonitoringDottedNameList")) {
            final MonitoringDottedNames dottedNames = getDomainRoot().getMonitoringDottedNames();

            final long start = now();

            final int numFound = testList(dottedNames, "server");
            assert (numFound >= 4);    // should be at least 4.\

            testList(dottedNames, "*");

            printElapsed("testRecursiveMonitoringDottedNameList", start);
        }
    }*/
}











