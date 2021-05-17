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

package com.sun.enterprise.server.logging;

import java.util.BitSet;

/**
 * @author sanshriv
 *
 */
public class ExcludeFieldsSupport {

    static enum SupplementalAttribute {TID, USERID, ECID, TIME_MILLIS, LEVEL_VALUE};

    private BitSet excludeSuppAttrsBits = new BitSet();

    void setExcludeFields(String excludeFields) {
        excludeSuppAttrsBits.clear();
        if (excludeFields != null) {
            String[] fields = excludeFields.split(",");
            for (String field : fields) {
                if (field.equals("tid")) {
                    excludeSuppAttrsBits.set(SupplementalAttribute.TID.ordinal());
                } else if (field.equals("userId")) {
                    excludeSuppAttrsBits.set(SupplementalAttribute.USERID.ordinal());
                } else if (field.equals("ecid")) {
                    excludeSuppAttrsBits.set(SupplementalAttribute.ECID.ordinal());
                } else if (field.equals("timeMillis")) {
                    excludeSuppAttrsBits.set(SupplementalAttribute.TIME_MILLIS.ordinal());
                } else if (field.equals("levelValue")) {
                    excludeSuppAttrsBits.set(SupplementalAttribute.LEVEL_VALUE.ordinal());
                }
            }
        }
    }

    boolean isSet(SupplementalAttribute attr) {
        return excludeSuppAttrsBits.get(attr.ordinal());
    }

}
