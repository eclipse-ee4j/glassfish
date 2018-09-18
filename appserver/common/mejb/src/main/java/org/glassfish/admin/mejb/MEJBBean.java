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

package org.glassfish.admin.mejb;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.*;
import javax.management.j2ee.ListenerRegistration;

import org.glassfish.external.amx.AMXGlassfish;

/**
 * @ejbHome <{org.glassfish.admin.mejb.MEJBHome}>
 * @ejbRemote <{org.glassfish.admin.mejb.MEJB}>
 */
public final class MEJBBean implements SessionBean
{
    private static final boolean debug = true;
    private static void debug( final String s ) { if ( debug ) { System.out.println(s); } }

    private volatile SessionContext ctx;
    private final MBeanServer mbeanServer = MEJBUtility.getInstance().getMBeanServer();
    private volatile String mDomain = null;

    public MEJBBean()
    {
    }
    
    public void setSessionContext(SessionContext context) {
        ctx = context;
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }
    
    public void ejbRemove() {
    }

    public void ejbCreate() throws CreateException {
        final ObjectName domainRoot = AMXGlassfish.DEFAULT.bootAMX(mbeanServer);
        
        if ( domainRoot == null )
        {
            throw new IllegalStateException( "Impossible: DomainRoot is null" );
        }
        
        mDomain = domainRoot.getDomain();
    }

    /**
        Restrict access to only JSR 77 MBeans
     */
        private Set<ObjectName>
    restrict( final Set<ObjectName> candidates )
    {
        final Set<ObjectName>  allowed = new HashSet<ObjectName>();
        for( final ObjectName candidate : candidates )
        {
            if ( oneOfOurs(candidate) )
            {
                allowed.add(candidate);
            }
        }
        return allowed;
    }

    private boolean oneOfOurs( final ObjectName candidate ) {
        return candidate != null &&
            candidate.getDomain().equals(mDomain) &&
            candidate.getKeyProperty( "j2eeType" ) != null  &&
            candidate.getKeyProperty( "name" ) != null;
    }

    private ObjectName bounce(final ObjectName o) throws InstanceNotFoundException
    {
        if ( ! oneOfOurs(o) )
        {
            throw new InstanceNotFoundException( "" + o );
        }
        return o;
    }
    
    // javax.management.j2ee.Management implementation starts here
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws RemoteException {
        try {
            return restrict( mbeanServer.queryNames(name, query) );
        } catch (Exception ex) {
            throw new RemoteException(this.toString() + "::queryNames", ex);
        }
    }

    public boolean isRegistered(ObjectName name) throws RemoteException {
        try {
            return mbeanServer.isRegistered(name);
        } catch (Exception ex) {
            throw new RemoteException(this.toString() + "::isRegistered", ex);
        }
    }

    public Integer getMBeanCount() throws RemoteException {
        try {
            final ObjectName pattern = new ObjectName( mDomain + ":*" );
            return queryNames( pattern, null ).size();
        } catch (Exception ex) {
            throw new RemoteException(this.toString() + "::getMBeanCount", ex);
        }
    }

    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, RemoteException {
        return mbeanServer.getMBeanInfo( bounce(name) );
    }

    public Object getAttribute(ObjectName name, String attribute) throws MBeanException,
            AttributeNotFoundException, InstanceNotFoundException,
            ReflectionException, RemoteException {
        //debug( "MEJBBean.getAttribute: " + attribute + " on " + name );
        return mbeanServer.getAttribute( bounce(name) , attribute);
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes)
            throws InstanceNotFoundException, ReflectionException, RemoteException {
        //debug( "MEJBBean.getAttributes: on " + name );
        return mbeanServer.getAttributes( bounce(name), attributes);
    }

    public void setAttribute(ObjectName name, Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException,
            ReflectionException, RemoteException {
        mbeanServer.setAttribute( bounce(name), attribute);
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
            throws InstanceNotFoundException, ReflectionException, RemoteException {
        return mbeanServer.setAttributes(name, attributes);
    }

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException,
            ReflectionException, RemoteException {
        return mbeanServer.invoke( bounce(name), operationName, params, signature);
    }

    /**
     * Returns the default domain used for naming the managed object.
     * The default domain name is used as the domain part in the ObjectName
     * of managed objects if no domain is specified by the user.
     */
    public String getDefaultDomain() throws RemoteException {
        return mDomain;
    }

    public ListenerRegistration getListenerRegistry() throws RemoteException {
        return MEJBUtility.getInstance().getListenerRegistry();
    }
}
