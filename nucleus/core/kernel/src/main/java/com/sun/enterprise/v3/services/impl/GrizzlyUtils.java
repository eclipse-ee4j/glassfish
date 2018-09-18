/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.services.impl;

/**
 * Set of Grizzly network utilities
 * 
 * @author Alexey Stashok
 */
public class GrizzlyUtils {
    /**
     * Reads bytes to the <code>WorkerThread</code> associated <code>ByteBuffer</code>s.
     * Could be used to read both raw and secured data.
     * 
     * @param key <code>SelectionKey</code>
     * @param timeout read timeout
     * @return number of read bytes
     * @throws java.io.IOException
     */
//    public static int readToWorkerThreadBuffers(SelectionKey key, int timeout) throws IOException {
//        Object attachment = key.attachment();
//        SSLEngine sslEngine = null;
//        if (attachment instanceof ThreadAttachment) {
//            sslEngine = ((ThreadAttachment) attachment).getSSLEngine();
//        }
//
//        WorkerThread thread = (WorkerThread) Thread.currentThread();
//
//        if (sslEngine == null) {
//            return Utils.readWithTemporarySelector(key.channel(),
//                    thread.getByteBuffer(), timeout).bytesRead;
//        } else {
//            // if ssl - try to unwrap secured buffer first
//            ByteBuffer byteBuffer = thread.getByteBuffer();
//            ByteBuffer securedBuffer = thread.getInputBB();
//
//            if (securedBuffer.position() > 0) {
//                int initialPosition = byteBuffer.position();
//                byteBuffer =
//                        SSLUtils.unwrapAll(byteBuffer, securedBuffer, sslEngine);
//                int producedBytes = byteBuffer.position() - initialPosition;
//                if (producedBytes > 0) {
//                    return producedBytes;
//                }
//            }
//
//            // if no bytes were unwrapped - read more
//            return SSLUtils.doSecureRead(key.channel(), sslEngine, byteBuffer,
//                    securedBuffer).bytesRead;
//        }
//    }
}
