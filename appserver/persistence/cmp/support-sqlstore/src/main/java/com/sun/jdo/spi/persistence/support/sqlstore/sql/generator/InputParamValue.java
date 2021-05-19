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
 * InputParamValue.java
 *
 * Created on March 13, 2002
 * @author  Daniel Tonn
 */
package com.sun.jdo.spi.persistence.support.sqlstore.sql.generator;

import org.netbeans.modules.dbschema.ColumnElement;

/**
 * An extended InputValue which represents a query input parameter value.
 * This class holds the index of the parameter, which is used
 * to get the real value for binding the input parameter.
 */
public class InputParamValue extends InputValue
{
    /**
     * Constructor.
     * @param index parameter index
     * @param columnElement The ColumnElment to which this parameter will be
     * bound
     */
    public InputParamValue(Integer index, ColumnElement columnElement) {
        super(index, columnElement);
    }

    public Integer getParamIndex() {
        return (Integer) getValue();
    }
}
