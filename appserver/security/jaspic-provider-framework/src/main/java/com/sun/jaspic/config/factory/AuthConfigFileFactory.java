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

package com.sun.jaspic.config.factory;

/**
 *
 * @author ronmonzillo
 */
public class AuthConfigFileFactory extends BaseAuthConfigFactory {

    // MUST "hide" regStore in derived class.
    static volatile RegStoreFileParser regStore = null;

    /**
     * to specialize the defaultEntries passed to the RegStoreFileParser
     * constructor, create another subclass of BaseAuthconfigFactory, that is
     * basically a copy of this class, with a change to the third argument
     * of the call to new ResSToreFileParser. 
     * to ensure runtime use of the the associated regStore, make sure that
     * the new subclass also contains an implementation of the getRegStore method.
     * As done within this class, use the locks defined in
     * BaseAuthConfigFactory to serialize access to the regStore (both within
     * the class constructor, and within getRegStore)
     *
     * All EentyInfo OBJECTS PASSED as deualtEntries MUST HAVE BEEN
     * CONSTRCTED USING THE FOLLOWING CONSTRUCTOR:
     *
     * EntryInfo(String className, Map<String, String> properties);
     *
     */
    public AuthConfigFileFactory() {
        rLock.lock();
        try {
            if (regStore != null) {
                return;
            }
        } finally {
            rLock.unlock();
        }
        String userDir = System.getProperty("user.dir");
        wLock.lock();
        try {
            if (regStore == null) {
                regStore = new RegStoreFileParser(userDir,
                        BaseAuthConfigFactory.CONF_FILE_NAME, null);
                _loadFactory();
            }
        } finally {
            wLock.unlock();
        }
    }

    @Override
    protected RegStoreFileParser getRegStore() {
        rLock.lock();
        try {
            return regStore;
        } finally {
            rLock.unlock();
        }
    }
}
