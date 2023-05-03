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

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Map;

import org.jvnet.hk2.component.MultiMap;
import org.jvnet.hk2.config.Configured;

/**
 * Represents a configuration transaction from an outside
 * configuration system.
 *
 * <p/>
 * In all of the methods that take configuration beans, it is
 * expected that those objects are annotated with
 * {@link Configured}.  Additionally, the object identity
 * of the bean must be unique; so that a call to delete
 * a bean must correspond to some previous addition of
 * the bean from earlier by object identity.
 *
 * @author Jeff Trent
 */
public interface ConfigTransaction {

  /**
   * Add configuration beans to the transaction.
   *
   * @param created - the configuration bean instance being created
   * @param name - optionally the name for the configuration
   * @param metadata - name,value(s)
   */
  void created(Object created, String name, MultiMap<String, String> metadata);

  /**
   * Mark configuration beans as having been updated (note that name and metadata cannot change here).
   */
  void updated(Object updatedConfigBean, PropertyChangeEvent event);
  void updated(Collection<?> updatedConfigBeans);

  /**
   * Marks configuration beans as having been deleted.
   **/
  void deleted(Object deletedConfigBean);
  void deleted(Collection<?> deletedConfigBeans);

  /**
   * Locks changes, calls prepare.
   *
   * @throws ConfigTransactionException
   */
  void prepare() throws ConfigTransactionException;

  /**
   * Locks changes, calls prepare (if not yet performed), followed by commit if no prepare errors.
   * If prepare errors exists, calls rollback on the constituent configuration beans.
   *
   * @throws ConfigTransactionException
   */
  void commit() throws ConfigTransactionException;

  /**
   * Same basic behavior as {@link #commit()} with the added ability to substitute configuration
   * beans used in the prepare phase with the final bean object replacements that should be managed.
   *
   * <p/>
   * This is an important variant when the configuration beans in the prepare phase are transient
   * in nature.
   *
   * @param finalBeanMapping
   *    mapping from the bean instance used in prepare, with the final version that should be managed
   *
   * @throws ConfigTransactionException
   */
  void commit(Map<Object, Object> finalBeanMapping) throws ConfigTransactionException;

  /**
   * Cancels the transaction, locking it out from further changes.
   */
  void rollback();

}
