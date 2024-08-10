/*
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.impl.j2ee;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.ObjectName;

public final class MetadataImpl implements Metadata {
    private final ConcurrentMap<String,Object>  mData;

    public MetadataImpl(final Map<String,Object> data )
    {
        mData = new ConcurrentHashMap<String,Object>(data);
    }

    public MetadataImpl( final Metadata meta)
    {
        this( meta.getAll() );
    }

    public MetadataImpl()
    {
        mData = new ConcurrentHashMap<String,Object>();
    }

    public Map<String,Object> getAll()
    {
        return Collections.unmodifiableMap(mData);
    }

    public void add( final String key, final Object value)
    {
        mData.put( key, value);
    }

    public void remove( final String key)
    {
        mData.remove( key );
    }


    public void setCorrespondingConfig( final ObjectName config)
    {
        add( CORRESPONDING_CONFIG, config);
    }

    public ObjectName getCorrespondingConfig()
    {
        return getMetadata( CORRESPONDING_CONFIG, ObjectName.class);
    }

    public ObjectName getCorrespondingRef()
    {
        return getMetadata( CORRESPONDING_REF, ObjectName.class);
    }

    public void setCorrespondingRef( final ObjectName config)
    {
        add( CORRESPONDING_REF, config);
    }

    public String getDeploymentDescriptor()
    {
        return getMetadata( DEPLOYMENT_DESCRIPTOR, String.class);
    }
    public void setDeploymentDescriptor(final String desc)
    {
        if ( desc == null )
        {
            throw new IllegalArgumentException( "setDeploymentDescriptor: null descriptor" );
        }
        add( DEPLOYMENT_DESCRIPTOR, desc);
    }

    public <T> T getMetadata(final String name, final Class<T> clazz)
    {
        final Object value = mData.get(name);

        return clazz.cast(value);
    }
}
