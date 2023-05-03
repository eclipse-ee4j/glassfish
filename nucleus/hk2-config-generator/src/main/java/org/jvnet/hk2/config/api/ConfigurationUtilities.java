/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.api;

import java.util.List;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.config.AttributeChanges;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionCallBack;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.WriteableView;

/**
 * @author jwells
 *
 */
@Contract
public interface ConfigurationUtilities {
    /**
     * Adds this child bean to the parent with the given attributes.  Does not
     * start or end a configuration transaction
     *
     * @param param
     * @param parent
     * @param childType
     * @param attributes
     * @param runnable
     * @return
     * @throws TransactionFailure
     */
    Object addChildWithAttributes(ConfigBeanProxy param,
            ConfigBean parent,
            Class<? extends ConfigBeanProxy> childType,
            List<AttributeChanges> attributes,
            TransactionCallBack<WriteableView> runnable) throws TransactionFailure;

}
