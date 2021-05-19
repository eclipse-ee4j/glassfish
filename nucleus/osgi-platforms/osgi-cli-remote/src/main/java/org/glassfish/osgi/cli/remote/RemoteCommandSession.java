/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi.cli.remote;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.UUID;
import org.apache.felix.service.command.CommandSession;

/**
 * This delegating class is used to overcome some limitations of the
 * {@link CommandSession} interface when it comes to session management.
 *
 * <p>
 * Once implementations are mature enough to not assume environmental behavior
 * this class will become obsolete.
 * </p>
 *
 * @author ancoron
 */
public class RemoteCommandSession {

    private final CommandSession delegate;
    private final String id;

    public RemoteCommandSession(CommandSession delegate)
    {
        this.delegate = delegate;
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Get the identifier for this session, which is a UUID of type 4.
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Attached the specified streams to the delegate of this instance and
     * returns the modified delegate.
     *
     * @param in The "stdin" stream for the session
     * @param out The "stdout" stream for the session
     * @param err The "stderr" stream for the session
     *
     * @return The modified {@link CommandSession} delegate
     *
     * @see #detach()
     */
    public CommandSession attach(InputStream in, PrintStream out, PrintStream err) {
        set(this.delegate, "in", in);
        set(this.delegate, "out", out);
        set(this.delegate, "err", err);
        ReadableByteChannel inCh = Channels.newChannel(in);
        WritableByteChannel outCh = Channels.newChannel(out);
        WritableByteChannel errCh = out == err ? outCh : Channels.newChannel(err);
        set(this.delegate, "channels", new Channel[] {inCh, outCh, errCh});
        return this.delegate;
    }

    /**
     * Detaches all previously attached streams and hence, ensures that there
     * are no stale references left.
     *
     * @see #attach(java.io.InputStream, java.io.PrintStream, java.io.PrintStream)
     */
    public void detach() {
        set(this.delegate, "in", null);
        set(this.delegate, "out", null);
        set(this.delegate, "err", null);
    }

    private void set(final Object obj, final String field, final Object value) {
        try {
            final Field f = obj.getClass().getDeclaredField(field);
            final boolean accessible = f.isAccessible();
            if(!accessible) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {

                    @Override
                    public Void run() {
                        f.setAccessible(true);
                        try {
                            f.set(obj, value);
                        } catch(Exception x) {
                            throw new RuntimeException(x);
                        }

                        // reset to previous state...
                        f.setAccessible(accessible);
                        return null;
                    }
                });
            } else {
                f.set(obj, value);
            }
        } catch(Exception x) {
            throw new RuntimeException(x);
        }
    }
}
