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

package com.sun.enterprise.web;

import org.jvnet.hk2.annotations.Contract;

/**
 * Used by {@link EmbeddedWebContainer} to give other modules an
 * opporunity to decorate/configure newly created {@link WebModule}s.
 *
 * <p>
 * TODO: I don't have enough domain expertise in the webtier to see
 * if this is how the abstraction should be defined, but this pattern
 * of hooking into various listeners of {@link WebModule} seem common
 * enough.
 *
 * Note in particular that there's no provision for controlling orders
 * of listeners.
 *
 * @author Kohsuke Kawaguchi
 */
@Contract
public interface WebModuleDecorator {
    /**
     * Invoked after the initial configuration of {@link WebModule}
     * is done. This gives
     */
    void decorate(WebModule module);
}
