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

import java.beans.PropertyChangeEvent;

import org.jvnet.hk2.annotations.Contract;

/**
 * Any object injected with Configured object and willing to receive notifications
 * of changes should implement this interface. The injection manager will hook up
 * automatically the injected resources with this listener implementation.
 *
 * public class Example implements ConfigListener {
 *
 *  @Inject
 *  MyConfiguredObject o;
 *
 *
 *  public void changed(PropertyChangeEvent[] events) {
 *      // notification that o changed.
 *  }
 * }
 *
 * @author Jerome Dochez
 */
@Contract
public interface ConfigListener {

    /**
     * Notification that @Configured objects that were injected have changed
     *
     * @param events list of changes
     * @return the list of unprocessed events (requiring a restart) or null if all reconfiguration
     * was processed successfully
     */
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) ;
}
