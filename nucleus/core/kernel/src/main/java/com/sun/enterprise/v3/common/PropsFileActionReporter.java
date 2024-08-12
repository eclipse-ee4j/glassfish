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

package com.sun.enterprise.v3.common;

import com.sun.enterprise.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Action reporter to a manifest file
 * @author Jerome Dochez
 */
@Service(name = "hk2-agent")
@PerLookup
public class PropsFileActionReporter extends ActionReporter {

    @Override
    public void setMessage(String message) {
        super.setMessage(encodeEOL(message));
    }

    @Override
    public void writeReport(OutputStream os) throws IOException {

        Manifest out = new Manifest();
        Attributes mainAttr = out.getMainAttributes();
        mainAttr.put(Attributes.Name.SIGNATURE_VERSION, "1.0");
        mainAttr.putValue("exit-code", exitCode.toString());
        mainAttr.putValue("use-main-children-attribute", Boolean.toString(useMainChildrenAttr));

        if (exitCode == ExitCode.FAILURE) {
            writeCause(mainAttr);
        }

        writeReport(null, topMessage, out, mainAttr);
        out.write(os);
    }

    public void writeReport(String prefix, MessagePart part, Manifest m, Attributes attr) {
        //attr.putValue("message", part.getMessage());
        StringBuilder sb = new StringBuilder();
        getCombinedMessages(this, sb);
        attr.putValue("message", sb.toString());
        if (part.getProps().size() > 0) {
            String keys = null;
            for (Map.Entry entry : part.getProps().entrySet()) {
                String key = fixKey(entry.getKey().toString());
                keys = (keys == null ? key : keys + ";" + key);
                attr.putValue(key + "_name", entry.getKey().toString());
                attr.putValue(key + "_value", encodeEOL(entry.getValue().toString()));
            }

            attr.putValue("keys", keys);
        }
        if (part.getChildren().size() > 0) {
            attr.putValue("children-type", part.getChildrenType());
            attr.putValue("use-main-children-attribute", "true");
            StringBuilder keys = null;
            for (MessagePart child : part.getChildren()) {
                // need to URL encode a ';' as %3B because it is used as a
                // delimiter
                String cm = child.getMessage();
                if (cm != null) {
                    try {
                        cm = URLEncoder.encode(cm, "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        // ignore - leave cm as it is
                    }
                }
                String newPrefix = (prefix == null ? cm : prefix + "." + cm);

                if(keys == null) {
                    keys = new StringBuilder();
                } else {
                    keys.append(';');
                }

                if(newPrefix != null) {
                    keys.append(newPrefix);
                }

                Attributes childAttr = new Attributes();
                m.getEntries().put(newPrefix, childAttr);
                writeReport(newPrefix, child, m, childAttr);
            }
            attr.putValue("children", keys.toString());
        }
    }

    private void writeCause(Attributes mainAttr) {
        Throwable t = getFailureCause();

        if (t == null) {
            return;
        }

        String causeMessage = t.toString();
        mainAttr.putValue("cause", causeMessage);
    }

    /* Issue 5918 Keep output sorted. If set to true ManifestManager will grab
     * "children" from main attributes. "children" is in original order of
     * output set by server-side
     */
    public void useMainChildrenAttribute(boolean useMainChildrenAttr) {
        this.useMainChildrenAttr = useMainChildrenAttr;
    }

    private String fixKey(String key) {
        // take a look at the  javadoc -- java.util.jar.Attributes.Name
        // < 70 chars in length and [a-zA-Z0-9_-]
        // then you can see in the code above that we take the key and add
        // _value to it.  So we simply hack it off at 63 characters.
        // We also replace "bad" characters with "_".  Note that asadmin will
        // display the correct real name.

        if (!StringUtils.ok(key)) {
            return key; // GIGO!
        }
        StringBuilder sb = new StringBuilder();
        boolean wasChanged = false;
        int len = key.length();

        if (len > LONGEST) {
            len = LONGEST;
            wasChanged = true;
        }

        for (int i = 0; i < len; i++) {
            char c = key.charAt(i);

            if (!isValid(c)) {
                wasChanged = true;
                sb.append('_');
            } else {
                sb.append(c);
            }
        }

        if (!wasChanged) {
            return key;
        }

        String fixedName = sb.toString();

        if (fixedNames.add(fixedName)) {
            return fixedName;
        }

        // perhaps they are using huge long names that differ just at the end?
        return doubleFixName(fixedName);
    }

    private String doubleFixName(String s) {
        // Yes, this is a nightmare!
        int len = s.length();

        if (len > LONGEST - 5) {
            s = s.substring(0, LONGEST - 5);
        }

        for (int i = 0; i < 10000; i++) {
            String num = String.format("%05d", i);
            String ret = s + num;

            if (fixedNames.add(ret)) {
                return ret;
            }
        }
        // Wow!!!
        throw new IllegalArgumentException("Could not come up with a unique name after 10000 attempts!!");
    }

    private static boolean isValid(char c) {
        return isAlpha(c) || isDigit(c) || c == '_' || c == '-';
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private static String encodeEOL(String m) {
        if (m != null) {
            m = m.replace("\n", EOL_MARKER).replace(System.getProperty("line.separator"), EOL_MARKER);
        }
        return m;
    }
    private boolean useMainChildrenAttr = false;
    private Set<String> fixedNames = new TreeSet<>();
    private static final int LONGEST = 62;
}
