/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina.fileupload;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;


/**
 * Unit tests {@link org.apache.commons.fileupload.MultipartStream}.
 *
 * @version $Id$
 */
public class MultipartStreamTest {

    static private final String BOUNDARY_TEXT = "myboundary";

    /**
     * The Carriage Return ASCII character value.
     */
    public static final byte CR = 0x0D;


    /**
     * The Line Feed ASCII character value.
     */
    public static final byte LF = 0x0A;


    /**
     * The dash (-) ASCII character value.
     */
    public static final byte DASH = 0x2D;

    @Test
    public void testThreeParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        byte[] BOUNDARY_PREFIX = {CR, LF, DASH, DASH};
        int iBufSize =
                boundary.length + BOUNDARY_PREFIX.length + 1;
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSmallBuffer() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        int iBufSize = 1;
        @SuppressWarnings("unused")
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
    }

    @Test
    public void testTwoParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        InputStream input = new ByteArrayInputStream(contents);
        byte[] boundary = BOUNDARY_TEXT.getBytes();
        MultipartStream ms = new MultipartStream(
                input,
                boundary,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

}
