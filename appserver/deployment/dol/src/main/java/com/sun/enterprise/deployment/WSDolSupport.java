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

package com.sun.enterprise.deployment;

import org.jvnet.hk2.annotations.Contract;

/**
 * this really should not exist but we can't move some of our DOL classes out of the
 * DOL into the respective container (jaxws in this case) so we have to use this
 * workarounds
 *
 * @author Jerome Dochez
 */
@Contract
public interface WSDolSupport {

    String getProtocolBinding(String value);

    String getSoapAddressPrefix(String protocolBinding);

    void setServiceRef(Class annotatedClass, ServiceReferenceDescriptor ref);

    Class getType(String className) throws ClassNotFoundException;
}
