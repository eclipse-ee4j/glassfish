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
import java.security.KeyStore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class KeyToolTest {

    private static final char[] PASSWORD = "passwordpassword".toCharArray();;

    @TempDir
    private static File tmp;
    private static KeyTool keyTool;

    @BeforeEach
    void createKeyTool() throws Exception {
        File file = File.createTempFile("keystore", "jks", tmp);
        file.delete();
        keyTool = new KeyTool(file, PASSWORD);
    }

    @Test
    void usualUseCase() throws Exception {
        keyTool.generateKeyPair("keypair001", "CN=mymachine", "RSA", 1);
        File copyKeyStoreFile = new File(tmp, "copy.jks");
        keyTool.copyCertificate("keypair001", copyKeyStoreFile);
        KeyStore keyStore = KeyStore.getInstance(copyKeyStoreFile, PASSWORD);
        assertTrue(keyStore.containsAlias("keypair001"));
        assertNull(keyStore.getKey("keypair001", PASSWORD));
        assertNotNull(keyStore.getCertificate("keypair001"));
    }

}
