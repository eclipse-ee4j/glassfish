/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.mbeanapi.config;

import java.util.HashMap;

/**
 * This is the class for element representing object.
 * It contains element name and attributes for testing element
 * Thhis object is using in cofig related generic tests (create/delete/update/list...)
 * @author alexkrav
 * @version $Revision: 1.2 $
 */
//************************************************************************************************
public class RegEntry
{

    String name;
    String dtdName;
    String[] requiredAttrs;
    Class[]  requiredAttrClasses;
    String   masterNode;

    RegEntry(String name, String dtdName, String[] required, String masterNode)
    {
        this.name = name;
        this.dtdName = dtdName;
        this.masterNode = masterNode;
        requiredAttrs = required;
        requiredAttrClasses = new Class[requiredAttrs.length];
        Class strClass = name.getClass();
        Class intClass = Integer.TYPE;
        for(int i=0; i<requiredAttrs.length; i++)
        {
            if(requiredAttrs[i].endsWith("*int"))
            {
                requiredAttrs[i] = requiredAttrs[i].substring(0, requiredAttrs[i].length()-4);
                requiredAttrClasses[i] = intClass;
            }
            else
            {
                requiredAttrClasses[i] = strClass;
            }
        }
    }
    public String[] getReqAttrs()
    {
        return requiredAttrs;
    }
    public Class[] getReqAttrClasses()
    {
        return requiredAttrClasses;
    }
    public String  getMasterNodeName()
    {
        return masterNode;
    }
}

