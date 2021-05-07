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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.types.Property;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.security.SecurityLoggerInfo;

/**
 *
 * @author kumar.jayanti
 */
@Service
@Singleton
public class RealmsManager {
    //per domain list of loaded Realms
    //Wanted to get rid of Hashtable but the API exporting  Enumeration<String> is preventing
    // it for now.
    private final Map<String, Hashtable<String, Realm>> loadedRealms =
            Collections.synchronizedMap(new HashMap<String, Hashtable<String, Realm>>());

    // Keep track of name of default realm for this domain. This is updated during startup
    // using value from server.xml
    private volatile String defaultRealmName="default";
    private final RealmsProbeProvider probeProvider = new RealmsProbeProvider();
    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    private String defaultDigestAlgorithm = null;

    private static final String DEFAULT_DIGEST_ALGORITHM = "default-digest-algorithm";


    public RealmsManager() {

    }

    /**
     * Checks if the given realm name is loaded/valid.
     * @param name of the realm to check.
     * @return true if realm present, false otherwise.
     */
    public  boolean isValidRealm(String name){
        if(name == null){
            return false;
        } else {
            return configContainsRealm(name, config.getName());
        }
    }

    /**
     * Checks if the given realm name is loaded/valid.
     * @param name of the realm to check.
     * @return true if realm present, false otherwise.
     */
    public  boolean isValidRealm(String configName, String name){
        if(name == null){
            return false;
        } else {
            return configContainsRealm(name, configName);
        }
    }

    /**
     * Returns the names of accessible realms.
     * @return set of realm names
     */
    public Enumeration<String> getRealmNames() {
        return getRealmNames(config.getName());
    }

    Realm _getInstance(String configName, String name) {
        Realm retval = null;
        retval = configGetRealmInstance(configName, name);

        // Some tools as well as numerous other locations assume that
        // getInstance("default") always works; keep them from breaking
        // until code can be properly cleaned up. 4628429

        // Also note that for example the appcontainer will actually create
        // a Subject always containing realm='default' so this notion
        // needs to be fixed/handled.
        if ( (retval == null) && (Realm.RI_DEFAULT.equals(name)) ) {
            retval = configGetRealmInstance(configName,getDefaultRealmName());
        }

        return retval;
    }

    Realm _getInstance(String name) {
        return _getInstance(config.getName(), name);
    }

    public void removeFromLoadedRealms(String realmName) {
        Realm r = removeFromLoadedRealms(config.getName(), realmName);
        if (r != null) {
            probeProvider.realmRemovedEvent(realmName);
        }
    }

    void putIntoLoadedRealms(String realmName, Realm realm) {
        putIntoLoadedRealms(config.getName(), realmName, realm);
        probeProvider.realmAddedEvent(realmName);
    }

    public Realm getFromLoadedRealms(String realmName) {
        return configGetRealmInstance(config.getName(),realmName);
    }

    public Realm getFromLoadedRealms(String configName, String realmName) {
        return configGetRealmInstance(configName,realmName);
    }

    public synchronized String getDefaultRealmName() {
        return defaultRealmName;
    }

    public synchronized void setDefaultRealmName(String defaultRealmName) {
        this.defaultRealmName = defaultRealmName;
    }

   /**
    * Returns names of predefined AuthRealms' classes supported by security service.
    * @returns array of predefind AuthRealms' classes
    *
    */
   public  List<String> getPredefinedAuthRealmClassNames()
   {
       //!!!!!!!!!!!! (hardcoded for now until ss will implement backemnd support)
      /* return new String[]{
               "com.sun.enterprise.security.auth.realm.file.FileRealm",
               "com.sun.enterprise.security.auth.realm.certificate.CertificateRealm",
               "com.sun.enterprise.security.auth.realm.ldap.LDAPRealm",
               "com.sun.enterprise.security.ee.auth.realm.jdbc.JDBCRealm",
               "com.sun.enterprise.security.auth.realm.solaris.SolarisRealm"};*/
       ServiceLocator habitat = Globals.getDefaultHabitat();
       List<ActiveDescriptor<?>> collection = habitat.getDescriptors(
               BuilderHelper.createContractFilter(Realm.class.getName()));
       List<String> arr = new ArrayList<String>();
       for (ActiveDescriptor<?> it : collection) {
           arr.add(it.getImplementation());
       }

       return arr;
   }

   public void createRealms() {
       createRealms(config.getSecurityService(), config);
   }

   public void createRealms(Config cfg) {
       if (cfg == null) {
           return;
       }
       createRealms(cfg.getSecurityService(), cfg);
   }

   private void setDefaultDigestAlgorithm() {
       SecurityService service = config.getSecurityService();
       if(service == null) {
           return;
       }
       List<Property> props = service.getProperty();
       if(props == null) {
           return;
       }
       Iterator<Property> propsIterator = props.iterator();
       while(propsIterator != null && propsIterator.hasNext()) {
           Property prop = propsIterator.next();
           if(prop != null && DEFAULT_DIGEST_ALGORITHM.equals(prop.getName())) {
               this.defaultDigestAlgorithm = prop.getValue();
               break;
           }
       }
   }

   public String getDefaultDigestAlgorithm() {
       return defaultDigestAlgorithm;
   }

   /**
     * Load all configured realms from server.xml and initialize each
     * one.  Initialization is done by calling Realm.initialize() with
     * its name, class and properties.  The name of the default realm
     * is also saved in the Realm class for reference during server
     * operation.
     *
     * <P>This method superceeds the RI RealmManager.createRealms() method.
     *
     * */
    private void createRealms(SecurityService securityBean, Config cfg) {
        //check if realms are already loaded by admin GUI ?
        if (realmsAlreadyLoaded(cfg.getName())) {
            return;
        }

        setDefaultDigestAlgorithm();
        try {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("Initializing configured realms from SecurityService in Domain.xml....");
            }

            if (securityBean == null) {
                securityBean = cfg.getSecurityService();
                assert (securityBean != null);
            }

            // grab default realm name
            String defaultRealm = securityBean.getDefaultRealm();

            // get set of auth-realms and process each
            List<AuthRealm> realms = securityBean.getAuthRealm();
            assert (realms != null);

            RealmConfig.createRealms(defaultRealm, realms, cfg.getName());

        } catch (Exception e) {
            _logger.log(Level.SEVERE, SecurityLoggerInfo.noRealmsError, e);
        }
    }

    private boolean realmsAlreadyLoaded(String cfgName) {
        Enumeration<String> en = getRealmNames(cfgName);
        return (en != null && en.hasMoreElements()) ? true : false;
    }

    private boolean configContainsRealm(String name, String configName) {
        Hashtable<String, Realm> containedRealms = loadedRealms.get(configName);
        return (containedRealms != null) ? containedRealms.containsKey(name) : false;
    }

    private Enumeration<String> getRealmNames(String configName) {
        Hashtable<String, Realm> containedRealms = loadedRealms.get(configName);
        return (containedRealms != null) ? containedRealms.keys() : null;
    }

    private Realm configGetRealmInstance(String configName, String realm) {
        Hashtable<String, Realm> containedRealms = loadedRealms.get(configName);
        return  (containedRealms != null) ? (Realm) containedRealms.get(realm) : null;
    }

    public Realm removeFromLoadedRealms (String configName, String realmName) {
         Hashtable<String, Realm> containedRealms = loadedRealms.get(configName);
         return (containedRealms != null) ?(Realm)containedRealms.remove(realmName) : null;
    }

    public void putIntoLoadedRealms (String configName, String realmName, Realm realm) {
         Hashtable<String, Realm> containedRealms = loadedRealms.get(configName);
         if (containedRealms == null) {
             containedRealms = new Hashtable<String, Realm>();
             if (configName == null) {
                 configName = config.getName();
             }
             loadedRealms.put(configName, containedRealms);
         }
         containedRealms.put(realmName, realm);
    }

    public void refreshRealm(String configName, String realmName) {
        if (realmName != null && realmName.length() > 0) {
            try {
                Realm realm = Realm.getInstance(configName, realmName);

                if (realm != null) {
                    realm.refresh(configName);
                }
            } catch (com.sun.enterprise.security.auth.realm.NoSuchRealmException nre) {
                //            _logger.fine("Realm: "+realmName+" is not configured");
            } catch (com.sun.enterprise.security.auth.realm.BadRealmException bre) {
                //            _logger.fine("Realm: "+realmName+" is not configured");
            }
        }
    }
}
