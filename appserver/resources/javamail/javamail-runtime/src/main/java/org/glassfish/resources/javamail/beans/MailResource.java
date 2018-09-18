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

package org.glassfish.resources.javamail.beans;

import com.sun.enterprise.deployment.interfaces.MailResourceIntf;
import org.glassfish.resources.api.JavaEEResource;
import org.glassfish.resources.api.JavaEEResourceBase;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

/**
 * Resource info for MailResource.
 * IASRI #4650786
 *
 * @author James Kong
 */
public class MailResource extends JavaEEResourceBase implements MailResourceIntf {

    private String resType_;
    private String factoryClass_;

    private String storeProtocol_;
    private String storeProtocolClass_;
    private String transportProtocol_;
    private String transportProtocolClass_;
    private String mailHost_;
    private String username_;
    private String mailFrom_;
    private boolean debug_;

    public MailResource(ResourceInfo resourceInfo) {
        super(resourceInfo);
    }

    protected JavaEEResource doClone(ResourceInfo resourceInfo) {
        MailResource clone = new MailResource(resourceInfo);
        clone.setResType(getResType());
        clone.setFactoryClass(getFactoryClass());
        return clone;
    }

    //unused implementation ie., com.sun.enterprise.deployment.MailConfiguration uses this, but is unused in-turn.
    public String getName() {
        return getResourceInfo().getName();
    }

    public int getType() {
        return JavaEEResource.MAIL_RESOURCE;
    }

    public String getResType() {
        return resType_;
    }

    public void setResType(String resType) {
        resType_ = resType;
    }

    public String getFactoryClass() {
        return factoryClass_;
    }

    public void setFactoryClass(String factoryClass) {
        factoryClass_ = factoryClass;
    }

    public String getStoreProtocol() {
        return storeProtocol_;
    }

    public void setStoreProtocol(String storeProtocol) {
        storeProtocol_ = storeProtocol;
    }

    public String getStoreProtocolClass() {
        return storeProtocolClass_;
    }

    public void setStoreProtocolClass(String storeProtocolClass) {
        storeProtocolClass_ = storeProtocolClass;
    }

    public String getTransportProtocol() {
        return transportProtocol_;
    }

    public void setTransportProtocol(String transportProtocol) {
        transportProtocol_ = transportProtocol;
    }

    public String getTransportProtocolClass() {
        return transportProtocolClass_;
    }

    public void setTransportProtocolClass(String transportProtocolClass) {
        transportProtocolClass_ = transportProtocolClass;
    }

    public String getMailHost() {
        return mailHost_;
    }

    public void setMailHost(String mailHost) {
        mailHost_ = mailHost;
    }

    public String getUsername() {
        return username_;
    }

    public void setUsername(String username) {
        username_ = username;
    }

    public String getMailFrom() {
        return mailFrom_;
    }

    public void setMailFrom(String mailFrom) {
        mailFrom_ = mailFrom;
    }

    public boolean isDebug() {
        return debug_;
    }

    public void setDebug(boolean debug) {
        debug_ = debug;
    }

    public String toString() {
        return "< Mail Resource : " + getResourceInfo() + " , " + getResType() + "... >";
    }
}
