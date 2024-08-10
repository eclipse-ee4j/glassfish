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

package org.glassfish.admin.amx.impl.mbean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.AttributeChangeNotification;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.glassfish.admin.amx.util.AMXDebug;
import org.glassfish.admin.amx.util.Output;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.jmx.AttributeChangeNotificationBuilder;
import org.glassfish.admin.amx.util.jmx.NotificationBuilder;
import org.glassfish.admin.amx.util.jmx.NotificationEmitterSupport;
import org.glassfish.admin.amx.util.jmx.NotificationSender;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;

/**
Absolute base impl class. Should contain only core functionality,
nothing to do with appserver specifics.
 */
public abstract class MBeanImplBase
        implements MBeanRegistration, NotificationSender {

    protected static final String[] EMPTY_STRING_ARRAY = new String[0];
    /**
    The MBeanServer in which this object is registered (if any)
     */
    protected volatile MBeanServer mServer;
    /**
    The ObjectName by which this object is registered (if registered).
    Multiple registrations, which are possible, overwrite this; the last registration
    wins.  If the MBean has not been registered, this name will be null.
     */
    protected volatile ObjectName mSelfObjectName;
    private volatile NotificationEmitterSupport mNotificationEmitter;
    private volatile Map<String, NotificationBuilder> mNotificationBuilders;
    /**
    We need debugging before the MBean is registered, so our
    only choice is to use an ID based on something other than
    the as-yet-unknown ObjectName, namely the classname.
     */
    private final Output mDebug = AMXDebug.getInstance().getOutput(getDebugID());

    public MBeanImplBase() {
    }

    /**
    Some subclasses need the MBeanServer set in advance.
     */
    public MBeanImplBase(final MBeanServer mbeanServer) {
        mServer = mbeanServer;
    }

    public final int getListenerCount() {
        return (getNotificationEmitter().getListenerCount());
    }

    public final int getNotificationTypeListenerCount(final String type) {
        return (getNotificationEmitter().getNotificationTypeListenerCount(type));
    }

    /**
    @return an empty array
    Subclass may wish to override this.
     */
    synchronized protected final NotificationEmitterSupport getNotificationEmitter() {
        if (mNotificationEmitter == null) {
            mNotificationEmitter = new NotificationEmitterSupport(true);
        }

        return (mNotificationEmitter);
    }

    public synchronized void addNotificationListener(
            final NotificationListener listener) {
        getNotificationEmitter().addNotificationListener(listener, null, null);
    }

    public synchronized void addNotificationListener(
            final NotificationListener listener,
            final NotificationFilter filter,
            final Object handback) {
        getNotificationEmitter().addNotificationListener(listener, filter, handback);
    }

    public synchronized void removeNotificationListener(final NotificationListener listener)
            throws ListenerNotFoundException {
        getNotificationEmitter().removeNotificationListener(listener);
    }

    public synchronized void removeNotificationListener(
            final NotificationListener listener,
            final NotificationFilter filter,
            final Object handback)
            throws ListenerNotFoundException {
        getNotificationEmitter().removeNotificationListener(listener, filter, handback);
    }

    public void sendNotification(final Notification notification) {
        getNotificationEmitter().sendNotification(notification);
    }

    protected NotificationBuilder createNotificationBuilder(final String notificationType) {
        NotificationBuilder builder = null;

        if (notificationType.equals(AttributeChangeNotification.ATTRIBUTE_CHANGE)) {
            builder = new AttributeChangeNotificationBuilder(getObjectName());
        } else {
            builder = new NotificationBuilder(notificationType, getObjectName());
        }

        return (builder);
    }

    /**
    Get a NotificationBuilder for the specified type of Notification
    whose source is this object.
     */
    protected synchronized NotificationBuilder getNotificationBuilder(final String notificationType) {
        if (mNotificationBuilders == null) {
            mNotificationBuilders = new HashMap<String, NotificationBuilder>();
        }

        NotificationBuilder builder = mNotificationBuilders.get(notificationType);

        if (builder == null) {
            builder = createNotificationBuilder(notificationType);
            mNotificationBuilders.put(notificationType, builder);
        }

        return (builder);
    }

    /**
    Send a Notification of the specified type containing no data.
     */
    protected void sendNotification(final String notificationType) {
        sendNotification(notificationType, notificationType, null, null);
    }

    /**
    Send a Notification of the specified type containing a single
    key/value pair for data.
     */
    protected void sendNotification(
            final String notificationType,
            final String key,
            final Serializable value) {
        final String message = "no message specified";

        sendNotification(notificationType, message, key, value);
    }

    /**
    Send a Notification of the specified type containing a single
    key/value pair for data.
     */
    protected void sendNotification(
            final String notificationType,
            final String message,
            final String key,
            final Serializable value) {
        final NotificationBuilder builder =
                getNotificationBuilder(notificationType);

        final Notification notif = builder.buildNew(message);
        NotificationBuilder.putMapData(notif, key, value);

        sendNotification(notif);
    }

    public final ObjectName getObjectName() {
        return (mSelfObjectName);
    }

    public String getJMXDomain() {
        return (getObjectName().getDomain());
    }

    public final MBeanServer getMBeanServer() {
        return (mServer);
    }

    protected static String toString(Object o) {
        if (o == null) {
            return ("");
        }

        return (SmartStringifier.toString(o));
    }

    protected final void trace(Object o) {
        debug(o);
    }

    protected final void logSevere(Object o) {
        final String msg = toString(o);
        debug(msg);

        if (getMBeanLogLevelInt() <= Level.SEVERE.intValue()) {
            getMBeanLogger().severe(msg);
        }
    }

    protected final void logWarning(Object o) {
        final String msg = toString(o);
        debug(msg);

        if (getMBeanLogLevelInt() <= Level.WARNING.intValue()) {
            getMBeanLogger().warning(msg);
        }
    }

    protected final void logInfo(Object o) {
        final String msg = toString(o);
        debug(msg);

        if (getMBeanLogLevelInt() <= Level.INFO.intValue()) {
            getMBeanLogger().info(msg);
        }
    }

    protected final void logFine(Object o) {
        final String msg = toString(o);
        debug(msg);

        if (getMBeanLogLevelInt() <= Level.FINE.intValue()) {
            getMBeanLogger().fine(msg);
        }
    }

    protected final void logFiner(Object o) {
        final String msg = toString(o);
        debug(msg);

        if (getMBeanLogLevelInt() <= Level.FINER.intValue()) {
            getMBeanLogger().finer(msg);
        }
    }

    protected final void logFinest(Object o) {
        final String msg = toString(o);
        debug(msg);

        if (getMBeanLogLevelInt() <= Level.FINEST.intValue()) {
            getMBeanLogger().finest(msg);
        }
    }

    protected final Logger getMBeanLogger() {
        return Logger.getLogger("MBeans");
    }

    protected final Level _getMBeanLogLevel() {
        Logger logger = getMBeanLogger();
        assert (logger != null);

        Level level = logger.getLevel();
        while (level == null) {
            logger = logger.getParent();
            level = logger.getLevel();
        }

        return (level);
    }

    public final String getMBeanLogLevel() {
        return "" + _getMBeanLogLevel();
    }

    public final void setMBeanLogLevel(final String level) {
        getMBeanLogger().setLevel(Level.parse(level));
    }

    protected final int getMBeanLogLevelInt() {
        return (_getMBeanLogLevel().intValue());
    }

    public final String getMBeanLoggerName() {
        return (getMBeanLogger().getName());
    }

    protected static String quote(final Object o) {
        return (StringUtil.quote("" + o));
    }

    public ObjectName preRegister(
            final MBeanServer server,
            final ObjectName nameIn)
            throws Exception {
        assert (nameIn != null);
        mServer = server;
        mSelfObjectName = nameIn;

        // ObjectName could still be modified by subclass
        return (mSelfObjectName);
    }

    protected void postRegisterHook(final Boolean registrationSucceeded) {
    }

    public final void postRegister(final Boolean registrationSucceeded) {
        if (registrationSucceeded.booleanValue()) {
            getMBeanLogger().finest("postRegister: " + getObjectName());
        } else {
            getMBeanLogger().finest("postRegister: FAILURE: " + getObjectName());
        }

        postRegisterHook(registrationSucceeded);
    }

    protected void preDeregisterHook()
            throws Exception {
    }

    public final void preDeregister()
            throws Exception {
        getMBeanLogger().finest("preDeregister: " + getObjectName());

        preDeregisterHook();
    }

    protected void postDeregisterHook() {
    }

    public final void postDeregister() {
        getMBeanLogger().finest("postDeregister: " + getObjectName());

        postDeregisterHook();

        if (mNotificationEmitter != null) {
            mNotificationEmitter.cleanup();
            mNotificationEmitter = null;
        }

        if (mNotificationBuilders != null) {
            mNotificationBuilders.clear();
            mNotificationBuilders = null;
        }

        // Leave these variables set, they may be desired after unregistration by some subclasses
        //mServer                                        = null;
        //mSelfObjectName                        = null;
    }

    protected String getDebugID() {
        return this.getClass().getName();
    }

    /**
    Although unusual, a subclass may override the debug state.
    Generally it is faster to NOT call getAMXDebug() before calling
    debug( x ) if 'x' is just a string.  If it is expensive to
    construct 'x', then preflighting with getAMXDebug() may
    be worthwhile.
     */
    public final boolean getAMXDebug() {
        return AMXDebug.getInstance().getDebug(getDebugID());
    }

    public boolean enableAMXDebug(final boolean enabled) {
        final boolean formerValue = getAMXDebug();
        if (formerValue != enabled) {
            setAMXDebug(enabled);
        }
        return formerValue;
    }

    protected Output getDebugOutput() {
        return AMXDebug.getInstance().getOutput(getDebugID());
    }

    public final void setAMXDebug(final boolean debug) {
        AMXDebug.getInstance().setDebug(getDebugID(), debug);
    }

    protected boolean shouldOmitObjectNameForDebug() {
        return mSelfObjectName == null;
    }

    protected final void debug(final Object o) {
        if (getAMXDebug() && mDebug != null) {
            final String newline = System.getProperty("line.separator");

            if (shouldOmitObjectNameForDebug()) {
                mDebug.println(o);
            } else {
                mDebug.println(mSelfObjectName.toString());
                mDebug.println("===> " + o);
            }
            mDebug.println(newline);
        }
    }

    protected void debugMethod(final String methodName, final Object... args) {
        if (getAMXDebug()) {
            debug(AMXDebug.methodString(methodName, args));
        }
    }

    protected void debugMethod(
            final String msg,
            final String methodName,
            final Object... args) {
        if (getAMXDebug()) {
            debug(AMXDebug.methodString(methodName, args) + ": " + msg);
        }
    }

    protected void debug(final Object... args) {
        if (getAMXDebug()) {
            debug(StringUtil.toString("", args));
        }
    }

    protected boolean sleepMillis(final long millis) {
        boolean interrupted = false;

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.interrupted();
            interrupted = true;
        }

        return interrupted;
    }
}








