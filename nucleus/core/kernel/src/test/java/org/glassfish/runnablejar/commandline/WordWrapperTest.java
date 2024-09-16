/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.runnablejar.commandline;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Ondro Mihalyi
 */
public class WordWrapperTest {

    @Test
    public void testWrapper() {
        final int MAX_LINE_LENGTH = 80;
        final WordWrapper wordWrapper = new WordWrapper(MAX_LINE_LENGTH, 8, "        ");
        String message = "        "
                            + Arrays.stream(Option.PROPERTIES.getHelpText().split(" "))
                                    .collect(wordWrapper.collector());
        final List<String> linesEndingWithSpace = message.lines()
                .filter(line -> line.endsWith(" "))
                .collect(toList());
        assertTrue(linesEndingWithSpace.isEmpty(), "Some lines end with a space: " + linesEndingWithSpace);

        final List<String> linesLongerThanMaxLength = message.lines()
                .filter(line -> line.length() > MAX_LINE_LENGTH)
                .collect(toList());
        assertTrue(linesLongerThanMaxLength.isEmpty(), "Some lines exceed max length: " + linesLongerThanMaxLength);
    }
}
