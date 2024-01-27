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
import com.sun.enterprise.security.web.GlassFishSingleSignOn;

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.SingleSignOnEntry;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.web.ha.LogFacade;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;

/**
 * @author Shing Wai Chan
 */
public class HASingleSignOn extends GlassFishSingleSignOn {
    private static final Logger logger = LogFacade.getLogger();

    private BackingStore<String, HASingleSignOnEntryMetadata> ssoEntryMetadataBackingStore = null;

    private JavaEEIOUtils ioUtils = null;

    public HASingleSignOn(JavaEEIOUtils ioUtils,
            BackingStore<String, HASingleSignOnEntryMetadata> ssoEntryMetadataBackingStore) {
        super();
        this.ioUtils = ioUtils;
        this.ssoEntryMetadataBackingStore = ssoEntryMetadataBackingStore;
    }

    @Override
    protected void deregister(String ssoId) {

        //S1AS8 6155481 START
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Deregistering sso id '" + ssoId + "'");
        }
        //S1AS8 6155481 END
        // Look up and remove the corresponding SingleSignOnEntry
        SingleSignOnEntry sso = null;
        synchronized (cache) {
            sso = cache.remove(ssoId);
        }

        if (sso == null)
            return;

        // Expire any associated sessions
        sso.expireSessions();

        try {
            ssoEntryMetadataBackingStore.remove(ssoId);
        } catch(BackingStoreException ex) {
            throw new IllegalStateException(ex);
        }
        // NOTE:  Clients may still possess the old single sign on cookie,
        // but it will be removed on the next request since it is no longer
        // in the cache
    }

    @Override
    protected void register(String ssoId, Principal principal, String authType,
                  String username, char[] password, String realmName) {

        if (logger.isLoggable(Level.FINE)) {
            String pName = ((principal != null)? principal.getName() : null);
            log("Registering sso id '" + ssoId + "' for principal '" + pName + "' and username '" + username
                + "' with auth type '" + authType + "' and realmName '" + realmName + "'");
        }

        HASingleSignOnEntry ssoEntry = null;
        synchronized (cache) {
            ssoEntry = new HASingleSignOnEntry(ssoId, principal, authType,
                    username, realmName,
                    // revisit maxIdleTime 1000000, version 0
                    System.currentTimeMillis(), 1000000, 0,
                    ioUtils);
            cache.put(ssoId, ssoEntry);
        }

        try {
            ssoEntryMetadataBackingStore.save(ssoId, ssoEntry.getMetadata(), true);
        } catch(BackingStoreException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void associate(String ssoId, long ssoVersion, Session session) {

        if (!started) {
            return;
        }

        if (debug >= 1)
            log("Associate sso id " + ssoId + " with session " + session);

        HASingleSignOnEntry sso = (HASingleSignOnEntry)lookup(ssoId, ssoVersion, null);
        if (sso != null) {
            session.setSsoId(ssoId);
            sso.addSession(this, session);

            try {
                ssoEntryMetadataBackingStore.save(ssoId, sso.getMetadata(), false);
            } catch(BackingStoreException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    @Override
    protected SingleSignOnEntry lookup(String ssoId, long ssoVersion, ClassLoader appClassLoader) {
        SingleSignOnEntry ssoEntry = super.lookup(ssoId, ssoVersion, appClassLoader);
        if (ssoEntry != null && ssoVersion > ssoEntry.getVersion()) {
            // clean the old cache
            synchronized(cache) {
                cache.remove(ssoId);
            }
            ssoEntry = null;
        }
        if (ssoEntry == null) {
            // load from ha store
            try {
                HASingleSignOnEntryMetadata mdata =
                    ssoEntryMetadataBackingStore.load(ssoId, null);
                if (mdata != null) {
                    ssoEntry = new HASingleSignOnEntry(getContainer(), mdata, ioUtils, appClassLoader);
                    cache.put(ssoId, ssoEntry);
                }
            } catch(BackingStoreException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return ssoEntry;
    }

    @Override
    protected void removeSession(String ssoId, Session session) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Removing session " + session + " from sso id " + ssoId );
        }

        // Get a reference to the SingleSignOn
        HASingleSignOnEntry entry = (HASingleSignOnEntry)lookup(ssoId);
        if (entry == null)
            return;

        // Remove the inactive session from SingleSignOnEntry
        entry.removeSession(session);

        // If there are not sessions left in the SingleSignOnEntry,
        // deregister the entry.
        if (entry.isEmpty()) {
            deregister(ssoId);
        } else {
            try {
                ssoEntryMetadataBackingStore.save(ssoId, entry.getMetadata(), false);
            } catch(BackingStoreException ex) {
            }
        }
    }

    @Override
    public boolean isVersioningSupported() {
        return true;
    }
}
