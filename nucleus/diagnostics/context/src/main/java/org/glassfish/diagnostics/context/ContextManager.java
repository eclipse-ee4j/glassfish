/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.diagnostics.context;

import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessagesResourceBundle;
import org.glassfish.logging.annotation.LoggerInfo;
import org.jvnet.hk2.annotations.Contract;

/**
 * The ContextManager provides access to diagnostics Contexts.
 */
@Contract
public interface ContextManager
{
  /**
   * The key under which instances of Context will be created
   * and found in the ContextMap.
   *
   * This key will be used by the ContextMap implementation
   * to label the data belonging to this Context when that
   * data is being propagated. Remote systems attempting to
   * use that data (e.g. to construct a propagated Context)
   * will use the value of this key to find the propagated
   * data. Therefore the value of this key should not be
   * changed...ever!
   */
  public static final String WORK_CONTEXT_KEY =
      "org.glassfish.diagnostics.context.Context";

  //
  // Logging metadata for this module.
  //
  // Message ids must be of the form NCLS-DIAG-##### where ##### is an
  // integer in the range reserved for this module: [03000, 03999].
  //
  // Message ids are to be explicit in code (rather than generated) to
  // facilitate fast locating by developers.
  //
  // Message ids are to be annotated with @LogMessageInfo

  @LogMessagesResourceBundle
  public static final String LOG_MESSAGE_RESOURCE = "org.glassfish.diagnostics.context.LogMessages";

  @LoggerInfo(subsystem="DIAG", description="Diagnostcis Context Logger", publish=true)
  public static final String LOGGER_NAME =
      "jakarta.enterprise.diagnostics.context.base";

  public static final Logger LOGGER = Logger.getLogger(LOGGER_NAME, LOG_MESSAGE_RESOURCE);

  /**
   * Get the Context associated with the currently executing task,
   * creating a new Context if required.
   */
  public Context getContext();
}
