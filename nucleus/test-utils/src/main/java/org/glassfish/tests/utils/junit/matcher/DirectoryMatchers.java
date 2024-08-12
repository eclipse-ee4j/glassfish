/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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
import java.nio.file.NotDirectoryException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

public final class DirectoryMatchers {

    private DirectoryMatchers() {
        throw new AssertionError();
    }

    public static Matcher<File> hasEntryCount(final long expected) {
        return hasEntryCount(equalTo(expected));
    }

    public static Matcher<File> hasEntryCount(final Matcher<Long> expected) {
        return new TypeSafeDiagnosingMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("A directory with a number of entries: ").appendDescriptionOf(expected);
            }

            @Override
            protected boolean matchesSafely(File file, Description mismatchDescription) {
                String[] entries = file.list();
                if (entries != null) {
                    long entryCount = entries.length;
                    boolean matches = expected.matches(entryCount);
                    if (!matches) {
                        expected.describeMismatch(entryCount, mismatchDescription);
                    }
                    return matches;
                } else {
                    return fail(new NotDirectoryException(file.getName()));
                }
            }
        };
    }
}
