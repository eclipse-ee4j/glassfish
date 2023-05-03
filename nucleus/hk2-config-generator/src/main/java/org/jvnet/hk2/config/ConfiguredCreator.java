/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import org.jvnet.hk2.config.provider.internal.Creator;

/**
 * {@link Creator} decorator that uses {@link ConfigInjector} to set values to objects
 * that are created.
 *
 * @author Kohsuke Kawaguchi
 */
class ConfiguredCreator<T> implements Creator<T> {
    private final Creator<T> core;
    private final Dom dom;

    public ConfiguredCreator(Creator<T> core, Dom dom) {
        this.core = core;
        this.dom = dom;
    }

    public T create() {
        T retVal = core.create();
        initialize(retVal);
        return retVal;
    }

    private void initialize(T t) {
        injectConfig(t);
    }

    private void injectConfig(T t) {
        dom.inject(t);
    }
}
