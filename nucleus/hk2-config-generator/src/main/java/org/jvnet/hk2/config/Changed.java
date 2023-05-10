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
 * Objects that do not wish to process the entire change set of a transaction can
 * implement this interface and use it to invoke utility methods on ConfigSupport.
 *
 * @author Jerome Dochez
 */
public interface Changed {

    /**
     * type of change on a particular instance
     */
    public static enum TYPE { ADD, REMOVE, CHANGE };

    /**
     * Notification of a change on a configuration object
     *
     * @param type type of change : ADD mean the changedInstance was added to the parent
     * REMOVE means the changedInstance was removed from the parent, CHANGE means the
     * changedInstance has mutated.
     * @param changedType type of the configuration object
     * @param changedInstance changed instance.
     */
    public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type, Class<T> changedType, T changedInstance);

}
