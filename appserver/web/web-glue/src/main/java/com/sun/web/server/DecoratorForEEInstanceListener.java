/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package com.sun.web.server;

import com.sun.enterprise.web.WebModule;
import com.sun.enterprise.web.WebModuleDecorator;

import org.jvnet.hk2.annotations.Service;

/**
 * {@link WebModuleDecorator} that inserts {@link EEInstanceListener}.
 *
 * <p>
 * TODO: this implementation of the extension point shouldn't belong to the webtier. AFAIK, this should live somewhere
 * in the EJB side.
 *
 * @author Kohsuke Kawaguchi
 */
@Service
public class DecoratorForEEInstanceListener implements WebModuleDecorator {
    @Override
    public void decorate(WebModule module) {
        module.addInstanceListener(new EEInstanceListener());
    }
}
