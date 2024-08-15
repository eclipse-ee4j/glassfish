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

package org.glassfish.admin.amx.util.jmx;

import java.util.Set;

import javax.management.ObjectName;

public interface ObjectNameQuery
{
    /**
    Return the ObjectNames of all MBeans whose properties match all the specified
    regular expressions.  Both property names and values may be searched.

    A starting set may be specified by using an ObjectName pattern.
    This can greatly improve the performance of the search by restricting the
    set of MBeans which are examined; otherwise all registered MBeans must be examined.

    The regexNames[ i ] pattern corresponds to regexValues[ i ].  A value of null
    for any item is taken to mean "match anything".  Thus specifing null for
    'regexNames' means "match any name" and specifying regexNames[ i ] = null means
    to match only based on regexValues[ i ] (and vice versa).

    @param startingSet                         optional ObjectName pattern for starting set to search
    @param regexNames                        optional series of regular expressions for Property names
    @param regexValues                        optional series of regular expressions for Property values
    @return                                         array of ObjectName (may be of zero length)
     */
    Set<ObjectName> matchAll(Set<ObjectName> startingSet, String[] regexNames, String[] regexValues);

    /**
    Return the ObjectNames of all MBeans whose properties match any of the specified
    regular expressions.  Both property names and values may be searched.

    A starting set may be specified by using an ObjectName pattern.
    This can greatly improve the performance of the search by restricting the
    set of MBeans which are examined; otherwise all registered MBeans must be examined.


    The regexNames[ i ] pattern corresponds to regexValues[ i ].  A value of null
    for any item is taken to mean "match anything".  Thus specifing null for
    'regexNames' means "match any name" and specifying regexNames[ i ] = null means
    to match only based on regexValues[ i ] (and vice versa).

    @param startingSet                         optional ObjectName pattern for starting set to search
    @param regexNames                        optional series of regular expressions for Property names
    @param regexValues                        optional series of regular expressions for Property values
    @return                                         array of ObjectName (may be of zero length)
     */
    Set<ObjectName> matchAny(Set<ObjectName> startingSet, String[] regexNames, String[] regexValues);

}






