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

package com.sun.enterprise.security.jauth;

/**
 * A marker interface that may be implemented by a CallBackHandler.
 * <p>
 * When a CallbackHandler that implements this interface, is passed as an argument to the getServerAuthContext or
 * getClientAuthContext methods of the AuthConfig class, the AuthConfig system will wrap the handler in a special
 * internal CallbackHandler that will delegate any unsupported Callbacks to the default CallbackHandler of the
 * AuthConfig system. The modules of the context will receive this wrapping handler at initialization, and the effect
 * will be to allow systems to extend or override the callbacks handled by the default handler of the config system with
 * those handled by the wrapped handler.
 *
 * @version %I%, %G%
 */
public interface DependentCallbackHandler {
}
