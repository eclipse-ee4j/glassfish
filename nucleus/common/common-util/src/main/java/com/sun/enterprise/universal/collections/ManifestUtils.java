/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.collections;

import java.util.*;
import java.util.jar.*;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

/**
 * all-static methods for handling operations with Manifests
 * It automatically replace all occurences of EOL_TOKEN with linefeeds
 * @author bnevins
 */
public class ManifestUtils {
    /**
     * Embed this token to encode linefeeds in Strings that are placed
     * in Manifest objects
     */
    public static final String EOL_TOKEN = "%%%EOL%%%";
    /**
     * The name of the Manifest's main attributes.
     */
    public static final String MAIN_ATTS = "main";

    /**
     * The line separator character on this OS
     */
    public static final String EOL = System.getProperty("line.separator");

    /**
     * Convert a Manifest into an easier data structure.  It returns a Map of Maps.
     * The main attributes become the map where the key is MAIN_ATTS.
     * Entries become named maps as in the Manifest
     * @param m
     * @return
     */
    public final static Map<String, Map<String,String>> normalize(Manifest m)
    {
        // first add the "main attributes
        Map<String, Map<String,String>> all = new HashMap<String, Map<String,String>>();
        Attributes mainAtt = m.getMainAttributes();
        all.put(MAIN_ATTS, normalize(mainAtt));

        // now add all the "sub-attributes"
        Map<String,Attributes> unwashed = m.getEntries();
        Set<Map.Entry<String,Attributes>> entries = unwashed.entrySet();

        for(Map.Entry<String,Attributes> entry : entries) {
            String name = entry.getKey();
            Attributes value = entry.getValue();

            if(name == null || value == null)
                continue;

            all.put(name, normalize(value));
        }
        return all;
    }

    /**
     * Convert an Aattributes object into a Map
     * @param att
     * @return
     */
    public final static Map<String,String> normalize(Attributes att)
    {
        Set<Map.Entry<Object,Object>> entries = att.entrySet();
        Map<String,String> pristine = new HashMap<String,String>(entries.size());

        for(Map.Entry<Object,Object> entry : entries) {
            String key = entry.getKey().toString();
            String value = decode(entry.getValue().toString());
            pristine.put(key, value);
        }

        return pristine;
    }

    public final static String encode(String s) {
        // do DOS linefeed first!
        s = s.replaceAll("\r\n", EOL_TOKEN);

        return s.replaceAll("\n", EOL_TOKEN);
    }

    public static Map<String,String> getMain(Map<String, Map<String,String>> exManifest) {
        Map<String,String> map = exManifest.get(MAIN_ATTS);

        // Never return null
        // do NOT return Collections.emptyMap because then we'll get an error when
        // they try to add to it!
        if(map == null)
            map = new HashMap<String,String>(0);

        return map;
    }

    public static String decode(String s) {
        // replace "null" with null
        if(s == null || s.equals("null"))
            return null;

        // replace special tokens with eol
        return s.replaceAll(EOL_TOKEN, EOL);
    }
    private ManifestUtils() {
    }

}
