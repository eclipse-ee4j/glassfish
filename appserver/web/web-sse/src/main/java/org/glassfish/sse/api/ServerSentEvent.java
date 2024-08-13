/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.sse.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@code ServerSentEvent} indicates a server component that pushes notifications to clients via a HTTP connection.
 * Browser applications typically use <a href="http://dev.w3.org/html5/eventsource/">EventSource</a> API to connect to
 * this source of server-sent events.
 *
 * <p>
 * Server component class needs to have {@code ServerSentEvent} annotation and provides the implementation of
 * {@code ServerSentEventHandler}. For example:
 *
 * <pre>
 * <code>
 *     &#64;ServerSentEvent("/foo")
 *     public class FooHandler extends ServerSentEventHandler {
 *         ...
 *     }
 * </code>
 * </pre>
 *
 * @author Jitendra Kotamraju
 * @author Santiago.PericasGeertsen@oracle.com
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ServerSentEvent {

    /**
     * The URL pattern of the source of server-sent events
     */
    public String value();

}
