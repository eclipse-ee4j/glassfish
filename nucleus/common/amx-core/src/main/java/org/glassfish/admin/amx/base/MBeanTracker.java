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

package org.glassfish.admin.amx.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.management.*;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.util.jmx.JMXUtil;
import org.glassfish.external.amx.AMX;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
 * Tracks the entire MBean parent/child hierarachy so that individual MBeans
 * need not do so. Can supply parents and children of any MBean, used by all AMX
 * implementations.
 */
@Taxonomy(stability = Stability.NOT_AN_INTERFACE)
@AMXMBeanMetadata(singleton = true, globalSingleton = true, leaf = true)
public final class MBeanTracker implements NotificationListener, MBeanRegistration, MBeanTrackerMBean {

    /**
     * maps a parent ObjectName to a Set of children
     */
    final ConcurrentMap<ObjectName, Set<ObjectName>> mParentChildren;
    /**
     * maps a child to its parent, needed because when unregistered we can't
     * obtain parent
     */
    final ConcurrentMap<ObjectName, ObjectName> mChildParent;
    private volatile MBeanServer mServer;
    private volatile ObjectName mObjectName;
    private final String mDomain;
    private volatile boolean mEmitMBeanStatus;

    public MBeanTracker(final String jmxDomain) {
        mParentChildren = new ConcurrentHashMap<ObjectName, Set<ObjectName>>();
        mChildParent = new ConcurrentHashMap<ObjectName, ObjectName>();

        mDomain = jmxDomain;

        mEmitMBeanStatus = false;
    }

    @Override
    public boolean getEmitMBeanStatus() {
        return mEmitMBeanStatus;
    }

    @Override
    public void setEmitMBeanStatus(final boolean emit) {
        mEmitMBeanStatus = emit;
    }

    @Override
    public void handleNotification(final Notification notifIn, final Object handback) {
        if (notifIn instanceof MBeanServerNotification) {
            final MBeanServerNotification notif = (MBeanServerNotification) notifIn;

            final String type = notif.getType();
            final ObjectName objectName = notif.getMBeanName();

            if (isRelevantMBean(objectName)) {
                // what happens if an MBean is removed before we can add it
                // eg the MBeanServer uses more than one thread to deliver notifications
                // to use? Even if we synchronize this method, the remove could still arrive
                // first and there's nothing we could do about it.
                if (type.equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
                    if (mEmitMBeanStatus) {
                        System.out.println("AMX MBean registered: " + objectName);
                    }
                    addChild(objectName);
                } else if (type.equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
                    if (mEmitMBeanStatus) {
                        System.out.println("AMX MBean UNregistered: " + objectName);
                    }
                    removeChild(objectName);
                }
            }
        }
    }

    @Override
    public ObjectName preRegister(
            final MBeanServer server,
            final ObjectName nameIn)
            throws Exception {
        mServer = server;
        mObjectName = nameIn;
        return (nameIn);
    }

    @Override
    public final void postRegister(final Boolean registrationSucceeded) {
        if (mServer == null) {
            return;
        }
        if (registrationSucceeded.booleanValue()) {
            try {
                mServer.addNotificationListener(JMXUtil.getMBeanServerDelegateObjectName(), this, null, null);
            } catch (Exception e) {
                throw new RuntimeException("Could not register with MBeanServerDelegate", e);
            }
            //debug( "MBeanTracker: registered as " + mObjectName );
        }
        // populate our list
        final ObjectName pattern = Util.newObjectNamePattern(mDomain, "");
        final Set<ObjectName> names = JMXUtil.queryNames(mServer, pattern, null);
        //debug( "MBeanTracker: found MBeans: " + names.size() );
        for (final ObjectName o : names) {
            addChild(o);
        }
    }

    @Override
    public final void preDeregister() throws Exception {
        if (mServer != null) {
            mServer.removeNotificationListener(mObjectName, this);
        }
    }

    @Override
    public final void postDeregister() {
    }

    private boolean isRelevantMBean(final ObjectName child) {
        return child != null && mDomain.equals(child.getDomain());
    }

    private void addChild(final ObjectName child) {
        if (mServer == null) {
            return;
        }
        ObjectName parent = null;
        try {
            parent = (ObjectName) mServer.getAttribute(child, AMX.ATTR_PARENT);
        } catch (final Exception e) {
            // nothing to be done, MBean gone missing, badly implemented, etc.
            //System.out.println( "No Parent for: " + child );
        }

        if (parent != null) {
            synchronized (this) {
                mChildParent.put(child, parent);
                Set<ObjectName> children = mParentChildren.get(parent);
                if (children == null) {
                    children = new HashSet<ObjectName>();
                    mParentChildren.put(parent, children);
                }
                children.add(child);
                //debug( "MBeanTracker: ADDED " + child + " with parent " + parent );
            }
        }
    }

    /**
     * Must be 'synchronized' because we're working on two different Maps.
     */
    private synchronized ObjectName removeChild(final ObjectName child) {
        final ObjectName parent = mChildParent.remove(child);
        if (parent != null) {
            final Set<ObjectName> children = mParentChildren.get(parent);
            if (children != null) {
                children.remove(child);
                if (children.isEmpty()) {
                    mParentChildren.remove(parent);
                    //debug( "MBeanTracker: REMOVED " + child + " from parent " + parent );
                }
            }
        }
        return parent;
    }

    @Override
    public ObjectName getParentOf(final ObjectName child) {
        return mChildParent.get(child);
    }

    @Override
    public synchronized Set<ObjectName> getChildrenOf(final ObjectName parent) {
        final Set<ObjectName> children = mParentChildren.get(parent);
        if (children == null) {
            return Collections.emptySet();
        }

        return new HashSet<ObjectName>(children);
    }
}
