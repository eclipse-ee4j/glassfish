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

package com.sun.enterprise.connectors;

import com.sun.enterprise.connectors.authentication.RuntimeSecurityMap;
import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;

import jakarta.resource.spi.ManagedConnectionFactory;

import javax.security.auth.Subject;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * Information about the ConnectorConnectionPool. Stored inofrmation is: 1. Default Subject 2. MCF Instance 3. Password,
 * UserName 4. The transaction-support attribute level in case of connector connection pools 5. The
 * allow-non-component-callers, non-trasnactional-connections attribs for jdbc connection pools
 *
 * @author Binod P.G., Aditya Gore
 */

public class PoolMetaData {

    private ManagedConnectionFactory mcf = null;
    private PoolInfo poolInfo = null;
    private Subject subj = null;
    private ResourcePrincipalDescriptor prin_;
    private int txSupport_;
    private boolean isPM_ = false;
    private boolean isNonTx_ = false;
    private RuntimeSecurityMap runtimeSecurityMap;
    private boolean lazyEnlistable_ = false;
    private boolean lazyAssoc_ = false;
    private boolean isAuthCredentialsDefinedInPool_ = true;

    public PoolMetaData(PoolInfo poolInfo, ManagedConnectionFactory mcf, Subject s, int txSupport, ResourcePrincipalDescriptor prin,
            boolean isPM, boolean isNonTx, boolean lazyEnlistable, RuntimeSecurityMap runtimeSecurityMap, boolean lazyAssoc) {
        this.poolInfo = poolInfo;
        this.mcf = mcf;
        this.subj = s;
        txSupport_ = txSupport;
        prin_ = prin;
        isPM_ = isPM;
        isNonTx_ = isNonTx;
        lazyEnlistable_ = lazyEnlistable;
        lazyAssoc_ = lazyAssoc;
        this.runtimeSecurityMap = runtimeSecurityMap;
    }

    public ManagedConnectionFactory getMCF() {
        return this.mcf;
    }

    public Subject getSubject() {
        return this.subj;
    }

    public int getTransactionSupport() {
        return txSupport_;
    }

    public ResourcePrincipalDescriptor getResourcePrincipal() {
        return prin_;
    }

    public void setIsNonTx(boolean flag) {
        isNonTx_ = flag;
    }

    public boolean isNonTx() {
        return isNonTx_;
    }

    public void setIsPM(boolean flag) {
        isPM_ = flag;
    }

    public boolean isPM() {
        return isPM_;
    }

    public RuntimeSecurityMap getRuntimeSecurityMap() {
        return this.runtimeSecurityMap;
    }

    public void setLazyEnlistable(boolean flag) {
        lazyEnlistable_ = flag;
    }

    public boolean isLazyEnlistable() {
        return lazyEnlistable_;
    }

    public void setLazyAssoc(boolean flag) {
        lazyAssoc_ = flag;
    }

    public boolean isLazyAssociatable() {
        return lazyAssoc_;
    }

    public void setAuthCredentialsDefinedInPool(boolean authCred) {
        this.isAuthCredentialsDefinedInPool_ = authCred;
    }

    public boolean isAuthCredentialsDefinedInPool() {
        return this.isAuthCredentialsDefinedInPool_;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("PoolMetaData : " + poolInfo);
        sb.append("\ntxSupport => " + txSupport_);
        sb.append("\nisPM_     => " + isPM_);
        sb.append("\nisNonTx_  => " + isNonTx_);
        sb.append("\nisLazyEnlistable_  => " + lazyEnlistable_);
        sb.append("\nisLazyAssociatable  => " + lazyAssoc_);
        sb.append("\nsecurityMap => " + runtimeSecurityMap.toString());
        return sb.toString();
    }
}
