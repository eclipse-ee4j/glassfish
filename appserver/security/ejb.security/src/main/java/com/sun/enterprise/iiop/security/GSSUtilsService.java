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

package com.sun.enterprise.iiop.security;

import com.sun.enterprise.common.iiop.security.GSSUtilsContract;
import java.io.IOException;

import org.jvnet.hk2.annotations.Service;
import javax.inject.Singleton;
import sun.security.util.ObjectIdentifier;

/**
 *
 * @author Kumar
 */
@Service
@Singleton
public class GSSUtilsService implements GSSUtilsContract {

    public String dumpHex(byte[] octets) {
        return GSSUtils.dumpHex(octets);
    }

    public byte[] importName(ObjectIdentifier oid, byte[] externalName) throws IOException {
        return GSSUtils.importName(oid, externalName);
    }

    public byte[] createExportedName(ObjectIdentifier oid, byte[] extName) throws IOException {
        return GSSUtils.createExportedName(oid, extName);
    }

    public ObjectIdentifier GSSUP_MECH_OID() {
        return GSSUtils.GSSUP_MECH_OID;
    }
    /**
     * TODO:V3 temporarily putting it inside this contract
     * @return the ORB
    public Object getORB() {
        
    }*/

}
