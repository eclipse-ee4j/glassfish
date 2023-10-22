/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config.provider;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.config.Configured;

/**
 * Provides {@link Configured} bean integration with
 * external configuration systems and providers.
 *
 * @author Jeff Trent
 */
@Contract
public interface ConfigTransactionFactory {

  /**
   * Gets the active transaction, optionally creating
   * a new transaction if no transaction is active.
   *
   * @param create
   *    indicates that a new transaction should be
   *    started if no active transaction open yet
   * @return the ConfigTransaction
   */
  ConfigTransaction getActiveTransaction(boolean create);

}
