/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.admin.amx.impl.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.ObjectName;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;

/**
 * Registry mapping ConfigBean to ObjectName and vice-versa.
 */
@Taxonomy( stability=Stability.NOT_AN_INTERFACE )
public final class ConfigBeanRegistry {

    public static final class MBeanInstance
    {
        public final ConfigBean mConfigBean;
        public final ObjectName mObjectName;
        public final Object     mImpl;
        public MBeanInstance( final ConfigBean cb, final ObjectName on, final Object impl )
        {
            mConfigBean = cb;
            mObjectName = on;
            mImpl = impl;
        }
    }

    private final ConcurrentMap<ConfigBean,MBeanInstance> mFromConfigBean;
    private final ConcurrentMap<ObjectName, MBeanInstance> mFromObjectName;

    private ConfigBeanRegistry() {
        mFromConfigBean = new ConcurrentHashMap<ConfigBean,MBeanInstance>();
        mFromObjectName = new ConcurrentHashMap<ObjectName, MBeanInstance>();
    }

    private static final ConfigBeanRegistry INSTANCE = new ConfigBeanRegistry();
    public static ConfigBeanRegistry getInstance() {
        return INSTANCE;
    }

    private MBeanInstance getMBeanInstance(final ObjectName objectName)
    {
        return mFromObjectName.get(objectName);
    }

    private MBeanInstance getMBeanInstance(final ConfigBean cb)
    {
        return mFromConfigBean.get(cb);
    }

    public synchronized void  add(final ConfigBean cb, final ObjectName objectName, final Object impl)
    {
        final MBeanInstance mb = new MBeanInstance(cb, objectName, impl);
        mFromConfigBean.put(cb, mb );
        mFromObjectName.put(objectName, mb);
        //debug( "ConfigBeanRegistry.add(): " + objectName );
    }

    public synchronized void  remove(final ObjectName objectName)
    {
        final MBeanInstance mb = mFromObjectName.get(objectName);
        if ( mb != null )
        {
            mFromObjectName.remove(objectName);
            mFromConfigBean.remove(mb.mConfigBean);
        }
        //debug( "ConfigBeanRegistry.remove(): " + objectName );

    }

    public ConfigBean getConfigBean(final ObjectName objectName)
    {
        final MBeanInstance mb = getMBeanInstance(objectName);
        return mb == null ? null: mb.mConfigBean;
    }

    public ObjectName getObjectName(final ConfigBean cb)
    {
        final MBeanInstance mb = getMBeanInstance(cb);
        return mb == null ? null: mb.mObjectName;
    }

    public Object getImpl(final ObjectName objectName)
    {
        final MBeanInstance mb = getMBeanInstance(objectName);
        return mb == null ? null: mb.mImpl;
    }

    public Object getImpl(final ConfigBean cb)
    {
        final MBeanInstance mb = getMBeanInstance(cb);
        return cb == null ? null: mb.mImpl;
    }

    public ObjectName getObjectNameForProxy(final ConfigBeanProxy cbp)
    {
        final Dom dom = Dom.unwrap(cbp);

        if ( dom instanceof ConfigBean )
        {
            return getObjectName( (ConfigBean)dom );
        }

        // not a config bean so return null
        return null;
    }


}




