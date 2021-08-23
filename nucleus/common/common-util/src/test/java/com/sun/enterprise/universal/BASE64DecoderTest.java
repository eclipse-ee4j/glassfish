/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal;

import java.io.IOException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * @author bnevins
 */
public class BASE64DecoderTest {

    private static final String[] ss = new String[] {"foo", "QQ234bbVVc", "\n\n\r\f\n"};

    @Test
    public void testEncodeDecode() throws IOException{
        GFBase64Encoder encoder = new GFBase64Encoder();
        GFBase64Decoder decoder = new GFBase64Decoder();

        for(String s : ss) {
            byte[] stringAsByteBuf = s.getBytes();
            String enc = encoder.encode(stringAsByteBuf);
            assertNotEquals(s, enc);
            byte[] decodedByteBuf = decoder.decodeBuffer(enc);
            String dec = new String(decodedByteBuf);
            assertEquals(dec, s);
        }
    }

    @Test
    public void testEncodeDecodeAgainstSun() throws IOException{
        GFBase64Encoder gfEncoder = new GFBase64Encoder();
        GFBase64Decoder gfDecoder = new GFBase64Decoder();
        Base64.Decoder jdkDecoder = Base64.getDecoder();
        Base64.Encoder jdkEncoder = Base64.getEncoder();

        for(String s : ss) {
            byte[] stringAsByteBuf = s.getBytes();
            String gfEnc = gfEncoder.encode(stringAsByteBuf);
            String sunEnc = new String(jdkEncoder.encode(stringAsByteBuf));

            assertEquals(gfEnc, sunEnc);

            byte[] gfDecodedByteBuf = gfDecoder.decodeBuffer(gfEnc);
            byte[] sunDecodedByteBuf = jdkDecoder.decode(sunEnc);

            assertArrayEquals(gfDecodedByteBuf, sunDecodedByteBuf);

            String gfDec = new String(gfDecodedByteBuf);
            String sunDec = new String(sunDecodedByteBuf);
            assertEquals(gfDec, s);
            assertEquals(gfDec, sunDec);
        }
    }

}
