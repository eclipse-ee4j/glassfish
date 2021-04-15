/*
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

package org.glassfish.config.support;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.config.ConfigBean;

/**
 * Listens to config bean creation.
 *
 * @author Jerome Dochez
 */
@Contract
public interface ConfigBeanListener {

    /**
     * Notification that a new config beans has entered the configuration tree.
     *
     * @param bean the new config bean
     */
    public void onEntered(ActiveDescriptor<ConfigBean> bean);

}
