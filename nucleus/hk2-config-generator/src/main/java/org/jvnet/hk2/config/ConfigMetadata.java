/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

/**
 * Constant names used in the metadata for configurable inhabitants.
 *
 * @author Kohsuke Kawaguchi
 */
public class ConfigMetadata {
    /**
     * Fully qualified name of the target class that an injector works with.
     */
    public static final String TARGET = "target";

    /**
     * Contracts that the target type implements.
     */
    public static final String TARGET_CONTRACTS = "target-contracts";

    /**
     * Target habitats in which this service should reside.
     */
    public static final String TARGET_HABITATS = "target-habitats"; //Should be same as GeneratorRunner.TARGET_HABITATS

    /**
     * If the {@link #TARGET target type} is keyed, the FQCN that defines
     * the key field. This type is always assignable from the target type.
     *
     * This allows a symbol space to be defined on a base class B, and
     * different subtypes can participate.
     */
    public static final String KEYED_AS = "keyed-as";

    /**
     * The name of the property used as a key for exposing inhabitants,
     * as well as resolving references.
     *
     * <p>
     * This is either "@attr" or "&lt;element>" indicating
     * where the key is read.
     *
     * @see Attribute#key()
     * @see Element#key()
     */
    public static final String KEY = "key";
}
