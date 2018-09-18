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

import javax.security.auth.message.config.AuthConfigFactory.RegistrationContext;

/*
 * Class used by GFAuthConfigFactory and EntryInfo.
 *
 * This class will not be used outside of its package.
 */
final class RegistrationContextImpl implements RegistrationContext {
    private final String messageLayer;
    private final String appContext;
    private final String description;
    private final boolean isPersistent;

    RegistrationContextImpl(String messageLayer, String appContext,
        String description, boolean persistent) {

        this.messageLayer = messageLayer;
        this.appContext = appContext;
        this.description = description;
        this.isPersistent = persistent;
    }

    // helper method to create impl class
    RegistrationContextImpl(RegistrationContext ctx) {
        this.messageLayer = ctx.getMessageLayer();
        this.appContext = ctx.getAppContext();
        this.description = ctx.getDescription();
        this.isPersistent = ctx.isPersistent();
    }

    public String getMessageLayer() {
        return messageLayer;
    }

    public String getAppContext() {
        return appContext;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof RegistrationContext)) {
            return false;
        }
        RegistrationContext target = (RegistrationContext) o;
        return ( EntryInfo.matchStrings(
            messageLayer, target.getMessageLayer()) &&
            EntryInfo.matchStrings(appContext, target.getAppContext()) &&
            isPersistent() == target.isPersistent() );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.messageLayer != null ? this.messageLayer.hashCode() : 0);
        hash = 17 * hash + (this.appContext != null ? this.appContext.hashCode() : 0);
        hash = 17 * hash + (this.isPersistent ? 1 : 0);
        return hash;
    }
}
