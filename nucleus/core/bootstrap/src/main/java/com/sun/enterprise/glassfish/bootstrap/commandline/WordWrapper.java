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
package com.sun.enterprise.glassfish.bootstrap.commandline;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author Ondro Mihalyi
 */
public class WordWrapper {

    final int MAX_LINE_LENGTH;
    final String HELP_LINE_INDENT;
    int characterCount;

    public WordWrapper(int maxLineLength, int initialIndent, String otherLinesIndentText) {
        this.MAX_LINE_LENGTH = maxLineLength;
        this.HELP_LINE_INDENT = otherLinesIndentText;
        this.characterCount = initialIndent;
    }

    public String map(String optionText) {
        String[] optionTextLines = optionText.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String optionTextLine : optionTextLines) {
            if (!first) {
                sb.append("\n" + HELP_LINE_INDENT);
                characterCount = HELP_LINE_INDENT.length();
            }
            characterCount += optionTextLine.length() + 1;
            if (characterCount > MAX_LINE_LENGTH) {
                final String newOptionText = HELP_LINE_INDENT + optionTextLine;
                characterCount = newOptionText.length();
                sb.append("\n").append(newOptionText);
            } else {
                sb.append(optionTextLine);
            }
            first = false;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        final WordWrapper wordWrapper = new WordWrapper(80, 8, "        ");
        String message = "        "
                            + Arrays.stream(Option.PROPERTIES.getHelpText().split(" "))
                                    .map(wordWrapper::map)
                                    .collect(Collectors.joining(" "));
        System.out.println(message);
    }

}
