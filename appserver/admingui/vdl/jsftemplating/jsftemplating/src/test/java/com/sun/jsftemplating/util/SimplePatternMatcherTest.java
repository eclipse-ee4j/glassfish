/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 *
 */
package com.sun.jsftemplating.util;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

/**
 * TestCase for <code>SimplePatternMatcher</code>.
 *
 * @author Imre O&szlig;wald (io@emx.jevelopers.com)
 *
 */
public class SimplePatternMatcherTest {

    /**
     * Test method for {@link com.sun.jsftemplating.util.SimplePatternMatcher#matches(java.lang.String)}.
     */
    @Test
    public void testMatches() {
        long start = System.nanoTime();

        Exception e = null;

        MatchTest fullPath = new MatchTest("/some/folder/view.id");
        MatchTest all = new MatchTest("*");
        MatchTest suffix = new MatchTest("*.jsft");
        MatchTest prefix = new MatchTest("/some/*");
        MatchTest oneStar = new MatchTest("/some/*/test.jsp");
        MatchTest complex = new MatchTest("/*/config/*/page?.jsp");

        long start2 = System.nanoTime();

        try {
            new MatchTest(null);
        } catch (NullPointerException npe) {
            e = npe;
        }
        Assert.assertNotNull(e);
        e = null;

        for (int i = 0; i < 100; i++) { // run common cases more often for
            // timing
            fullPath.setMatching("/some/folder/view.id");
            fullPath.setNonMatching(null, "", "/test", "/some", "/some/folder/view.i", "/some/folder/view.id2");
            fullPath.test();

            all.setMatching("", "/", "/page.jsp", "/some/page.jsp");
            all.setNonMatching((String) null);
            all.test();

            suffix.setMatching("/.jsft", "/a.jsft", "/folder/x.jsft");
            suffix.setNonMatching(null, "", "/jsft", "somexjsft", "/some.jsp", "/someother.jsftx");
            suffix.test();

            prefix.setMatching("/some/", "/some/page", "/some/page.jsp", "/some/more/page.jsp");
            prefix.setNonMatching(null, "", "/", "/some", "/somex/", "/somex/page.jsp");
            prefix.test();
        }

        oneStar.setMatching("/some/thing/test.jsp", "/some/thing/more/test.jsp", "/some/even.more/test.jsp");
        oneStar.setNonMatching(null, "/test.jsp", "/somelonger/test.jsp", "/some/more/test.jsft");
        oneStar.test();

        complex.setMatching("/f1/config/downloads/page1.jsp", "//config//page2.jsp", "/f2/config/one/two/three/pageA.jsp",
                "/a/b/c/d/config/e/f/g/pageX.jsp");
        complex.setNonMatching(null, "x/config//pageN.jsp", "/x/config/x/page12.jsp", "/f1/config/downloads/page1.jspx",
                "/f1/nonconfig/downloads/pageA.jsp");
        complex.test();
        long end = System.nanoTime();
        System.out.println("testMatches() took: " + ((end - start) / 1000) + " mics");
        System.out.println("matching took: " + ((end - start2) / 1000) + " mics");
    }

    /**
     * Test method for {@link com.sun.jsftemplating.util.SimplePatternMatcher#parseMultiPatternString(String, String)}.
     */
    @Test
    public void testParseMultiPatter() {
        String mpattern = null;
        Collection<SimplePatternMatcher> result = null;

        result = SimplePatternMatcher.parseMultiPatternString(mpattern, ";");
        Assert.assertTrue("null pattern should return an empty collection", result.isEmpty());

        mpattern = "";
        result = SimplePatternMatcher.parseMultiPatternString(mpattern, ";");
        Assert.assertTrue("empty pattern should return an empty collection", result.isEmpty());

        mpattern = "   \n\t   ";
        result = SimplePatternMatcher.parseMultiPatternString(mpattern, ";");
        Assert.assertTrue("trimmed empty pattern should return an empty collection", result.isEmpty());

        mpattern = ";   ;   ;   ;  ;;   ;";
        result = SimplePatternMatcher.parseMultiPatternString(mpattern, ";");
        Assert.assertTrue("multiple (trimmed) empty pattern should return an empty collection", result.isEmpty());

        mpattern = "/faces/*;;*.jsf";
        result = SimplePatternMatcher.parseMultiPatternString(mpattern, ";");
        Assert.assertTrue("'" + mpattern + "' should return an collection of size==2", result.size() == 2);
    }

    /**
     * Test method for {@link com.sun.jsftemplating.util.SimplePatternMatcher#regexify(java.lang.CharSequence)}.
     */
    @Test
    public void testRegexify() {
        long start = System.nanoTime();

        String source = null;
        String expected = null;
        Exception e = null;
        try {
            SimplePatternMatcher.regexify(source);
        } catch (NullPointerException npe) {
            e = npe;
        }
        Assert.assertNotNull(e);
        e = null;

        source = "";
        expected = source;
        Assert.assertEquals(SimplePatternMatcher.regexify(source), expected);

        source = "/something/without/any/special/chars";
        expected = source;
        Assert.assertEquals(SimplePatternMatcher.regexify(source), expected);

        source = "testingOne.inTheViewId";
        expected = "testingOne\\.inTheViewId";
        Assert.assertEquals(SimplePatternMatcher.regexify(source), expected);

        source = "/testing.points/in.the./view.id";
        expected = "/testing\\.points/in\\.the\\./view\\.id";
        Assert.assertEquals(SimplePatternMatcher.regexify(source), expected);

        source = "testingOne*inTheViewId";
        expected = "testingOne(.*)inTheViewId";
        Assert.assertEquals(SimplePatternMatcher.regexify(source), expected);

        source = "testing*Multiple*in*The*View*Id";
        expected = "testing(.*)Multiple(.*)in(.*)The(.*)View(.*)Id";
        Assert.assertEquals(SimplePatternMatcher.regexify(source), expected);

        source = "testingQuestionMarks?";
        expected = "testingQuestionMarks.";
        Assert.assertEquals(SimplePatternMatcher.regexify(source), expected);
        long end = System.nanoTime();
        System.out.println("regexify() took: " + ((end - start) / 1000) + " mics");

    }

    private static final class MatchTest {
        private SimplePatternMatcher matcher;
        private String[] matching;
        private String[] nonMatching;

        MatchTest(String pattern) {
            this.matcher = new SimplePatternMatcher(pattern);
        }

        void setMatching(String... strings) {
            this.matching = strings;
        }

        void setNonMatching(String... strings) {
            this.nonMatching = strings;
        }

        void test() {
            for (String match : matching) {
                Assert.assertTrue(match + " should match " + this.matcher, this.matcher.matches(match));
            }
            for (String dontMatch : nonMatching) {
                Assert.assertFalse(dontMatch + " should not match " + this.matcher, this.matcher.matches(dontMatch));
            }
        }
    }
}
