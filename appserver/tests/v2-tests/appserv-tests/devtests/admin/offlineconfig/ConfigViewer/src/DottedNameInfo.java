/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
 * DottedNameInfo.java
 *
 * Created on April 21, 2006, 1:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author kravtch
 */
public class DottedNameInfo
{
    String _name;
    String _parent;


    /** Creates a new instance of DottedNameInfo */
    public DottedNameInfo(String name, String parentName)
    {
        _name = name;
        _parent = parentName;
    }

    public String toString()
    {
       if(_name.startsWith(_parent+"."))
           return _name.substring(_parent.length()+1);
       return _name;
    }
}
