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

package org.glassfish.internal.api;

import javax.security.auth.Subject;

import org.jvnet.hk2.annotations.Contract;

/**
 * Represents an internal system admin.
 * <p>
 * An internal system admin is useful for running commands using
 * the command framework from trusted server code that does not have a
 * previously-authenticated subject on whose behalf the work is being done.
 * For example, autodeployment runs automatically, without a user-linked
 * subject, and internally submits a "deploy" command for execution.
 * <p>
 * It's safer to require such uses to specify an internal system admin rather than
 * to assume that if no subject is specified to authorization then it should
 * be treated as a system admin.
 *
 * @author tjquinn
 */
@Contract
public interface InternalSystemAdministrator {

    public Subject getSubject();

}
