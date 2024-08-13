/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Byron Nevins
 */

public class PropertiesDecoder {
     /**
      * There are several CLI commands that take properties arguments.  The properties
      * are "flattened". This class will unflatten them back into a Map for you.
      * <p>Example Input:  <b>foo=goo:xyz:hoo=ioo</b>
      *  <p>Output would be 3 pairs:
      * <ul>
      *  <li>foo, goo
      *  <li>xyz, null
      *  <li>hoo, ioo
      *  </ul>
      * @param props The flattened string properties
      * @return A Map of the String keys and values.  It will return an
      */

    public static Map<String,String> unflatten(final String s) {
        if(!ok(s))
            return Collections.emptyMap();

        Map<String,String> map = new HashMap<String,String>();
        String[] elements = s.split(":");

        for(String element : elements) {
            addPair(map, element);
        }

        return map;
    }

    private static void addPair(Map<String, String> map, String element) {
        // TODO this method is a perfect candidate for unit tests...
        // note: It is quite tricky and delicate finding every possible weirdness
        // that a user is capable of!

        // element is one of these:
        // 0.   ""
        // 1.   "foo"
        // 2.   "foo=goo"
        // 3.   "foo="
        // if we get garbage like a=b=c=d  we change to "a", "b=c=d"

        // 0.
        if(!ok(element))
            return; // no harm, no foul

        int index = element.indexOf("=");

        // 1.
        if(index < 0)
            map.put(element, null);

        // 3.
        else if(element.length() - 1 <= index ) {
            // lose the '='
            map.put(element.substring(0, index), null);
        }
        // 2
        else // guarantee:  at least one char after the '='
            map.put(element.substring(0, index), element.substring(index + 1));
    }


    private static boolean ok(String s) {
        return s != null && s.length() > 0;
    }
}
