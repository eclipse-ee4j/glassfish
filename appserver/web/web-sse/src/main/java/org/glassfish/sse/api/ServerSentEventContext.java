/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifier for ServerSentEventHandlerContext. If there is Server-Sent Event
 * source, its context can be injected in any EE component. This qualifier
 * identifies the the event source by path.
 *
 * <p>
 * For e.g.:
 * <pre><code>
 * &#64;ServerSentEvent("/foo")
 * public class Foo extends ServerSentEventHandler {
 *     &#64;Inject &#64;ServerSentEventContext("/foo")
 *     ServerSentEventHandlerContext ctxt;
 *
 *     ...
 * }
 * </code></pre>
 *
 * @see ServerSentEventHandlerContext
 * @author Jitendra Kotamraju
 * @author Santiago.PericasGeertsen@oracle.com
 */
@Qualifier
@Target(value = {ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ServerSentEventContext {
    /**
     * The URL pattern/path of the source of server-sent events
     */
    public String value() default "";
}
