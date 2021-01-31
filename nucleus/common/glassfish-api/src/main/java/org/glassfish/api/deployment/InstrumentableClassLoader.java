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

package org.glassfish.api.deployment;

import java.lang.instrument.ClassFileTransformer;

/**
 * Providers of class loaders for GlassFish applications can optionally implements this interface to indicate their
 * class loader is capable of byte code enhancement.
 *
 * @author Persistence Team
 */
public interface InstrumentableClassLoader {

    /**
     * Create and return a temporary loader with the same visibility as this loader. The temporary loader may be used to
     * load resources or any other application classes for the purposes of introspecting them for annotations. The
     * persistence provider should not maintain any references to the temporary loader, or any objects loaded by it.
     *
     * @return A temporary classloader with the same classpath as this loader
     */
    public ClassLoader copy();

    /**
     * Add a new ClassFileTransformer to this class loader. This transfomer should be called for each class loading event.
     *
     * @param transformer new class file transformer to do byte code enhancement.
     */
    public void addTransformer(ClassFileTransformer transformer);

}
