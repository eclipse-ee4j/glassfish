/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config;

import org.glassfish.grizzly.http.server.HttpHandler;

/**
 * Class represents context-root associated information
 */
public final class ContextRootInfo {

    /**
     * The interface, which is responsible for holding <tt>ContextRootInfo</tt>,
     * which makes possible to initialize <tt>ContextRootInfo<tt> lazily.
     */
    public static interface Holder {

        /**
         * Gets the Grizzly {@link HttpHandler}, associated with the context.
         *
         * @return the Grizzly {@link HttpHandler}, associated with the context.
         */
        public HttpHandler getHttpHandler();

        /**
         * Gets the application container, associated with the context.
         *
         * @return the application container, associated with the context.
         */
        public Object getContainer();
    }
    private final Holder holder;

    /**
     * Create <tt>ContextRootInfo</tt> using prepared {@link HttpHandler} and
     * application container parameters.
     *
     * @param handler Grizzly {@link HttpHandler}, associated with the context.
     * @param container application container, associated with the context.
     */
    public ContextRootInfo(final HttpHandler handler,
            final Object container) {
        holder = new SimpleHolder(handler, container);
    }

    /**
     * Create <tt>ContextRootInfo</tt> using passed {@link Holder} object, which
     * might be initialized lazily.
     *
     * @param holder context info {@link Holder}.
     */
    public ContextRootInfo(final Holder holder) {
        this.holder = holder;
    }

    /**
     * Gets the Grizzly {@link HttpHandler}, associated with the context.
     *
     * @return the Grizzly {@link HttpHandler}, associated with the context.
     */
    public HttpHandler getHttpHandler() {
        return holder.getHttpHandler();
    }

    /**
     * Gets the application container, associated with the context.
     *
     * @return the application container, associated with the context.
     */
    public Object getContainer() {
        return holder.getContainer();
    }

    private static class SimpleHolder implements Holder {

        private final HttpHandler handler;
        private final Object container;

        public SimpleHolder(HttpHandler handler, Object container) {
            this.handler = handler;
            this.container = container;
        }

        @Override
        public HttpHandler getHttpHandler() {
            return handler;
        }

        @Override
        public Object getContainer() {
            return container;
        }
    }
}
