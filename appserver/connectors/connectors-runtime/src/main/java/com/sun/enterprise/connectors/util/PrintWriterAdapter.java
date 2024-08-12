/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.util;

import com.sun.enterprise.connectors.ConnectorRuntime;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * PrintWriter adapter that will be used by resource adapters
 */
public class PrintWriterAdapter extends PrintWriter implements Externalizable {

    private transient PrintWriter writer;

    public PrintWriterAdapter(PrintWriter writer) {
        super(writer); // since all the methods of super-class is overridden, writer will not be used.
        this.writer = writer;
    }

    /**
     * Used during de-serialization.
     */
    public PrintWriterAdapter() {
        this(getResourceAdapterLogWriter());
    }

    private static PrintWriter getResourceAdapterLogWriter() {
        return ConnectorRuntime.getRuntime().getResourceAdapterLogWriter();
    }

    public void initialize() {
        if (writer == null) {
            writer = getResourceAdapterLogWriter();
        }
    }

    @Override
    public void flush() {
        initialize();
        writer.flush();
    }

    @Override
    public void close() {
        initialize();
        writer.close();
    }

    @Override
    public boolean checkError() {
        initialize();
        return writer.checkError();
    }

    @Override
    public void write(int c) {
        initialize();
        writer.write(c);
    }

    @Override
    public void write(char[] buf, int off, int len) {
        initialize();
        writer.write(buf, off, len);
    }

    @Override
    public void write(char[] buf) {
        initialize();
        writer.write(buf);
    }

    @Override
    public void write(String s, int off, int len) {
        initialize();
        writer.write(s, off, len);
    }

    @Override
    public void write(String s) {
        initialize();
        writer.write(s);
    }

    @Override
    public void print(boolean b) {
        initialize();
        writer.print(b);
    }

    @Override
    public void print(char c) {
        initialize();
        writer.print(c);
    }

    @Override
    public void print(int i) {
        initialize();
        writer.print(i);
    }

    @Override
    public void print(long l) {
        initialize();
        writer.print(l);
    }

    @Override
    public void print(float f) {
        initialize();
        writer.print(f);
    }

    @Override
    public void print(double d) {
        initialize();
        writer.print(d);
    }

    @Override
    public void print(char[] s) {
        initialize();
        writer.print(s);
    }

    @Override
    public void print(String s) {
        initialize();
        writer.print(s);
    }

    @Override
    public void print(Object obj) {
        initialize();
        writer.print(obj);
    }

    @Override
    public void println() {
        initialize();
        writer.println();
    }

    @Override
    public void println(boolean x) {
        initialize();
        writer.println(x);
    }

    @Override
    public void println(char x) {
        initialize();
        writer.println(x);
    }

    @Override
    public void println(int x) {
        initialize();
        writer.println(x);
    }

    @Override
    public void println(long x) {
        initialize();
        writer.println(x);
    }

    @Override
    public void println(float x) {
        initialize();
        writer.println(x);
    }

    @Override
    public void println(double x) {
        initialize();
        writer.println(x);
    }

    @Override
    public void println(char[] x) {
        initialize();
        writer.println(x);
    }

    @Override
    public void println(String x) {
        initialize();
        writer.println(x);
    }

    @Override
    public void println(Object x) {
        initialize();
        writer.println(x);
    }

    @Override
    public PrintWriter printf(String format, Object... args) {
        initialize();
        return writer.printf(format, args);
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
        initialize();
        return writer.printf(l, format, args);
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        initialize();
        return writer.format(format, args);
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
        initialize();
        return writer.format(l, format, args);
    }

    @Override
    public PrintWriter append(CharSequence csq) {
        initialize();
        return writer.append(csq);
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
        initialize();
        return writer.append(csq, start, end);
    }

    @Override
    public PrintWriter append(char c) {
        initialize();
        return writer.append(c);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writer = null;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        initialize();
    }
}
