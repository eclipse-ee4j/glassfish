/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client.acc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.embeddable.client.ApplicationClientCLIEncoding;

import static java.util.Collections.emptyList;
import static java.util.regex.Pattern.DOTALL;

/**
 * Self-contained scanner for an agent argument string.
 *
 * <p>
 * The agent arguments are a comma-separated sequence of <code>[keyword=]quoted-or-unquoted-string</code>. The
 * "keyword=" part is optional. A given keyword can appear multiple times, so after analysis each keyword can map to
 * potentially multiple values (as a List<String>).
 *
 * <p>
 * This class organizes the agent arguments into named and anonymous values.
 */
public class AgentArguments {

    /**
     * Pattern notes: The first group is non-capturing and tries to match the keyword= part zero or one time. The next group
     * (*.*?) matches and captures any keyword. The ? immediately after that group means 0 or 1 times.
     *
     * The next group is a quoted string not itself containing a quotation mark. The next group, an alternative (indicated
     * by the | mark) to the quoted string group, is a non-quoted string not containing a comma. The pattern ends
     * (non-capturing group) with an optional comma (could be end-of-input so the comma is optional)
     */
    private static Pattern agentArgPattern = Pattern.compile("(?:([^=,]*?)=)?((?:\"([^\"]*)\")|[^,]+)", DOTALL);

    /* groups matching interesting parts of the regex */
    private static final int KEYWORD = 1;
    private static final int QUOTED = 2;
    private static final int UNQUOTED = 3;

    private final Map<String, List<String>> values = new HashMap<>();

    public static AgentArguments newInstance(String args) {
        AgentArguments result = new AgentArguments();
        result.scan(args);
        return result;
    }

    /**
     * Returns the list of values associated with the specified keyword.
     *
     * @param keyword the keyword whose values are needed
     * @return the values associated with the keyword; null if the keyword never appeared in the input
     */
    public List<String> namedValues(String keyword) {
        return actualOrEmptyList(keyword);
    }

    /**
     * Returns the unnamed values as a list of strings.
     *
     * @return List of Strings, one for each unnamed value in the scanned string
     */
    public List<String> unnamedValues() {
        return actualOrEmptyList(null);
    }

    private List<String> actualOrEmptyList(String keyword) {
        return values.get(keyword) != null ? values.get(keyword) : emptyList();
    }

    /**
     * Scans the input args string, updating the nameValuePairs properties object using items with a keyword and updated the
     * singleWordArgs list with items without a keyword.
     *
     * @param args           input line to scan
     * @param nameValuePairs properties to augment with keyword entries in the input
     * @param singleWordArgs list of strings to augment with un-keyworded entries in the input
     */
    private void scan(String args) {
        if (args == null) {
            return;
        }

        Matcher agentArgMatcher = agentArgPattern.matcher(args);
        while (agentArgMatcher.find()) {
            String keyword = agentArgMatcher.group(KEYWORD);
            // Either the quoted string group or the unquoted string group from the matcher will be valid.
            String value = ApplicationClientCLIEncoding.decodeArg(agentArgMatcher.group(QUOTED) == null
                ? agentArgMatcher.group(UNQUOTED)
                : agentArgMatcher.group(QUOTED));

            values.computeIfAbsent(keyword, e -> new ArrayList<>()).add(value);
        }
    }

}
