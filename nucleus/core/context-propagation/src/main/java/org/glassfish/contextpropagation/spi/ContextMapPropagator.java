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

package org.glassfish.contextpropagation.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.internal.AccessControlledMap;
import org.glassfish.contextpropagation.wireadapters.WireAdapter;

/**
 * Used by messaging protocols to integrate with the context-propagation framework.
 */
public interface ContextMapPropagator {
  /**
   * Transfers the entries with the specified {@link org.glassfish.contextpropagation.PropagationMode}
   * from {@link org.glassfish.contextpropagation.ContextMap}s,
   * in thread-local storage, to
   * the protocol {@link java.io.OutputStream} while it is
   * sending an out-bound request. This must be called at the
   * appropriate time before any thread context-switches.  This call
   * does not affect the contents of thread-local storage in any way.
   *
   * @param out The{@link java.io.OutputStream} that the protocol wants the data written to
   * @param propagationMode The {@link org.glassfish.contextpropagation.PropagationMode} being
   * utilized in this call. Only entries that support that propagation mode are propagated.
   * @exception IOException if the data cannot be serialized.
   */
  public void sendRequest(OutputStream out, PropagationMode propagationMode) throws IOException;

  /**
   * Transfers the entries with the specified {@link org.glassfish.contextpropagation.PropagationMode}
   * from {@link org.glassfish.contextpropagation.ContextMap}s,
   * in thread-local storage, to
   * the protocol {@link java.io.OutputStream} while it is
   * sending an out-bound response. This must be called at the
   * appropriate time before any thread context-switches.  This call
   * does not affect the contents of thread-local storage in any way.
   *
   * @param out The{@link java.io.OutputStream} that the protocol wants the data written to
   * @param propagationMode The {@link org.glassfish.contextpropagation.PropagationMode} being
   * utilized in this call. Only entries that support that propagation mode are propagated.
   * @exception IOException if the data cannot be serialized.
   */
  public void sendResponse(OutputStream out, PropagationMode propagationMode) throws IOException;

  /**
   * Deserializes context from an {@link java.io.InputStream} provided by
   * a protocol that is receiving a request. This must be
   * called at the appropriate time after any thread context-switches.
   * All existing thread-local contexts are overwritten, although in
   * general the thread execution model should ensure that there are
   * no existing thread-local contexts.
   *
   * While the receiver will attempt to read all and only the context propagation
   * data, it may not do so under unusual circumstances such as when there is a
   * bug in a third-party context implementation. For that reason, if IOException
   * is thrown, the sender is responsible for positioning the stream to the point
   * immediately after the context-propagation data.
   *
   * @param in A {@link java.io.InputStream} provided by the protocol and containing the serialized contexts
   * serialized context propagation bytes and no more.
   * @exception IOException if the data cannot be read.
   */
  public void receiveRequest(InputStream in) throws IOException;

  /**
   * Deserializes context from an {@link java.io.InputStream} provided by
   * a protocol that is receiving a request. This must be
   * called at the appropriate time after any thread context-switches.
   * All existing thread-local contexts with the specified propagation mode are
   * removed before the context entries are read from the specified input stream
   * <code>in</code> may be null which means that the remote server removed
   * all of the contexts with propagation modes that include the specified
   * propagation mode.
   *
   * @param in A {@link java.io.InputStream} provided by the protocol and containing the serialized contexts
   * serialized context propagation bytes and no more. Its read methods
   * must return -1 when the end of the context data has been reached
   * @param mode The {@link org.glassfish.contextpropagation.PropagationMode}
   * associated to the protocol invoking this method.
   * @exception IOException if the data cannot be read.
   */
  public void receiveResponse(InputStream in, PropagationMode mode) throws IOException;

  /**
   * Copies the entries that have the propagation mode THREAD to this thread's
   * ContextMap.
   *
   * @param contexts an {@link ContextMapInterceptor} obtained via
   * {@link #copyThreadContexts}.
   */
  public void restoreThreadContexts(AccessControlledMap contexts);

  /**
   * A protocol that propagates context data can choose an alternate
   * WireAdapter, and thus a different encoding format on the wire.
   * @param wireAdapter
   */
  public void useWireAdapter(WireAdapter wireAdapter);
}
