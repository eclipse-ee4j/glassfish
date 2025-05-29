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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyToolTest {

    private static final char[] PASSWORD = "passwordpassword".toCharArray();;
    private static final char[] PASSWORD2 = "123456".toCharArray();

    @TempDir
    private static File tmp;
    private static KeyTool keyTool;
    private static File keyStoreFile;

    @BeforeAll
    static void createKeyTool() throws Exception {
        keyStoreFile = File.createTempFile("keystore", ".p12", tmp);
        keyStoreFile.delete();
        keyTool = new KeyTool(keyStoreFile, PASSWORD);
    }

    @Test
    void usualUseCase() throws Exception {
        keyTool.generateKeyPair("keypair001", "CN=mymachine", "RSA", 1);
        File copyKeyStoreFile = new File(tmp, "copy.p12");
        keyTool.copyCertificate("keypair001", copyKeyStoreFile);

        File copyKeyStoreFile2 = new File(tmp, "copy2.jks");
        KeyTool keyTool2 = KeyTool.createEmptyKeyStore(copyKeyStoreFile2, "JKS", PASSWORD);
        keyTool.copyCertificate("keypair001", copyKeyStoreFile2);
        assertThrows(IOException.class, () -> keyTool.changeKeyStorePassword("short".toCharArray()));
        keyTool.changeKeyStorePassword(PASSWORD2);
        keyTool.changeKeyStorePassword(PASSWORD);
        keyTool.changeKeyPassword("keypair001", PASSWORD, PASSWORD2);

        KeyStore keyStore = keyTool.loadKeyStore();
        assertThrows(UnrecoverableKeyException.class, () -> keyStore.getKey("keypair001", "WrongPwd".toCharArray()));
        assertNotNull(keyStore.getKey("keypair001", PASSWORD2));

        KeyStore copy1 = new KeyTool(copyKeyStoreFile, PASSWORD).loadKeyStore();
        assertEquals("PKCS12", copy1.getType());
        assertTrue(copy1.containsAlias("keypair001"));
        assertNull(copy1.getKey("keypair001", PASSWORD));
        assertNotNull(copy1.getCertificate("keypair001"));

        KeyStore copy2 = keyTool2.loadKeyStore();
        assertEquals("JKS", copy2.getType());
    }

}
