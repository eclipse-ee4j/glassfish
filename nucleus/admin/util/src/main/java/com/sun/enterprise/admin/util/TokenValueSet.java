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

package com.sun.enterprise.admin.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Represents the Set of TokenValue objects.
 *
 * @author  kedar
 */

public class TokenValueSet implements Cloneable {
    
    private final Set<TokenValue> values;
    
    public TokenValueSet() {
        values = new HashSet<TokenValue>();
    }
    
    public TokenValueSet(final Set<TokenValue> values) {
        //if (!isTokenValueSet(values)) {
          //  throw new IllegalArgumentException("Invalid set");
        //}
        this.values = new HashSet<TokenValue>();
        this.values.addAll(values);
    }
    
    public void add(final TokenValue tokenValue) {
        this.values.add(tokenValue);
    }
    
    public void addAll(final Set<TokenValue> more) {
        this.values.addAll(more);
    }
    
    public void remove(final TokenValue tokenValue) {
        this.values.remove(tokenValue);
    }
    
    public void clear() {
        this.values.clear();
    }
    
    public Iterator<TokenValue> iterator() {
        return ( this.values.iterator() );
    }
    
    public boolean isEmpty() {
        return ( this.values.isEmpty() );
    }
    
    public Object[] toArray() {
        return ( this.values.toArray() );
    }
    
    public int size() {
        return ( this.values.size() );
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return ( super.clone() );
    }
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        Iterator<TokenValue> iter = this.iterator();
        while(iter.hasNext()) {
            TokenValue tv = iter.next();
            buf.append(tv.toString());
            buf.append(System.getProperty("line.separator"));
        }
        return buf.toString();
    }
}
