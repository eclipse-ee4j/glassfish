/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.jauth;

import java.util.Map;

/**
 * Authentication context for login modules.
 *
 * @author David Matejcek
 */
public interface AuthContext {

    /**
     * Dispose of the Subject (remove Principals or credentials from the Subject object that were
     * stored during validation.
     * <p>
     * This method invokes configured modules to dispose the Subject.
     *
     * @param subject the subject to be disposed.
     * @param sharedState a Map for modules to save state across a sequence of calls
     *            from <code>validateRequest</code> to <code>secureResponse</code> to
     *            <code>disposeSubject</code>.
     * @throws AuthException if the operation failed.
     */
    void disposeSubject(javax.security.auth.Subject subject, Map sharedState) throws AuthException;

}
