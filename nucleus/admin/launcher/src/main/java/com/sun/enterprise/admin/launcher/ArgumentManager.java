/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.launcher;

import java.util.*;

/**
 * API -- For now sticking with the draft1 API and behavior
 * This class will be handy for fixing error detection of bad input as below.
 * 
 * -name1 value1 -name2 value2 value3 value4 value5 -name3 -name4 -name5
 * --> "-name1":"value1",  "-name2":"value2", "default":"value5", "-name3":"-name4" 
 * 
 * @author bnevins
 */

public class ArgumentManager 
{
    public static Map<String,String> argsToMap(String[] sargs)
    {
        ArgumentManager mgr = new ArgumentManager(sargs);
        return mgr.getArgs();
    }
 
    public static Map<String,String> argsToMap(List<String>sargs)
    {
        ArgumentManager mgr = new ArgumentManager(sargs);
        return mgr.getArgs();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //////   ALL PRIVATE BELOW      ///////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    private ArgumentManager(String[] sargs)
    {
        args = new ArrayList<String>();
        
        for(String s : sargs)
            args.add(s);
    }

    private ArgumentManager(List<String> sargs)
    {
        args = sargs;
    }

    private Map<String, String> getArgs()
    {
        int len = args.size();
        
        // short-circuit out of here!
        if(len <= 0)
            return map;
        
        for(int i = 0; i < len; i++)
        {
            String name = args.get(i);
            
            if(name.startsWith("-"))
            {
                // throw it away if there is no value left
                if(i + 1 < len)
                {
                    map.put(name, args.get(++i));
                }
            }
            else
            {
                // default --> last one wins!
                map.put("default", args.get(i));
            }
        }
        return map;
    }

    Map<String,String>  map     = new HashMap<String,String>();
    List<String>        args;
}
