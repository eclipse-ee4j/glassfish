/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

//ByteCodeEnhancerHelper - Java Source


//***************** package ***********************************************

package com.sun.jdo.api.persistence.enhancer;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


//#########################################################################
/**
 *  This is a helper-class to perform some useful operations outside a
 *  byte code enhancer and delegate the real work to the enhancer.
 */
//#########################################################################

public class ByteCodeEnhancerHelper
{


    /**********************************************************************
     *  Enhances a classfile.
     *
     *  @param  enhancer  The enhancer to delegate the work to.
     *  @param  in        The input stream with the Java class.
     *  @param  out       The output stream to write the enhanced class to.
     *
     *  @return  Has the input stream been enhanced?
     *
     *  @exception  EnhancerUserException  If something went wrong.
     *  @exception  EnhancerFatalError     If something went wrong.
     *
     *  @see  ByteCodeEnhancer#enhanceClassFile
     *********************************************************************/

    public static final boolean enhanceClassFile (ByteCodeEnhancer enhancer,
                                                  InputStream      in,
                                                  OutputStream     out)
                                throws EnhancerUserException,
                                       EnhancerFatalError
    {

        return enhancer.enhanceClassFile (in, new OutputStreamWrapper (out));

    }  //ByteCodeEnhancerHelper.enhanceClassFile()


    /**********************************************************************
     *  Enhances a zip file. The zip file is given as a uip input stream.
     *  It's entries are read and - if necessary - individually enhanced.
     *  The output stream has the same compression (if any) as the input
     *  stream.
     *
     *  @param  enhancer  The enhancer.
     *  @param  zip_in    The zip input stream.
     *  @param  zip_out   The zip output stream.
     *
     *
     *
     *  @exception  EnhancerUserException  If something went wrong.
     *  @exception  EnhancerFatalError     If something went wrong.
     *
     *  @see  ByteCodeEnhancer#enhanceClassFile
     *********************************************************************/

    public static final boolean enhanceZipFile (ByteCodeEnhancer enhancer,
                                                ZipInputStream   zip_in,
                                                ZipOutputStream  zip_out)
                                throws EnhancerUserException,
                                       EnhancerFatalError
    {

        boolean enhanced = false;
        try
        {
            CRC32 crc32 = new CRC32 ();
            ZipEntry entry;
            while ((entry = zip_in.getNextEntry ()) != null)
            {
                InputStream in = zip_in;
                ZipEntry    out_entry = new ZipEntry (entry);

                //try to enhance
                if  (isClassFileEntry (entry))  //enhance the classfile
                {
                    //we have to copy the classfile, because if it won't be enhanced,
                    //the OutputStream is empty and we have to re-read the InputStream,
                    //which is impossiblewith a ZipInputStream (no mark/reset)
                    in = openZipEntry (zip_in);
                    in.mark (Integer.MAX_VALUE);
                    ByteArrayOutputStream tmp = new ByteArrayOutputStream ();
                    if  (enhancer.enhanceClassFile (in, tmp))
                    {
                        enhanced = true;
                        byte [] bytes = tmp.toByteArray ();
                        tmp.close ();
                        in.close ();
                        modifyZipEntry (out_entry, bytes, crc32);
                        in = new ByteArrayInputStream (bytes);
                    }
                    else
                    {
                        //the classfile has not been enhanced
                        in.reset ();
                    }
                }

                //copy the entry
                zip_out.putNextEntry (out_entry);
                copyZipEntry (in, zip_out);
                zip_out.closeEntry ();

                if  (in != zip_in)
                {
                    in.close ();
                }
            }
        }
        catch (IOException ex)
        {
            throw new EnhancerFatalError (ex);
        }

        return enhanced;

    }  //ByteCodeEnhancerHelper.enhanceZipFile()


    /**********************************************************************
     *  Copies a zip entry from one stream to another.
     *
     *  @param  in   The inout stream.
     *  @param  out  The output stream.
     *
     *  @exception  IOException  If the stream access failed.
     *********************************************************************/

    private static final void copyZipEntry (InputStream  in,
                                            OutputStream out)
                              throws IOException
    {

        int b;
        while ((in.available () > 0)  &&  (b = in.read ()) > -1)
        {
            out.write (b);
        }

    }  //ByteCodeEnhancerHelper.copyZipEntry()


    /**********************************************************************
     *  Opens the next zip entry of a zip input stream and copies it to
     *  a <code>java.io.ByteArrayOutputStream</code>. It's byte array is made
     *  available via an <code>java.io.ByteArrayInputStream</code> which is
     *  returned.
     *
     *  @param  in  The zip input stream.
     *
     *  @return  The newly created input stream with the next zip entry.
     *
     *  @exception  IOException  If an I/O operation failed.
     *********************************************************************/

    private static final InputStream openZipEntry (ZipInputStream in)
                                     throws IOException
    {

        ByteArrayOutputStream out = new ByteArrayOutputStream ();
        copyZipEntry (in, out);

        return new ByteArrayInputStream (out.toByteArray ());

    }  //ByteCodeEnhancerHelper.openZipEntry()


    /**********************************************************************
     *  Modifies the given zip entry so that it can be added to zip file.
     *  The given zip entry represents an enhanced class, so the zip entry
     *  has to get the correct size and checksum (but only it the entry won't
     *  be compressed).
     *
     *  @param  entry  The zip entry to modify.
     *  @param  bytes  The uncompressed byte representation of the classfile.
     *  @param  crc32  The checksum evaluator.
     *********************************************************************/

    private static final void modifyZipEntry (ZipEntry entry,
                                              byte []  bytes,
                                              CRC32    crc32)
    {

        entry.setSize (bytes.length);
        if  (entry.getMethod () == 0) //no compression (ZipInputStream.STORED - not accessible)
        {
            crc32.reset ();
            crc32.update (bytes);
            entry.setCrc (crc32.getValue ());
            entry.setCompressedSize (bytes.length);
        }

    }  //ByteCodeEnhancerHelper.modifyZipEntry()


    /**********************************************************************
     *  Determines if a given entry represents a classfile.
     *
     *  @return  Does the given entry represent a classfile?
     *********************************************************************/

    private static final boolean isClassFileEntry (ZipEntry entry)
    {

        return entry.getName ().endsWith (".class");

    }  //ByteCodeEnhancerHelper.isClassFileEntry()


}  //ByteCodeEnhancerHelper


//ByteCodeEnhancer - Java Source End
