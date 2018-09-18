/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import javax.enterprise.context.ApplicationScoped;

/**
 * Validate the ISBN number as described in
 * http://en.wikipedia.org/wiki/International_Standard_Book_Number
 */
@ApplicationScoped
public class ISBNValidator {
    /**
     * For convenience, we will ignore '-', ' '.
     */
    public boolean isValid(String isbnStr) {
        char[] isbnChars = isbnStr.toCharArray();
        if (isbnChars.length < 10) {
            return false;
        }

        boolean valid = true;
        boolean hasX = false;
        int[] is = new int[13];
        int len = 0;
        // read one more if there is
        for (int i = 0; i < isbnChars.length && len < 14; i++) {
            char c = isbnChars[i];
            if ((c >= '0' && c <= '9')) {
                if (len < 13) {
                    is[len] = c - '0';
                }
                len++;
            } else if (c == '-' || c == ' ') {
                //skip
            } else if ((c == 'X' || c == 'x') && len == 9) { // for isbn 10
                is[len++] = 10;
                hasX = true;
            } else {
                valid = false;
                break;
            }
        }

        if (!valid || (len != 10 && len != 13) || (len != 10 && hasX)) {
            return false;
        }

        if (len == 10) {
            return isISBN10(is);
        } else { // len == 13
            return isISBN13(is);
        }
    }

    // only look at the first 10 elements and 'X' has changed to 10
    // 10x_1 + 9x_2 + 8x_3 + ... + 2x_9 + x_10 = 0 (mod 11)
    private boolean isISBN10(int[] is) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (10 - i) * is[i];
        }   
        return (sum % 11 == 0);
    }

    // x_13 = (10 - (x_1 + 3x_2 + x_3 + 3x_4 + ... + x_11 + 3x_12)) mod 10
    private boolean isISBN13(int[] is) {
        int sum = 0;
        for (int i = 0; i < 12; i += 2) {
            sum += is[i] + 3 * is[i + 1];
        }
        sum += is[12];
        return (sum % 10 == 0);
    }
}
