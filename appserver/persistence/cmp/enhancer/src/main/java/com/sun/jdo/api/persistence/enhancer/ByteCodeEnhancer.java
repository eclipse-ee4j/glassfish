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

package com.sun.jdo.api.persistence.enhancer;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * A JDO enhancer, or byte-code enhancer, modifies the byte-codes of
 * Java class files to enable transparent loading and storing of the
 * fields of the persistent instances.
 */
public interface ByteCodeEnhancer
{
    /**
     * Enhances a given class according to the JDO meta-data. If the
     * input class has been enhanced or not - the output stream is
     * always written, either with the enhanced class or with the
     * non-enhanced class.
     *
     * @param inByteCode  The byte-code of the class to be enhanced.
     * @param outByteCode The byte-code of the enhanced class.
     *
     * @return  <code>true</code> if the class has been enhanced,
     *          <code>false</code> otherwise.
     */
   boolean enhanceClassFile(InputStream inByteCode,
                            OutputStream outByteCode)
        throws EnhancerUserException, EnhancerFatalError;


    /**
     * Enhances a given class according to the JDO meta-data. If the
     * input class has been enhanced or not - the output stream is
     * always written, either with the enhanced class or with the
     * non-enhanced class.
     * <br>
     * Furthermore the enhancer has to set the classname of
     * the enhanced class to the output stream wrapper object (it's
     * possible to get the input stream without knowing the classname).
     *
     * @param in  The byte-code of the class to be enhanced.
     * @param out The byte-code of the enhanced class.
     *
     * @return  <code>true</code> if the class has been enhanced,
     *          <code>false</code> otherwise.
     */
    boolean enhanceClassFile (InputStream         in,
                              OutputStreamWrapper out)
            throws EnhancerUserException,
                   EnhancerFatalError;

}
