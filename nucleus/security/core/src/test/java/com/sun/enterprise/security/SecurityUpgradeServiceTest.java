/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link SecurityUpgradeService#convertToPkcs12} migrates a legacy {@code domain-passwords}
 * (JCEKS, secret-key entries) store to PKCS12 while preserving its entries.
 */
public class SecurityUpgradeServiceTest {

    private static final char[] MASTER_PASSWORD = "changeit".toCharArray();

    @Test
    public void convertsLegacyDomainPasswordsToPkcs12(@TempDir File configDir) throws Exception {
        File legacy = new File(configDir, "domain-passwords");
        byte[] secret1 = "first-secret".getBytes();
        byte[] secret2 = "second-secret".getBytes();
        writeJceksWithSecrets(legacy, secret1, secret2);

        File target = new File(configDir, "domain-passwords.p12");
        SecurityUpgradeService.convertToPkcs12(legacy, "JCEKS", target, MASTER_PASSWORD);

        assertTrue(target.exists(), "PKCS12 target should have been created");

        KeyStore migrated = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(target)) {
            migrated.load(in, MASTER_PASSWORD);
        }

        assertThat("alias-one preserved", migrated.containsAlias("alias-one"), is(true));
        assertThat("alias-two preserved", migrated.containsAlias("alias-two"), is(true));

        Key key1 = migrated.getKey("alias-one", MASTER_PASSWORD);
        Key key2 = migrated.getKey("alias-two", MASTER_PASSWORD);
        assertThat(key1.getEncoded(), equalTo(secret1));
        assertThat(key2.getEncoded(), equalTo(secret2));
    }

    @Test
    public void producesPkcs12TypeWithOnlyExpectedAliases(@TempDir File configDir) throws Exception {
        File legacy = new File(configDir, "domain-passwords");
        writeJceksWithSecrets(legacy, "a".getBytes(), "b".getBytes());

        File target = new File(configDir, "domain-passwords.p12");
        SecurityUpgradeService.convertToPkcs12(legacy, "JCEKS", target, MASTER_PASSWORD);

        KeyStore migrated = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(target)) {
            migrated.load(in, MASTER_PASSWORD);
        }
        assertEquals("PKCS12", migrated.getType());
        assertThat(java.util.Collections.list(migrated.aliases()), containsInAnyOrder("alias-one", "alias-two"));
    }

    private static void writeJceksWithSecrets(File file, byte[]... secrets) throws Exception {
        KeyStore jceks = KeyStore.getInstance("JCEKS");
        jceks.load(null, MASTER_PASSWORD);
        int i = 1;
        for (byte[] secret : secrets) {
            jceks.setKeyEntry("alias-" + ordinal(i++), new SecretKeySpec(secret, "AES"), MASTER_PASSWORD, null);
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            jceks.store(out, MASTER_PASSWORD);
        }
    }

    private static String ordinal(int i) {
        return i == 1 ? "one" : "two";
    }
}
