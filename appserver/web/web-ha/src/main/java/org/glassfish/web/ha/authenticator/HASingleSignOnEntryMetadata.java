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

package org.glassfish.web.ha.authenticator;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Shing Wai Chan
 */
public class HASingleSignOnEntryMetadata implements Serializable {
    protected String id = null;

    protected String authType = null;

    protected byte[] principalBytes = null;

    protected Set<HASessionData> sessionDataSet = new HashSet<HASessionData>();

    protected String userName = null;

    protected String realmName = null;

    protected long lastAccessTime;

    protected long maxIdleTime;

    protected long version;

    // default constructor is required by backing store
    public HASingleSignOnEntryMetadata() {
    }

    public HASingleSignOnEntryMetadata(String id, long version,
            byte[] principalBytes, String authType,
            String userName, String realmName,
            long lastAccessTime, long maxIdleTime) {

        this.id = id;
        this.version = version;
        this.principalBytes = ((principalBytes != null) ? ((byte[])principalBytes.clone()) : null);
        this.authType = authType;
        this.userName = userName;;
        this.realmName = realmName;
        this.lastAccessTime = lastAccessTime;
        this.maxIdleTime = maxIdleTime;
    }

    public String getId() {
        return id;
    }

    public byte[] getPrincipalBytes() {
        return principalBytes;
    }

    public String getAuthType() {
        return authType;
    }

    public String getUserName() {
        return userName;
    }

    public String getRealmName() {
        return realmName;
    }

    public Set<HASessionData> getHASessionDataSet() {
        return sessionDataSet;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public long getVersion() {
        return version;
    }

    void setVersion(long version) {
        this.version = version;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    boolean addHASessionData(HASessionData sessionData) {
        return sessionDataSet.add(sessionData);
    }

    boolean removeHASessionData(HASessionData sessionData) {
        return sessionDataSet.remove(sessionData);
    }

    @Override
    public String toString() {
        return "HASingleSignOnEntryMetadata{" +
                "id='" + id + '\'' +
                ", version=" + version +
                ", authType='" + authType + '\'' +
                ", sessionDataSet.size=" + sessionDataSet.size() +
                ", userName='" + userName + '\'' +
                ", realmName='" + realmName + '\'' +
                ", lastAccessTime=" + lastAccessTime +
                ", maxIdleTime=" + maxIdleTime +
                '}';
    }
}
