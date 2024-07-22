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

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * Grizzly config elements like protocols, filters, which implement this
 * interface, will get correspondent dom configuration element.
 *
 * @author Alexey Stashok
 */
public interface ConfigAwareElement<E extends ConfigBeanProxy> {
    void configure(ServiceLocator habitat, NetworkListener networkListener, E configuration);
}
