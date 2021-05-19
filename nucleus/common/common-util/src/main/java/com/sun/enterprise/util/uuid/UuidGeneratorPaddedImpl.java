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

/*
 * UuidGeneratorPaddedImpl.java
 *
 * Created on May 26, 2005, 11:54 AM
 */

package com.sun.enterprise.util.uuid;

/**
 *
 * @author  lwhite
 */
public class UuidGeneratorPaddedImpl implements UuidGenerator {

    /** Creates a new instance of UuidGeneratorPaddedImpl */
    public UuidGeneratorPaddedImpl() {
    }

    public String generateUuid() {
        return UuidUtilPadded.generateUuid();
    }

    public String generateUuid(Object obj) {
        return UuidUtilPadded.generateUuid(obj, UuidUtilPadded.DESIRED_UUID_LENGTH);
    }

}
