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

package com.sun.jdo.api.persistence.enhancer.util;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;


/**
 * Utility class for simple performance analysis.
 */
//@olsen: added class
public final class Timer {
    // a method's timing descriptor
    static private class MethodDescriptor {
        final String name;
        int instantiations;
        int calls;
        long self;
        long total;

        MethodDescriptor(String name) {
            this.name = name;
        }
    }

    // a method call's timing descriptor
    static private class MethodCall {
        final MethodDescriptor method;
        final String message;
        long self;
        long total;

        MethodCall(MethodDescriptor method,
            String message,
            long self,
            long total) {
            this.method = method;
            this.message = message;
            this.self = self;
            this.total = total;
        }
    }

    // output device
    PrintWriter out = new PrintWriter(System.out, true);

    // methods
    HashMap methods = new HashMap();

    // method call stack
    private final ArrayList calls = new ArrayList(16);

    public Timer() {
    }


    public Timer(PrintWriter out) {
        this.out = out;
    }


    public final synchronized void push(String name) {
        push(name, name);
    }

    public final synchronized void push(String name, String message) {
        // get time
        final long now = System.currentTimeMillis();

        // get a method descriptor
        MethodDescriptor current = (MethodDescriptor)methods.get(name);
        if (current == null) {
            current = new MethodDescriptor(name);
            methods.put(name, current);
        }

        // update method descriptor
        current.calls++;
        current.instantiations++;

        // update method call stack
        calls.add(new MethodCall(current, message, now, now));
    }


    public final synchronized void pop() {
        // get time
        final long now = System.currentTimeMillis();

        // update method call stack
        final MethodCall call = (MethodCall)calls.remove(calls.size()-1);

        // get current call's time
        final long currentSelf = now - call.self;
        final long currentTotal = now - call.total;

        // update previous call's self time
        if (calls.size() > 0) {
            final MethodCall previous = (MethodCall)calls.get(calls.size()-1);
            previous.self += currentTotal;
        }

        // update method descriptor
        final MethodDescriptor current = call.method;
        current.self += currentSelf;
        if (--current.instantiations == 0) {
            current.total += currentTotal;
        }

        if (false) {
            out.println("Timer (n,g): " + call.message + " : ("
                + currentSelf + ", " + currentTotal + ")");
        }
    }


    static private final String pad(String s, int i) {
        StringBuffer b = new StringBuffer();
        for (i -= s.length(); i > 0; i--)
            b.append((char)' ');
        b.append(s);
        return b.toString();
    }


    public final synchronized void print() {
        out.println("Timer : printing accumulated times ...");
        final Object[] calls = methods.values().toArray();

        Arrays.sort(calls,
            new Comparator() {
            public int compare(Object o1,
                Object o2) {
                return (int)(((MethodDescriptor)o2).total
                    - ((MethodDescriptor)o1).total);
            }
            public boolean equals(Object obj) {
                return (obj != null && compare(this, obj) == 0);
            }
        });

        out.println("Timer :  total s    self s  #calls  name");
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        //nf.applyPattern("#,##0.00");
        //out.println("Timer : pattern = " + nf.toPattern());
        for (int i = 0; i < calls.length; i++) {
            final MethodDescriptor current = (MethodDescriptor)calls[i];

            out.println("Timer : "
                + pad(nf.format(current.total / 1000.0), 8) + "  "
                + pad(nf.format(current.self / 1000.0), 8) + "  "
                + pad(String.valueOf(current.calls), 6) + "  "
                + current.name);
        }
    }
}
