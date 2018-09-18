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

package amxtest;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.glassfish.admin.amx.core.*;
import org.glassfish.admin.amx.base.*;
import org.glassfish.admin.amx.monitoring.*;
import org.glassfish.admin.amx.j2ee.*;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.logging.Logging;


/** Maps types to interface to maximize explicit testing by introspecting the interface */
class Interfaces {
    private final Map<String, Class<? extends AMXProxy>> mInterfaces;
    public Interfaces()
    {
        mInterfaces = new HashMap<String, Class<? extends AMXProxy>>();
        
        add(
            DomainRoot.class,
            Ext.class,
            Pathnames.class,
            Query.class,
            BulkAccess.class,
            Realms.class,
            RuntimeRoot.class,
            SystemInfo.class,
            Sample.class,
            Tools.class,
            Logging.class,
            MonitoringRoot.class,
            ServerMon.class
        );
    }

        public final void
    add( final Class<? extends AMXProxy>...  args )
    {
        for( final Class<? extends AMXProxy> clazz : args )
        {
            add( clazz );
        }
    }
    
    public final void add( final Class<? extends AMXProxy> clazz )
    {
        final String type = Util.deduceType(clazz);
        if ( mInterfaces.get(type) != null )
        {
            throw new IllegalArgumentException("Interface already exists for type " + type );
        }
        
        mInterfaces.put( type, clazz );
    }
    
    public List<Class<? extends AMXProxy>> all()
    {
        return new ArrayList( mInterfaces.values() );
    }
    
    public Class<? extends AMXProxy> get(final String type)
    {
        Class<? extends AMXProxy> intf = mInterfaces.get(type);
        
        if ( intf == null )
        {
            // a type is not required to have an interface
            //System.out.println( "No AMXProxy interface for type: " + type + " (undesirable, but OK)" );
            intf = AMXProxy.class;
        }
        
        return intf;
    }
}


































