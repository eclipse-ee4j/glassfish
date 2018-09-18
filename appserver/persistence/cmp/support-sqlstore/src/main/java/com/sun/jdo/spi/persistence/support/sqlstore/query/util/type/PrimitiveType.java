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
 * PrimitiveType.java
 *
 * Created on March 8, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.query.util.type;

/**
 * Super class for boolean type, char type,
 * and all numeric types.
 *
 * @author  Michael Bouschen
 * @version 0.1
 */
public class PrimitiveType
  extends Type
{
    /**
     *
     */
    protected WrapperClassType wrapper;

    /**
     *
     */
    public PrimitiveType(String name, Class clazz, int enumType)
    {
        super(name, clazz, enumType);
    }

    /**
     *
     */
    public boolean isCompatibleWith(Type type)
    {
        if (type instanceof PrimitiveType)
            return ((PrimitiveType)type).clazz.isAssignableFrom(clazz);
        else
            return false;
    }

    /**
     *
     */
    public WrapperClassType getWrapper()
    {
        return wrapper;
    }

    /**
     *
     */
    public void setWrapper(WrapperClassType wrapper)
    {
        this.wrapper = wrapper;
    }


}
