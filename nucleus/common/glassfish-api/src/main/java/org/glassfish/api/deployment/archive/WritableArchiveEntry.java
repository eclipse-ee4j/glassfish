/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.api.deployment.archive;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.function.Supplier;


/**
 * Archive entry used for the output.
 *
 * @author David Matejcek
 */
public class WritableArchiveEntry extends OutputStream {

    private final Supplier<OutputStream> output;
    private final CloseAction closeAction;


    /**
     * @param output provider of the output stream to write to
     * @param closeAction what should do the entry when it is closing.
     */
    public WritableArchiveEntry(Supplier<OutputStream> output, CloseAction closeAction) {
        this.output = output;
        this.closeAction = closeAction;
    }


    @Override
    public void write(int b) throws IOException {
        output.get().write(b);
    }


    /**
     * @return output channel. No need to close it, it will be closed by the original provider.
     */
    public WritableByteChannel getChannel() {
        OutputStream out = output.get();
        return Channels.newChannel(out);
    }


    @Override
    public void close() throws IOException {
        closeAction.close();
    }


    /**
     * Action to close the entry.
     */
    @FunctionalInterface
    public interface CloseAction {

        /**
         * Action to close the entry.
         *
         * @throws IOException
         */
        void close() throws IOException;
    }
}
