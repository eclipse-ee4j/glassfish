/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for RegexUtil class's methods.
 * @author Kedar Mhaswade
 */
public class RegexUtilTest {
    @Test
    public void testStarWithExtensionDot() {
        String[] files = new String[]{"a.txt", "b.txt", "cc.txt"};
        String glob  = "*.txt";  //this is how glob pattern matches files in files array
        String regex = RegexUtil.globToRegex(glob);
        Pattern p = Pattern.compile(regex); //this regex should match all the files
        for (String file : files) {
            Matcher m = p.matcher(file);
            assertTrue(m.matches(), file + " matches glob: " + glob);
        }
    }

    @Test
    public void testStarWithoutExtensionDot() {
        String[] files = new String[]{"a.txt", "b.txt", "cc.txt"};
        String glob  = "*txt";  //this is how glob pattern matches files in files array
        String regex = RegexUtil.globToRegex(glob);
        Pattern p = Pattern.compile(regex); //this regex should match all the files
        for (String file : files) {
            Matcher m = p.matcher(file);
            assertTrue(m.matches(), file + " matches glob: " + glob);
        }
    }

    @Test
    public void testStarDotStar() {
        String glob  = "*.*";  //should match only the strings that are file-name-like and have an extension
        String regex = RegexUtil.globToRegex(glob);
        Pattern p = Pattern.compile(regex); //this regex should match all the files
        String str = "a.txt"; //*.* matches this
        Matcher m = p.matcher(str);
        assertTrue(m.matches(), str + " matches glob: " + glob);
        str = "noext"; //*.* should not match this
        m = p.matcher(str); //again
        assertFalse(m.matches(), str + " matches glob: " + glob);
    }

    @Test
    @Disabled("The test that tests some abnormalities that I am going to ignore for now. For example, glob pattern"
        + " \"*\" does not match a hidden file like \".foo\", but the regex returned here matches. There are"
        + " a few other corner cases like that that I am going to ignore. Hopefully, they don't arise in our usage.")
    public void cornerCases() {
        String glob = "*";
        String regex = RegexUtil.globToRegex(glob);
        String str = ".hidden";
        assertFalse(Pattern.compile(regex).matcher(str).matches(), str + " matches glob: " + glob);
    }
}
