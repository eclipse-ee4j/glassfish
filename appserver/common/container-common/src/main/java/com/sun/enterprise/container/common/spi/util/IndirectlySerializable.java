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

package com.sun.enterprise.container.common.spi.util;

import org.jvnet.hk2.annotations.Contract;

/**
 * An interface that allows Non-Serializable objects to be persisted. Any non
 * serializable object that needs to be persisted needs to implement this
 * interface. The getSerializableObjectFactory() method will be called to get a
 * SerilizableObjectFactory that can be persisted. The SerializableObjectFactory
 * can later be de-serialized and the createObject() will be invoked to get the
 * original Non-Serializable object. It is assumed that the
 * SerializableObjectFactory contains enough data that can be used to restore
 * the original state of the object that existed at the time of Serilization
 *
 * @author Mahesh Kannan
 */
@Contract
public interface IndirectlySerializable {

    public SerializableObjectFactory getSerializableObjectFactory()
            throws java.io.IOException;

}
