/*
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

package com.iplanet.ias.security.auth.realm;

import com.sun.enterprise.security.BaseRealm;

/**
 * Parent class for iAS Realm classes.
 *
 *  This class no longer implements the methods of Realm, instead it extends
 *  from BaseRealm and now is only a place holder for migration and is a
 *  candidate for deprecation.
 *  This class is provided for migration of realms written for 7.0 to 8.x
 */

public abstract class IASRealm extends BaseRealm{
}
