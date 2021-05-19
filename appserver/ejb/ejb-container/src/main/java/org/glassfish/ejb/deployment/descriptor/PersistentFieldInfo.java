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

package org.glassfish.ejb.deployment.descriptor;

import java.io.Serializable;
import java.lang.reflect.Field;


/**
 * This class stores information about CMP and Foreign Key fields.
 * Note: the "field" variable is only available at runtime.
 *
 * @author Sanjeev Krishnan
 */

public final class PersistentFieldInfo implements Serializable {

    public PersistentFieldInfo() {}

    public PersistentFieldInfo(PersistentFieldInfo other) {
        field       = other.field;
        name        = other.name;
        type        = other.type;
        relatedName = other.relatedName;
        relatedObj  =  other.relatedObj;
    }

    public transient Field field;
    public String name;
    public transient Class type;

    // For fkey fields (including fields in Join objects)
    // these two fields are the corresponding pkey field and related object,
    // For CMP fields these are null.
    public String relatedName;
    public PersistenceDescriptor relatedObj;
}
