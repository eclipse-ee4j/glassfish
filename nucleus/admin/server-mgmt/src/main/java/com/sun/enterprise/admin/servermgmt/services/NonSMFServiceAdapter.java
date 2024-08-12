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

package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.universal.PropertiesDecoder;
import com.sun.enterprise.util.io.ServerDirs;

import java.io.File;
import java.util.Map;

/**
 * The original implementation of Services had serious design problems. The Service interface is ENORMOUSLY fat and non
 * OO in the sense that outside callers had to set things to make things work. The interface is not generic -- it is
 * very SMF specific It is extremely difficult to implement the interface because it has SO MANY methods that non-SMF
 * don't need. This "Adapter" makes it easier to implement the interface. Eventually we should have one adapter for all
 * services but the SMF code is difficult and time-consuming to change. Meantime I'm adding new functionality (August
 * 2010, bnevins) for instances. I'm moving implementations of the new interface methods to "ServiceAdapter" which ALL
 * services extend.
 *
 * @author bnevins
 */
public abstract class NonSMFServiceAdapter extends ServiceAdapter {

    NonSMFServiceAdapter(ServerDirs dirs, AppserverServiceType type) {
        super(dirs, type);
    }

    @Override
    public final int getTimeoutSeconds() {
        throw new UnsupportedOperationException("getTimeoutSeconds() is not supported on this platform");
    }

    @Override
    public final void setTimeoutSeconds(int number) {
        throw new UnsupportedOperationException("setTimeoutSeconds() is not supported on this platform");
    }

    @Override
    public final String getServiceProperties() {
        return flattenedServicePropertes;
    }

    /*
     * @author Byron Nevins
     * 11/14/11
     * The --serviceproperties option was being completely ignored!
     * The existing structure is brittle, hard to understand, and has wired-in
     * the implementation details to the interface.  I.e. there are tons of problems
     * maintaining the code.
     * What I'm doing here is taking the map with all of the built-in values and
     * overlaying it with name-value pairs that the user specified.
     * I discovered the original problem by trying to change the display name, "ENTITY_NAME"
     * at the command line as a serviceproperty.  It was completely ignored!!
     */
    final Map<String, String> getFinalTokenMap() {
        Map<String, String> map = getTokenMap();
        map.putAll(tokensAndValues());
        return map;
    }

    @Override
    public final void setServiceProperties(String cds) {
        flattenedServicePropertes = cds;
    }

    @Override
    public final Map<String, String> tokensAndValues() {
        return PropertiesDecoder.unflatten(flattenedServicePropertes);
    }

    @Override
    public final String getManifestFilePath() {
        throw new UnsupportedOperationException("getManifestFilePath() is not supported in this platform.");
    }

    @Override
    public final String getManifestFileTemplatePath() {
        throw new UnsupportedOperationException("getManifestFileTemplatePath() is not supported in this platform.");
    }

    @Override
    public final boolean isConfigValid() {
        // SMF-only
        return true;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////  pkg-private //////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    File getTemplateFile() {
        return templateFile;
    }

    void setTemplateFile(String name) {
        templateFile = new File(info.libDir, "install/templates/" + name);
    }

    private String flattenedServicePropertes;
    private File templateFile;
}
