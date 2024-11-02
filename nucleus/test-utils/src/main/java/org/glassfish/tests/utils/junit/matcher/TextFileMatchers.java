/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.utils.junit.matcher;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Matchers for text files.
 *
 * @author David Matejcek
 */
public class TextFileMatchers {

    /**
     * Creates a matcher for matching file's line count.
     * File encoding.hsould be known or expected at least.
     *
     * @param expected expected number of lines.
     * @param charset the file encoding
     * @return generated matcher.
     */
    public static Matcher<File> hasLineCount(final long expected, final Charset charset) {
        return hasLineCount(equalTo(expected), charset);
    }


    /**
     * Creates a matcher for matching file's line count.
     * File encoding.hsould be known or expected at least.
     *
     * @param expected {@link Matcher} for expected number of lines.
     * @param charset the file encoding
     * @return generated matcher.
     */
    public static Matcher<File> hasLineCount(final Matcher<Long> expected, final Charset charset) {
        return new TypeSafeDiagnosingMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("A file with a number of lines: ").appendDescriptionOf(expected);
            }

            @Override
            protected boolean matchesSafely(File item, Description mismatchDescription) {
                try (LineNumberReader reader = new LineNumberReader(new FileReader(item, charset))) {
                    long lineCount = reader.lines().count();
                    boolean result = expected.matches(lineCount);
                    if (!result) {
                        expected.describeMismatch(lineCount, mismatchDescription);
                    }
                    return result;
                } catch (IOException e) {
                    return fail(e);
                }
            }
        };
    }


    /**
     * Creates a matcher for matching some file feature.
     *
     * @param fileGetter Converter of {@link File} instance to <code>T</code> type.
     * @param expected {@link Matcher} for <code>T</code> instance obtained from
     *            the <code>fileGetter</code>.
     * @param <T> Type of the file's feature
     * @return generated matcher.
     */
    public static <T> Matcher<File> getterMatches(final Function<File, T> fileGetter, final Matcher<T> expected) {
        return new TypeSafeDiagnosingMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("A file getter `").appendValue(fileGetter).appendText("'")
                    .appendDescriptionOf(expected);

            }

            @Override
            protected boolean matchesSafely(File item, Description mismatchDescription) {
                T value = fileGetter.apply(item);
                boolean result = expected.matches(value);
                if (!result) {
                    expected.describeMismatch(value, mismatchDescription);
                }
                return result;
            }
        };
    }
}
