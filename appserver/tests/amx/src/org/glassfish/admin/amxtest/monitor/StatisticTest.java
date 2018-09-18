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

package org.glassfish.admin.amxtest.monitor;

import com.sun.appserv.management.j2ee.statistics.*;
import com.sun.appserv.management.util.j2ee.J2EEUtil;
import com.sun.appserv.management.util.misc.TypeCast;
import org.glassfish.admin.amxtest.AMXTestBase;

import org.glassfish.j2ee.statistics.Statistic;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.Set;


public final class StatisticTest
        extends AMXTestBase {
    public StatisticTest() {
    }

    private void
    checkOpenDataConversion(final Statistic s)
            throws OpenDataException {
        final CompositeData d = J2EEUtil.statisticToCompositeData(s);
        final Statistic roundTrip = StatisticFactory.create(d);

        assert (s.equals(roundTrip)) :
                "Conversion to CompositeData and back to Statistic failed:\n" +
                        toString(s) + " != " + toString(roundTrip);
    }

    public static final class TestStatistic
            extends StatisticImpl {
        public static final long serialVersionUID = 9999999;

        private final int Foo;
        private final String Bar;

        public TestStatistic() {
            super("Test", "test dummy", "none", 0, System.currentTimeMillis());

            Foo = 999;
            Bar = "Bar";
        }

        public int getFoo() { return (Foo); }

        public String getBar() { return (Bar); }
    }

    public void
    testAnyOpenDataConversion()
            throws OpenDataException {
        // verify that anything implementing Statistic works correctly
        final TestStatistic test = new TestStatistic();
        final MapStatisticImpl testMap = new MapStatisticImpl(test);
        assert (testMap.getValue("Foo").equals(new Integer(test.getFoo())));
        assert (testMap.getValue("Bar").equals(test.getBar()));

        final CompositeData d = J2EEUtil.statisticToCompositeData(testMap);
        final CompositeType t = d.getCompositeType();
        final Set<String> values = TypeCast.asSet(t.keySet());
        assert (values.contains("Name"));
        assert (values.contains("Foo"));
        assert (values.contains("Bar"));

        final MapStatisticImpl roundTrip = (MapStatisticImpl) StatisticFactory.create(d);
        assert (new Integer(test.getFoo()).equals(roundTrip.getValue("Foo")));
        assert (test.getBar().equals(roundTrip.getValue("Bar")));
    }

    public void
    testStdOpenDataConversion()
            throws OpenDataException {
        final CountStatisticImpl c =
                new CountStatisticImpl("Count", "desc", "number", 0, now(), 99);

        final RangeStatisticImpl r =
                new RangeStatisticImpl("Range", "desc", "number", 0, now(), 0, 50, 100);

        final BoundaryStatisticImpl b =
                new BoundaryStatisticImpl("Boundary", "desc", "number", 0, now(), 0, 100);

        final BoundedRangeStatisticImpl br =
                new BoundedRangeStatisticImpl("BoundedRange", "desc", "number", 0, now(), 0, 50, 100, 0, 100);

        final TimeStatisticImpl t =
                new TimeStatisticImpl("Time", "desc", "seconds", 0, now(), 1, 10, 100, 1000);

        final StringStatisticImpl s =
                new StringStatisticImpl("String", "desc", "chars", 0, now(), "hello");

        final NumberStatisticImpl n =
                new NumberStatisticImpl("Number", "desc", "number", 0, now(), 1234.56);

        checkOpenDataConversion(c);
        checkOpenDataConversion(r);
        checkOpenDataConversion(b);
        checkOpenDataConversion(br);
        checkOpenDataConversion(t);
        checkOpenDataConversion(s);
        checkOpenDataConversion(n);
    }

}






