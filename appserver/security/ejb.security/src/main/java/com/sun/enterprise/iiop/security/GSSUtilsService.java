/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.iiop.security;

import com.sun.enterprise.common.iiop.security.GSSUtilsContract;

import jakarta.inject.Singleton;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Kumar
 */
@Service
@Singleton
public class GSSUtilsService implements GSSUtilsContract {

    @Override
    public String dumpHex(byte[] octets) {
        return GSSUtils.dumpHex(octets);
    }

    @Override
    public byte[] importName(Oid oid, byte[] externalName) throws GSSException {
        return GSSUtils.importName(oid, externalName);
    }

    @Override
    public byte[] createExportedName(Oid oid, byte[] extName) throws GSSException {
        return GSSUtils.createExportedName(oid, extName);
    }

    @Override
    public Oid GSSUP_MECH_OID() {
        return GSSUtils.GSSUP_MECH_OID;
    }
}
