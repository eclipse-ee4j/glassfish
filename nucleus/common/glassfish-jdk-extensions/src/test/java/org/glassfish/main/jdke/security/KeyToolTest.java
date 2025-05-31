/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.jdke.security;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyToolTest {

    private static final char[] PASSWORD = "passwordpassword".toCharArray();
    private static final char[] PASSWORD2 = "123456".toCharArray();

    @TempDir
    private static File tmp;

    @Test
    void messingWithKeys() throws Exception {
        File keyStoreOrig = File.createTempFile("keystore", ".p12", tmp);
        keyStoreOrig.delete();
        KeyTool keyToolOrig = new KeyTool(keyStoreOrig, PASSWORD);
        keyToolOrig.generateKeyPair("keypair001", "CN=mymachine", "RSA", 1);

        File ksFileP12 = new File(tmp, "copy.p12");
        keyToolOrig.copyCertificate("keypair001", ksFileP12);
        KeyTool keyToolP12 = new KeyTool(ksFileP12, PASSWORD);

        assertThrows(IOException.class, () -> keyToolOrig.changeKeyStorePassword("short".toCharArray()));
        keyToolOrig.changeKeyStorePassword(PASSWORD2);
        assertNotNull(keyToolOrig.loadKeyStore().getKey("keypair001", PASSWORD2));
        keyToolOrig.changeKeyStorePassword(PASSWORD);
        assertNotNull(keyToolOrig.loadKeyStore().getKey("keypair001", PASSWORD));
        keyToolOrig.changeKeyPassword("keypair001", PASSWORD, PASSWORD2);
        assertNotNull(keyToolOrig.loadKeyStore().getKey("keypair001", PASSWORD2));
        keyToolOrig.changeKeyPassword("keypair001", PASSWORD2, PASSWORD);

        // certificate chain: must be joined with private key
        KeyStore origKS = keyToolOrig.loadKeyStore();
        assertEquals("PKCS12", origKS.getType());
        assertTrue(origKS.containsAlias("keypair001"));
        assertThrows(UnrecoverableKeyException.class, () -> origKS.getKey("keypair001", "WrongPwd".toCharArray()));
        assertNotNull(origKS.getKey("keypair001", PASSWORD));
        assertNotNull(origKS.getCertificate("keypair001"));
        assertNotNull(origKS.getCertificateChain("keypair001"));

        KeyStore p12KS = keyToolP12.loadKeyStore();
        assertEquals("PKCS12", p12KS.getType());
        assertTrue(p12KS.containsAlias("keypair001"));
        assertNull(p12KS.getKey("keypair001", PASSWORD));
        assertNotNull(p12KS.getCertificate("keypair001"));
        assertNull(p12KS.getCertificateChain("keypair001"));

        File ksFileJKS = new File(tmp, "copy2.jks");
        KeyTool keyToolJKS = KeyTool.createEmptyKeyStore(ksFileJKS, PASSWORD);
        keyToolOrig.copyCertificate("keypair001", ksFileJKS);

        KeyStore jKS = keyToolJKS.loadKeyStore();
        assertEquals("JKS", jKS.getType());
        assertTrue(jKS.containsAlias("keypair001"));
        assertNull(jKS.getKey("keypair001", PASSWORD));
        assertNotNull(jKS.getCertificate("keypair001"));
        assertNull(jKS.getCertificateChain("keypair001"));
    }

    /**
     * Changing keystore password doesn't change key passwords in JKS, in PKCS12 it does.
     * {@link KeyTool} compensates it automatically.
     *
     * @throws Exception
     */
    @Test
    void messingWithKeysJKS() throws Exception {
        KeyTool keyTool = new KeyTool(new File(tmp, "messingWithKeys.jks"), PASSWORD);
        keyTool.generateKeyPair("keypair001", "CN=mymachine", "RSA", 1);
        keyTool.changeKeyStorePassword(PASSWORD2);
        assertNotNull(keyTool.loadKeyStore().getKey("keypair001", PASSWORD2));
        keyTool.changeKeyPassword("keypair001", PASSWORD2, PASSWORD);
        assertNotNull(keyTool.loadKeyStore().getKey("keypair001", PASSWORD));
        KeyStore jKS = keyTool.loadKeyStore();
        assertEquals("JKS", jKS.getType());
        assertNotNull(jKS.getKey("keypair001", PASSWORD));
    }
}
