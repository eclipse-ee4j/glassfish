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

/*
 * TokenResolver.java
 *
 * Created on April 20, 2007, 11:59 AM
 * Updated for V3 on March 4, 2008
 */
package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.util.SystemPropertyConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Here is the contract:
 * You give me a Map<String,String> object.
 * Then you can call  resolve(List<String>) and/or resolve(String) and/or
 * resolve(Map<String,String>)
 * I will find and replace the tokens, e.g.,  ${foo} with the value of "foo" in the properties.
 * If the token has no such property -- then I leave the token as is.
 * It purposely does not handle nested tokens.  E.g. if the "foo" property has another
 * token embedded in the value -- it will not be further resolved.
 * This is the KISS principle in action...
 * @author bnevins
 */
public class TokenResolver {

    /**
     * Empty constructor means use System Properties
     *
     */
    public TokenResolver() {
        this(new HashMap<String, String>((Map) (System.getProperties())));
    }

    public TokenResolver(Map<String, String> map) {
        props = map;
    }
    /**
     * Replace $[variables} in map with a matching property from the map that this
     *  instance was constructed with.  Both names and values are replaced.
     * @param map Map of Strings to be token-replaced
     */
    public void resolve(Map<String, String> map) {
        // we may be concurrently changing the map so we have to be careful!

        // can't add to "map" arg while we are in the loop -- add all new
        // entries AFTER the loop.

        Map<String, String> newEntries = new HashMap<String,String>();

        Set<Map.Entry<String,String>> set = map.entrySet();
        Iterator<Map.Entry<String,String>> it = set.iterator();

        while(it.hasNext()) {
            Map.Entry<String,String> entry = it.next();
            String key = entry.getKey();
            String value = entry.getValue();

            // usual case -- the RHS has a token
            // will not get a concurrent mod exception -- it is just the value
            // that changes...
            if (hasToken(value)) {
                value = resolve(value);
                map.put(key, value);
            }

            // less usual case -- the LHS has a token.  Need to remove the entry
            // from the map and replace.
            // We have to worry about ConcurrentModification here!
            if(hasToken(key)) {
                String newKey = resolve(key);
                newEntries.put(newKey, value);
                it.remove(); // safe!!!
            }
        }
        map.putAll(newEntries);
    }

    /**
     * Replace $[variables} in list with a matching property from the map
     * @param list List of Strings to be token-replaced
     */
    public void resolve(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);

            if (hasToken(s)) {
                list.set(i, resolve(s));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Replace $[variables} with a matching property in the map
     * @param s String to be token-replaced
     * @return the replaced String
     */
    public String resolve(String s)
    {
        if(s == null || s.length() <= 0)
            return s;

        if (hasWindowsToken(s)) {
            s = windowsToUnixTokens(s);
        }

        List<Token> tokens = getTokens(s);
        String resolved = s;

        for (Token token : tokens) {
            resolved = GFLauncherUtils.replace(resolved, token.token, token.value);
        }

        return resolved;
    }

    /**
     *
     * @param s A String that may contain %token%
     * @return the UNIX-ified format ${token}
     */
    private String windowsToUnixTokens(String s) {
        String replaced = s;

        while (true) {
            if (replaced == null || replaced.indexOf('%') < 0) {
                break;
            }

            replaced = GFLauncherUtils.replace(replaced, "%", "${");
            replaced = GFLauncherUtils.replace(replaced, "%", "}");
        }
        if (replaced == null) {
            return s;
        }
        else {
            return replaced;
        }
    }

    private static boolean hasWindowsToken(String s) {
        // Need at least 2 "%"
        int index = s.indexOf('%');

        if (index < 0 || index >= s.length() - 1) {
            return false;
        }

        return s.indexOf('%', index + 1) >= 0;
    }

    ///////////////////////////////////////////////////////////////////////////
    private List<Token> getTokens(String s) {
        int index = 0;
        List<Token> tokens = new ArrayList<Token>();

        while (true) {
            Token token = getToken(s, index);

            if (token == null) {
                break;
            }

            tokens.add(token);
            index = token.start + Token.TOKEN_START.length();
        }

        return tokens;
    }

    ///////////////////////////////////////////////////////////////////////////
    private Token getToken(String s, int index) {
        if (s == null || index >= s.length()) {
            return null;
        }

        Token token = new Token();
        token.start = s.indexOf(Token.TOKEN_START, index);
        token.end = s.indexOf(Token.TOKEN_END, token.start + 2);

        if (token.end <= 0 || token.start < 0) {
            return null;
        }

        token.token = s.substring(token.start, token.end + 1);
        token.name = s.substring(token.start + Token.TOKEN_START.length(), token.end);

        // if the token exists, but it's value is null -- then set the value
        // back to the token.

        token.value = props.get(token.name);

        if (token.value == null) {
            token.value = token.token;
        }

        return token;
    }

    ///////////////////////////////////////////////////////////////////////////
    public static boolean hasToken(String s) {
        if (s == null) {
            return false;
        }
        if (GFLauncherUtils.isWindows() && hasWindowsToken(s)) {
            return true;
        }
        if (s.indexOf(Token.TOKEN_START) >= 0) {
            return true;
        }
        return false;
    }
    ///////////////////////////////////////////////////////////////////////////
    private final Map<String, String> props;

    private static class Token {

        int start;
        int end;
        String token;
        String name;
        String value;
        final static String TOKEN_START = SystemPropertyConstants.OPEN;
        final static String TOKEN_END = SystemPropertyConstants.CLOSE;

        @Override
        public String toString() {
            return "name: " + name + ", token: " + token + ", value: " + value;
        }
    }
}
