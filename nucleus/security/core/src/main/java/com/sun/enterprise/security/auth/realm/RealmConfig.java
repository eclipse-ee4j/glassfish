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

package com.sun.enterprise.security.auth.realm;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.security.SecurityLoggerInfo;

import org.jvnet.hk2.config.types.Property;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RealmConfig usable by standalone : Admin CLI for creating Realms
 * It has a subset of functionality defined in com.sun.enterprise.security.RealmConfig
 */
public class RealmConfig {

    private static Logger logger =
            SecurityLoggerInfo.getLogger();

    public static void createRealms(String defaultRealm, List<AuthRealm> realms) {
        createRealms(defaultRealm, realms, null);
    }
    public static void createRealms(String defaultRealm, List<AuthRealm> realms, String configName) {
        assert(realms != null);

        String goodRealm = null; // need at least one good realm

        for (AuthRealm aRealm : realms) {
            String realmName = aRealm.getName();
            String realmClass = aRealm.getClassname();
            assert (realmName != null);
            assert (realmClass != null);

            try {
                List<Property> realmProps = aRealm.getProperty();
                /*V3 Commented ElementProperty[] realmProps =
                    aRealm.getElementProperty();*/
                Properties props = new Properties();
                for (Property realmProp : realmProps) {
                    props.setProperty(realmProp.getName(), realmProp.getValue());
                }
                Realm.instantiate(realmName, realmClass, props, configName);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Configured realm: " + realmName);
                }

                if (goodRealm == null) {
                    goodRealm = realmName;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING,
                           SecurityLoggerInfo.realmConfigDisabledError, realmName);
                logger.log(Level.WARNING, SecurityLoggerInfo.securityExceptionError, e);
            }
        }

        // done loading all realms, check that there is at least one
        // in place and that default is installed, or change default
        // to the first one loaded (arbitrarily).

        if (goodRealm == null) {
            logger.severe(SecurityLoggerInfo.noRealmsError);

        } else {
            try {
                Realm.getInstance(defaultRealm);
            } catch (Exception e) {
                defaultRealm = goodRealm;
            }
            Realm.setDefaultRealm(defaultRealm);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Default realm is set to: " + defaultRealm);
            }
        }
    }
}
