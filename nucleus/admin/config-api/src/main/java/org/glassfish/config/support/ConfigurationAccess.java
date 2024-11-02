/*
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

package org.glassfish.config.support;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import org.jvnet.hk2.annotations.Contract;

/**
 * Service to lock the configuration elements for a particular domain configuration. All changes to the domain
 * configuration changes being the domain.xml or the security artifacts must go through this service to ensure proper
 * synchronization.
 *
 * The access gate must be implemented using a read-write locking where multiple users can access in read mode the
 * configuration while a write access requires a exclusive lock.
 *
 * A try {...} finally {...} block should be used to ensure the Lock returned for access is released when the access to
 * the configuration is not needed any longer.
 *
 * @author Jerome Dochez
 */
@Contract
@Deprecated(forRemoval = true, since = "7.1.0")
public interface ConfigurationAccess {

    /**
     * Wait and return an read access {@link Lock} to the configuration elements. Once the lock is returned, other threads
     * can access the configuration is read mode, but no thread can access it in write mode.
     *
     * The lock instance must be released in the same thread that obtained it.
     *
     * @return the read access lock to be released once the configuration access is not needed any longer.
     * @throws IOException if the configuration cannot be accessed due to a file access error.
     * @throws TimeoutException if the lock cannot be obtained before the system defined time out runs out.
     */
    public Lock accessRead() throws IOException, TimeoutException;

    /**
     * Wait and return an exclusive write access {@link Lock} to the configuration elements. Once the lock is returned, no
     * other thread can access the configuration is read or write mode.
     *
     * The lock instance must be released in the same thread that obtained it.
     *
     * @return the read access lock to be released once the configuration access is not needed any longer.
     * @throws IOException if the configuration cannot be accessed due to a file access error.
     * @throws TimeoutException if the lock cannot be obtained before the system defined time out runs out.
     */

    public Lock accessWrite() throws IOException, TimeoutException;
}
