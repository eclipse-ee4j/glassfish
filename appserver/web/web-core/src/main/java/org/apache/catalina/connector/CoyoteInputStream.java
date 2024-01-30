/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.connector;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.IOException;
import java.util.ResourceBundle;
import org.apache.catalina.LogFacade;

/**
 * This class handles reading bytes.
 *
 * @author Remy Maucherat
 * @author Jean-Francois Arcand
 */
public class CoyoteInputStream extends ServletInputStream {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    // ----------------------------------------------------- Instance Variables

    protected InputBuffer ib;

    // ----------------------------------------------------------- Constructors

    public CoyoteInputStream(InputBuffer ib) {
        this.ib = ib;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Prevent cloning the facade.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // -------------------------------------------------------- Package Methods

    /**
     * Clear facade.
     */
    void clear() {
        ib = null;
    }

    // --------------------------------------------- ServletInputStream Methods

    @Override
    public int read() throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ib == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));

        }

        return ib.readByte();
    }

    @Override
    public int available() throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ib == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        return ib.available();
    }

    @Override
    public int read(final byte[] b) throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ib == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        return ib.read(b, 0, b.length);
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ib == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        return ib.read(b, off, len);
    }

    @Override
    public int readLine(byte[] b, int off, int len) throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ib == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        return super.readLine(b, off, len);
    }

    @Override
    public boolean isFinished() {
        if (ib == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        return ib.isFinished();
    }

    @Override
    public boolean isReady() {
        if (ib == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        return ib.isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        if (ib == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        if (readListener == null) {
            throw new NullPointerException(rb.getString(LogFacade.NULL_READ_LISTENER_EXCEPTION));
        }

        ib.setReadListener(readListener);
    }

    /**
     * Close the stream Since we re-cycle, we can't allow the call to super.close() which would permanently disable us.
     */
    @Override
    public void close() throws IOException {
        // Disallow operation if the object has gone out of scope
        if (ib == null) {
            throw new IllegalStateException(rb.getString(LogFacade.OBJECT_INVALID_SCOPE_EXCEPTION));
        }

        ib.close();
    }
}
