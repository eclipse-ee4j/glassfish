/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.commands;
import java.io.BufferedReader;
import java.io.StringReader;

class StringBuilderNewLineAppender {

    private  StringBuilder sb;
    static final String SEP = System.getProperty("line.separator");
    /** Creates a new instance of StringBuilderNewLineAppender */
    StringBuilderNewLineAppender(final StringBuilder sb) {
        this.sb = sb;
    }
    StringBuilderNewLineAppender append(final String s) {
        sb.append(s);
        sb.append(SEP);
        return ( this );
    }
    @Override
    public String toString() {
        return ( sb.toString() );
    }
    public String toString(String... filterOut) {
        String sbString = sb.toString();
        BufferedReader in = new BufferedReader(new StringReader(sbString));
        sb = new StringBuilder();

        try
        {
            readloop:
                for(String s = in.readLine(); s != null; s = in.readLine()){
                    for(String filter : filterOut){
                        if(s.startsWith(filter))
                         {
                            continue readloop; // continue to outer loop
                        }
                    }
                    append(s);
                }
        }
        catch(Exception e)
        {
            // bail
            return sbString;
        }

        return toString();
    }

}
