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

package org.glassfish.enterprise.iiop.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.Stats;

/** Provides for generic implementation of any Stats interface. This class facilitates
 * composition over inheritance for all the classes that implement their
 * specific Stats interfaces. None of them has to implement the methods defined
 * by the {@link javax.management.j2ee.statistics.Stats} interface. This class
 * implements the same interface and does that job. All that implementing classes
 * have to do is implement the specific accessing methods in their Stats interfaces
 * and delegate the rest to this class. <b> This class invokes all these methods in
 * implementing class through introspection. </b>
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version $Version$
 */
public class GenericStatsImpl implements Stats {

    private final Class     statsInterface;
    private final Object    statsProvider;
    /** A map with binding of a String XXX to a method with name <b>get</b>XXX */
    private final Map       getters;

    /**
     */
    public GenericStatsImpl(String statsInterfaceName, Object statsProvider)
    throws ClassNotFoundException {
        this(statsInterfaceName, GenericStatsImpl.class.getClassLoader(), statsProvider);
    }

    /**
     */
    public GenericStatsImpl(String statsInterfaceName, ClassLoader loader,
    Object statsProvider) throws ClassNotFoundException {
        this(Class.forName(statsInterfaceName, true, loader), statsProvider);
    }

    /** Constructs a new instance of this class for a given interface and its implementation.
     * It is mandatory that following contract is satisfied to call this satisfactorily:
     * <ul>
     *  <li> None of the parameters are null. </li>
     *  <li> Given statsProvider implements the given statsInterface. </li>
     *  <li> Given statsInterface has to extend the @{link Stats} interface. </li>
     * </ul>
     * Note that it is expected (though not mandatory) to have a getXXX method that
     * does not return an instance of {@link Statistic} interface.
     * @throws NullPointerException     if any of the given parameters are null
     * @throws IllegalArgumentException if the contract is not satisfied by given parameters
     */
    public GenericStatsImpl(Class statsInterface, Object statsProvider) {
        if (!implementsInterface(statsInterface, statsProvider) ||
           ! extendsStatsInterface(statsInterface)) {
            throw new IllegalArgumentException("Contract violation: invalid interface-implementation pair");
        }
        this.statsProvider  = statsProvider;
        this.statsInterface = statsInterface;
        this.getters        = new HashMap();
        populateGetterMap();
    }

    public Statistic getStatistic(String statisticName) {
        final Method getter = (Method) getters.get(statisticName);
        assert (getter != null) : ("Getter not initialized properly: " + statisticName);
        Object result = null;
        try {
            result = getter.invoke(statsProvider);
        }
        catch(Exception e) {
            final RuntimeException oe = new IllegalStateException();
            oe.initCause(e);
            throw oe;
        }
        return ( (Statistic)result );
    }

    public String[] getStatisticNames() {
        /* The return array is fixed at the construction time */
        final String[] names = new String[getters.size()];
        return ( (String[])getters.keySet().toArray(names) ); //TODOOOOOOO
    }

    public Statistic[] getStatistics() {
        return ( getStatisticsOneByOne() );         //invokes sequentially
    }

    private Statistic[] getStatisticsOneByOne() {
        final Iterator iter     = getters.keySet().iterator();
        final Statistic[] stats = new Statistic[getters.keySet().size()];
        int i = 0;
        while (iter.hasNext()) {
            final String sn = (String) iter.next();
            stats[i++] = this.getStatistic(sn);
        }
        assert (stats.length == i);
        return ( stats );
    }

    private boolean implementsInterface(Class c, Object o) {
        boolean impls = false;
        final Class[] interfaces = o.getClass().getInterfaces();
        for (int i = 0 ; i < interfaces.length ; i++) {
            if (interfaces[i].equals(c)){
                impls = true;
                break;
            }
        }
        return ( impls );
    }

    private boolean extendsStatsInterface(Class i) {
        final Class statInterface = org.glassfish.external.statistics.Stats.class;
        return ( statInterface.isAssignableFrom(i) );
    }

    private void populateGetterMap() {
        // Fix for Bugs 5045435, 6172088
        //final Method[] apis     = statsInterface.getDeclaredMethods(); //all of these should be PUBLIC.
        final Method[] m = statsInterface.getMethods();
        // exclude methods that belong to the javax.management.j2ee.Stats
        final Method[] apis     = filterStatsMethods(m);
        final Method[] methods  = getGetters(apis);
        final String[] names    = methods2Statistics(methods);
        assert (names.length == methods.length) : ("Statistic names array is not having same length as that of array of getters");
        int i;
        for (i = 0 ; i < names.length ; i++) {
            getters.put(names[i], methods[i]);
        }
        assert (getters.size() == i) : ("Getters map is incorrect, names.length = " + names.length + " methods.length = " + methods.length);
    }

    private Method[] getGetters(Method[] all) {
        final ArrayList l = new ArrayList();
        for (int i = 0 ; i < all.length ; i++) {
            final Method am = all[i];
            if (isValidGetter(am)) {
                l.add(am);
            }
        }
        final Method[] m = new Method[l.size()];
        return ( (Method[])l.toArray(m) );
    }

    private boolean isValidGetter(Method m) {
        final boolean startsWithGet     = m.getName().startsWith("get");
        final boolean hasNoParams       = m.getParameterTypes().length == 0;
        final boolean returnsStatistic  = Statistic.class.isAssignableFrom(m.getReturnType());

        return ( startsWithGet && hasNoParams && returnsStatistic );
    }

    private String[] methods2Statistics(Method[] methods) {
        final String[] names = new String[methods.length];
        for (int i = 0 ; i < methods.length ; i++) {
            final String    m = methods[i].getName();
            final int       s = "get".length();
            names[i] = m.substring(s);
        }
        return ( names );
    }

    private boolean isStatsInterfaceMethod(String name) {
        final Method[] methods = org.glassfish.external.statistics.Stats.class.getMethods();
        boolean isInterfaceMethod = false;
        for (int i = 0 ; i < methods.length ; i++) {
            if (methods[i].getName().equals(name)) {
                isInterfaceMethod = true;
                break;
            }
        }
        return ( isInterfaceMethod );
    }

    private Method[] filterStatsMethods(Method[] m) {
        ArrayList methodList = new ArrayList();
        for(int i = 0; i < m.length; i++) {
            if(! isStatsInterfaceMethod(m[i].getName()))
                methodList.add(m[i]);
        }
        final Method[] methods = new Method[methodList.size()];
        return (Method[])methodList.toArray(methods);
    }
}
