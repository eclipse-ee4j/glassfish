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

package com.sun.enterprise.universal.glassfish;

import com.sun.enterprise.universal.NameValue;
import com.sun.enterprise.universal.collections.ManifestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * Wraps the Manifest object returned by the Server.  The Manifest object has
 * an internally defined format over and above the Manifest itself.  This is a
 * central place where we are aware of all the details so that callers don't
 * have to be.  If the format changes or the returned Object type changes then
 * this class will be the thing to change.
 *
 * @author bnevins
 */
public class AdminCommandResponse {
    public static final String GENERATED_HELP = "GeneratedHelp";
    public static final String MANPAGE = "MANPAGE";
    public static final String SYNOPSIS = "SYNOPSIS";
    public static final String MESSAGE = "message";
    public static final String CHILDREN_TYPE = "children-type";
    public static final String EXITCODE = "exit-code";
    public static final String SUCCESS = "Success";
    public static final String WARNING = "Warning";
    public static final String FAILURE = "Failure";

    public AdminCommandResponse(InputStream inStream) throws IOException {
        Manifest m = new Manifest(inStream);
        m.read(inStream);
        allRaw = ManifestUtils.normalize(m);
        mainRaw = ManifestUtils.getMain(allRaw);
        makeMain();
    }

    public boolean isGeneratedHelp() {
        return isGeneratedHelp;
    }

    public String getMainMessage() {
        return mainMessage;
    }

    public boolean wasSuccess() {
        return exitCode == 0;
    }

    public boolean wasWarning() {
        return exitCode == 1;
    }

    public boolean wasFailure() {
        return exitCode == 2;
    }

    public String getCause() {
        return cause;
    }
    public Map<String,String> getMainAtts() {
        return mainRaw;
    }
    public List<NameValue<String,String>> getMainKeys() {
        return mainKeys;
    }

    public String getValue(String key) {
        for(NameValue<String,String> nv : mainKeys) {
            if(nv.getName().equals(key))
                return nv.getValue();
        }
        return null;
    }

    public List<NameValue<String,String>> getKeys(Map<String,String> map) {
        List<NameValue<String,String>> list = new LinkedList<NameValue<String,String>>();

        String keysString = map.get("keys");

        if(ok(keysString)) {
            String[] keys = keysString.split(";");

            for(String key : keys) {
                String name = map.get(key + "_name");
                String value = null;
                try {
                    value = map.get(key + java.net.URLDecoder.decode("_value", "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    value = map.get(key + "_value");
                }

                if(!ok(name))
                    continue;
                list.add(new NameValue<String,String>(name, value));
            }
        }

        return list;
    }

    public Map<String,Map<String,String>> getChildren(Map<String,String> map) {
        // keep the child elements in order
        Map<String,Map<String,String>> children = new LinkedHashMap<String,Map<String,String>>();
        String kidsString = map.get("children");

        if(ok(kidsString)) {
            String[] kids = kidsString.split(";");

            for(String kid : kids) {
                // kid is the name of the Attributes
                Map<String,String> kidMap = allRaw.get(kid);
                if(kidMap != null)
                    children.put(kid, kidMap);
            }
        }
        if(children.isEmpty())
            return null;
        else
            return children;
    }

    private void makeMain() {
        mainMessage = mainRaw.get(MESSAGE);

        String exitCodeString = mainRaw.get(EXITCODE);
        if(SUCCESS.equalsIgnoreCase(exitCodeString))
            exitCode = 0;
        else if(WARNING.equalsIgnoreCase(exitCodeString))
            exitCode = 1;
        else
            exitCode = 2;
        cause = mainRaw.get("cause");
        makeMainKeys();
    }

    /**
     *  Format:
     * (1) Main Attributes usually have the bulk of the data.  Say you have 3 items
     * in there: a, b and c.  The Manifest main attributes will end up with this:
     * keys=a;b;c
     * a_name=xxx
     * a_value=xxx
     * b_name=xxx
     * b_value=xxx
     * c_name=xxx
     * c_value=xxx
     */
    private void makeMainKeys() {
        mainKeys = getKeys(mainRaw);

        for(NameValue<String,String> nv : mainKeys) {
            if(nv.getName().equals(GENERATED_HELP)) {
                isGeneratedHelp = Boolean.parseBoolean(nv.getValue());
                mainKeys.remove(nv);
                break;
            }
        }
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null");
    }

    private Map<String, Map<String, String>> allRaw;
    private Map<String, String> mainRaw;
    private List<NameValue<String,String>> mainKeys;
    private String  mainMessage;
    private String  cause;
    private int exitCode = 0;   // 0=success, 1=failure
    private boolean isGeneratedHelp;
}
