/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.runnable.tool;

import java.io.BufferedReader;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Processing {@link BufferedReader} - useful to check STDERR/STDOUT streams on the fly.
 *
 * @author David Matejcek
 */
public class BufferedReaderMatcher extends CustomTypeSafeMatcher<BufferedReader> {
    private static final Logger LOG = System.getLogger(BufferedReaderMatcher.class.getName());

    private final String[] expected;

    private StringBuilder output;
    private List<String> expectedLines;

    /**
     * @param expected Expected content in sequence, matcher ignores other lines but puts them to
     *            the error message.
     * @return matcher processing {@link BufferedReader} - useful to check STDERR/STDOUT streams
     *         on the fly.
     */
    public static BufferedReaderMatcher readerContains(String... expected) {
        return new BufferedReaderMatcher(expected);
    }

    private BufferedReaderMatcher(String... expected) {
        super("Expected lines containing: \n" + String.join("\n", expected));
        this.expected = Objects.requireNonNull(expected, "Provide at least one expectation!");
    }

    @Override
    protected boolean matchesSafely(BufferedReader reader) {
        final AtomicBoolean allFound = new AtomicBoolean();
        expectedLines = new ArrayList<>(Arrays.asList(expected));
        output = new StringBuilder();
        LOG.log(DEBUG, "Processing stream of lines ...");
        final long lineCount = reader.lines().peek(line -> {
            LOG.log(DEBUG, "All found: {0}. Line: {1}", allFound, line);
            if (line.contains(expectedLines.get(0))) {
                expectedLines.remove(0);
                output.setLength(0);
                if (expectedLines.isEmpty()) {
                    allFound.set(true);
                    LOG.log(DEBUG, () -> "Found them all: \n" + String.join("\n", expected));
                }
            } else {
                output.append(line).append('\n');
            }
        }).takeWhile(Predicate.not(s -> allFound.get())).count();
        LOG.log(DEBUG, "Processed {0} lines.", lineCount);
        return allFound.get();
    }


    @Override
    public void describeMismatchSafely(BufferedReader item, Description mismatchDescription) {
        mismatchDescription.appendText("missing:\n");
        mismatchDescription.appendText(expectedLines.get(0));
        mismatchDescription.appendText("\ncontained:\n").appendText(output.toString());
    }
}
