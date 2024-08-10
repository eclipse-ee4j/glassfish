/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.api;

import java.io.Serializable;

/**
 *
 * @author Kenneth Saks
 */

import org.jvnet.hk2.annotations.Contract;

/**
 * Various container services needed by other modules. E.g., the CDI integration module.
 */

@Contract
public interface EjbContainerServices extends Serializable {

    <S> S getBusinessObject(Object ejbRef, java.lang.Class<S> sClass);

    void remove(Object ejbRef);

    boolean isRemoved(Object ejbRef);

    boolean isEjbManagedObject(Object ejbDesc, Class c);

}
