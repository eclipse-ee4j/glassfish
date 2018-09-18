/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * KEDAR/MURALI has made some changes to this class
 * so that it works with installer(LogDomains esp.).
 */
package admin.util;

import java.io.*;


import java.nio.channels.*;
import java.nio.ByteBuffer;

public class FileUtils {
    /**
     * Copies a file.
     *
     * @param from Name of file to copy
     * @param to   Name of new file
     * @throws IOException if an error while copying the content
     */
    private static void copy(String from, String to) throws IOException {
        //if(!StringUtils.ok(from) || !StringUtils.ok(to))
        if (from == null || to == null)
            throw new IllegalArgumentException("null or empty filename argument");

        File fin = new File(from);
        File fout = new File(to);

        copy(fin, fout);
    }

    public static void copy(File fin, File fout) throws IOException {

        InputStream inStream = new BufferedInputStream(new FileInputStream(fin));
        FileOutputStream outStream = new FileOutputStream(fout);
        copy(inStream, outStream, fin.length());
    }

    private static void copy(InputStream in, FileOutputStream out, long size) throws IOException {

        try {
            copyWithoutClose(in, out, size);
        }
        finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    private static void copyWithoutClose(InputStream in, FileOutputStream out, long size) throws IOException {

        ReadableByteChannel inChannel = Channels.newChannel(in);
        FileChannel outChannel = out.getChannel();
        outChannel.transferFrom(inChannel, 0, size);
    }

    private static void copy(InputStream in, OutputStream os, long size) throws IOException {
        if (os instanceof FileOutputStream) {
            copy(in, (FileOutputStream) os, size);
        }
        else {
            ReadableByteChannel inChannel = Channels.newChannel(in);
            WritableByteChannel outChannel = Channels.newChannel(os);
            if (size == 0) {

                ByteBuffer byteBuffer = ByteBuffer.allocate(10240);
                int read;
                do {
                    read = inChannel.read(byteBuffer);
                    if (read > 0) {
                        byteBuffer.limit(byteBuffer.position());
                        byteBuffer.rewind();
                        outChannel.write(byteBuffer);
                        byteBuffer.clear();
                    }
                }
                while (read != -1);
            }
            else {
                ByteBuffer byteBuffer = ByteBuffer.allocate(Long.valueOf(size).intValue());
                inChannel.read(byteBuffer);
                byteBuffer.rewind();
                outChannel.write(byteBuffer);
            }
        }
    }
    public static void main(String... args) {
        try {
            copy(args[0], args[1]);
        }
        catch (IOException ex) {
            System.out.println("ERROR: " + ex);
        }
    }
}
