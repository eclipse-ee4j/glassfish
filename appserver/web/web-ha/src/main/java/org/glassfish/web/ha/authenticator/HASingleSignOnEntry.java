/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.ha.authenticator;

import com.sun.enterprise.container.common.spi.util.JavaEEIOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.authenticator.SingleSignOnEntry;
import org.apache.catalina.core.StandardContext;
import org.glassfish.web.ha.LogFacade;

/**
 * @author Shing Wai Chan
 */
public class HASingleSignOnEntry extends SingleSignOnEntry {
    private static final Logger logger = LogFacade.getLogger();

    protected long maxIdleTime;

    protected JavaEEIOUtils ioUtils;

    protected HASingleSignOnEntryMetadata metadata = null;

    protected ClassLoader appClassLoader;

    // default constructor is required by backing store
    public HASingleSignOnEntry() {
        this(null, null, null, null, null, 0, 0, 0, null, null, null);
    }

    public HASingleSignOnEntry(Container container, HASingleSignOnEntryMetadata m, JavaEEIOUtils ioUtils, ClassLoader appClassLoader) {
        this(m.getId(), null, m.getAuthType(),
                m.getUserName(), m.getRealmName(),
                m.getLastAccessTime(), m.getMaxIdleTime(), m.getVersion(),
                ioUtils, m.getPrincipalBytes(), appClassLoader);

        for (HASessionData data: m.getHASessionDataSet()) {
            StandardContext context = (StandardContext)container.findChild(data.getContextPath());
            Session session = null;
            try {
                session = context.getManager().findSession(data.getSessionId());
            } catch(IOException ex) {
                throw new IllegalStateException(ex);
            }
            if (session != null) {
                sessions.add(session);
            }
        }
    }

    public HASingleSignOnEntry(String id, Principal principal, String authType,
            String username, String realmName,
            long lastAccessTime, long maxIdleTime, long version,
            JavaEEIOUtils ioUtils) {

        this(id, principal, authType, username, realmName, lastAccessTime,
            maxIdleTime, version, ioUtils, convertToByteArray(principal, ioUtils), null);
    }

    private HASingleSignOnEntry(String id, Principal principal, String authType,
            String username, String realmName,
            long lastAccessTime, long maxIdleTime, long version,
            JavaEEIOUtils ioUtils, byte[] principalBytes, ClassLoader appClassLoader) {

        super(id, version, principal, authType, username, realmName);
        this.lastAccessTime = lastAccessTime;
        this.maxIdleTime = maxIdleTime;
        this.ioUtils = ioUtils;
        this.appClassLoader = appClassLoader;

        if (principal == null && principalBytes != null) {
            this.principal = parse(principalBytes);
        }

        metadata = new HASingleSignOnEntryMetadata(
                id, version, principalBytes, authType,
                username, realmName,
                lastAccessTime, maxIdleTime);

        if (logger.isLoggable(Level.FINER)) {
            String pName = ((principal != null)? principal.getName() : null);
            logger.log(Level.FINER, "Loaded HA SSO entry with principal: " + pName);
        }
    }

    public HASingleSignOnEntryMetadata getMetadata() {
        return metadata;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    @Override
    public synchronized boolean addSession(SingleSignOn sso, Session session) {
        boolean result = super.addSession(sso, session);
        if (result) {
            metadata.addHASessionData(new HASessionData(session.getId(),
                session.getManager().getContainer().getName()));
        }

        return result;
    }

    @Override
    public synchronized void removeSession(Session session) {
        super.removeSession(session);
        metadata.removeHASessionData(new HASessionData(session.getId(),
                session.getManager().getContainer().getName()));
    }

    @Override
    public void setLastAccessTime(long lastAccessTime) {
        super.setLastAccessTime(lastAccessTime);
        metadata.setLastAccessTime(lastAccessTime);
    }

    @Override
    public long incrementAndGetVersion() {
        long ver = super.incrementAndGetVersion();
        metadata.setVersion(ver);
        return ver;
    }

    // convert a Principal object into byte array
    private static byte[] convertToByteArray(Principal obj, JavaEEIOUtils ioUtils) {
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            bos = new BufferedOutputStream(baos);
            oos = ioUtils.createObjectOutputStream(bos, true);
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        } catch(Exception ex) {
            throw new IllegalStateException(ex);
        } finally {
            closeSafely(baos);
            closeSafely(bos);
            closeSafely(oos);
        }
    }

    private static void closeSafely(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch(IOException ex) {
                // ignore
            }
        }
    }

    private Principal parse(byte[] pbytes) {
        ByteArrayInputStream bais = null;
        BufferedInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(pbytes);
            bis = new BufferedInputStream(bais);
            ois = ioUtils.createObjectInputStream(bis, true, this.getClass().getClassLoader());

            if (appClassLoader == null) {
                return (Principal) ois.readObject();
            }

            try {
                return (Principal) ois.readObject();
            } catch (ClassNotFoundException e) {
                closeSafely(bais);
                closeSafely(bis);
                closeSafely(ois);

                bais = new ByteArrayInputStream(pbytes);
                bis = new BufferedInputStream(bais);
                ois = ioUtils.createObjectInputStream(bis, true, appClassLoader);

                return (Principal) ois.readObject();
            }
        } catch(Exception ex) {
            throw new IllegalStateException(ex);
        } finally {
            closeSafely(bais);
            closeSafely(bis);
            closeSafely(ois);
        }
    }
}
