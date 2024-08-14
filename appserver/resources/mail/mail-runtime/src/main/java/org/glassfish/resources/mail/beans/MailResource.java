/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.resources.mail.beans;

import com.sun.enterprise.deployment.interfaces.MailResourceIntf;

import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.glassfish.resources.api.JavaEEResource;
import org.glassfish.resources.api.JavaEEResourceBase;

/**
 * Resource info for MailResource.
 * IASRI #4650786
 *
 * @author James Kong
 */
public class MailResource extends JavaEEResourceBase implements MailResourceIntf {

    private static final long serialVersionUID = 1L;
    private String resType;
    private String factoryClass;

    private String storeProtocol;
    private String storeProtocolClass;
    private String transportProtocol;
    private String transportProtocolClass;
    private String mailHost;
    private String username;
    private String mailFrom;
    private boolean debug;

    public MailResource(ResourceInfo resourceInfo) {
        super(resourceInfo);
    }

    @Override
    protected JavaEEResource doClone(ResourceInfo resourceInfo) {
        MailResource clone = new MailResource(resourceInfo);
        clone.setResType(getResType());
        clone.setFactoryClass(getFactoryClass());
        return clone;
    }

    //unused implementation ie., com.sun.enterprise.deployment.MailConfiguration uses this, but is unused in-turn.
    @Override
    public String getName() {
        return getResourceInfo().getName().toString();
    }

    @Override
    public int getType() {
        return JavaEEResource.MAIL_RESOURCE;
    }

    @Override
    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    @Override
    public String getFactoryClass() {
        return factoryClass;
    }

    public void setFactoryClass(String factoryClass) {
        this.factoryClass = factoryClass;
    }

    @Override
    public String getStoreProtocol() {
        return storeProtocol;
    }

    public void setStoreProtocol(String storeProtocol) {
        this.storeProtocol = storeProtocol;
    }

    @Override
    public String getStoreProtocolClass() {
        return storeProtocolClass;
    }

    public void setStoreProtocolClass(String storeProtocolClass) {
        this.storeProtocolClass = storeProtocolClass;
    }

    @Override
    public String getTransportProtocol() {
        return transportProtocol;
    }

    public void setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
    }

    @Override
    public String getTransportProtocolClass() {
        return transportProtocolClass;
    }

    public void setTransportProtocolClass(String transportProtocolClass) {
        this.transportProtocolClass = transportProtocolClass;
    }

    @Override
    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public String toString() {
        return "< Mail Resource : " + getResourceInfo() + " , " + getResType() + "... >";
    }
}
