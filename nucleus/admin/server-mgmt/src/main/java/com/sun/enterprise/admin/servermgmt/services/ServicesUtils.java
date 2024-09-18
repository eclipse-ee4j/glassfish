/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.admin.util.LineTokenReplacer;
import com.sun.enterprise.admin.util.TokenValue;
import com.sun.enterprise.admin.util.TokenValueSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author bnevins
 */
public final class ServicesUtils {

    private static final String SEP = "==========================================";

    private ServicesUtils() {
    }

    static TokenValueSet map2Set(final Map<String, String> map) {
        final Set<TokenValue> set = new HashSet<>();
        for (final Map.Entry<String, String> e : map.entrySet()) {
            final String key = e.getKey();
            final String value = e.getValue();
            final TokenValue tv = new TokenValue(key, value);
            set.add(tv);
        }
        return new TokenValueSet(set);
    }

    static void tokenReplaceTemplateAtDestination(Map<String, String> map, File templatePath, File targetPath) {

        final LineTokenReplacer tr = new LineTokenReplacer(map2Set(map));
        tr.replace(templatePath, targetPath);
    }

    static void appendTextToFile(File to, String what) {

        // It is very annoying in Windows when text files have "\n" instead of
        // \n\r -- the following fixes that.

        String[] lines = what.split("\n");

        try (PrintWriter pw = new PrintWriter(new FileOutputStream(to, true), false, UTF_8)) {
            pw.println(SEP);
            pw.println(new Date());

            for (String s : lines) {
                pw.println(s);
            }

            pw.println(SEP);
            pw.println();
            pw.println();
            pw.flush();
        } catch (IOException e) {
            throw new IllegalStateException(";", e);
        }
    }

}
