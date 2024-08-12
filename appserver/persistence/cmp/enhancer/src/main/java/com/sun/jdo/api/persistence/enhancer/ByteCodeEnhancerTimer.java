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

import com.sun.jdo.api.persistence.enhancer.util.Support;

import java.io.InputStream;
import java.io.OutputStream;


//@lars: changes to reflect to new ByteCodeEnhancer interface


//@olsen: new class
public final class ByteCodeEnhancerTimer
    extends Support
    implements ByteCodeEnhancer
{
    // delegate
    final protected ByteCodeEnhancer delegate;

    /**
     * Creates an instance.
     */
    public ByteCodeEnhancerTimer(ByteCodeEnhancer delegate)
    {
        affirm(delegate);
        this.delegate = delegate;
    }

    public boolean enhanceClassFile(InputStream inByteCode,
                                    OutputStream outByteCode)
        throws EnhancerUserException, EnhancerFatalError
    {
        try {
            timer.push("ByteCodeEnhancer.enhanceClassFile(InputStream,OutputStream)");//NOI18N
            return delegate.enhanceClassFile(inByteCode, outByteCode);
        } finally {
            timer.pop();
        }
    }

    public boolean enhanceClassFile(InputStream         inByteCode,
                                    OutputStreamWrapper outByteCode)
        throws EnhancerUserException, EnhancerFatalError
    {
        try {
            timer.push("ByteCodeEnhancer.enhanceClassFile(InputStream,OutputStreamWrapper)");//NOI18N
            return delegate.enhanceClassFile(inByteCode, outByteCode);
        } finally {
            timer.pop();
        }
    }
}
