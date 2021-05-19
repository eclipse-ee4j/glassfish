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

package com.sun.s1asdev.ejb.timer.timertests;

import java.io.*;
import jakarta.ejb.*;

public class BarPrimaryKey implements Serializable
{
    public Long id;
    public String value2;

    public BarPrimaryKey(){}

    public BarPrimaryKey(Long id, String value2)
    {
        this.id = id;
        this.value2 = value2;
    }

    public boolean equals(Object other)
    {
        if ( other instanceof BarPrimaryKey ) {
            BarPrimaryKey bpk = (BarPrimaryKey) other;
            return ((id.equals(bpk.id))
                    && (value2.equals(bpk.value2)));
        }
        return false;
    }

    public int hashCode()
    {
        return id.hashCode();
    }

    public String toString()
    {
        return id + "_" + value2;
    }
}
