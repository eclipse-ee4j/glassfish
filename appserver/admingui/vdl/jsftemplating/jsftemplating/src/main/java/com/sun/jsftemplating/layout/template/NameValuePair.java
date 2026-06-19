/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.template;

/**
 * <p>
 * This class is used to represent NVP information. This information consists of 2 or 3 parts. If this is a simple Name
 * Value Pair, it will contain a <code>name</code> and a <code>value</code>. If it is an NVP that is used to map a
 * return value, then it also contains a <code>target</code> as well (which should be set to "session" or "attribute").
 * <code>name</code> and <code>target</code> contain <code>String</code> values. The <code>value</code> property
 * contains an <code>Object</code> value because it may be a <code>String</code>, <code>java.util.List</code>, or an
 * <code>array[]</code>.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class NameValuePair {
    public NameValuePair(String name, Object value, String target) {
        _name = name;
        _value = value;
        _target = target;
    }

    /**
     * <p>
     * Name accessor.
     * </p>
     */
    public String getName() {
        return _name;
    }

    /**
     * <p>
     * Value accessor.
     * </p>
     */
    public Object getValue() {
        return _value;
    }

    /**
     * <p>
     * Target accessor. If this value is non-null it can be assumed that this is an output mapping. However, if it is null
     * it cannot be assumed to be an input mapping -- it may still be an output mapping or an in-out mapping which uses EL.
     * Valid values for this are currently: (null), "pageSession", "attribute", or "session" (this list may be expanded in
     * the future).
     * </p>
     */
    public String getTarget() {
        return _target;
    }

    /**
     * <p>
     * Customized to reconstruct the NVP.
     * </p>
     */
    @Override
    public String toString() {
        if (getTarget() == null) {
            return getName() + "=\"" + getValue() + '"';
        } else {
            return getName() + "=>$" + getTarget() + '{' + getValue() + '}';
        }
    }

    private String _name = null;
    private Object _value = null;
    private String _target = null;
}
