/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.admin.test.progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hamcrest.CustomTypeSafeMatcher;

/**
 * Parse progress status message.
 *
 * @author martinmares
 */
public class ProgressMessage {

    private static final Pattern PATTERN = Pattern.compile("[ ]*(\\d+)(%)?:(.+:)?(.*)");
    private static final Predicate<String> PREDICATE = PATTERN.asMatchPredicate();

    private final int value;
    private final boolean percentage;
    private final String scope;
    private final String message;

    private ProgressMessage(String txt) throws IllegalArgumentException {
        Matcher matcher = PATTERN.matcher(txt);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Arg txt is not progress message");
        }
        this.value = Integer.parseInt(matcher.group(1));
        this.percentage = matcher.group(2) != null;
        this.scope = nvlTrim(matcher.group(3));
        this.message = nvlTrim(matcher.group(4));
    }

    private static String nvlTrim(String txt) {
        if (txt != null) {
            txt = txt.trim();
        }
        return txt;
    }

    public int getValue() {
        return value;
    }

    public boolean isPercentage() {
        return percentage;
    }

    public String getScope() {
        return scope;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return Arrays.toString(new Object[] {value, percentage, scope, message});
    }

    public static List<ProgressMessage> grepProgressMessages(String txt) {
        StringTokenizer stok = new StringTokenizer(txt, System.lineSeparator());
        return Collections.list(stok).stream().map(Object::toString).filter(PREDICATE).map(ProgressMessage::new)
            .collect(Collectors.toList());
    }

    /** Unique only that not equal with previous.
     */
    public static String[] uniqueMessages(List<ProgressMessage> pms) {
        List<String> messages = new ArrayList<>();
        for (ProgressMessage pm : pms) {
            if (pm.getMessage() != null &&
                    (messages.isEmpty() || !pm.getMessage().equals(messages.get(messages.size() - 1)))) {
                messages.add(pm.getMessage());
            }
        }
        String[] result = new String[messages.size()];
        return messages.toArray(result);
    }

    public static CustomTypeSafeMatcher<List<ProgressMessage>> isIncreasing() {
        return new CustomTypeSafeMatcher<>("is increasing") {

            @Override
            protected boolean matchesSafely(List<ProgressMessage> pms) {
                if (pms == null) {
                    return false;
                }
                int lastVal = Integer.MIN_VALUE;
                for (ProgressMessage pm : pms) {
                    if (pm.getValue() < lastVal) {
                        return false;
                    }
                    lastVal = pm.getValue();
                }
                return true;
            }
        };
    }

}
