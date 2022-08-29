/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.apf;

import java.util.Set;

import org.jvnet.hk2.annotations.Contract;

/**
 * This interface is responsible for scanning the binary location
 * provided and provide each binary file through a pull interfaces
 *
 * @author Jerome Dochez
 */
@Contract
public interface Scanner {

    /**
     * Returns a ClassLoader capable of loading classes from the
     * underlying medium
     *
     * @return a class loader capable of loading the classes
     */
    ClassLoader getClassLoader();

    /**
     * Return a complete set of classes available from this location.
     *
     * @return the complete set of classes
     */
    Set<Class<?>> getElements();

    /**
     * Sometimes, annotations processing requires more than a single class,
     * especially when such classes end up being a Java Component (Java Beans,
     * Java EE). The implementation returned from the getComponent will be
     * responsible for defining the complete view of this component starting
     * from it's implementation class.
     *
     * @param componentImpl class of the component.
     * @return {@link ComponentInfo}
     */
    ComponentInfo getComponentInfo(Class<?> componentImpl);

}
