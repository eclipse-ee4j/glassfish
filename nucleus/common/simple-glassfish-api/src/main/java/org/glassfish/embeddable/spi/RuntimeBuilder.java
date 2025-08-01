/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable.spi;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;


/**
 * This is an SPI for plugging in a GlassFishRuntime.
 * <p>
 * By default different implementations exist to provide different runtime
 * enviornment such as Felix/Equinox based or non-OSGi based runtime.
 */
public interface RuntimeBuilder {

    /**
     * Builds a custom GlassFishRuntime with the supplied bootstrap options
     *
     * @param options
     * @return {@link GlassFishRuntime}
     * @throws GlassFishException
     */
    default GlassFishRuntime build(BootstrapProperties options) throws GlassFishException {
        return build(options, getClass().getClassLoader());
    }


    /**
     * Builds a custom GlassFishRuntime with the supplied bootstrap options
     *
     * @param options
     * @param classloader
     * @return {@link GlassFishRuntime}
     * @throws GlassFishException
     */
    GlassFishRuntime build(BootstrapProperties options, ClassLoader classloader) throws GlassFishException;


    /**
     * @param options
     * @return Returns true if this RuntimeBuilder is capable of creating a GlassFishRuntime
     *         for the supplied BootstrapProperties
     */
    boolean handles(BootstrapProperties options);

}
