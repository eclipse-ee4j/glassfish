/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.integration;

/**
 *
 * Constants used by SecurityModule and also Constants Exported by Security Module for Other Modules
 */
public interface SecurityConstants {
    //TODO: Not a very good idea, revisit.
    String WEB_PRINCIPAL_CLASS = "com.sun.enterprise.security.ee.web.integration.WebPrincipal";

    //TODO V3: Temporary till we have AppContainer integration design sorted out
    int APPCONTAINER_CERTIFICATE = 2;
}
